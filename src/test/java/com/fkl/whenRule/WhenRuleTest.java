package com.fkl.whenRule;

import com.fkl.whenRule.condition.impl.HolidayCondition;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.MatchingModelEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhenRuleTest {

    @AfterEach
    void tearDown() {
        HolidayCondition.clearCache();
    }

    private WhenRuleBuilder builderAt(LocalDateTime time) {
        return new WhenRuleBuilder()
                .calculateTime(ZonedDateTime.of(time, ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void shouldReturnTrueWhenNoConditions() {
        assertTrue(WhenRule.when(new WhenRuleBuilder()));
    }

    @Test
    void shouldMatchAllConditionsInAllMode() {
        WhenRuleBuilder builder = builderAt(LocalDateTime.of(2026, 3, 10, 12, 0))
                .matchingModel(MatchingModelEnums.ALL)
                .holiday(false)
                .weekend(false)
                .specifyMonth(List.of(3));

        assertTrue(WhenRule.when(builder));
    }

    @Test
    void shouldFailAllModeWhenHolidayAndWeekendConflictOnAdjustedWorkday() {
        WhenRuleBuilder builder = builderAt(LocalDateTime.of(2026, 2, 14, 12, 0))
                .matchingModel(MatchingModelEnums.ALL)
                .country(CountryEnums.CN)
                .region(RegionEnums.ASIA_SHANGHAI)
                .holiday(true)
                .weekend(true);

        assertFalse(WhenRule.when(builder));
    }

    @Test
    void shouldMatchAnyModeWhenOneConditionMatches() {
        WhenRuleBuilder builder = builderAt(LocalDateTime.of(2026, 2, 14, 12, 0))
                .matchingModel(MatchingModelEnums.ANY)
                .country(CountryEnums.CN)
                .region(RegionEnums.ASIA_SHANGHAI)
                .holiday(true)
                .weekend(true);

        assertTrue(WhenRule.when(builder));
    }

    @Test
    void shouldMatchTimeRangeAtBoundary() {
        WhenRuleBuilder builder = builderAt(LocalDateTime.of(2026, 1, 1, 9, 0))
                .timeRange(new WhenRuleTimeRange(
                        LocalDateTime.of(2026, 1, 1, 9, 0),
                        LocalDateTime.of(2026, 1, 1, 18, 0)
                ));

        assertTrue(WhenRule.when(builder));
    }
}
