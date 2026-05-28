package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.WhenRuleTimeRange;
import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author fkl
 * @since 2026/5/28 14:58
 */
@AllArgsConstructor
@Slf4j
public class TimeRangeCondition implements RuleCondition {
    private WhenRuleTimeRange timeRange;

    @Override
    public boolean test(LocalDateTime calculateTime, CountryEnums country, RegionEnums region) {
        if (Objects.isNull(timeRange) ||
                (Objects.isNull(timeRange.getStartTime()) && Objects.isNull(timeRange.getEndTime()))) {
            return true;
        }
        if (Objects.isNull(calculateTime)) {
            return false;
        }
        LocalDateTime startTime = timeRange.getStartTime();
        LocalDateTime endTime = timeRange.getEndTime();

        return (Objects.isNull(startTime) || !calculateTime.isBefore(startTime)) &&
                (Objects.isNull(endTime) || !calculateTime.isAfter(endTime));
    }
}
