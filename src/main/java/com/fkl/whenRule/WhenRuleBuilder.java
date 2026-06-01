package com.fkl.whenRule;

import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.condition.impl.*;
import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;
import com.fkl.whenRule.condition.impl.holiday.JdbcHolidayDataProvider;
import com.fkl.whenRule.entity.WhenRuleBuilderCombination;
import com.fkl.whenRule.enums.ActionEnums;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.MatchingModelEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

/**
 * @author fkl
 * @since 2026/5/28 11:49
 * TODO：基础字段跟新 是否需要根据conditionList里面关联的值
 */
@Getter
@Slf4j
public class WhenRuleBuilder {

    private final List<WhenRuleBuilderCombination> combinationList = new ArrayList<>();

    private final List<RuleCondition> conditionList = new ArrayList<>();

    /*
     * ----------------------------------基础字段------------------------------------
     * */

    /*
     * 国家
     * */
    private CountryEnums country = CountryEnums.CN;
    /*
     * 地区
     * */
    private RegionEnums region = this.country.getDefaultRegion();
    /*
     * 计算时间
     * */
    private ZonedDateTime calculateTime = ZonedDateTime.now(getDefaultZoneId());
    /*
     * 匹配条件
     * */
    private MatchingModelEnums matchingModel = MatchingModelEnums.ALL;

    /*
     * 调用级节假日数据源（可空），仅本次规则计算生效，优先级高于 WhenRuleConfig 全局配置
     * */
    private HolidayDataProvider holidayDataProvider = null;

    /*
     * ----------------------------------------判断字段-----------------------------------
     * */

    /*
     * 匹配时间区间，start为空表示无开始时间限制，end为空表示无结束时间限制，全为空或者null表示无时间限制
     * */
    private WhenRuleTimeRange timeRange = null;
    /*
     * true:节假日 ； false（工作日，包括调休） ； null（不做处理）
     * */
    private Boolean holiday = null;
    /*
     *  true:周末 ； false（周一-周五） ； null（不做处理）
     * */
    private Boolean weekend = null;
    /*
     * 指定周几（1-7），1表示周一，7表示周日，null表示不限制，空表示直接false
     * */
    private List<Integer> specifyWeekday = null;
    /*
     * 指定几号（1-31），1表示一号，31表示三十一号，null表示不限制，空表示直接false
    * */
    private List<Integer> specifyDay = null;
    /*
     * 指定月份（1-12），1表示一月，12表示十二月，null表示不限制，空表示直接false
     * */
    private List<Integer> specifyMonth = null;
    /*
     * 指定年（4位数），例如2026，null表示不限制，空表示直接false
     * */
    private List<Integer> specifyYear = null;
    /*
     * 每日时间窗口（仅时分秒），start为空表示无下限，end为空表示无上限，
     * 全为空或 null 表示无限制；start 晚于 end 表示跨午夜（如 22:00 ~ 02:00）
     * */
    private WhenRuleDailyTimeRange dailyTimeRange = null;
    /*
     * true: 每月最后一天
     * */
    private Boolean lastDayOfMonth = null;


    /*
     * build方法
     * */

    public WhenRuleBuilder and(){
        combinationList.add(new WhenRuleBuilderCombination(ActionEnums.AND, this.copy()));
        this.init();
        return this;
    }

    public WhenRuleBuilder or(){
        combinationList.add(new WhenRuleBuilderCombination(ActionEnums.OR, this.copy()));
        this.init();
        return this;
    }

    public WhenRuleBuilder country(CountryEnums country) {
        if (Objects.isNull(country)) {
            log.error("国家不能为空");
        } else {
            this.country = country;
            this.region = country.getDefaultRegion();
        }
        return this;
    }

    public WhenRuleBuilder region(RegionEnums region) {
        if (Objects.isNull(region)) {
            log.error("地区不能为空");
        } else {
            this.region = region;
        }
        return this;
    }

    public WhenRuleBuilder calculateTime(ZonedDateTime calculateTime) {
        if (Objects.isNull(calculateTime)) {
            log.error("计算时间不能为空");
        } else {
            this.calculateTime = calculateTime;
        }
        return this;
    }

    public WhenRuleBuilder matchingModel(MatchingModelEnums matchingModel) {
        if (Objects.isNull(matchingModel)) {
            log.error("匹配条件不能为空");
        } else {
            this.matchingModel = matchingModel;
        }
        return this;
    }

    public WhenRuleBuilder timeRange(WhenRuleTimeRange timeRange) {
        if (Objects.isNull(timeRange)) {
            log.error("时间区间不能为空");
        } else {
            this.timeRange = timeRange;
            replaceCondition(TimeRangeCondition.class, new TimeRangeCondition(timeRange));
        }
        return this;
    }

    public WhenRuleBuilder holiday(boolean holiday) {
        this.holiday = holiday;
        replaceCondition(HolidayCondition.class, new HolidayCondition(holiday, holidayDataProvider));
        return this;
    }

    /**
     * 设置调用级节假日数据源（仅本次规则计算生效，不影响全局 {@link com.fkl.whenRule.config.WhenRuleConfig}）。
     * <p>
     * 调用顺序无要求：无论 {@link #holiday(boolean)} 在前还是在后，最终都会作用到当前 builder 内已存在的
     * {@link HolidayCondition} 上。
     *
     * @param holidayDataProvider 自定义数据源；传 {@code null} 表示回退到全局
     */
    public WhenRuleBuilder holidayDataProvider(HolidayDataProvider holidayDataProvider) {
        this.holidayDataProvider = holidayDataProvider;
        // 同步给已存在的 HolidayCondition 实例（避免调用顺序敏感）
        for (RuleCondition condition : conditionList) {
            if (condition instanceof HolidayCondition) {
                ((HolidayCondition) condition).setHolidayDataProvider(holidayDataProvider);
            }
        }
        return this;
    }

