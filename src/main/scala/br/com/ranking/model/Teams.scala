package br.com.ranking.model

object Teams extends Enumeration {
  type Teams = Value

  val without_team = Value("Não torço para ninguém")
  val corinthians = Value("Corinthians")
  val sao_paulo = Value("São Paulo")
  val palmeiras = Value("Palmeiras")
  val santos = Value("Santos")
  val botafogo = Value("Botafogo")
  val vasco = Value("Vasco")
  val flamengo = Value("Flamengo")
  val fluminense = Value("Fluminense")
  val cruzeiro = Value("Cruzeiro")
  val atletico_mineiro = Value("Atlético Mineiro")
  val gremio = Value("Grêmio")
  val internacional = Value("Internacional")

  def getTeamValue(value: Option[String]) =
    try {
      withName(value.getOrElse(without_team.toString))
    } catch {
      case e: Throwable => without_team
    }

  def getTeamLogo(team: String): String = {
    val src = "images/teams/%s.png"
    getTeamValue(Some(team)) match {
      case this.corinthians => src.format("corinthians")
      case this.sao_paulo => src.format("sao_paulo")
      case this.palmeiras => src.format("palmeiras")
      case this.santos => src.format("santos")
      case this.botafogo => src.format("botafogo")
      case this.vasco => src.format("vasco")
      case this.flamengo => src.format("flamengo")
      case this.fluminense => src.format("fluminense")
      case this.cruzeiro => src.format("cruzeiro")
      case this.atletico_mineiro => src.format("atletico_mineiro")
      case this.gremio => src.format("gremio")
      case this.internacional => src.format("internacional")
      case _ => ""
    }
  }

}
