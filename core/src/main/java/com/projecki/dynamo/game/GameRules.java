package com.projecki.dynamo.game;

import com.google.common.collect.ImmutableMap;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a map of {@link GameRule} accounting for their static and generic nature.
 * As of Spigot-Api 1.19, all GameRules conform to either {@link Integer} or {@link Boolean} type.
 * Any other types will need to be supported explicitly in the future.
 */
public class GameRules {

    private final Map<GameRule<Integer>, Integer> integerRules = new HashMap<>();
    private final Map<GameRule<Boolean>, Boolean> booleanRules = new HashMap<>();

    @Unmodifiable
    public Map<GameRule<Integer>, Integer> getIntegerRules() {
        return ImmutableMap.copyOf(integerRules);
    }

    @Unmodifiable
    public Map<GameRule<Boolean>, Boolean> getBooleanRules() {
        return ImmutableMap.copyOf(booleanRules);
    }

    public GameRules setGameRule(GameRule<Integer> rule, Integer value) {
        integerRules.put(rule, value);
        return this;
    }

    public GameRules setGameRule(GameRule<Boolean> rule, Boolean value) {
        booleanRules.put(rule, value);
        return this;
    }

    public void formatWorld(World world) {
        getIntegerRules().forEach(world::setGameRule);
        getBooleanRules().forEach(world::setGameRule);
    }

}
