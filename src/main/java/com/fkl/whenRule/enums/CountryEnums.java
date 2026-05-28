package com.fkl.whenRule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fkl
 * @since 2026/5/28 11:44
 */
@Getter
@AllArgsConstructor
public enum CountryEnums {
    CN("CN", "China", RegionEnums.ASIA_SHANGHAI),
    US("US", "United States", RegionEnums.AMERICA_NEW_YORK);
    private String code;
    private String desc;
    private RegionEnums defaultRegion;
}
