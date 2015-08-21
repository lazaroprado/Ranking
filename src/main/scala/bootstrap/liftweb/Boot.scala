package bootstrap.liftweb

import java.io.{FileInputStream, File}
import javax.naming.InitialContext

import br.com.ranking.config.MongoConfig
import br.com.ranking.rest.RankingRest
import net.liftweb.common.{Failure, Full, Box, Loggable}
import net.liftweb.http.{Html5Properties, LiftRules, Req, S}
import net.liftweb.json.DefaultFormats
import net.liftweb.util.Helpers._
import net.liftweb.util.{Helpers, Props}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    implicit def formats = DefaultFormats

    val resourceDir = getJndiEntryOrDefault("resourcePath", "./resources")

    val whereToLook = resourceDir.map(
      dir =>
        for {
          name <- Props.toTry
          fullName = dir + name() + "props"
          file = new File(fullName)
          if file.exists
        } yield (fullName, () => Full(new FileInputStream(file)))
    )

    whereToLook.foreach {
      w =>
        Props.whereToLook = () => {
          w
        }
    }

    //Databases Connections
    MongoConfig.init()

    // running stateless
    LiftRules.enableContainerSessions = false

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.autoIncludeAjax = session => false

    // where to search snippet
    LiftRules.addToPackages("br.com.ranking")

    //Services
    LiftRules.dispatch.append(RankingRest)

    LiftRules.setSiteMap(Sitemap.siteMap)

    LiftRules.defaultHeaders = {
    case _ =>
      val origin = getOrigin
      List(
        "Access-Control-Allow-Origin" -> origin,
        "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With",
        "Access-Control-Allow-Credentials" -> "true")
  }

  }

  def getJndiEntryOrDefault[T](lookUpKey: String, defaultValue: T): Box[T] = {
    tryo((new InitialContext).lookup(lookUpKey).asInstanceOf[T]) match {
      case f: Full[T] => f
      case f: Failure => {
        Full(defaultValue)
      }
      case _ => Full(defaultValue)
    }
  }

  def getOrigin = {
    val origin: String = S.getRequestHeader("Origin").getOrElse("")
    if (origin.indexOf(Props.get("access_control.allow_origin_domain").openOr("N/A")) >= 0) {
      origin
    } else {
      "*"
    }
  }
}
