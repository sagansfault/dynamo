package com.projecki.dynamo;

import com.projecki.dynamo.game.MatchStats;
import com.projecki.fusion.statistic.StatisticType;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatchStatsTest {

    private static final UUID UUID1 = UUID.randomUUID();
    private static final UUID UUID2 = UUID.randomUUID();
    private static final UUID UUID3 = UUID.randomUUID();

    @Test
    public void matchStatHighestScoreTest() {
        MatchStats matchStats = getTestMatchStats();
        Optional<Map.Entry<UUID, Long>> highestStat = matchStats.getHighestStat(TestStatType.DEATHS);
        Optional<Map.Entry<UUID, Long>> empty = matchStats.getHighestStat(TestStatType.DO_NOT_ENTER);
        assertTrue(highestStat.isPresent());
        assertTrue(empty.isEmpty());
        Map.Entry<UUID, Long> entry = highestStat.get();
        assertEquals(entry.getKey(), UUID2);
        assertEquals(entry.getValue(), 878241L);
    }

    @Test
    public void diffStatTest() {
        MatchStats matchStats = getTestMatchStats();

        matchStats.incrementMatchStat(UUID1, TestStatType.KILLS, 2);
        Optional<Long> statOpt = matchStats.getStat(UUID1, TestStatType.KILLS);
        assertTrue(statOpt.isPresent());
        assertEquals(statOpt.get(), 2L);

        matchStats.incrementMatchStat(UUID1, TestStatType.KILLS, 2);
        statOpt = matchStats.getStat(UUID1, TestStatType.KILLS);
        assertTrue(statOpt.isPresent());
        assertEquals(statOpt.get(), 4L);
    }

    private MatchStats getTestMatchStats() {
        MatchStats matchStats = new MatchStats();
        matchStats.setMatchStat(UUID1, TestStatType.DEATHS, 3);
        matchStats.setMatchStat(UUID2, TestStatType.DEATHS, 878241);
        matchStats.setMatchStat(UUID3, TestStatType.DEATHS, 2);
        return matchStats;
    }

    private enum TestStatType implements StatisticType {
        KILLS("kills"),
        DEATHS("deaths"),
        DO_NOT_ENTER("dne")
        ;

        private final String id;

        TestStatType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }
}