    /**
     * 设置调用级 JDBC 节假日数据源（内部封装为 {@link JdbcHolidayDataProvider}）。
     * 传 {@code null} 表示回退到全局配置。
     */
    public WhenRuleBuilder holidayDataSource(DataSource dataSource) {
        if (Objects.isNull(dataSource)) {
            return holidayDataProvider(null);
        }
        return holidayDataProvider(new JdbcHolidayDataProvider(dataSource));
    }


    public WhenRuleBuilder weekend(boolean weekend) {
        this.weekend = weekend;
        replaceCondition(WeekendCondition.class, new WeekendCondition(weekend));
        return this;
    }

    public WhenRuleBuilder specifyDay(List<Integer> specifyDay) {
        this.specifyDay = specifyDay;
        replaceCondition(SpecifyDayCondition.class, Objects.nonNull(specifyDay) ? new SpecifyDayCondition(specifyDay) : null);
        return this;
    }

    public WhenRuleBuilder specifyWeekday(List<Integer> specifyWeekday) {
        this.specifyWeekday = specifyWeekday;
        replaceCondition(SpecifyWeekDayCondition.class, Objects.nonNull(specifyWeekday) ? new SpecifyWeekDayCondition(specifyWeekday) : null);
        return this;
    }

    public WhenRuleBuilder specifyMonth(List<Integer> specifyMonth) {
        this.specifyMonth = specifyMonth;
        replaceCondition(SpecifyMonthCondition.class, Objects.nonNull(specifyMonth) ? new SpecifyMonthCondition(specifyMonth) : null);
        return this;
    }

    public WhenRuleBuilder specifyYear(List<Integer> specifyYear) {
        this.specifyYear = specifyYear;
        replaceCondition(SpecifyYearCondition.class, Objects.nonNull(specifyYear) ? new SpecifyYearCondition(specifyYear) : null);
        return this;
    }

    public WhenRuleBuilder dailyTimeRange(WhenRuleDailyTimeRange dailyTimeRange) {
        if (Objects.isNull(dailyTimeRange)) {
            log.error("每日时间窗口不能为空");
        } else {
            this.dailyTimeRange = dailyTimeRange;
            replaceCondition(DailyTimeRangeCondition.class, new DailyTimeRangeCondition(dailyTimeRange));
        }
        return this;
    }

    public WhenRuleBuilder lastDayOfMonth() {
        this.lastDayOfMonth = true;
        replaceCondition(LastDayOfMonthCondition.class, new LastDayOfMonthCondition());
        return this;
    }

    private void replaceCondition(Class<? extends RuleCondition> type, RuleCondition condition) {
        conditionList.removeIf(type::isInstance);
        if (Objects.nonNull(condition)) {
            conditionList.add(condition);
        }
    }

    /**
     * 重置判断字段以进入下一个条件组。
     * 保留基础配置（country / region / calculateTime / matchingModel），
     * 避免用户在每个段重复设置。
     */
    public void init(){
        this.timeRange = null;
        this.holiday = null;
        this.weekend = null;
        this.specifyDay = null;
        this.specifyWeekday = null;
        this.specifyMonth = null;
        this.specifyYear = null;
        this.dailyTimeRange = null;
        this.lastDayOfMonth = null;
        this.conditionList.clear();
    }

    public WhenRuleBuilder copy(){
        WhenRuleBuilder whenRuleBuilder = new WhenRuleBuilder();
        whenRuleBuilder.country(this.country);
        whenRuleBuilder.region(this.region);
        whenRuleBuilder.calculateTime(this.calculateTime);
        whenRuleBuilder.matchingModel(this.matchingModel);
        // 注意：先复制 holidayDataProvider，再调用 holiday()，
        // 这样新 builder 中的 HolidayCondition 实例能直接拿到自定义 provider
        if (Objects.nonNull(this.holidayDataProvider)) {
            whenRuleBuilder.holidayDataProvider(this.holidayDataProvider);
        }
        if (Objects.nonNull(this.timeRange)) {
            whenRuleBuilder.timeRange(this.timeRange);
        }
        if (Objects.nonNull(this.holiday)) {
            whenRuleBuilder.holiday(this.holiday);
        }
        if (Objects.nonNull(this.weekend)) {
            whenRuleBuilder.weekend(this.weekend);
        }
        whenRuleBuilder.specifyDay(this.specifyDay);
        whenRuleBuilder.specifyWeekday(this.specifyWeekday);
        whenRuleBuilder.specifyMonth(this.specifyMonth);
        whenRuleBuilder.specifyYear(this.specifyYear);
        if (Objects.nonNull(this.dailyTimeRange)) {
            whenRuleBuilder.dailyTimeRange(this.dailyTimeRange);
        }
        if (Objects.nonNull(this.lastDayOfMonth)) {
            whenRuleBuilder.lastDayOfMonth();
        }
        return whenRuleBuilder;
    }

    // 根据国家/地区返回默认时区
    private ZoneId getDefaultZoneId() {
        return ZoneId.of(this.region.getCode());
    }

    public LocalDateTime getCalculateTimeLocalDateTime() {
        return this.calculateTime.toLocalDateTime();
    }

    public LocalDate getCalculateTimeLocalDate() {
        return this.calculateTime.toLocalDate();
    }

    public LocalTime getCalculateTimeLocalTime() {
        return this.calculateTime.toLocalTime();
    }
}
