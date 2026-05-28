package com.fkl.whenRule;

import cn.hutool.core.collection.CollUtil;
import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.MatchingModelEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
