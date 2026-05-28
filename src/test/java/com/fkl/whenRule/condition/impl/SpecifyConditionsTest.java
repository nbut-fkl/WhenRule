package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpecifyConditionsTest {

    private static final CountryEnums CN = CountryEnums.CN;
    private static final RegionEnums REGION = RegionEnums.ASIA_SHANGHAI;

    @Test
    void specifyWeekDayShouldMatchConfiguredDay() {
        SpecifyWeekDayCondition condition = new SpecifyWeekDayCondition(List.of(1, 5));
        assertTrue(condition.test(LocalDateTime.of(2026, 1, 5, 0, 0), CN, REGION));
        assertFalse(condition.test(LocalDateTime.of(2026, 1, 6, 0, 0), CN, REGION));
    }

    @Test
    void specifyWeekDayShouldReturnFalseForEmptyList() {
        SpecifyWeekDayCondition condition = new SpecifyWeekDayCondition(Collections.emptyList());
        assertFalse(condition.test(LocalDateTime.of(2026, 1, 5, 0, 0), CN, REGION));
    }

    @Test
    void specifyDayShouldMatchConfiguredDayOfMonth() {
        SpecifyDayCondition condition = new SpecifyDayCondition(List.of(1, 15));
        assertTrue(condition.test(LocalDateTime.of(2026, 3, 1, 0, 0), CN, REGION));
        assertFalse(condition.test(LocalDateTime.of(2026, 3, 2, 0, 0), CN, REGION));
    }

    @Test
    void specifyMonthShouldMatchConfiguredMonth() {
        SpecifyMonthCondition condition = new SpecifyMonthCondition(List.of(1, 6));
        assertTrue(condition.test(LocalDateTime.of(2026, 1, 10, 0, 0), CN, REGION));
        assertFalse(condition.test(LocalDateTime.of(2026, 3, 10, 0, 0), CN, REGION));
    }

    @Test
    void specifyYearShouldMatchConfiguredYear() {
        SpecifyYearCondition condition = new SpecifyYearCondition(List.of(2025, 2026));
        assertTrue(condition.test(LocalDateTime.of(2026, 1, 1, 0, 0), CN, REGION));
        assertFalse(condition.test(LocalDateTime.of(2024, 1, 1, 0, 0), CN, REGION));
    }

    @Test
    void specifyConditionsShouldReturnTrueWhenDataListIsNull() {
        assertTrue(new SpecifyWeekDayCondition(null).test(LocalDateTime.of(2026, 1, 1, 0, 0), CN, REGION));
        assertTrue(new SpecifyDayCondition(null).test(LocalDateTime.of(2026, 1, 1, 0, 0), CN, REGION));
        assertTrue(new SpecifyMonthCondition(null).test(LocalDateTime.of(2026, 1, 1, 0, 0), CN, REGION));
        assertTrue(new SpecifyYearCondition(null).test(LocalDateTime.of(2026, 1, 1, 0, 0), CN, REGION));
    }
}
