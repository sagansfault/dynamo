package com.projecki.dynamo.death;

import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.team.Team;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Optional;

public abstract class DeathMessageSupplier<T extends EntityDamageEvent> {

    private final Class<T> type;

    /**
     * @param type The class type this death message supplier handles
     */
    public DeathMessageSupplier(Class<T> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public final Optional<Component> accept(EntityDamageEvent event) {
        if (event.getClass() == type) {
            return get((T) event); // funny java
        }
        return Optional.empty();
    }

    /**
     * Returns an optional containing the death message. If this supplier deems that this is an event it does not handle
     * then an empty optional is returned. Note that the type of the event has already been checked to be the same class
     * (not inheritors).
     *
     * @param event The event to handle
     * @return An optional containing the generated death message or empty if this supplier chose not to handle it
     */
    public abstract Optional<Component> get(T event);

    /**
     * Returns the player's name formatted with the color of their team color. On the edge case that a player's team
     * isn't present, just their name is returned with no current formatting applied.
     *
     * @param player The player of the name for format
     * @return The player's name formatted with their team color
     */
    protected Component formatNameWithTeam(Player player) {
        Optional<Team> teamOpt = Dynamo.getEngine().getGame().getPlayerHandler().getTeam(player.getUniqueId());
        if (teamOpt.isPresent()) {
            Team team = teamOpt.get();
            return player.name().color(team.getColor().getTextColor());
        } else {
            return player.name();
        }
    }
}
