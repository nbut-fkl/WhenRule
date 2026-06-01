package com.fkl.whenRule.condition.impl;

import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;
import com.fkl.whenRule.config.WhenRuleConfig;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fkl
 * @since 2026/5/28 14:58
 */
@Slf4j
public class HolidayCondition implements RuleCondition {

    private final boolean flag;

    /**
     * 调用级数据源覆盖（可空）。
     * 优先级：condition 级 &gt; 全局 {@link WhenRuleConfig}。
     * 由 {@code WhenRuleBuilder.holidayDataProvider(...)} 注入。
     */
    private HolidayDataProvider holidayDataProvider;

    public HolidayCondition(boolean flag) {
        this.flag = flag;
    }

    public HolidayCondition(boolean flag, HolidayDataProvider holidayDataProvider) {
        this.flag = flag;
        this.holidayDataProvider = holidayDataProvider;
    }

    /**
     * 设置当前 condition 实例的专属数据源（仅本次规则计算生效，不影响全局配置）。
     */
    public void setHolidayDataProvider(HolidayDataProvider holidayDataProvider) {
        this.holidayDataProvider = holidayDataProvider;
    }

    /**
     * 清空数据源缓存（委托给当前生效的 {@link HolidayDataProvider}）。
     * 注意：调用此静态方法仅会清理 <b>全局</b> provider 的缓存，
     * 不会影响那些通过构造函数或 setter 注入了私有 provider 的 condition 实例。
     */
    public static void clearCache() {
        WhenRuleConfig.getHolidayDataProvider().clearCache();
    }

    @Override
    public boolean test(LocalDateTime calculateTime, CountryEnums country, RegionEnums region) {
        if (Objects.isNull(calculateTime)) {
            return false;
        }
        if (!CountryEnums.CN.equals(country)) {
            log.error("【{}】国家的节假日/工作日匹配暂不支持", country.getDesc());
            return false;
        }

        // 优先使用 condition 级覆盖，否则回退到全局配置（保证默认开箱即用）
        HolidayDataProvider provider = Objects.nonNull(holidayDataProvider)
                ? holidayDataProvider
                : WhenRuleConfig.getHolidayDataProvider();
        List<DaysEntity> list = provider.load(country, calculateTime.getYear());

        List<LocalDate> holidayList = list.stream()
                .filter(day -> Objects.equals(day.getIsOffDay(), true))
                .map(DaysEntity::getDate)
                .collect(Collectors.toList());
        List<LocalDate> workdayList = list.stream()
                .filter(day -> Objects.equals(day.getIsOffDay(), false))
                .map(DaysEntity::getDate)
                .collect(Collectors.toList());

        LocalDate date = calculateTime.toLocalDate();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

        if (flag) {
            // 节假日：包含周末 + holidayList，排除 workdayList
            if (workdayList.contains(date)) {
                return false;
            }
            return isWeekend || holidayList.contains(date);
        } else {
            // 工作日：包含周一-周五 + workdayList，排除 holidayList
            if (holidayList.contains(date)) {
                return false;
            }
            return !isWeekend || workdayList.contains(date);
        }
    }
}
