package com.fkl.whenRule.condition;

import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;

import java.time.LocalDateTime;

/**
 * @author fkl
 * @since 2026/5/28 14:46
 */
public interface RuleCondition {
    public boolean test(LocalDateTime calculateTime, CountryEnums country, RegionEnums region);
}
