package com.fkl.whenRule;

import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.condition.impl.*;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.MatchingModelEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author fkl
 * @since 2026/5/28 11:49
 * TODO：基础字段跟新 是否需要根据conditionList里面关联的值
 */
@Getter
@Slf4j
public class WhenRuleBuilder {
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
     * build方法
     * */
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
        replaceCondition(HolidayCondition.class, new HolidayCondition(holiday));
        return this;
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

    private void replaceCondition(Class<? extends RuleCondition> type, RuleCondition condition) {
        conditionList.removeIf(type::isInstance);
        if (Objects.nonNull(condition)) {
            conditionList.add(condition);
        }
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
