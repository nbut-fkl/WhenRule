package com.fkl.whenRule.template;

import com.fkl.whenRule.WhenRuleBuilder;
import com.fkl.whenRule.WhenRuleDailyTimeRange;
import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内置规则模板，一行代码快速启用常见场景。
 * <p>
 * 返回的 {@link WhenRuleBuilder} 未设置 {@code calculateTime}，调用方需自行指定待判断时间。
 *
 * @author fkl
 * @since 2026/6/1
 */
public final class WhenRuleTemplates {

    private static final LocalTime DEFAULT_MORNING_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime DEFAULT_AFTERNOON_START = LocalTime.of(14, 0);
    private static final LocalTime DEFAULT_AFTERNOON_END = LocalTime.of(18, 0);
    private static final LocalTime DEFAULT_BUSINESS_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_BUSINESS_END = LocalTime.of(18, 0);
    private static final LocalTime DEFAULT_EVENING_START = LocalTime.of(18, 0);
    private static final LocalTime DEFAULT_EVENING_END = LocalTime.of(22, 0);
    private static final LocalTime DEFAULT_LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime DEFAULT_LUNCH_END = LocalTime.of(13, 30);
    private static final LocalTime DEFAULT_NIGHT_SHIFT_START = LocalTime.of(22, 0);
    private static final LocalTime DEFAULT_NIGHT_SHIFT_END = LocalTime.of(6, 0);

    private static final List<Integer> QUARTER_END_MONTHS = List.of(3, 6, 9, 12);
    private static final List<Integer> PAYDAY_DAYS = List.of(10, 15);

    private WhenRuleTemplates() {
    }

    // ── 节假日 / 工作日 ──────────────────────────────────────────────

    /**
     * 法定节假日（含自然周末，排除调休补班）。
     */
    public static WhenRuleBuilder officialHoliday() {
        return new WhenRuleBuilder().holiday(true);
    }

    /**
     * 法定节假日，并指定本次计算使用的节假日数据源。
     */
    public static WhenRuleBuilder officialHoliday(HolidayDataProvider holidayDataProvider) {
        return new WhenRuleBuilder()
                .holiday(true)
                .holidayDataProvider(holidayDataProvider);
    }

    /**
     * 工作日（周一至周五 + 调休补班，排除法定放假）。
     */
    public static WhenRuleBuilder workday() {
        return new WhenRuleBuilder().holiday(false);
    }

    /**
     * 工作日，并指定本次计算使用的节假日数据源。
     */
    public static WhenRuleBuilder workday(HolidayDataProvider holidayDataProvider) {
        return new WhenRuleBuilder()
                .holiday(false)
                .holidayDataProvider(holidayDataProvider);
    }

    /**
     * 自然周末（周六、周日，不感知调休）。
     */
    public static WhenRuleBuilder weekend() {
        return new WhenRuleBuilder().weekend(true);
    }

    /**
     * 自然周一至周五（不感知调休）。
     */
    public static WhenRuleBuilder weekday() {
        return new WhenRuleBuilder().weekend(false);
    }

    /**
     * 自然周一至周五（按周几指定，等效于 {@link #weekday()}）。
     */
    public static WhenRuleBuilder mondayToFriday() {
        return weeklyOnWeekdays(1, 2, 3, 4, 5);
    }

    // ── 每日时段（含调休感知的工作日组合）────────────────────────────

    /**
     * 工作日上午（默认 09:00–12:00，含调休感知的工作日判断）。
     */
    public static WhenRuleBuilder workdayMorning() {
        return workdayMorning(DEFAULT_MORNING_START, DEFAULT_MORNING_END);
    }

