package com.fkl.whenRule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fkl
 * @since 2026/5/28 14:05
 */
@Getter
@AllArgsConstructor
public enum MatchingModelEnums {
    ALL("ALL", "全部匹配"),
    ANY("ANY", "任意匹配");
    private String code;
    private String desc;
}
