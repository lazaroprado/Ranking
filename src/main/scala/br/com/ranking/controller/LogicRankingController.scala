package br.com.ranking.controller

import java.util.Date

import br.com.ranking.model.Game.GameCC
import br.com.ranking.model._

object LogicRankingController {

  val V = 2000 // vitória por menos de 2 gols de diferença
  val V_+ = 3000 // vitória por mais de 2 gols de diferença
  //bonus
  val V_3 = 1000 // 3 vitórias seguidas
  val V_# = 3000 // vitória sobre o líder atual

  val V_C = 500 //vitória em um confronto
  val DRAW = 500 // empate - somente em rodada oficial
  val DEFEAT = -500 // derrota
  val OK = true
  val ERROR = true
  val THIRD_STRAIGHT_WIN_TRUE = true
  val THIRD_STRAIGHT_WIN_FALSE = false

  private var playerDAO: PlayerDAO = PlayerDAO
  private var gameDAO: GameDAO = GameDAO

  def inject(playerDAO: PlayerDAO, gameDAO: GameDAO): Unit = {
    this.playerDAO = playerDAO
    this.gameDAO = gameDAO
  }

  def createGame(game: GameCC): Boolean = {

    //lógica de atualização da classificação // Fazer essa lógica após qualquer cadastro de resultado (Game)

    try {
      val principal = GamePlayer.createRecord.player_id(game.principal.player_id).score(game.principal.score)
      val visitor = GamePlayer.createRecord.player_id(game.visitor.player_id).score(game.visitor.score)

      // criando o jogo
      //TODO verificar se o jogo é possível de ser realizado obedecendo a seguinte regra: Se for um confronto, e um dos jogadores tiver 0(zero) pontos, não pode ser feito o jogo
      val newGame = {
        if(isADrawGame(principal, visitor)){
          Game.createRecord.principal(principal).visitor(visitor).registration(getDate(game.registration)).official(game.official).save
        } else {
          Game.createRecord.principal(principal.victory(isWinner(principal, visitor))).visitor(visitor.victory(isWinner(visitor, principal))).registration(getDate(game.registration)).official(game.official).save
        }
      }
      // salvando dados dos jogadores
      val victoryThree = updatePlayerData(principal, visitor, game.official)
      //atualizado o flag de jogo processado
      newGame.processed(OK).victoryThree(victoryThree).update
      OK
    } catch {
      case e: Exception =>  {
        println(e.getStackTraceString)
        ERROR
      }
    }
  }

  private def getDate(date: Option[String]): Date = {
    if(date.getOrElse("").nonEmpty) Game.stringToDate(date.get)
    else new Date()
  }

  // método que atualiza todos os dados dos jogadores da partida
  def updatePlayerData(principal: GamePlayer, visitor: GamePlayer, isOficialGame: Boolean): Boolean = {
    try {
      //vendo se a partida é oficial
      if(isOficialGame) {

        // lógica para atualização do total_score e do historical_percentage //

        if(isADrawGame(principal, visitor)) {
          val principalDraw = PlayerDAO.findById(principal.player_id.value).get
          principalDraw.total_score(principalDraw.total_score.value + DRAW)
            .historical_percentage(calcHistoricalPercentage(principalDraw, isTheWinner = false))
            .draws(principalDraw.draws.value + 1).update

          val visitorDraw = PlayerDAO.findById(visitor.player_id.value).get
          visitorDraw.total_score(visitorDraw.total_score.value + DRAW)
            .historical_percentage(calcHistoricalPercentage(visitorDraw, isTheWinner = false))
            .draws(visitorDraw.draws.value + 1).update

          THIRD_STRAIGHT_WIN_FALSE
        } else {
          val winner = getWinner(principal, visitor).get
          val loser = getLoser(principal, visitor).get
          val differScore = winner.score - loser.score

          val winnerData = getWinnerScore(winner, loser) //devolve o score e se foi uma terceira vitória seguida //
          winner.player.total_score(winner.player.total_score.value + winnerData._1)
            .goals_balance(winner.player.goals_balance.value + differScore)
            .historical_percentage(calcHistoricalPercentage(winner.player, isTheWinner = true))
            .victories(winner.player.victories.value + 1).update

          loser.player.total_score(loser.player.total_score.value + (if(loser.player.total_score.value == 0) 0 else DEFEAT))
            .goals_balance(loser.player.goals_balance.value - differScore)
            .historical_percentage(calcHistoricalPercentage(loser.player, isTheWinner = false))
            .defeats(loser.player.defeats.value + 1).update

          winnerData._2 // é uma terceira vitória seguida? //
        }

      } else {
        // jogo de confronto terminado em empate, não gera nenhuma atualização
        //Jogo de confronto não atualiza historico dos jogadores, nem número de vitórias/empates/derrotas, somente saldo de gols e pontuação
        if(notIsADrawGame(principal, visitor)) {
          val winner = getWinner(principal, visitor).get
          val loser = getLoser(principal, visitor).get
          val differScore = winner.score - loser.score

          winner.player.total_score(winner.player.total_score.value + V_C)
            .goals_balance(winner.player.goals_balance.value + differScore).update

          loser.player.total_score(loser.player.total_score.value + DEFEAT)
            .goals_balance(loser.player.goals_balance.value - differScore).update
        }
        THIRD_STRAIGHT_WIN_FALSE
      }
    } catch {
      case e: Exception =>  {
        println(e.getStackTraceString)
        THIRD_STRAIGHT_WIN_FALSE
      }
    }

  }
  
