package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author fkl
 * @since 2026/5/28 14:58
 */
@AllArgsConstructor
@Slf4j
public class WeekendCondition implements RuleCondition {
    private boolean flag;

    @Override
    public boolean test(LocalDateTime calculateTime, CountryEnums country, RegionEnums region) {
        if (Objects.isNull(calculateTime)) {
            return false;
        }
        DayOfWeek dayOfWeek = calculateTime.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        return flag == isWeekend;
    }
}
