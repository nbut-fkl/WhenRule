package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 匹配每月最后一天。
 *
 * @author fkl
 * @since 2026/6/1
 */
@Slf4j
public class LastDayOfMonthCondition implements RuleCondition {

    @Override
    public boolean test(LocalDateTime calculateTime, CountryEnums country, RegionEnums region) {
        if (Objects.isNull(calculateTime)) {
            return false;
        }
        return calculateTime.getDayOfMonth() == calculateTime.toLocalDate().lengthOfMonth();
    }
}
