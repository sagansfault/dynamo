package com.projecki.dynamo.player;

import com.projecki.dynamo.Bounds;
import com.projecki.dynamo.team.TeamColor;
import org.bukkit.entity.Player;

/**
 * Represents a player handler that should be used when the game in question is planned to have each player work alone
 * for themselves. This does not force a free-for-all type game structure, but it does ensure that every player is on
 * their own team of size 1 and whose name is the player's name.
 */
public abstract class IndividualPlayerHandler extends PlayerHandler {

    /**
     * Constructs a new IndividualPlayerHandler with the given overall player requirements. These requirements are not
     * team-based as each player will be on their own team.
     *
     * @param playerRequirements The player requirements
     */
    public IndividualPlayerHandler(Bounds playerRequirements) {
        super.setTotalPlayerBounds(playerRequirements);
    }

    /**
     * A function used to generate a team name for each generated individual team. This, by default, just returns
     * the player's name as their team name. This method is to be overridden if a custom generation is needed.
     *
     * @param player The player of this team
     * @return The generated name
     */
    public String generateTeamName(Player player) {
        return player.getName();
    }

    /**
     * A function used to generate a team color for each generated individual team. This, by default, just returns
     * the RED {@link TeamColor}. This method is to be overridden if a custom generation is needed.
     *
     * @param player The player of this team
     * @return The generated color
     */
    public TeamColor generateTeamColor(Player player) {
        return TeamColor.RED;
    }
}
