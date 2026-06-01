package com.fkl.whenRule.condition.impl.holiday;

import com.fkl.whenRule.config.WhenRuleConfig;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.entity.HolidayRecord;
import com.fkl.whenRule.enums.CountryEnums;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 节假日数据管理 Service，仅在 JDBC 模式下可用。
 * <p>
 * 当全局或当前使用的是 {@link ClasspathJsonHolidayDataProvider} 等非 JDBC 实现时，
 * 请通过 {@link #isAvailable()} / {@link #fromConfig()} 判断后再调用写操作。
 *
 * @author fkl
 */
public class HolidayAdminService {

    private final JdbcHolidayDataProvider jdbcHolidayDataProvider;

    public HolidayAdminService(DataSource dataSource) {
        this(new JdbcHolidayDataProvider(dataSource));
    }

    public HolidayAdminService(JdbcHolidayDataProvider jdbcHolidayDataProvider) {
        if (Objects.isNull(jdbcHolidayDataProvider)) {
            throw new IllegalArgumentException("jdbcHolidayDataProvider 不能为空");
        }
        this.jdbcHolidayDataProvider = jdbcHolidayDataProvider;
    }

    /**
     * 当前全局配置是否为 JDBC 节假日数据源。
     */
    public static boolean isAvailable() {
        return WhenRuleConfig.getHolidayDataProvider() instanceof JdbcHolidayDataProvider;
    }

    /**
     * 基于当前全局 {@link WhenRuleConfig} 创建 AdminService；非 JDBC 模式返回 empty。
     */
    public static Optional<HolidayAdminService> fromConfig() {
        HolidayDataProvider provider = WhenRuleConfig.getHolidayDataProvider();
        if (provider instanceof JdbcHolidayDataProvider jdbcProvider) {
            return Optional.of(new HolidayAdminService(jdbcProvider));
        }
        return Optional.empty();
    }

    /**
     * 基于当前全局配置创建 AdminService；非 JDBC 模式抛出异常。
     */
    public static HolidayAdminService requireFromConfig() {
        return fromConfig().orElseThrow(() -> new UnsupportedOperationException(
                "当前未使用 JDBC 节假日数据源，HolidayAdminService 不可用。"
                        + "请先调用 WhenRuleConfig.setHolidayDataSource(dataSource)。"));
    }

    /**
     * 基于指定 provider 创建 AdminService；非 JDBC 实现抛出异常。
     */
    public static HolidayAdminService of(HolidayDataProvider provider) {
        if (provider instanceof JdbcHolidayDataProvider jdbcProvider) {
            return new HolidayAdminService(jdbcProvider);
        }
        throw new UnsupportedOperationException(
                "仅 JdbcHolidayDataProvider 支持 HolidayAdminService 管理操作");
    }

    public long save(HolidayRecord record) {
        long id = repository().insert(record);
        clearProviderCache();
        return id;
    }

    public void update(HolidayRecord record) {
        repository().update(record);
        clearProviderCache();
    }

    public void deleteById(long id) {
        repository().deleteById(id);
        clearProviderCache();
    }

    public void deleteByCountryAndYear(CountryEnums country, int year) {
        repository().deleteByCountryAndYear(country.name(), year);
        clearProviderCache();
    }

    public Optional<HolidayRecord> findById(long id) {
        return repository().findById(id);
    }

    public List<HolidayRecord> listByCountryAndYear(CountryEnums country, int year) {
        return repository().findRecordsByCountryAndYear(country.name(), year);
    }

    /**
     * 整年覆盖：先删后批量插入。
     */
    public int replaceYear(CountryEnums country, int year, List<HolidayRecord> records) {
        repository().deleteByCountryAndYear(country.name(), year);
        int inserted = repository().batchInsert(records);
        clearProviderCache();
        return inserted;
    }

    /**
     * 从 {@link DaysEntity} 批量导入指定国家某年数据（会先清空该年已有数据）。
     */
    public int importDays(CountryEnums country, int year, List<DaysEntity> days) {
        List<HolidayRecord> records = new ArrayList<>();
        if (Objects.nonNull(days)) {
            for (DaysEntity day : days) {
                records.add(HolidayRecord.builder()
                        .name(day.getName())
                        .country(country.name())
                        .date(day.getDate())
                        .isOffDay(day.getIsOffDay())
                        .build());
            }
        }
        return replaceYear(country, year, records);
    }

    public JdbcHolidayDataProvider getJdbcHolidayDataProvider() {
        return jdbcHolidayDataProvider;
    }

    private JdbcHolidayRepository repository() {
        return jdbcHolidayDataProvider.getRepository();
    }

    private void clearProviderCache() {
        jdbcHolidayDataProvider.clearCache();
    }
}
