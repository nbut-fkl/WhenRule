package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.WhenRuleDailyTimeRange;
import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 每日时间窗口条件，仅判断时分秒，不关心日期。
 * 支持跨午夜场景（startTime 晚于 endTime，例如 22:00 ~ 02:00）。
 *
 * @author fkl
 * @since 2026/5/31
 */
@AllArgsConstructor
@Slf4j
public class DailyTimeRangeCondition implements RuleCondition {
    private WhenRuleDailyTimeRange dailyTimeRange;

    @Override
    public boolean test(LocalDateTime calculateTime, CountryEnums country, RegionEnums region) {
        if (Objects.isNull(dailyTimeRange) ||
                (Objects.isNull(dailyTimeRange.getStartTime()) && Objects.isNull(dailyTimeRange.getEndTime()))) {
            return true;
        }
        if (Objects.isNull(calculateTime)) {
            return false;
        }
        LocalTime startTime = dailyTimeRange.getStartTime();
        LocalTime endTime = dailyTimeRange.getEndTime();
        LocalTime time = calculateTime.toLocalTime();

        // 跨午夜场景，例如 22:00 ~ 02:00
        if (Objects.nonNull(startTime) && Objects.nonNull(endTime) && startTime.isAfter(endTime)) {
            return !time.isBefore(startTime) || !time.isAfter(endTime);
        }

        return (Objects.isNull(startTime) || !time.isBefore(startTime)) &&
                (Objects.isNull(endTime) || !time.isAfter(endTime));
    }
}
