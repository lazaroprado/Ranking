package br.com.ranking.model

import br.com.ranking.config.MongoRankingIdentifier
import br.com.ranking.model.Player.PlayerCC
import com.foursquare.rogue.LiftRogue._
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.{EnumNameField, IntField, StringField}
import org.bson.types.ObjectId

class Player extends MongoRecord[Player] with ObjectIdPk[Player] {
  def meta = Player

  object name                   extends StringField(this, 256) { override def displayName = "Nome do Jogador"; override def optional_? = false; override def defaultValue = "Nobody" }
  object total_score            extends IntField(this) { override def displayName = "Pontuação Total"; override def optional_? = false; override def defaultValue = 0 }
  object goals_balance          extends IntField(this) { override def displayName = "Saldo de Gols"; override def optional_? = false; override def defaultValue = 0 }
  object victories              extends IntField(this) { override def displayName = "Vitórias"; override def optional_? = false; override def defaultValue = 0 }
  object draws                  extends IntField(this) { override def displayName = "Empates"; override def optional_? = false; override def defaultValue = 0 }
  object defeats                extends IntField(this) { override def displayName = "Derrotas"; override def optional_? = false; override def defaultValue = 0 }
  object team                   extends EnumNameField(this, Teams) { override def displayName = "Time de Coração"; override def optional_? = true; override def defaultValue = Teams.without_team }
  object historical_percentage  extends IntField(this) { override def displayName = "Aproveitamento Histórico"; override def optional_? = true; override def defaultValue = 100 }

  def toPlayerCC: PlayerCC = {
    PlayerCC(Some(id.value.toString), name.value, total_score.value, goals_balance.value, victories.value, draws.value, defeats.value, team.value.toString, historical_percentage.valueBox)
  }

}

object Player extends PlayerMetaRecord {
  override def collectionName = "players"
  case class PlayerCC(id: Option[String], name: String, total_score: Int, goals_balance: Int, victories: Int, draws: Int, defeats: Int, team: String, historical_percentage: Option[Int])

  def toPlayerRecord(player: PlayerCC): Player = {
    Player.createRecord.name(player.name).total_score(player.total_score).goals_balance(player.goals_balance).
      team(Teams.getTeamValue(Some(player.team))).historical_percentage(player.historical_percentage)
  }
}

trait PlayerMetaRecord extends Player with MongoRankingIdentifier[Player] {

  def findById(id: String): Option[Player] = {
    Player.where(_.id eqs new ObjectId(id)).fetch(1).headOption
  }

  def findAllSortByScore: List[Player] = {
    Player.orderDesc(_.total_score).fetch()
  }

  def findLeader: Option[Player] = {
    Player.orderDesc(_.total_score).fetch(1).headOption
  }

  def findAllExcept(ids: List[String]): List[Player] = {
    Player.where(_.id nin ids.map(new ObjectId(_))).fetch()
  }

}

trait PlayerDAO {

  def findById(id: String): Option[Player] = {
    Player.findById(id) match {
      case Some(player) => Some(player)
      case _ => None
    }
  }

  def findAll: List[Player] = Player.findAll

  def findAllExcept(ids: List[String]): List[Player] = Player.findAllExcept(ids)

  def findAllSortByScore: List[Player] = Player.findAllSortByScore

  def findLeader: Option[Player] = Player.findLeader

}

object PlayerDAO extends PlayerDAO
