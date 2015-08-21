package bootstrap.liftweb

import net.liftweb.sitemap.Loc.{LocGroup, Stateless}
import net.liftweb.sitemap.{Menu, SiteMap}

object Sitemap {

  private val topBarGroup = LocGroup("topbar")

  val linksMenu = Menu("Geral") / "index" >> topBarGroup

  lazy val ranking = Menu("ranking", "Ranking de Classificação") / "ranking" >> LocGroup("general")
  lazy val player = Menu("player", "Cadastro de Jogadores") / "player" >> LocGroup("general")
  lazy val game = Menu("game", "Cadastro de Jogos") / "game" >> LocGroup("general")
  lazy val rules = Menu("rules", "Regras") / "rules" >> LocGroup("general")

  val menus = List(
    linksMenu,
    ranking,
    player,
    game,
    rules
  )

  val siteMap = SiteMap(menus:_*)
}
