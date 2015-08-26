package br.com.ranking.rest

import br.com.ranking.controller.LogicRankingController
import br.com.ranking.model.Game.GameCC
import br.com.ranking.model.{GameDAO, PlayerDAO, Player}
import br.com.ranking.model.Player.PlayerCC
import net.liftweb.http.rest.RestHelper
import java.util.Date

case class RestResponse(code: Int, message: String, value: Any)
case class RankingCC(name: String, score: Int, goals_balance: Int, historical_percentage: Int)
case class SuggestGames(opponentId: String, opponentName: String, qty: Int, registration: Date)

object RankingRest extends RestHelper {

  val OK = 200
  val ERROR = -1
  val OK_MESSAGE = "Success"
  val ERROR_MESSAGE = "Error"

  //TODO Criar a idéia de campeonato. O campeonato terá jogadores e jogos, sendo que os jogadores podem setar em vários campeonatos. Isso vai gerar um ranking para cada campeonato
  //TODO Criar o login para ver o ranking online. Deve se salvar no banco cada alteração feita, e cada cadastro efetuado.

  //TODO fazer um serviço que processe todos os jogos novamente. Ele primeiro deve limpar a pontuação dos jogadores, e o flag de processed do game, e então
  //TODO processador todos.

  serve {

    case "api" :: "game" :: _ JsonPost json => saveGame(json._1.extract[GameCC])

    case "api" :: "games" :: player_id :: _ Get _ => getAllPlayerGames(player_id)

    case "api" :: "games" :: _ Get _ => anyToJValue(GameDAO.findAll.map(_.toGameCC))

    case "api" :: "players" :: _ Get _ => anyToJValue(PlayerDAO.findAll.map(_.toPlayerCC))

    case "api" :: "player" :: _ JsonPost json => savePlayer(json._1.extract[PlayerCC])

    case "api" :: "player" :: "show-suggestions" :: player_ids :: _ Get _ => getSuggestions(player_ids)

    case "api" :: "player" :: player_id :: _ Get _ => anyToJValue(PlayerDAO.findById(player_id).map(_.toPlayerCC))

    case "api" :: "ranking" :: _ Get _ => getRanking

    case "api" :: "last-two-games" :: player_id :: _ Get _ => getLastTwoGames(player_id)

    case "api" :: "reset-players" :: _ Post _  => resetPlayers

    case "api" :: "reset-games" :: _ Post _  => resetGames

    case "api" :: "divide-score" :: _ Post _  => divideScore

  }

  def divideScore = {
    try {
      val players = PlayerDAO.findAll
      players.map(p => p.total_score(p.total_score.value / 2).update)
      anyToJValue("Pontuação dividida com sucesso!")
    } catch {
      case e: Exception =>  anyToJValue(RestResponse(ERROR, ERROR_MESSAGE, "Não foi possível dividir a pontuação por 2. -> " + e ))
    }
  }

  def resetPlayers = {
    try {
      val players = PlayerDAO.findAll
      players.map(p => p.total_score(0).historical_percentage(0).goals_balance(0).victories(0).draws(0).defeats(0).update)
      anyToJValue("Jogadores zerados com sucesso!")
    } catch {
      case e: Exception =>  anyToJValue(RestResponse(ERROR, ERROR_MESSAGE, "Não foi possível zerar os jogadores. -> " + e ))
    }
  }

  def resetGames = {
    try {
      val games = GameDAO.findAll
      games.map(p => p.delete_!)
      anyToJValue("Jogos zerados com sucesso!")
    } catch {
      case e: Exception =>  anyToJValue(RestResponse(ERROR, ERROR_MESSAGE, "Não foi possível zerar os jogos. -> " + e ))
    }
  }

  def saveGame(game: GameCC) = {
    try {
      val result = LogicRankingController.createGame(game)
      if(result) anyToJValue(RestResponse(OK, OK_MESSAGE, ""))
      else anyToJValue(RestResponse(ERROR, ERROR_MESSAGE, "Não foi possível salvar esse jogo."))
    } catch {
      case e: Exception =>  anyToJValue(RestResponse(ERROR, ERROR_MESSAGE, "Não foi possível salvar esse jogo. -> " + e ))
    }
  }

  def savePlayer(player: PlayerCC) = {
    try {
      val record = Some(Player.toPlayerRecord(player).save)
      if(record.isDefined) anyToJValue(RestResponse(OK, OK_MESSAGE, record.get.asJValue))
      else anyToJValue(RestResponse(ERROR, ERROR_MESSAGE, "Não foi possível salvar esse jogador."))
    } catch {
      case e: Exception =>  anyToJValue(RestResponse(ERROR, ERROR_MESSAGE, "Não foi possível salvar esse jogador. -> " + e ))
    }
  }

  def getRanking = {
    val players = PlayerDAO.findAllSortByScore
    val ranking: List[RankingCC] = players.map(p => RankingCC(p.name.value, p.total_score.value, p.goals_balance.value, p.historical_percentage.value))
    anyToJValue(ranking)
  }

  def getLastTwoGames(playerId: String) = {
    val games = GameDAO.findLastTwoByPlayerId(playerId)
    anyToJValue(games.map(_.toGameCC))
  }

  def getAllPlayerGames(playerId: String) = {
    val games = GameDAO.findByPlayerId(playerId)
    anyToJValue(games.map(_.toGameCC))
  }

  def getSuggestions(playerIds: String) = {
    val ids = playerIds.split(",").toList
    val games = GameDAO.findOfficialByPlayerId(ids.head)
    val otherPlayers = PlayerDAO.findAllExcept(ids)
    val suggestions = otherPlayers.map(p => {
      val opponentGames = games.filter(g => g.principal.value.player_id.toString == p.id.toString || g.visitor.value.player_id.toString == p.id.toString)
      if(opponentGames.isEmpty)
        SuggestGames(p.id.value.toString, p.name.value, 0, new Date())
      else
        SuggestGames(p.id.value.toString, p.name.value, opponentGames.size, opponentGames.reverse.head.registration.value)
    }).sortBy(_.registration).sortBy(_.qty)
    anyToJValue(suggestions)
  }

}
