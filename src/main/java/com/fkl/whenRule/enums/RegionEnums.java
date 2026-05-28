package com.fkl.whenRule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fkl
 * @since 2026/5/28 13:48
 */
@Getter
@AllArgsConstructor
public enum RegionEnums {
    ASIA_SHANGHAI("Asia/Shanghai", "UTC+8北京时间"),
    AMERICA_NEW_YORK("America/New_York", "UTC-5东海岸时间");
    private String code;
    private String desc;
}
