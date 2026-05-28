package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author fkl
 * @since 2026/5/28 14:58
 */
@AllArgsConstructor
@Slf4j
public class SpecifyDayCondition implements RuleCondition {
    private List<Integer> dataList;

    @Override
    public boolean test(LocalDateTime calculateTime, CountryEnums country, RegionEnums region) {
        if (Objects.isNull(dataList)) {
            return true;
        }
        if (dataList.isEmpty()) {
            return false;
        }
        if (Objects.isNull(calculateTime)) {
            return false;
        }
        int dayOfMonth = calculateTime.getDayOfMonth();
        return dataList.contains(dayOfMonth);
    }
}
