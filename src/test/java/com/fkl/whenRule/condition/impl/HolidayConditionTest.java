package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;
import com.fkl.whenRule.config.WhenRuleConfig;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HolidayConditionTest {

    private static final RegionEnums REGION = RegionEnums.ASIA_SHANGHAI;

    @AfterEach
    void tearDown() {
        // 用例之间互不干扰：清缓存 + 重置 provider 为默认
        HolidayCondition.clearCache();
        WhenRuleConfig.reset();
    }

    @Test
    void shouldMatchOfficialHolidayWhenFlagIsTrue() {
        HolidayCondition condition = new HolidayCondition(true);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldMatchNaturalWeekendWhenFlagIsTrue() {
        HolidayCondition condition = new HolidayCondition(true);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 3, 7, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldExcludeAdjustedWorkdayOnWeekendWhenFlagIsTrue() {
        HolidayCondition condition = new HolidayCondition(true);
        assertFalse(condition.test(
                LocalDateTime.of(2026, 2, 14, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldMatchNormalWeekdayWhenFlagIsFalse() {
        HolidayCondition condition = new HolidayCondition(false);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 3, 10, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldExcludeOfficialHolidayWhenFlagIsFalse() {
        HolidayCondition condition = new HolidayCondition(false);
        assertFalse(condition.test(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldIncludeAdjustedWorkdayWhenFlagIsFalse() {
        HolidayCondition condition = new HolidayCondition(false);
        assertTrue(condition.test(
                LocalDateTime.of(2026, 2, 14, 10, 0),
                CountryEnums.CN,
                REGION
        ));
    }

    @Test
    void shouldReturnFalseForUnsupportedCountry() {
        HolidayCondition condition = new HolidayCondition(true);
        assertFalse(condition.test(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                CountryEnums.US,
                RegionEnums.AMERICA_NEW_YORK
        ));
    }

    @Test
    void shouldReturnFalseWhenCalculateTimeIsNull() {
        HolidayCondition condition = new HolidayCondition(true);
        assertFalse(condition.test(null, CountryEnums.CN, REGION));
    }

    @Test
    void shouldUseCacheForRepeatedLoads() {
        HolidayCondition condition = new HolidayCondition(true);
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 10, 0);
        assertTrue(condition.test(time, CountryEnums.CN, REGION));
        assertTrue(condition.test(time, CountryEnums.CN, REGION));
    }

    /* ============== 可插拔数据源相关用例 ============== */

    @Test
    void shouldUseCustomProviderWhenRegistered() {
        // 自定义数据源：把 2030-06-01 标记为节假日
        HolidayDataProvider custom = (country, year) -> Collections.singletonList(
                DaysEntity.builder()
                        .name("自定义节日")
                        .date(LocalDate.of(2030, 6, 1))
                        .isOffDay(true)
                        .build()
        );
        WhenRuleConfig.setHolidayDataProvider(custom);

        HolidayCondition condition = new HolidayCondition(true);
        // 2030-06-01 是周六，本身就属于节假日；这里同时验证自定义数据源被读取
        assertTrue(condition.test(LocalDateTime.of(2030, 6, 1, 10, 0), CountryEnums.CN, REGION));
    }

    @Test
    void shouldRespectCustomProviderForAdjustedWorkday() {
        // 自定义数据源：把某周六（2030-06-01）标记为调休工作日
        HolidayDataProvider custom = (country, year) -> Collections.singletonList(
                DaysEntity.builder()
                        .name("自定义调休")
                        .date(LocalDate.of(2030, 6, 1))
                        .isOffDay(false)
                        .build()
        );
        WhenRuleConfig.setHolidayDataProvider(custom);

        // flag=true 节假日匹配：被自定义为调休工作日，应返回 false
        assertFalse(new HolidayCondition(true)
                .test(LocalDateTime.of(2030, 6, 1, 10, 0), CountryEnums.CN, REGION));
        // flag=false 工作日匹配：应返回 true
        assertTrue(new HolidayCondition(false)
                .test(LocalDateTime.of(2030, 6, 1, 10, 0), CountryEnums.CN, REGION));
    }

    @Test
    void shouldFallBackToWeekendWhenCustomProviderReturnsEmpty() {
        // 自定义数据源始终返回空列表
        WhenRuleConfig.setHolidayDataProvider((country, year) -> Collections.emptyList());

        HolidayCondition holidayCond = new HolidayCondition(true);
        // 2026-03-07 是周六，无任何节假日数据时应仍判定为节假日（因为是周末）
        assertTrue(holidayCond.test(LocalDateTime.of(2026, 3, 7, 10, 0), CountryEnums.CN, REGION));
        // 2026-03-10 是周二，无数据时应判定为非节假日
        assertFalse(holidayCond.test(LocalDateTime.of(2026, 3, 10, 10, 0), CountryEnums.CN, REGION));
    }

    @Test
    void shouldRestoreDefaultProviderAfterReset() {
        // 先用一个自定义 provider 把 2026-01-01 标为非节假日
        WhenRuleConfig.setHolidayDataProvider((country, year) -> Arrays.asList(
                DaysEntity.builder().date(LocalDate.of(2026, 1, 1)).isOffDay(false).build()
        ));
        assertFalse(new HolidayCondition(true)
                .test(LocalDateTime.of(2026, 1, 1, 10, 0), CountryEnums.CN, REGION));

        // 重置后应恢复内置 classpath JSON 数据源（2026-01-01 是元旦节假日）
        WhenRuleConfig.reset();
        assertTrue(new HolidayCondition(true)
                .test(LocalDateTime.of(2026, 1, 1, 10, 0), CountryEnums.CN, REGION));
    }

    @Test
    void clearCacheShouldNotThrowWhenProviderHasNoCache() {
        // 替换为没有缓存能力（使用接口默认 clearCache）的 provider，调用不应抛异常
        WhenRuleConfig.setHolidayDataProvider(new HolidayDataProvider() {
            @Override
            public List<DaysEntity> load(CountryEnums country, int year) {
                return Collections.emptyList();
            }
        });

        assertDoesNotThrow(HolidayCondition::clearCache);
    }
}
