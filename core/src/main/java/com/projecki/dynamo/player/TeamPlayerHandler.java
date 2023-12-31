package com.projecki.dynamo.player;

import com.projecki.dynamo.Bounds;
import com.projecki.dynamo.team.Team;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Represents a team-style player handler structure. This player handler works on the premise that the game has
 * predefined teams for players to be joined to such as a cops-vs-robbers game.
 */
public abstract class TeamPlayerHandler extends PlayerHandler {

    /**
     * This function asks the implementation to generate a set of teams; a team-template. These teams are the structure
     * for the game and are the teams used in the actual game. Rarely should this function be dependent on anything. The
     * teams generated by this function should almost always be the same every time.
     *
     * In the case of a cops-vs-robbers game, the teams would be Cops, and Robbers. Design these teams as you wish (name,
     * color, player bounds etc.).
     *
     * @return A set of generated teams
     */
    public abstract Set<Team> generateEmptyTeams();

    public final void initializeTeamData() {
        super.setTeams(this.generateEmptyTeams());

        int min = super.teams.stream().mapToInt(t -> t.getRequiredPlayers().min()).sum();
        int max = super.teams.stream().mapToInt(t -> t.getRequiredPlayers().max()).sum();
        super.setTotalPlayerBounds(new Bounds(min, max));
    }

    public Team getCustomTargetTeam(Player player, Team original) {
        return original;
    }
}
