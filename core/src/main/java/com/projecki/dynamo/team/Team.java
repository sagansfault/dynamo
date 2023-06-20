package com.projecki.dynamo.team;

import com.projecki.dynamo.Bounds;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.unmodifiableSet;

/**
 * Represents a team/collection of players.
 *
 * Note, there are no hard checks for if a team can 'fit' more players; if you add 4 players to a team of max players 3,
 * dynamo will not stop you.
 */
public class Team {

    private final String teamName;
    private final TeamColor color;
    private final Set<UUID> players;
    private final Bounds requiredPlayers;

    private @Nullable Component colorIconUnicodeOverride;
    private @Nullable Component colorTitleUnicodeOverride;

    public Team(String teamName, TeamColor color, Bounds requiredPlayers, @Nullable Component colorIconUnicodeOverride, @Nullable Component colorTitleUnicodeOverride) {
        this.teamName = teamName;
        this.color = color;
        this.players = new HashSet<>();
        this.requiredPlayers = requiredPlayers;
        this.colorIconUnicodeOverride = colorIconUnicodeOverride;
        this.colorTitleUnicodeOverride = colorTitleUnicodeOverride;
    }

    public Team(String teamName, TeamColor color, Bounds requiredPlayers) {
        this(teamName, color, requiredPlayers, null, null);
    }

    public TeamColor getColor() {
        return color;
    }

    public String getName() {
        return teamName;
    }

    public int getSize() {
        return players.size();
    }

    public int getAvailable() {
        return this.requiredPlayers.max() - this.players.size();
    }

    public Set<UUID> getPlayers() {
        return unmodifiableSet(players);
    }

    public Bounds getRequiredPlayers() {
        return requiredPlayers;
    }

    public Optional<Component> getColorIconUnicodeOverride() {
        return Optional.ofNullable(colorIconUnicodeOverride);
    }

    public Optional<Component> getColorTitleUnicodeOverride() {
        return Optional.ofNullable(colorTitleUnicodeOverride);
    }

    public void setColorIconUnicodeOverride(Component colorIconUnicodeOverride) {
        this.colorIconUnicodeOverride = colorIconUnicodeOverride;
    }

    public void setColorTitleUnicodeOverride(Component colorTitleUnicodeOverride) {
        this.colorTitleUnicodeOverride = colorTitleUnicodeOverride;
    }

    public double getFractionFull() {
        return Math.floor((double) players.size() / (double) requiredPlayers.max());
    }

    public void addPlayer(UUID player) {
        this.players.add(player);
    }

    public void addPlayers(Collection<UUID> players) {
        this.players.addAll(players);
    }

    public void removePlayer(UUID player) {
        this.players.remove(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return teamName.equalsIgnoreCase(team.teamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName);
    }
}
