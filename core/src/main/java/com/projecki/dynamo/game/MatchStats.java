package com.projecki.dynamo.game;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.projecki.fusion.statistic.StatisticLoader;
import com.projecki.fusion.statistic.StatisticType;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A sort of temporary cache for statistics that will eventually be saved when the game ends
 */
public class MatchStats {

    private final Table<UUID, StatisticType, Long> matchStats = HashBasedTable.create();

    /**
     * Sets a statistic in the match stats cache. This statistic is put, not added so any previous value, null or
     * present, is overwritten. This works almost exactly like the way statistic loader does.
     *
     * @param uuid The uuid key to associate with this statistic
     * @param type The type of this statistic
     * @param value The statistic value
     */
    public void setMatchStat(UUID uuid, StatisticType type, long value) {
        this.matchStats.put(uuid, type, value);
    }

    /**
     * Increment an existing statistic by the difference given. If the existing statistic doesn't exist then this
     * method acts similar to {@code setMatchStat} in which difference value given will be the value stored.
     *
     * @param uuid The uuid key to associate with this statistic
     * @param type The type of this statistic
     * @param diff The difference to increment this statistic by. Can be negative.
     */
    public void incrementMatchStat(UUID uuid, StatisticType type, long diff) {
        long existing = Optional.ofNullable(this.matchStats.get(uuid, type)).orElse(0L);
        long toSet = existing + diff;
        this.setMatchStat(uuid, type, toSet);
    }

    /**
     * Save this cache of match stats to a statistic loader. At the time of writing, an SQL statistic loader is provided
     * in fusion paper.
     *
     * @param loader The loader to save this cache to
     */
    public void saveToStatistics(StatisticLoader loader) {
        matchStats.rowMap().forEach((uuid, map) -> map.forEach((type, value) -> loader.incrementStatistic(uuid, type, value)));
    }

    /**
     * Gets the statistic associated with the given uuid and given statistic type. If there isn't a statistic value
     * associated with the given uuid and statistic type then an empty optional is returned.
     *
     * @param uuid The uuid to find the statistic value of
     * @param type The type of statistic to get
     * @return The statistic associated with the given uuid and type or an empty optional if there was none found
     */
    public Optional<Long> getStat(UUID uuid, StatisticType type) {
        return Optional.ofNullable(matchStats.get(uuid, type));
    }

    /**
     * @return An unmodifiable copy of the table stored in this match stat cache.
     */
    @Unmodifiable
    public ImmutableTable<UUID, StatisticType, Long> getMatchStats() {
        return ImmutableTable.copyOf(matchStats);
    }

    /**
     * Gets the largest statistic value of a given statistic type returned in an optional of a map entry from the uuid
     * associated with this statistic and the value of the statistic. If there are no statistics of the given type
     * stored in this cache then an empty optional is returned.
     *
     * @param type The type of match stat to look for the highest value of.
     * @return An optional containing the mapped entry of the uuid to the highest statistic value of the given type or
     *      an empty optional if there were no statistics of the given type.
     */
    public Optional<Map.Entry<UUID, Long>> getHighestStat(StatisticType type) {
        return Optional.ofNullable(matchStats.columnMap().get(type))
                .flatMap(map -> map.entrySet().stream().max(Comparator.comparingLong(Map.Entry::getValue)));
    }
}