    /**
     * 工作日上午，自定义每日时间窗口。
     */
    public static WhenRuleBuilder workdayMorning(LocalTime start, LocalTime end) {
        return workday().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 工作日下午（默认 14:00–18:00）。
     */
    public static WhenRuleBuilder workdayAfternoon() {
        return workdayAfternoon(DEFAULT_AFTERNOON_START, DEFAULT_AFTERNOON_END);
    }

    /**
     * 工作日下午，自定义每日时间窗口。
     */
    public static WhenRuleBuilder workdayAfternoon(LocalTime start, LocalTime end) {
        return workday().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 工作日晚上（默认 18:00–22:00）。
     */
    public static WhenRuleBuilder workdayEvening() {
        return workdayEvening(DEFAULT_EVENING_START, DEFAULT_EVENING_END);
    }

    /**
     * 工作日晚上，自定义每日时间窗口。
     */
    public static WhenRuleBuilder workdayEvening(LocalTime start, LocalTime end) {
        return workday().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 工作时段（默认 09:00–18:00，含调休感知的工作日判断）。
     */
    public static WhenRuleBuilder businessHours() {
        return businessHours(DEFAULT_BUSINESS_START, DEFAULT_BUSINESS_END);
    }

    /**
     * 工作时段，自定义每日时间窗口。
     */
    public static WhenRuleBuilder businessHours(LocalTime start, LocalTime end) {
        return workday().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 午休时段（默认 12:00–13:30，仅判断时间，不含工作日限制）。
     */
    public static WhenRuleBuilder lunchBreak() {
        return lunchBreak(DEFAULT_LUNCH_START, DEFAULT_LUNCH_END);
    }

    /**
     * 午休时段，自定义每日时间窗口。
     */
    public static WhenRuleBuilder lunchBreak(LocalTime start, LocalTime end) {
        return new WhenRuleBuilder().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 夜班跨午夜时段（默认 22:00–次日 06:00，仅判断时间）。
     */
    public static WhenRuleBuilder nightShift() {
        return nightShift(DEFAULT_NIGHT_SHIFT_START, DEFAULT_NIGHT_SHIFT_END);
    }

    /**
     * 跨午夜时段，自定义每日时间窗口。
     */
    public static WhenRuleBuilder nightShift(LocalTime start, LocalTime end) {
        return new WhenRuleBuilder().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 工作日夜班（工作日 + 跨午夜时段，默认 22:00–次日 06:00）。
     */
    public static WhenRuleBuilder workdayNightShift() {
        return workdayNightShift(DEFAULT_NIGHT_SHIFT_START, DEFAULT_NIGHT_SHIFT_END);
    }

    /**
     * 工作日夜班，自定义跨午夜时段。
     */
    public static WhenRuleBuilder workdayNightShift(LocalTime start, LocalTime end) {
        return workday().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    // ── 周末 / 节假日 + 时段 ────────────────────────────────────────

    /**
     * 周末上午（默认 09:00–12:00）。
     */
    public static WhenRuleBuilder weekendMorning() {
        return weekendMorning(DEFAULT_MORNING_START, DEFAULT_MORNING_END);
    }

    /**
     * 周末上午，自定义每日时间窗口。
     */
    public static WhenRuleBuilder weekendMorning(LocalTime start, LocalTime end) {
        return weekend().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 周末下午（默认 14:00–18:00）。
     */
    public static WhenRuleBuilder weekendAfternoon() {
        return weekendAfternoon(DEFAULT_AFTERNOON_START, DEFAULT_AFTERNOON_END);
    }

    /**
     * 周末下午，自定义每日时间窗口。
     */
    public static WhenRuleBuilder weekendAfternoon(LocalTime start, LocalTime end) {
        return weekend().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 节假日上午（默认 09:00–12:00，含调休感知）。
     */
    public static WhenRuleBuilder holidayMorning() {
        return holidayMorning(DEFAULT_MORNING_START, DEFAULT_MORNING_END);
    }

    /**
     * 节假日上午，自定义每日时间窗口。
     */
    public static WhenRuleBuilder holidayMorning(LocalTime start, LocalTime end) {
        return officialHoliday().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    /**
     * 节假日下午（默认 14:00–18:00，含调休感知）。
     */
    public static WhenRuleBuilder holidayAfternoon() {
        return holidayAfternoon(DEFAULT_AFTERNOON_START, DEFAULT_AFTERNOON_END);
    }

    /**
     * 节假日下午，自定义每日时间窗口。
     */
    public static WhenRuleBuilder holidayAfternoon(LocalTime start, LocalTime end) {
        return officialHoliday().dailyTimeRange(new WhenRuleDailyTimeRange(start, end));
    }

    // ── 按月 / 按周 / 按年 ───────────────────────────────────────────

    /**
     * 每月 1 号。
     */
    public static WhenRuleBuilder firstDayOfMonth() {
        return monthlyOnDays(1);
    }

    /**
     * 每月 15 号（月中）。
     */
    public static WhenRuleBuilder midMonth() {
        return monthlyOnDays(15);
    }

    /**
     * 每月最后一天（如 1 月 31 日、2 月 28/29 日、4 月 30 日）。
     */
    public static WhenRuleBuilder lastDayOfMonth() {
        return new WhenRuleBuilder().lastDayOfMonth();
    }

    /**
     * 常见发薪日（每月 10 号、15 号）。
     */
    public static WhenRuleBuilder payday() {
        return monthlyOnDays(PAYDAY_DAYS);
    }

    /**
     * 指定每月若干日期（1–31）。
     */
    public static WhenRuleBuilder monthlyOnDays(int... days) {
        return new WhenRuleBuilder().specifyDay(toIntegerList(days));
    }

    /**
     * 指定每月若干日期。
     */
    public static WhenRuleBuilder monthlyOnDays(List<Integer> days) {
        return new WhenRuleBuilder().specifyDay(days);
    }

    /**
     * 每周一。
     */
    public static WhenRuleBuilder everyMonday() {
        return weeklyOnWeekdays(1);
    }

    /**
     * 每周五。
     */
    public static WhenRuleBuilder everyFriday() {
        return weeklyOnWeekdays(5);
    }

    /**
     * 每周日。
     */
    public static WhenRuleBuilder everySunday() {
        return weeklyOnWeekdays(7);
    }

    /**
     * 指定每周若干星期（1=周一，7=周日）。
     */
    public static WhenRuleBuilder weeklyOnWeekdays(int... weekdays) {
        return new WhenRuleBuilder().specifyWeekday(toIntegerList(weekdays));
    }

    /**
     * 指定每周若干星期。
     */
    public static WhenRuleBuilder weeklyOnWeekdays(List<Integer> weekdays) {
        return new WhenRuleBuilder().specifyWeekday(weekdays);
    }

    /**
     * 第一季度（1–3 月）。
     */
    public static WhenRuleBuilder q1() {
        return inMonths(1, 2, 3);
    }

    /**
     * 第二季度（4–6 月）。
     */
    public static WhenRuleBuilder q2() {
        return inMonths(4, 5, 6);
    }

    /**
     * 第三季度（7–9 月）。
     */
    public static WhenRuleBuilder q3() {
        return inMonths(7, 8, 9);
    }

    /**
     * 第四季度（10–12 月）。
     */
    public static WhenRuleBuilder q4() {
        return inMonths(10, 11, 12);
    }

    /**
     * 季度末最后一天（3/6/9/12 月的最后一天）。
     */
    public static WhenRuleBuilder quarterEnd() {
        return lastDayOfMonth().specifyMonth(QUARTER_END_MONTHS);
    }

    /**
     * 指定若干月份（1–12）。
     */
    public static WhenRuleBuilder inMonths(int... months) {
        return new WhenRuleBuilder().specifyMonth(toIntegerList(months));
    }

    /**
     * 指定若干月份。
     */
    public static WhenRuleBuilder inMonths(List<Integer> months) {
        return new WhenRuleBuilder().specifyMonth(months);
    }

    /**
     * 指定年份（四位数，如 2026）。
     */
    public static WhenRuleBuilder inYear(int year) {
        return new WhenRuleBuilder().specifyYear(List.of(year));
    }

    /**
     * 指定若干年份。
     */
    public static WhenRuleBuilder inYears(int... years) {
        return new WhenRuleBuilder().specifyYear(toIntegerList(years));
    }

    private static List<Integer> toIntegerList(int[] values) {
        return Arrays.stream(values).boxed().collect(Collectors.toList());
    }
}
