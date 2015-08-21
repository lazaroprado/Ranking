//package br.com.ranking.controller
//
//import br.com.ranking.model.Game.{GamePlayerCC, GameCC}
//import br.com.ranking.model.Player.PlayerCC
//import br.com.ranking.model.{GameDAO, PlayerDAO, Player}
//import org.bson.types.ObjectId
//import org.junit.runner.RunWith
//import org.scalatest.junit.JUnitRunner
//import org.scalatest.mock.MockitoSugar
//import org.scalatest.{BeforeAndAfter, FunSuite}
//import org.mockito.Mockito._
//import org.mockito.Matchers._
//
//@RunWith(classOf[JUnitRunner])
//class LogicRankingControllerTest extends FunSuite with MockitoSugar with BeforeAndAfter with TestData {
//
//  val playerDAO = mock[PlayerDAO]
//  val gameDAO = mock[GameDAO]
//  val controller = LogicRankingController
//  val data = TestData
//
//  before {
//    LogicRankingController.inject(playerDAO, gameDAO)
//  }
//
//  // Testes de atualização de usuários //
//
//  test("Principal_1 1 x 0 Visitor_1") {
//    when(playerDAO.findById(data.ronaldoId)).thenReturn(Some(data.ronaldo))
//    controller.createGame(data.game_1)
//
//
//
//    assert("asd" === "asd")
//
//  }
//
//
//
//
//
//}
//
//object TestData extends TestData
//
//trait TestData {
//  val ronaldoId = "12345"
//  val zicoId = "54321"
//  val ronaldoCC = PlayerCC(Some(ronaldoId), "Ronaldo", 5000, 0, "Corinthians", Some(0))
//  val ronaldo = Player.toPlayerRecord(ronaldoCC)
//  val zicoCC = PlayerCC(Some(zicoId), "Zico", 3000, 0, "Flamengo", Some(0))
//  val zico = Player.toPlayerRecord(zicoCC)
//
//  val game_ronaldo0 = GamePlayerCC("12345", 0)
//  val game_ronaldo1 = GamePlayerCC("12345", 1)
//  val game_ronaldo3 = GamePlayerCC("12345", 3)
//  val game_zico0 = GamePlayerCC("54321", 0)
//  val game_zico1 = GamePlayerCC("54321", 1)
//  val game_zico2 = GamePlayerCC("54321", 2)
//
//  val game_1 = GameCC(game_ronaldo1, game_zico0, "2015-08-06 22:00:00", processed = false, oficial = false)
//  val game_2 = GameCC(game_ronaldo3, game_zico2, "2015-08-06 22:00:00", processed = false, oficial = false)
//  val game_3 = GameCC(game_ronaldo0, game_zico0, "2015-08-06 22:00:00", processed = false, oficial = false)
//  val game_4 = GameCC(game_ronaldo0, game_zico1, "2015-08-06 22:00:00", processed = false, oficial = false)
//
//
//}
