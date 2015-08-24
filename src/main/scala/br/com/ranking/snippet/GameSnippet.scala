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
    }) ::: List(<option value="default" selected="selected">Selecione um Jogador</option>)) &
    ".player_exceptions *" #> allPlayers.map(player => {
      <div class="right-space suggest-checkbox">
        <label class="label-control-checkbox" for={player.id.get}>{player.name}</label>
        <input id={player.id.get} type="checkbox" class="control-checkbox filter-checkbox" onclick="players.exceptionCheckbox();" />
      </div>
    })
  }

}
