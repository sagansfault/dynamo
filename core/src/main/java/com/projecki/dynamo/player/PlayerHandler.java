package com.projecki.dynamo.player;

import com.projecki.dynamo.Bounds;
import com.projecki.dynamo.Util;
import com.projecki.dynamo.menu.SpectatorUI;
import com.projecki.dynamo.team.Team;
import com.projecki.fusion.item.HotbarItem;
import com.projecki.fusion.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.unmodifiableSet;

/**
 * Represents a player handler to be implemented by the user.
 */
public abstract class PlayerHandler {

    protected final Set<Team> teams = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();
    private Bounds totalPlayerBounds = Bounds.ONE;

    public void setTeams(Set<Team> teams) {
        this.teams.clear();
        this.teams.addAll(teams);
    }

    protected void setTotalPlayerBounds(Bounds totalPlayerBounds) {
        this.totalPlayerBounds = totalPlayerBounds;
    }

    public Bounds getTotalPlayerBounds() {
        return totalPlayerBounds;
    }

    public Optional<Team> getTeam(UUID player) {
        for (Team team : this.teams) {
            if (team.getPlayers().contains(player)) {
                return Optional.of(team);
            }
        }
        return Optional.empty();
    }

    /**
     * @return The total players on all teams (does not include spectators)
     */
    public int getTotalPlayers() {
        return this.teams.stream().mapToInt(Team::getSize).sum();
    }

    /**
     * @return The total slots left in all teams combined
     */
    public int getTotalAvailable() {
        return this.totalPlayerBounds.max() - this.teams.stream().mapToInt(team -> team.getPlayers().size()).sum();
    }

    public Set<UUID> getSpectators() {
        return unmodifiableSet(this.spectators);
    }

    public boolean isSpectator(UUID uuid) {
        return this.spectators.contains(uuid);
    }

    public final Set<Team> getTeams() {
        return unmodifiableSet(this.teams);
    }

    /**
     * A general purpose function to send a player to 'spectator'.
     *
     * @param plugin The plugin
     * @param player The player to send to spectator
     */
    public void sendToSpectator(JavaPlugin plugin, Player player) {
        Util.cleanPlayer(player);
        player.setGameMode(GameMode.ADVENTURE);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(plugin, player);
        }
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.closeInventory();
        HotbarItem.give(player, 0, ItemBuilder.of(Material.COMPASS).name("Players").build(), p -> new SpectatorUI(plugin).open(p));
        // HotbarItem.give(player, 4, ItemBuilder.of(Material.COMPARATOR).name("Spectator Options").build(), p -> new SpectatorOptionsUI(plugin).open(p));
        this.spectators.add(player.getUniqueId());
    }

    /**
     * Essentially 'removes' a player from spectator. This removes them from the internal spectator set and leaves them
     * just on the team as they were before they were sent to spectator. If this player wasn't a spectator to being with
     * then nothing is done.
     *
     * This will 'clean' the player which includes removing all effects, clearing inventory and more. To see all changes
     * made to the player look in the method itself and in {@code Util.cleanPlayer()} specifically
     *
     * @param plugin The plugin
     * @param player The player to take out of spectator.
     */
    public void removeFromSpectator(JavaPlugin plugin, Player player) {
        boolean contained = this.spectators.remove(player.getUniqueId());
        if (!contained) {
            return;
        }
        player.setCollidable(true);
        player.closeInventory();
        Util.cleanPlayer(player);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(plugin, player);
        }
    }

    /**
     * Removes a given player from all player collections in this handler. This function should not be used in any
     * practice of any game. This is purely intended for internal use.
     *
     * @param player The player to remove
     */
    public void removePlayer(UUID player) {
        this.spectators.remove(player);
        this.teams.forEach(team -> team.removePlayer(player));
    }

    /**
     * Clears all player collections in this player handler rendering them empty. This function should not be used in
     * any practice of any game. This is purely intended for internal use.
     */
    public void clearAll() {
        this.spectators.clear();
        this.teams.clear();
    }

    /**
     * Adds a team to this player handler, overwriting any existing team with the same name
     *
     * @param team The team to add
     */
    public void putTeam(Team team) {
        this.teams.remove(team);
        this.teams.add(team);
    }

    /**
     * Forcefully removes a team from the collection of teams in this player handler
     *
     * @param team The team to remove
     */
    public void removeTeam(Team team) {
        this.teams.remove(team);
    }

    /**
     * Attempts to add a player to the 'best fit' team as defined in this method and such team is returned in an optional.
     * If a player could not be added to any team, an empty optional is returned. This function is not intended for use
     * in any game implementation, purely internal.
     *
     * @param player The player to find a team for
     * @return An optional containing the team found and injected with the player or empty if none could be found
     */
    public Optional<Team> addToBestFitTeam(UUID player) {
        Team lowest = null;
        for (Team team : this.teams) {
            if (team.getFractionFull() < 1.0 && (lowest == null || lowest.getFractionFull() > team.getFractionFull())) {
                lowest = team;
            }
        }
        if (lowest == null) {
            return Optional.empty();
        } else {
            lowest.addPlayer(player);
            return Optional.of(lowest);
        }
    }
}
