package com.fkl.whenRule.condition.impl.holiday;

import com.fkl.whenRule.WhenRule;
import com.fkl.whenRule.WhenRuleBuilder;
import com.fkl.whenRule.config.WhenRuleConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author fkl
 * @since 2026/6/1 12:14
 */
@Slf4j
public class HolidayUserCustomTest {
    @Test
    public void userCustomTest(){
        WhenRuleConfig.setHolidayDataProvider(new UserCustomHolidayDataProvider());

        WhenRuleBuilder builder = new WhenRuleBuilder();
        builder.holiday(true);
        boolean result = WhenRule.when(builder);
        log.info("result: {}", result);
    }
}
