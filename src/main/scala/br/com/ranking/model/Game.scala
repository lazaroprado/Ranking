package br.com.ranking.model

import java.text.SimpleDateFormat

import br.com.ranking.config.MongoRankingIdentifier
import br.com.ranking.model.Game.{GameCC, GamePlayerCC}
import com.foursquare.rogue.LiftRogue._
import net.liftweb.mongodb.record.field.{BsonRecordField, DateField, ObjectIdPk}
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoRecord}
import net.liftweb.record.field.{BooleanField, IntField, StringField}
import org.bson.types.ObjectId

class GamePlayer private () extends BsonRecord[GamePlayer] {
  def meta = GamePlayer

  object player_id            extends StringField(this, 256)
  object score                extends IntField(this)
  object victory              extends BooleanField(this, false) {override def optional_? = false; override def displayName = "É uma vitória?"}

}

object GamePlayer extends GamePlayer with BsonMetaRecord[GamePlayer]

class Game extends MongoRecord[Game] with ObjectIdPk[Game] {
  def meta = Game

  object principal         extends BsonRecordField(this, GamePlayer)
  object visitor           extends BsonRecordField(this, GamePlayer)
  object registration      extends DateField(this)
  object processed         extends BooleanField(this, false) {override def optional_? = true; override def displayName = "Game já foi computado?"}
  object oficial           extends BooleanField(this, false) {override def optional_? = true; override def displayName = "É um jogo oficial?"}
  object victoryThree      extends BooleanField(this, false) {override def optional_? = false; override def displayName = "É uma vitória por mais de 2 gols?"}

  def toGameCC: GameCC = {
    GameCC(GamePlayerCC(principal.value.player_id.value, principal.value.score.value, principal.value.victory.value),
      GamePlayerCC(visitor.value.player_id.value, visitor.value.score.value, visitor.value.victory.value),
      dateToString, processed.value, oficial.value, victoryThree.value)
  }

  def dateToString = Some(new SimpleDateFormat("dd/MM/yyyy").format(registration.value))

}

object Game extends GameMetaRecord {
  override def collectionName = "games"
  case class GameCC(principal: GamePlayerCC, visitor: GamePlayerCC, registration: Option[String], processed: Boolean, oficial: Boolean, victoryThree: Boolean)
  case class GamePlayerCC(player_id: String, score: Int, victory: Boolean)

  def toGameRecord(game: GameCC): Game = {
    val principal = GamePlayer.createRecord.player_id(game.principal.player_id).score(game.principal.score).victory(game.principal.victory)
    val visitor = GamePlayer.createRecord.player_id(game.visitor.player_id).score(game.visitor.score).victory(game.visitor.victory)
    Game.createRecord.principal(principal).visitor(visitor).registration(stringToDate(game.registration.get)).processed(game.processed)
  }

  def stringToDate(date: String) = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)

}

trait GameMetaRecord extends Game with MongoRankingIdentifier[Game] {

  def findById(id: String): Option[Game] = {
    Game.where(_.id eqs new ObjectId(id)).fetch(1).headOption
  }

  def findByPlayerId(playerId: String): List[Game] = {
    Game.or(_.where(_.principal.subfield(_.player_id) eqs playerId), _.where(_.visitor.subfield(_.player_id) eqs playerId)).orderAsc(_.registration).fetch()
  }

  def findLastTwoByPlayerId(playerId: String): List[Game] = {
    Game.where(_.processed eqs true).or(_.where(_.principal.subfield(_.player_id) eqs playerId), _.where(_.visitor.subfield(_.player_id) eqs playerId)).orderDesc(_.registration).fetch(2)
  }

}

trait GameDAO {

  def all = Game.fetch()

  def findById(id: String): Option[GameCC] = {
    Game.findById(id) match {
      case Some(game) => Some(game.toGameCC)
      case _ => None
    }
  }

  def findByPlayerId(playerId: String): List[Game] = {
    Game.findByPlayerId(playerId) match {
      case head :: tail => head :: tail
      case _ => Nil
    }
  }
  
  def findLastTwoByPlayerId(playerId: String): List[Game] = {
    Game.findLastTwoByPlayerId(playerId) match {
      case head :: tail => head :: tail
      case _ => Nil
    }
  }

  def findAll: List[Game] = Game.findAll

}

object GameDAO extends GameDAO