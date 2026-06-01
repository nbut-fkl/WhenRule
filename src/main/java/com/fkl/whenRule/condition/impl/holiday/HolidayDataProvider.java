package com.fkl.whenRule.condition.impl.holiday;

import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;

import java.util.List;

/**
 * 节假日数据源 SPI 接口。
 * <p>
 * 用户可实现该接口接入自己的数据源（DB / Redis / HTTP / 自定义文件等），
 * 然后通过 {@link com.fkl.whenRule.config.WhenRuleConfig#setHolidayDataProvider} 注册生效。
 * 未注册时使用内置的 {@link ClasspathJsonHolidayDataProvider}（读取 jar 内置 JSON 数据）。
 *
 * @author fkl
 */
public interface HolidayDataProvider {

    /**
     * 加载指定国家、指定年份的"非常规日"数据（节假日 + 调休工作日）。
     *
     * @param country 国家
     * @param year    年份
     * @return 当年节假日数据集合，无数据时返回空集合（不要返回 null）
     */
    List<DaysEntity> load(CountryEnums country, int year);

    /**
     * 清空内部缓存，默认空实现。
     * 实现类如有缓存可重写此方法。
     */
    default void clearCache() {
        // 默认无缓存
    }
}
