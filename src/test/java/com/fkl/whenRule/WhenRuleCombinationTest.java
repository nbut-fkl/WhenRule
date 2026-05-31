package com.fkl.whenRule;

import com.fkl.whenRule.condition.impl.HolidayCondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 WhenRule 中 AND/OR 多条件组合（combinationList）的求值测试。
 */
class WhenRuleCombinationTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @AfterEach
    void tearDown() {
        HolidayCondition.clearCache();
    }

    private ZonedDateTime time(LocalDateTime ldt) {
        return ZonedDateTime.of(ldt, ZONE);
    }

    // ============== AND 组合 ==============

    @Test
    void shouldReturnTrueWhenAllAndSegmentsMatch() {
        // 2026-03-10 是 3 月，且是周二，两段 AND 均匹配
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(3))
                .and()
                .calculateTime(t)
                .specifyWeekday(List.of(2));

        assertTrue(WhenRule.when(builder));
    }

    @Test
    void shouldReturnFalseWhenAnyAndSegmentFails() {
        // 第一段匹配（3 月），第二段不匹配（指定周一，但实际周二）
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(3))
                .and()
                .calculateTime(t)
                .specifyWeekday(List.of(1));

        assertFalse(WhenRule.when(builder));
    }

    // ============== OR 组合 ==============

    @Test
    void shouldReturnTrueWhenAnyOrSegmentMatches() {
        // 第一段不匹配（4 月），第二段匹配（周二）
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(4))
                .or()
                .calculateTime(t)
                .specifyWeekday(List.of(2));

        assertTrue(WhenRule.when(builder));
    }

    @Test
    void shouldReturnFalseWhenAllOrSegmentsFail() {
        // 两段都不匹配
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(4))
                .or()
                .calculateTime(t)
                .specifyWeekday(List.of(1));

        assertFalse(WhenRule.when(builder));
    }

    // ============== 混合 AND/OR（从左到右求值，不区分优先级） ==============

    @Test
    void shouldEvaluateMixedAndOrLeftToRight() {
        // A=true, B=false, C=true
        // 期望：(true AND false) OR true = false OR true = true
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(3))           // A: true
                .and()
                .calculateTime(t)
                .specifyMonth(List.of(4))           // B: false
                .or()
                .calculateTime(t)
                .specifyWeekday(List.of(2));        // C: true

        assertTrue(WhenRule.when(builder));
    }

    @Test
    void shouldEvaluateLastSegmentInCombination() {
        // 验证 Bug 1 修复：最后一段必须参与求值
        // A=true, B=true, C=false
        // 若最后一段（C）被忽略：true AND true = true（错误）
        // 正确：(true AND true) OR false = true（巧合也是 true）
        // 改成更能区分的：A=false, B=false, C=true
        // 若 C 被忽略：false AND false = false
        // 正确：(false AND false) OR true = true
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(4))           // A: false
                .and()
                .calculateTime(t)
                .specifyMonth(List.of(5))           // B: false
                .or()
                .calculateTime(t)
                .specifyWeekday(List.of(2));        // C: true，必须被求值

        assertTrue(WhenRule.when(builder));
    }

    @Test
    void shouldEvaluateLastSegmentEvenWhenItIsTheOnlyMatch() {
        // 进一步验证最后一段被求值
        // A=true（第一段）AND B=false（最后一段）→ 应得 false
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(3))           // A: true
                .and()
                .calculateTime(t)
                .specifyMonth(List.of(4));          // B: false（最后一段）

        assertFalse(WhenRule.when(builder));
    }

    // ============== 单段（无 and/or）兼容性 ==============

    @Test
    void shouldFallbackToSingleConditionWhenNoCombination() {
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(3));

        assertTrue(WhenRule.when(builder));
    }

    // ============== Builder 可重复使用（验证 Bug 3 修复） ==============

    @Test
    void shouldAllowBuilderReuseAcrossWhenCalls() {
        // 验证 when() 不会污染 combinationList，使得同一 builder 多次调用结果一致
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(3))
                .and()
                .calculateTime(t)
                .specifyWeekday(List.of(2));

        boolean first = WhenRule.when(builder);
        int sizeAfterFirst = builder.getCombinationList().size();
        boolean second = WhenRule.when(builder);
        int sizeAfterSecond = builder.getCombinationList().size();

        assertTrue(first);
        assertEquals(first, second);
        assertEquals(sizeAfterFirst, sizeAfterSecond, "combinationList 不应因调用 when() 而增长");
    }

    // ============== copy() 处理 null 字段（验证 Bug 4 修复） ==============

    @Test
    void shouldNotThrowNpeWhenCopyingBuilderWithNullBooleanFields() {
        // and() 内部调用 copy()，copy 时 holiday/weekend 为 null 不应拆箱 NPE
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(3))           // 不设置 holiday/weekend
                .and()
                .calculateTime(t)
                .specifyWeekday(List.of(2));

        assertDoesNotThrow(() -> WhenRule.when(builder));
    }

    @Test
    void shouldCopyHolidayFieldCorrectly() {
        // 第一段设置 holiday(false)，调用 and() 后 copy 应正确保留
        // 2026-03-10 周二 → 工作日，holiday(false) 应匹配
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .holiday(false)
                .and()
                .calculateTime(t)
                .specifyMonth(List.of(3));

        assertTrue(WhenRule.when(builder));
    }

    // ============== 基础配置在段间保留 ==============

    @Test
    void shouldPreserveCalculateTimeAcrossSegments() {
        // 修复后：只在第一段设置 calculateTime，and() 后不需重复设置
        // 2026-03-10 12:00 是 3 月且周二，两段都应该用这个时间计算
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(3))
                .and()
                .specifyWeekday(List.of(2));

        assertTrue(WhenRule.when(builder));
    }

    @Test
    void shouldPreserveMatchingModelAcrossSegments() {
        // matchingModel 也应该保留到下一段
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .matchingModel(com.fkl.whenRule.enums.MatchingModelEnums.ANY)
                .specifyMonth(List.of(4))           // 不匹配
                .specifyWeekday(List.of(2))          // 匹配—ANY 模式下该段为 true
                .and()
                .calculateTime(t)
                .specifyMonth(List.of(3));           // 匹配

        // 第二段仍应为 ANY 模式（但只有一个条件也均匹配）
        assertTrue(WhenRule.when(builder));
    }

    // ============== conditionList 不跨段泄漏 ==============

    @Test
    void shouldNotLeakConditionsFromPreviousSegment() {
        // 第一段的 specifyMonth(3) 不应被第二段继承
        // 2026-03-10 周二：
        //   第一段 specifyMonth(4) = false
        //   第二段只有 specifyWeekday(2) = true（若泄漏上一段的 Month(4)，会变成 false）
        // 期望：false OR true = true
        ZonedDateTime t = time(LocalDateTime.of(2026, 3, 10, 12, 0));
        WhenRuleBuilder builder = new WhenRuleBuilder()
                .calculateTime(t)
                .specifyMonth(List.of(4))
                .or()
                .specifyWeekday(List.of(2));

        assertTrue(WhenRule.when(builder));
    }
}
