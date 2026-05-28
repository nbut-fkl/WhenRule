package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.WhenRuleTimeRange;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeRangeConditionTest {

    private final TimeRangeCondition condition = new TimeRangeCondition(
            new WhenRuleTimeRange(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 18, 0)
            )
    );

    @Test
    void shouldMatchInclusiveStartBoundary() {
        assertTrue(condition.test(
                LocalDateTime.of(2026, 1, 1, 9, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldMatchInclusiveEndBoundary() {
        assertTrue(condition.test(
                LocalDateTime.of(2026, 1, 1, 18, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldMatchInsideRange() {
        assertTrue(condition.test(
                LocalDateTime.of(2026, 1, 1, 12, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldNotMatchBeforeStart() {
        assertFalse(condition.test(
                LocalDateTime.of(2026, 1, 1, 8, 59),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldNotMatchAfterEnd() {
        assertFalse(condition.test(
                LocalDateTime.of(2026, 1, 1, 18, 1),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldReturnFalseWhenCalculateTimeIsNull() {
        assertFalse(condition.test(null, CountryEnums.CN, RegionEnums.ASIA_SHANGHAI));
    }

    @Test
    void shouldMatchWhenOnlyStartIsSet() {
        TimeRangeCondition onlyStart = new TimeRangeCondition(
                new WhenRuleTimeRange(LocalDateTime.of(2026, 1, 1, 9, 0), null)
        );
        assertTrue(onlyStart.test(
                LocalDateTime.of(2026, 12, 31, 23, 59),
                CountryEnums.CN,
                null
        ));
    }

    @Test
    void shouldMatchWhenOnlyEndIsSet() {
        TimeRangeCondition onlyEnd = new TimeRangeCondition(
                new WhenRuleTimeRange(null, LocalDateTime.of(2026, 1, 1, 18, 0))
        );
        assertTrue(onlyEnd.test(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                CountryEnums.CN,
                null
        ));
    }

    @Test
    void shouldPassWhenRangeHasNoBounds() {
        TimeRangeCondition noBounds = new TimeRangeCondition(new WhenRuleTimeRange(null, null));
        assertTrue(noBounds.test(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                CountryEnums.CN,
                null
        ));
    }
}
