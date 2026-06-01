package com.fkl.whenRule.config;

import com.fkl.whenRule.condition.impl.holiday.ClasspathJsonHolidayDataProvider;
import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link WhenRuleConfig} 全局配置中心单元测试。
 *
 * @author fkl
 */
class WhenRuleConfigTest {

    @AfterEach
    void tearDown() {
        // 每个用例后回到默认实现，避免影响其他测试
        WhenRuleConfig.reset();
    }

    @Test
    void shouldUseDefaultProviderWhenNotConfigured() {
        HolidayDataProvider provider = WhenRuleConfig.getHolidayDataProvider();

        assertNotNull(provider);
        assertInstanceOf(ClasspathJsonHolidayDataProvider.class, provider);
    }

    @Test
    void shouldReplaceProviderWhenSetCalled() {
        HolidayDataProvider custom = new HolidayDataProvider() {
            @Override
            public List<DaysEntity> load(CountryEnums country, int year) {
                return Collections.emptyList();
            }
        };

        WhenRuleConfig.setHolidayDataProvider(custom);

        assertSame(custom, WhenRuleConfig.getHolidayDataProvider());
    }

    @Test
    void shouldIgnoreNullWhenSetCalled() {
        HolidayDataProvider before = WhenRuleConfig.getHolidayDataProvider();

        WhenRuleConfig.setHolidayDataProvider(null);

        // 传 null 应被忽略，仍使用之前的 provider
        assertSame(before, WhenRuleConfig.getHolidayDataProvider());
    }

    @Test
    void shouldResetToDefaultProvider() {
        WhenRuleConfig.setHolidayDataProvider((country, year) -> Collections.emptyList());

        WhenRuleConfig.reset();

        HolidayDataProvider after = WhenRuleConfig.getHolidayDataProvider();
        assertInstanceOf(ClasspathJsonHolidayDataProvider.class, after);
    }

    @Test
    void shouldSetJdbcDataSource() {
        org.h2.jdbcx.JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:config-test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        WhenRuleConfig.setHolidayDataSource(dataSource);

        assertTrue(WhenRuleConfig.isJdbcHolidayEnabled());
        assertInstanceOf(com.fkl.whenRule.condition.impl.holiday.JdbcHolidayDataProvider.class,
                WhenRuleConfig.getHolidayDataProvider());
    }
}
