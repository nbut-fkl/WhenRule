package com.fkl.whenRule.entity;

import com.fkl.whenRule.WhenRuleBuilder;
import com.fkl.whenRule.enums.ActionEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fkl
 * @since 2026/5/31 18:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhenRuleBuilderCombination {
    private ActionEnums action;
    private WhenRuleBuilder whenRuleBuilder;
}
