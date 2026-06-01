package com.fkl.whenRule.template;

import com.fkl.whenRule.WhenRule;
import com.fkl.whenRule.WhenRuleBuilder;
import com.fkl.whenRule.condition.impl.HolidayCondition;
import com.fkl.whenRule.condition.impl.holiday.ClasspathJsonHolidayDataProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class WhenRuleTemplatesTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    @AfterEach
    void tearDown() {
        HolidayCondition.clearCache();
    }

    private boolean match(Supplier<WhenRuleBuilder> template, LocalDateTime time) {
        return WhenRule.when(template.get()
                .calculateTime(ZonedDateTime.of(time, SHANGHAI)));
    }

    @Test
    void officialHolidayShouldMatchLegalHoliday() {
        assertTrue(match(
                WhenRuleTemplates::officialHoliday,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        ));
    }

    @Test
    void officialHolidayShouldExcludeAdjustedWorkday() {
        assertFalse(match(
                WhenRuleTemplates::officialHoliday,
                LocalDateTime.of(2026, 2, 14, 10, 0)
        ));
    }

    @Test
    void workdayShouldMatchNormalWeekday() {
        assertTrue(match(
                WhenRuleTemplates::workday,
                LocalDateTime.of(2026, 3, 10, 10, 0)
        ));
    }

    @Test
    void workdayShouldExcludeLegalHoliday() {
        assertFalse(match(
                WhenRuleTemplates::workday,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        ));
    }

    @Test
    void workdayShouldIncludeAdjustedWorkdayOnWeekend() {
        assertTrue(match(
                WhenRuleTemplates::workday,
                LocalDateTime.of(2026, 2, 14, 10, 0)
        ));
    }

    @Test
    void weekendShouldMatchNaturalSaturday() {
        assertTrue(match(
                WhenRuleTemplates::weekend,
                LocalDateTime.of(2026, 3, 7, 10, 0)
        ));
    }

    @Test
    void weekdayShouldMatchNaturalTuesday() {
        assertTrue(match(
                WhenRuleTemplates::weekday,
                LocalDateTime.of(2026, 3, 10, 10, 0)
        ));
    }

    @Test
    void workdayMorningShouldMatchWorkdayWithinMorningHours() {
        assertTrue(match(
                WhenRuleTemplates::workdayMorning,
                LocalDateTime.of(2026, 3, 10, 10, 0)
        ));
    }

    @Test
    void workdayMorningShouldRejectAfternoonOnWorkday() {
        assertFalse(match(
                WhenRuleTemplates::workdayMorning,
                LocalDateTime.of(2026, 3, 10, 14, 0)
        ));
    }

    @Test
    void workdayMorningShouldRejectHolidayEvenInMorning() {
        assertFalse(match(
                WhenRuleTemplates::workdayMorning,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        ));
    }

    @Test
    void businessHoursShouldUseCustomRange() {
        assertTrue(match(
                () -> WhenRuleTemplates.businessHours(LocalTime.of(8, 0), LocalTime.of(17, 0)),
                LocalDateTime.of(2026, 3, 10, 16, 30)
        ));
        assertFalse(match(
                () -> WhenRuleTemplates.businessHours(LocalTime.of(8, 0), LocalTime.of(17, 0)),
                LocalDateTime.of(2026, 3, 10, 17, 30)
        ));
    }

    @Test
    void lastDayOfMonthShouldMatchMonthEnd() {
        assertTrue(match(
                WhenRuleTemplates::lastDayOfMonth,
                LocalDateTime.of(2026, 1, 31, 12, 0)
        ));
        assertTrue(match(
                WhenRuleTemplates::lastDayOfMonth,
                LocalDateTime.of(2026, 2, 28, 12, 0)
        ));
        assertFalse(match(
                WhenRuleTemplates::lastDayOfMonth,
                LocalDateTime.of(2026, 1, 30, 12, 0)
        ));
    }

    @Test
    void officialHolidayShouldAcceptCustomProvider() {
        assertTrue(match(
                () -> WhenRuleTemplates.officialHoliday(new ClasspathJsonHolidayDataProvider()),
                LocalDateTime.of(2026, 1, 1, 10, 0)
        ));
    }

    @Test
    void templateShouldAllowFurtherChaining() {
        boolean matched = WhenRule.when(
                WhenRuleTemplates.workday()
                        .calculateTime(ZonedDateTime.of(LocalDateTime.of(2026, 3, 10, 10, 0), SHANGHAI))
                        .specifyMonth(List.of(3))
        );
        assertTrue(matched);
    }

    @Test
    void firstDayOfMonthShouldMatchFirstDayOnly() {
        assertTrue(match(WhenRuleTemplates::firstDayOfMonth, LocalDateTime.of(2026, 3, 1, 10, 0)));
        assertFalse(match(WhenRuleTemplates::firstDayOfMonth, LocalDateTime.of(2026, 3, 2, 10, 0)));
    }

    @Test
    void paydayShouldMatchTenthAndFifteenth() {
        assertTrue(match(WhenRuleTemplates::payday, LocalDateTime.of(2026, 3, 10, 10, 0)));
        assertTrue(match(WhenRuleTemplates::payday, LocalDateTime.of(2026, 3, 15, 10, 0)));
        assertFalse(match(WhenRuleTemplates::payday, LocalDateTime.of(2026, 3, 16, 10, 0)));
    }

    @Test
    void mondayToFridayShouldMatchWeekdayNotWeekend() {
        assertTrue(match(WhenRuleTemplates::mondayToFriday, LocalDateTime.of(2026, 3, 10, 10, 0)));
        assertFalse(match(WhenRuleTemplates::mondayToFriday, LocalDateTime.of(2026, 3, 7, 10, 0)));
    }

    @Test
    void everyFridayShouldMatchOnlyFriday() {
        assertTrue(match(WhenRuleTemplates::everyFriday, LocalDateTime.of(2026, 3, 6, 10, 0)));
        assertFalse(match(WhenRuleTemplates::everyFriday, LocalDateTime.of(2026, 3, 10, 10, 0)));
    }

    @Test
    void q1ShouldMatchJanuaryOnly() {
        assertTrue(match(WhenRuleTemplates::q1, LocalDateTime.of(2026, 1, 15, 10, 0)));
        assertFalse(match(WhenRuleTemplates::q1, LocalDateTime.of(2026, 4, 15, 10, 0)));
    }

    @Test
    void quarterEndShouldMatchLastDayOfQuarter() {
        assertTrue(match(WhenRuleTemplates::quarterEnd, LocalDateTime.of(2026, 3, 31, 10, 0)));
        assertFalse(match(WhenRuleTemplates::quarterEnd, LocalDateTime.of(2026, 3, 30, 10, 0)));
        assertFalse(match(WhenRuleTemplates::quarterEnd, LocalDateTime.of(2026, 4, 30, 10, 0)));
    }

    @Test
    void inYearShouldMatchSpecifiedYear() {
        assertTrue(match(() -> WhenRuleTemplates.inYear(2026), LocalDateTime.of(2026, 5, 1, 10, 0)));
        assertFalse(match(() -> WhenRuleTemplates.inYear(2026), LocalDateTime.of(2025, 5, 1, 10, 0)));
    }

    @Test
    void weekendMorningShouldMatchSaturdayMorning() {
        assertTrue(match(WhenRuleTemplates::weekendMorning, LocalDateTime.of(2026, 3, 7, 10, 0)));
        assertFalse(match(WhenRuleTemplates::weekendMorning, LocalDateTime.of(2026, 3, 7, 14, 0)));
    }

    @Test
    void workdayEveningShouldMatchWorkdayEveningHours() {
        assertTrue(match(WhenRuleTemplates::workdayEvening, LocalDateTime.of(2026, 3, 10, 19, 0)));
        assertFalse(match(WhenRuleTemplates::workdayEvening, LocalDateTime.of(2026, 3, 10, 12, 0)));
    }

    @Test
    void nightShiftShouldMatchCrossMidnightWindow() {
        assertTrue(match(WhenRuleTemplates::nightShift, LocalDateTime.of(2026, 3, 10, 23, 0)));
        assertTrue(match(WhenRuleTemplates::nightShift, LocalDateTime.of(2026, 3, 10, 2, 0)));
        assertFalse(match(WhenRuleTemplates::nightShift, LocalDateTime.of(2026, 3, 10, 12, 0)));
    }

    @Test
    void holidayMorningShouldMatchHolidayInMorning() {
        assertTrue(match(WhenRuleTemplates::holidayMorning, LocalDateTime.of(2026, 1, 1, 10, 0)));
        assertFalse(match(WhenRuleTemplates::holidayMorning, LocalDateTime.of(2026, 3, 10, 10, 0)));
    }

    @Test
    void lunchBreakShouldMatchLunchWindow() {
        assertTrue(match(WhenRuleTemplates::lunchBreak, LocalDateTime.of(2026, 3, 10, 12, 30)));
        assertFalse(match(WhenRuleTemplates::lunchBreak, LocalDateTime.of(2026, 3, 10, 14, 0)));
    }
}
