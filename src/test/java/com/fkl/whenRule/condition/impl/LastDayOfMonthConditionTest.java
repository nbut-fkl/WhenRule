package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LastDayOfMonthConditionTest {

    private final LastDayOfMonthCondition condition = new LastDayOfMonthCondition();

    @Test
    void shouldMatchLastDayOfJanuary() {
        assertTrue(condition.test(
                LocalDateTime.of(2026, 1, 31, 12, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldMatchLastDayOfFebruary() {
        assertTrue(condition.test(
                LocalDateTime.of(2026, 2, 28, 12, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldNotMatchNonLastDay() {
        assertFalse(condition.test(
                LocalDateTime.of(2026, 4, 29, 12, 0),
                CountryEnums.CN,
                RegionEnums.ASIA_SHANGHAI
        ));
    }

    @Test
    void shouldReturnFalseWhenCalculateTimeIsNull() {
        assertFalse(condition.test(null, CountryEnums.CN, RegionEnums.ASIA_SHANGHAI));
    }
}
