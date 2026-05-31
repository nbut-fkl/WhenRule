package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.WhenRuleDailyTimeRange;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DailyTimeRangeConditionTest {

    private final DailyTimeRangeCondition condition = new DailyTimeRangeCondition(
            new WhenRuleDailyTimeRange(LocalTime.of(14, 0), LocalTime.of(17, 0))
    );

    @Test
    void shouldMatchInclusiveStartBoundary() {
        assertTrue(condition.test(
                LocalDateTime.of(2026, 3, 10, 14, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldMatchInclusiveEndBoundary() {
        assertTrue(condition.test(
                LocalDateTime.of(2026, 3, 10, 17, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldMatchInsideRangeRegardlessOfDate() {
        // 跨年、跨月、随便哪一天，只要时分在区间内都应匹配
        assertTrue(condition.test(
                LocalDateTime.of(2025, 12, 31, 15, 30),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        assertTrue(condition.test(
                LocalDateTime.of(2030, 6, 1, 16, 59),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldNotMatchBeforeStart() {
        assertFalse(condition.test(
                LocalDateTime.of(2026, 3, 10, 13, 59),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldNotMatchAfterEnd() {
        assertFalse(condition.test(
                LocalDateTime.of(2026, 3, 10, 17, 1),
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
        DailyTimeRangeCondition onlyStart = new DailyTimeRangeCondition(
                new WhenRuleDailyTimeRange(LocalTime.of(9, 0), null)
        );
        assertTrue(onlyStart.test(
                LocalDateTime.of(2026, 1, 1, 23, 59),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        assertFalse(onlyStart.test(
                LocalDateTime.of(2026, 1, 1, 8, 59),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldMatchWhenOnlyEndIsSet() {
        DailyTimeRangeCondition onlyEnd = new DailyTimeRangeCondition(
                new WhenRuleDailyTimeRange(null, LocalTime.of(18, 0))
        );
        assertTrue(onlyEnd.test(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        assertFalse(onlyEnd.test(
                LocalDateTime.of(2026, 1, 1, 18, 1),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldPassWhenBothBoundsAreNull() {
        DailyTimeRangeCondition noBounds = new DailyTimeRangeCondition(
                new WhenRuleDailyTimeRange(null, null)
        );
        assertTrue(noBounds.test(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldPassWhenDailyTimeRangeIsNull() {
        DailyTimeRangeCondition nullRange = new DailyTimeRangeCondition(null);
        assertTrue(nullRange.test(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldHandleOvernightRange() {
        // 22:00 ~ 02:00 跨午夜
        DailyTimeRangeCondition overnight = new DailyTimeRangeCondition(
                new WhenRuleDailyTimeRange(LocalTime.of(22, 0), LocalTime.of(2, 0))
        );

        // 当晚 23:30 命中
        assertTrue(overnight.test(
                LocalDateTime.of(2026, 3, 10, 23, 30),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        // 次日凌晨 1:30 命中
        assertTrue(overnight.test(
                LocalDateTime.of(2026, 3, 11, 1, 30),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        // 起点 22:00 命中（闭区间）
        assertTrue(overnight.test(
                LocalDateTime.of(2026, 3, 10, 22, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        // 终点 02:00 命中（闭区间）
        assertTrue(overnight.test(
                LocalDateTime.of(2026, 3, 11, 2, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        // 中午 12:00 不命中
        assertFalse(overnight.test(
                LocalDateTime.of(2026, 3, 10, 12, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        // 21:59 不命中
        assertFalse(overnight.test(
                LocalDateTime.of(2026, 3, 10, 21, 59),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
        // 02:01 不命中
        assertFalse(overnight.test(
                LocalDateTime.of(2026, 3, 11, 2, 1),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }
}
