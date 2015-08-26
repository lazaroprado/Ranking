package group

import br.com.ranking.config.MongoConfig
import br.com.ranking.controller.LogicRankingController
import br.com.ranking.model.Game.{GameCC, GamePlayerCC}
import br.com.ranking.model.Player.PlayerCC
import br.com.ranking.model._
import org.joda.time.{DateTime, DateTimeZone}

/**
 * Hello world!
 *
 */
object App extends Application {
//  MongoConfig.init()

//  val data = TestData
//  val controller = LogicRankingController

//  val ronaldo = PlayerDAO.findById(data.ronaldoId).get
//  ronaldo.total_score(5000).update
//  val zico = PlayerDAO.findById(data.zicoId).get
//  zico.total_score(3000).update

  //executando o controller
//  controller.createGame(data.game_1)
//  controller.createGame(data.game_2)



  val games = 16
  val victories = 5

  println(100 * victories / games)


}

object TestData extends TestData

trait TestData {
  val ronaldoId = "55c2d4f6cd6aa03672d81890"
  val zicoId = "55c2d507cd6aa03672d81891"
  val ronaldoCC = PlayerCC(Some(ronaldoId), "Ronaldo", 5000, 0, 0, 0, 0, "Corinthians", Some(0))
  val ronaldo = Player.toPlayerRecord(ronaldoCC)
  val zicoCC = PlayerCC(Some(zicoId), "Zico", 3000, 0, 0, 0, 0, "Flamengo", Some(0))
  val zico = Player.toPlayerRecord(zicoCC)

  val game_ronaldo0 = GamePlayerCC(ronaldoId, 0, false)
  val game_ronaldo1 = GamePlayerCC(ronaldoId, 1, false)
  val game_ronaldo2 = GamePlayerCC(ronaldoId, 2, false)
  val game_ronaldo3 = GamePlayerCC(ronaldoId, 3, false)
  val game_zico0 = GamePlayerCC(zicoId, 0, false)
  val game_zico1 = GamePlayerCC(zicoId, 1, false)
  val game_zico2 = GamePlayerCC(zicoId, 2, false)
  val game_zico3 = GamePlayerCC(zicoId, 3, false)


  // jogos não oficiais //
  val game_1 = GameCC(game_ronaldo1, game_zico0, Some("2015-08-06 20:00:00"), processed = false, official = false, victoryThree = false)
  val game_2 = GameCC(game_ronaldo3, game_zico2, Some("2015-08-06 20:30:00"), processed = false, official = false, victoryThree = false)
  val game_3 = GameCC(game_ronaldo0, game_zico0, Some("2015-08-06 21:00:00"), processed = false, official = false, victoryThree = false)
  val game_4 = GameCC(game_ronaldo0, game_zico1, Some("2015-08-06 21:30:00"), processed = false, official = false, victoryThree = false)
Some()
  // jogos oficiais //Some()
  val game_5 = GameCC(game_ronaldo1, game_zico0, Some("2015-08-06 20:00:00"), processed = false, official = true, victoryThree = false)
  val game_6 = GameCC(game_ronaldo2, game_zico2, Some("2015-08-06 20:30:00"), processed = false, official = true, victoryThree = false)
  val game_7 = GameCC(game_ronaldo0, game_zico2, Some("2015-08-06 21:00:00"), processed = false, official = true, victoryThree = false)
  val game_8 = GameCC(game_ronaldo3, game_zico0, Some("2015-08-06 21:30:00"), processed = false, official = true, victoryThree = false)
  val game_9 = GameCC(game_ronaldo0, game_zico1, Some("2015-08-06 21:30:00"), processed = false, official = true, victoryThree = false)
  val game_10 = GameCC(game_ronaldo0, game_zico1,Some("2015-08-06 21:30:00"), processed = false, official = true, victoryThree = false)


}
