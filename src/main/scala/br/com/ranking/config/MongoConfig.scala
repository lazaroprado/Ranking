package br.com.ranking.config

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.util.Props
import net.liftweb.mongodb.{MongoIdentifier, MongoDB}
import com.mongodb.{ReadPreference, MongoClientOptions, MongoClient, ServerAddress}
import net.liftweb.common.{Loggable, Full, Empty}
import scala.collection.JavaConversions._

object MongoRankingIdentifier extends MongoIdentifier {
  override def jndiName: String = "mongo.ranking"
}

trait MongoRankingIdentifier[BaseRecord <: MongoRecord[BaseRecord]] extends MongoMetaRecord[BaseRecord] { self: BaseRecord =>
  override def mongoIdentifier: MongoIdentifier = MongoRankingIdentifier
}

object MongoConfig extends MongoConfig {

  override val identifier: MongoIdentifier = MongoRankingIdentifier
  override val propsId: String = "mongo"
  override val db: String = "ranking"

}

trait MongoConfig extends Loggable {

  val identifier: MongoIdentifier
  val propsId: String
  val db: String

  def init() {
    val database = Props.get("%s.db".format(propsId), db)
    val connections = Props.get("%s.connections_per_host".format(propsId), "5").toInt
    val options = new MongoClientOptions.Builder()
      .connectionsPerHost(connections)
      .readPreference(ReadPreference.secondaryPreferred())
      .build()
    val servers = mongoServers
    if(servers.size == 1) MongoDB.defineDb(identifier, new MongoClient(servers(0), options), database)
    else MongoDB.defineDb(identifier, new MongoClient(servers, options), database)
  }

  def mongoServers: List[ServerAddress] = {
    def readServer(acc: List[ServerAddress], server: Int): List[ServerAddress] = Props.get("%s.%s.host".format(propsId, server)) match {
      case Empty => acc
      case Full(host) => readServer(new ServerAddress(host, Props.getInt("%s.%s.port".format(propsId, server), 27017)) :: acc, server+1)
      case _ => readServer(acc, server+1)
    }
    readServer(Nil, server = 1)
  }

}