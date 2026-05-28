package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WeekendConditionTest {

    @Test
    void shouldMatchWeekendWhenFlagIsTrue() {
        WeekendCondition condition = new WeekendCondition(true);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 1, 3, 10, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldMatchWeekdayWhenFlagIsFalse() {
        WeekendCondition condition = new WeekendCondition(false);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldNotMatchWeekdayWhenFlagIsTrue() {
        WeekendCondition condition = new WeekendCondition(true);
        assertFalse(condition.test(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldReturnFalseWhenCalculateTimeIsNull() {
        WeekendCondition condition = new WeekendCondition(true);
        assertFalse(condition.test(null, CountryEnums.CN, RegionEnums.ASIA_SHANGHAI));
    }
}
