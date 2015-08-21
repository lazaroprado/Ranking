package br.com.ranking.snippet

import br.com.ranking.model.{Teams, PlayerDAO}
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._

class GameSnippet {

  val allPlayers = PlayerDAO.findAll.map(_.toPlayerCC)

  def render: CssSel = {
    ".principal_players *" #> (allPlayers.map(player => {
      <option value={player.id.get}>{player.name}</option>
    }) ::: List(<option value="default" selected="selected">Selecione um Jogador</option>)) &
    ".visitor_players *" #> (allPlayers.map(player => {
      <option value={player.id.get}>{player.name}</option>
    }) ::: List(<option value="default" selected="selected">Selecione um Jogador</option>))
  }

}
