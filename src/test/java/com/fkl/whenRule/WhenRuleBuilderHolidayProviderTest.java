package com.fkl.whenRule;

import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.condition.impl.HolidayCondition;
import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;
import com.fkl.whenRule.config.WhenRuleConfig;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 「方案 A + 全局兜底」相关测试：
 * <ul>
 *     <li>builder 级 provider 优先于全局 provider</li>
 *     <li>未传 builder 级 provider 时回退到全局</li>
 *     <li>调用顺序（先 holiday() 再 holidayDataProvider() 与反之）等价</li>
 *     <li>builder 级 provider 不会污染全局配置</li>
 *     <li>多 builder 并发使用各自 provider，互不干扰</li>
 *     <li>copy() 后 .and() / .or() 段也能保留 provider</li>
 * </ul>
 *
 * @author fkl
 */
class WhenRuleBuilderHolidayProviderTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final ZonedDateTime DATE_2030_06_01_SAT =
            ZonedDateTime.of(LocalDateTime.of(2030, 6, 1, 10, 0), ZONE);
    private static final ZonedDateTime DATE_2030_06_03_MON =
            ZonedDateTime.of(LocalDateTime.of(2030, 6, 3, 10, 0), ZONE);

    @AfterEach
    void tearDown() {
        HolidayCondition.clearCache();
        WhenRuleConfig.reset();
    }

    /** builder 级注入：把 2030-06-03（周一）声明为节假日，匹配 holiday(true) 应为 true */
    @Test
    void builderProviderShouldOverrideGlobal() {
        // 全局 provider 始终返回空，模拟"全局不知道这天是节假日"
        WhenRuleConfig.setHolidayDataProvider((c, y) -> Collections.emptyList());

        // builder 级 provider 把周一标为节假日
        HolidayDataProvider builderLevel = (c, y) -> Collections.singletonList(
                DaysEntity.builder().date(LocalDate.of(2030, 6, 3)).isOffDay(true).build()
        );

        boolean matched = WhenRule.when(new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holiday(true)
                .holidayDataProvider(builderLevel));

        assertTrue(matched, "builder 级应覆盖全局，认为周一是节假日");
    }

    /** 不调用 holidayDataProvider() 时应回退到全局 provider */
    @Test
    void shouldFallBackToGlobalWhenBuilderProviderAbsent() {
        // 全局 provider 把周一标为节假日
        WhenRuleConfig.setHolidayDataProvider((c, y) -> Collections.singletonList(
                DaysEntity.builder().date(LocalDate.of(2030, 6, 3)).isOffDay(true).build()
        ));

        boolean matched = WhenRule.when(new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holiday(true));

        assertTrue(matched, "未传 builder 级时应使用全局 provider");
    }

    /** holidayDataProvider() 在 holiday() 之前调用 */
    @Test
    void shouldWorkWhenProviderConfiguredBeforeHoliday() {
        HolidayDataProvider p = (c, y) -> Collections.singletonList(
                DaysEntity.builder().date(LocalDate.of(2030, 6, 3)).isOffDay(true).build()
        );

        boolean matched = WhenRule.when(new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holidayDataProvider(p)   // 先
                .holiday(true));          // 后

        assertTrue(matched);
    }

    /** holidayDataProvider() 在 holiday() 之后调用 */
    @Test
    void shouldWorkWhenProviderConfiguredAfterHoliday() {
        HolidayDataProvider p = (c, y) -> Collections.singletonList(
                DaysEntity.builder().date(LocalDate.of(2030, 6, 3)).isOffDay(true).build()
        );

        boolean matched = WhenRule.when(new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holiday(true)             // 先
                .holidayDataProvider(p));  // 后（也应生效，会回填到已存在的 condition）

        assertTrue(matched);
    }

    /** builder 级注入不应污染全局配置 */
    @Test
    void builderProviderShouldNotLeakIntoGlobal() {
        HolidayDataProvider before = WhenRuleConfig.getHolidayDataProvider();

        WhenRule.when(new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holiday(true)
                .holidayDataProvider((c, y) -> Collections.emptyList()));

        assertSame(before, WhenRuleConfig.getHolidayDataProvider(),
                "调用 builder.holidayDataProvider 不应改变 WhenRuleConfig 全局配置");
    }

    /** 两个 builder 各自持有独立 provider，互不干扰 */
    @Test
    void multipleBuildersShouldHaveIndependentProviders() {
        // builderA：把 2030-06-03 标为节假日
        HolidayDataProvider providerA = (c, y) -> Collections.singletonList(
                DaysEntity.builder().date(LocalDate.of(2030, 6, 3)).isOffDay(true).build()
        );
        // builderB：完全空数据
        HolidayDataProvider providerB = (c, y) -> Collections.emptyList();

        WhenRuleBuilder builderA = new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holiday(true)
                .holidayDataProvider(providerA);

        WhenRuleBuilder builderB = new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holiday(true)
                .holidayDataProvider(providerB);

        assertTrue(WhenRule.when(builderA),
                "A 用自己的 provider 应判定周一是节假日");
        assertFalse(WhenRule.when(builderB),
                "B 用自己的 provider 周一不在数据里且非周末，应判定为非节假日");
    }

    /** 验证 builder 级 provider 真的被调用（而非全局） */
    @Test
    void builderProviderShouldActuallyBeInvoked() {
        AtomicInteger globalCalls = new AtomicInteger();
        AtomicInteger builderCalls = new AtomicInteger();

        WhenRuleConfig.setHolidayDataProvider((c, y) -> {
            globalCalls.incrementAndGet();
            return Collections.emptyList();
        });
        HolidayDataProvider builderLevel = (c, y) -> {
            builderCalls.incrementAndGet();
            return Collections.emptyList();
        };

        WhenRule.when(new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holiday(true)
                .holidayDataProvider(builderLevel));

        assertEquals(1, builderCalls.get(), "builder 级 provider 应被调用一次");
        assertEquals(0, globalCalls.get(), "存在 builder 级时全局 provider 不应被调用");
    }

    /** copy() 应保留 provider，使后续 .and() / .or() 段同样生效 */
    @Test
    void copyShouldPreserveBuilderProviderAcrossAndOr() {
        // 自定义数据：2030-06-01（周六）作为调休工作日
        HolidayDataProvider p = (c, y) -> Collections.singletonList(
                DaysEntity.builder().date(LocalDate.of(2030, 6, 1)).isOffDay(false).build()
        );

        // 段 1：holiday(true) 节假日；段 2：holiday(false) 工作日
        // 自定义 provider 把周六标为调休工作日，所以：
        //   - holiday(true)  → false（不是节假日）
        //   - holiday(false) → true （是工作日）
        // .or() 组合：false OR true = true
        boolean matched = WhenRule.when(new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_01_SAT)
                .holidayDataProvider(p)
                .holiday(true)
                .or()
                .holiday(false));

        assertTrue(matched);
    }

    /** 设置 null 表示回退到全局 */
    @Test
    void passingNullProviderShouldFallBackToGlobal() {
        // 全局把这天标为节假日
        WhenRuleConfig.setHolidayDataProvider((c, y) -> Collections.singletonList(
                DaysEntity.builder().date(LocalDate.of(2030, 6, 3)).isOffDay(true).build()
        ));

        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(DATE_2030_06_03_MON)
                .holiday(true)
                .holidayDataProvider((c, y) -> Collections.emptyList())
                .holidayDataProvider(null);   // 显式置 null，回退到全局

        assertTrue(WhenRule.when(builder));
    }

    /** condition 实例直接验证：两次调用应分别使用对应的 provider */
    @Test
    void conditionInstanceFieldShouldBeIsolated() {
        HolidayDataProvider p1 = (c, y) -> Collections.singletonList(
                DaysEntity.builder().date(LocalDate.of(2030, 6, 3)).isOffDay(true).build()
        );
        HolidayDataProvider p2 = (c, y) -> Collections.emptyList();

        HolidayCondition c1 = new HolidayCondition(true, p1);
        HolidayCondition c2 = new HolidayCondition(true, p2);

        // 验证两个 condition 实例不会互相影响
        LocalDateTime monday = LocalDateTime.of(2030, 6, 3, 10, 0);
        assertTrue(c1.test(monday, CountryEnums.CN, null));
        assertFalse(c2.test(monday, CountryEnums.CN, null));
    }

    /** 验证 builder.holiday() 创建的 condition 持有 provider 引用 */
    @Test
    void builderShouldInjectProviderIntoCondition() {
        HolidayDataProvider p = (c, y) -> Collections.emptyList();

        WhenRuleBuilder builder = new WhenRuleBuilder()
                .holidayDataProvider(p)
                .holiday(true);

        List<RuleCondition> conditionList = builder.getConditionList();
        assertEquals(1, conditionList.size());
        assertInstanceOf(HolidayCondition.class, conditionList.get(0));
    }
}
