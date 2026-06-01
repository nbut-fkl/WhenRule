package com.fkl.whenRule.config;

import com.fkl.whenRule.condition.impl.holiday.ClasspathJsonHolidayDataProvider;
import com.fkl.whenRule.condition.impl.holiday.HolidayAdminService;
import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;
import com.fkl.whenRule.condition.impl.holiday.JdbcHolidayDataProvider;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Optional;

/**
 * WhenRule 全局配置中心（线程安全）。
 * <p>
 * 用户在程序启动时调用 {@code setXxx} 注入自定义实现；不调用则使用默认 JSON 实现。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 接入 JDBC 数据源
 * WhenRuleConfig.setHolidayDataSource(dataSource);
 *
 * // 管理节假日数据（仅 JDBC 模式）
 * HolidayAdminService admin = WhenRuleConfig.requireHolidayAdminService();
 * }</pre>
 *
 * @author fkl
 */
public final class WhenRuleConfig {

    /** 节假日数据源，默认走 classpath JSON */
    private static volatile HolidayDataProvider holidayDataProvider = new ClasspathJsonHolidayDataProvider();

    private WhenRuleConfig() {
    }

    public static HolidayDataProvider getHolidayDataProvider() {
        return holidayDataProvider;
    }

    /**
     * 注册自定义节假日数据源。
     *
     * @param provider 自定义实现，若为 null 则忽略
     */
    public static void setHolidayDataProvider(HolidayDataProvider provider) {
        if (Objects.nonNull(provider)) {
            WhenRuleConfig.holidayDataProvider = provider;
        }
    }

    /**
     * 注册 JDBC 节假日数据源（内部封装为 {@link JdbcHolidayDataProvider}）。
     *
     * @param dataSource 用户应用提供的数据源，若为 null 则忽略
     */
    public static void setHolidayDataSource(DataSource dataSource) {
        if (Objects.nonNull(dataSource)) {
            WhenRuleConfig.holidayDataProvider = new JdbcHolidayDataProvider(dataSource);
        }
    }

    /**
     * 当前全局是否使用 JDBC 节假日数据源。
     */
    public static boolean isJdbcHolidayEnabled() {
        return holidayDataProvider instanceof JdbcHolidayDataProvider;
    }

    /**
     * 获取全局 AdminService；非 JDBC 模式返回 empty。
     */
    public static Optional<HolidayAdminService> holidayAdminService() {
        return HolidayAdminService.fromConfig();
    }

    /**
     * 获取全局 AdminService；非 JDBC 模式抛出 {@link UnsupportedOperationException}。
     */
    public static HolidayAdminService requireHolidayAdminService() {
        return HolidayAdminService.requireFromConfig();
    }

    /**
     * 重置为默认 JSON 实现，便于单元测试。
     */
    public static void reset() {
        HolidayDataProvider provider = holidayDataProvider;
        if (provider instanceof JdbcHolidayDataProvider jdbcProvider) {
            jdbcProvider.clearCache();
        } else if (provider instanceof ClasspathJsonHolidayDataProvider jsonProvider) {
            jsonProvider.clearCache();
        }
        holidayDataProvider = new ClasspathJsonHolidayDataProvider();
    }
}