  case class PlayerAndScore(player: Player, score: Int)

  private def getWinnerScore(winner: PlayerAndScore, loser: PlayerAndScore): (Int,Boolean) = {
    val differ = winner.score - loser.score
    val thirdStraightWin = isThirdStraightWin(winner.player.id.value.toString)
    ((if(differ <= 2) V else V_+) + (if(isLoserTheLeader(loser.player)) V_# else 0) + (if(thirdStraightWin) V_3 else 0), thirdStraightWin)
  }

  private def isThirdStraightWin(playerId: String): Boolean = {
    val games = gameDAO.findLastTwoByPlayerId(playerId).filter(game => notIsADrawGame(game.principal.value, game.visitor.value))
    val size = games.size
    size > 1 &&
    size == games.count(g => { getWinner(g.principal.value, g.visitor.value).get.player.id.toString equals playerId}) &&
    games.filter(_.victoryThree.value).isEmpty
  }

  private def isLoserTheLeader(loser: Player): Boolean = {
    val leader = playerDAO.findLeader.get
    leader.id.toString == loser.id.toString() && leader.total_score.value > 0
  }

  def isADrawGame(principal: GamePlayer, visitor: GamePlayer) = principal.score.value == visitor.score.value
  def notIsADrawGame(principal: GamePlayer, visitor: GamePlayer) = !isADrawGame(principal, visitor)

  def getWinner(principal: GamePlayer, visitor: GamePlayer): Option[PlayerAndScore] = getWinnerOrLoser(principal, visitor, isWinner = true)
  def getLoser(principal: GamePlayer, visitor: GamePlayer): Option[PlayerAndScore] = getWinnerOrLoser(principal, visitor, isWinner = false)

  private def getWinnerOrLoser(principal: GamePlayer, visitor: GamePlayer, isWinner: Boolean): Option[PlayerAndScore] = {
    if(principal.score.value > visitor.score.value) {
      if(isWinner) Some(PlayerAndScore(PlayerDAO.findById(principal.player_id.value).get, principal.score.value))
      else Some(PlayerAndScore(PlayerDAO.findById(visitor.player_id.value).get, visitor.score.value))
    } else if(visitor.score.value > principal.score.value) {
      if(isWinner) Some(PlayerAndScore(PlayerDAO.findById(visitor.player_id.value).get, visitor.score.value))
      else Some(PlayerAndScore(PlayerDAO.findById(principal.player_id.value).get, principal.score.value))
    } else
      None
  }

  private def isWinner(player: GamePlayer, other: GamePlayer): Boolean = player.score.value > other.score.value

  def calcHistoricalPercentage(player: Player, isTheWinner: Boolean): Int = {
    val games = gameDAO.findByPlayerId(player.id.value.toString).count(_.official.value)
    val victories = player.victories.value + (if(isTheWinner) 1 else 0)

    if((games == 1 && !isTheWinner) || victories == 0) 0
    else 100 * victories / games
  }



}