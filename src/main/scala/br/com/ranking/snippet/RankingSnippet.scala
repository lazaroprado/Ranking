package br.com.ranking.snippet

import br.com.ranking.model.{Teams, PlayerDAO}
import net.liftweb.util.Helpers._

class RankingSnippet {

  val allPlayers = PlayerDAO.findAllSortByScore.map(_.toPlayerCC)

  def render = {
    ".rows" #> allPlayers.map(player => {
      val team = if(player.team != Teams.without_team.toString) <img src={Teams.getTeamLogo(player.team)}></img> else "-"
      <tr>
        <td>{team}</td>
        <td><a href="#games" class="player_id" id={player.id.get}>{player.name}</a></td>
        <td>{player.total_score}</td>
        <td>{player.victories}</td>
        <td>{player.draws}</td>
        <td>{player.defeats}</td>
        <td>{player.goals_balance}</td>
        <td>{player.historical_percentage.getOrElse(0)}%</td>
      </tr>
    })

  }

}
