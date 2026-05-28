package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HolidayConditionTest {

    private static final RegionEnums REGION = RegionEnums.ASIA_SHANGHAI;

    @AfterEach
    void tearDown() {
        HolidayCondition.clearCache();
    }

    @Test
    void shouldMatchOfficialHolidayWhenFlagIsTrue() {
        HolidayCondition condition = new HolidayCondition(true);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldMatchNaturalWeekendWhenFlagIsTrue() {
        HolidayCondition condition = new HolidayCondition(true);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 3, 7, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldExcludeAdjustedWorkdayOnWeekendWhenFlagIsTrue() {
        HolidayCondition condition = new HolidayCondition(true);
        assertFalse(condition.test(
                LocalDateTime.of(2026, 2, 14, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldMatchNormalWeekdayWhenFlagIsFalse() {
        HolidayCondition condition = new HolidayCondition(false);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 3, 10, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldExcludeOfficialHolidayWhenFlagIsFalse() {
        HolidayCondition condition = new HolidayCondition(false);
        assertFalse(condition.test(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldIncludeAdjustedWorkdayWhenFlagIsFalse() {
        HolidayCondition condition = new HolidayCondition(false);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 2, 14, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldReturnFalseForUnsupportedCountry() {
        HolidayCondition condition = new HolidayCondition(true);
        assertFalse(condition.test(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                CountryEnums.US,
                RegionEnums.AMERICA_NEW_YORK
        ));
    }

    @Test
    void shouldReturnFalseWhenCalculateTimeIsNull() {
        HolidayCondition condition = new HolidayCondition(true);
        assertFalse(condition.test(null, CountryEnums.CN, REGION));
    }

    @Test
    void shouldUseCacheForRepeatedLoads() {
        HolidayCondition condition = new HolidayCondition(true);
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 10, 0);
        assertTrue(condition.test(time, CountryEnums.CN, REGION));
        assertTrue(condition.test(time, CountryEnums.CN, REGION));
    }
}
