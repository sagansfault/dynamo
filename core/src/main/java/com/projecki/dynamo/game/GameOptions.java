package com.projecki.dynamo.game;

import com.projecki.dynamo.team.TeamColor;
import com.projecki.fusion.util.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record GameOptions(
        boolean glowingTeammates,
        Map<String, Integer> worldLoadingDiameters,
        GameRules gameRules,
        GameMode gameMode,
        @Nullable Function<Player, Pair<Integer, Component>> playerHealthBarAboveHeadFormat,
        TeamOptions teamOptions
) {

    // default options
    public GameOptions() {
        this(
                false,
                new HashMap<>(),
                new GameRules().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true),
                GameMode.SURVIVAL,
                null,
                new TeamOptions(true, true, true)
        );
    }

    public static final class Builder {

        private boolean glowingTeammates = false;
        private final Map<String, Integer> worldLoadingDiameters = new HashMap<>();
        private GameRules gameRules = new GameRules();
        private GameMode gameMode = GameMode.SURVIVAL;
        private @Nullable Function<Player, Pair<Integer, Component>> playerHealthBarAboveHeadFormat = null;
        private TeamOptions teamOptions = new TeamOptions(true, true, true);

        public Builder glowingTeammates(boolean glowingTeammates) {
            this.glowingTeammates = glowingTeammates;
            return this;
        }

        public Builder setWorldLoadingDiameter(String world, int diameter) {
            this.worldLoadingDiameters.put(world, diameter);
            return this;
        }

        public Builder setGameRules(GameRules gameRules) {
            this.gameRules = gameRules;
            return this;
        }

        public Builder setGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder setHealthBarAboveHead(@Nullable Function<Player, Pair<Integer, Component>> playerHealthBarAboveHeadFormat) {
            this.playerHealthBarAboveHeadFormat = playerHealthBarAboveHeadFormat;
            return this;
        }

        public Builder setTeamOptions(TeamOptions teamOptions) {
            this.teamOptions = teamOptions;
            return this;
        }

        public GameOptions build() {
            return new GameOptions(
                    this.glowingTeammates,
                    this.worldLoadingDiameters,
                    this.gameRules,
                    this.gameMode,
                    this.playerHealthBarAboveHeadFormat,
                    this.teamOptions
            );
        }
    }

    public record TeamOptions(boolean showActionBarUnicode, boolean showTabUnicode, boolean teamColorInTab) {}
}
