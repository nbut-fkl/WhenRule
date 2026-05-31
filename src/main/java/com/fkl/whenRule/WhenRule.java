package com.fkl.whenRule;

import cn.hutool.core.collection.CollUtil;
import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.entity.WhenRuleBuilderCombination;
import com.fkl.whenRule.enums.ActionEnums;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.MatchingModelEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author fkl
 * @since 2026/5/28 11:43
 */
@Slf4j
public class WhenRule {
    public static boolean when(WhenRuleBuilder builder) {
        if (Objects.isNull(builder)) {
            builder = new WhenRuleBuilder();
        }
        if (CollUtil.isEmpty(builder.getCombinationList())) {
            return condition(builder);
        }
        // 使用本地副本，避免修改原 builder 的 combinationList，让 builder 可重复使用
        List<WhenRuleBuilderCombination> combinationList = new ArrayList<>(builder.getCombinationList());
        // 末尾追加当前 builder 作为最后一段条件组，action 为 null（终止符）
        combinationList.add(new WhenRuleBuilderCombination(null, builder));

        // action 表示当前元素与下一个元素之间的连接操作符
        // 从左到右顺序求值，不区分 AND/OR 优先级
        boolean result = condition(combinationList.get(0).getWhenRuleBuilder());
        for (int i = 0; i < combinationList.size() - 1; i++) {
            ActionEnums action = combinationList.get(i).getAction();
            boolean nextResult = condition(combinationList.get(i + 1).getWhenRuleBuilder());
            if (ActionEnums.AND.equals(action)) {
                result = result && nextResult;
            } else if (ActionEnums.OR.equals(action)) {
                result = result || nextResult;
            } else {
                log.error("逻辑操作符【{}】不存在", action);
            }
        }
        return result;
    }

    private static boolean condition(WhenRuleBuilder builder) {
        if (CollUtil.isEmpty(builder.getConditionList())) {
            return true;
        } else {
            LocalDateTime calculateTime = builder.getCalculateTimeLocalDateTime();
            CountryEnums country = builder.getCountry();
            RegionEnums region = builder.getRegion();

            Stream<Boolean> result = builder.getConditionList().stream().map(condition -> condition.test(calculateTime, country, region));
            if (MatchingModelEnums.ALL.equals(builder.getMatchingModel())) {
                return result.allMatch(a -> a);
            }
            if (MatchingModelEnums.ANY.equals(builder.getMatchingModel())) {
                return result.anyMatch(a -> a);
            } else {
                log.error("匹配模型【{}】不存在", builder.getMatchingModel());
            }
        }
        return false;
    }
}
