package br.com.ranking.snippet

import br.com.ranking.model.Teams
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._

class PlayerSnippet {

  def render: CssSel = {
    ".player_team *" #> (Teams.values.toList.map(team => {
      <option value={team.toString}>{team.toString}</option>
    }) ::: List(<option value="default" selected="selected">Selecione seu time de coração</option>))
  }



}
