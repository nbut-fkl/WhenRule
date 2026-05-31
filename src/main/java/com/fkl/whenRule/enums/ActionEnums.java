package com.fkl.whenRule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fkl
 * @since 2026/5/31 18:49
 */
@AllArgsConstructor
@Getter
public enum ActionEnums {
    AND("and", "与"),
    OR("or", "或");
    private String code;
    private String desc;
}
