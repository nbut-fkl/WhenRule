package com.fkl.whenRule;

import com.fkl.whenRule.condition.impl.HolidayCondition;
import com.fkl.whenRule.condition.impl.TimeRangeCondition;
import com.fkl.whenRule.condition.impl.WeekendCondition;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhenRuleBuilderTest {

    @Test
    void shouldReplaceExistingConditionWhenCalledTwice() {
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .holiday(true)
                .holiday(false);

        assertEquals(1, builder.getConditionList().size());
        assertInstanceOf(HolidayCondition.class, builder.getConditionList().get(0));
    }

    @Test
    void shouldKeepOnlyLatestWeekendCondition() {
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .weekend(true)
                .weekend(false);

        assertEquals(1, builder.getConditionList().size());
        assertInstanceOf(WeekendCondition.class, builder.getConditionList().get(0));
    }

    @Test
    void shouldKeepOnlyLatestTimeRangeCondition() {
        WhenRuleTimeRange first = new WhenRuleTimeRange(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 2, 0, 0)
        );
        WhenRuleTimeRange second = new WhenRuleTimeRange(
                LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 2, 2, 0, 0)
        );

        WhenRuleBuilder builder = new WhenRuleBuilder()
                .timeRange(first)
                .timeRange(second);

        assertEquals(1, builder.getConditionList().size());
        assertInstanceOf(TimeRangeCondition.class, builder.getConditionList().get(0));
        assertSame(second, builder.getTimeRange());
    }

    @Test
    void shouldSyncRegionWhenCountryChanges() {
        WhenRuleBuilder builder = new WhenRuleBuilder().country(CountryEnums.US);
        assertEquals(CountryEnums.US, builder.getCountry());
        assertEquals(RegionEnums.AMERICA_NEW_YORK, builder.getRegion());
    }

    @Test
    void shouldRemoveSpecifyConditionWhenNullIsPassed() {
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .specifyMonth(List.of(1, 2))
                .specifyMonth(null);

        assertTrue(builder.getConditionList().isEmpty());
        assertNull(builder.getSpecifyMonth());
    }
}
