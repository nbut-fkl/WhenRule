package com.fkl.whenRule.condition.impl.holiday;

import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 JDBC {@link DataSource} 的节假日数据源实现。
 * <p>
 * 默认读取表 {@link JdbcHolidayRepository#DEFAULT_TABLE_NAME}，
 * 按 {@code country + year} 查询并缓存。
 *
 * @author fkl
 */
@Slf4j
public class JdbcHolidayDataProvider implements HolidayDataProvider {

    private final JdbcHolidayRepository repository;
    private final Map<String, List<DaysEntity>> cache = new ConcurrentHashMap<>();

    public JdbcHolidayDataProvider(DataSource dataSource) {
        this(new JdbcHolidayRepository(dataSource));
    }

    public JdbcHolidayDataProvider(JdbcHolidayRepository repository) {
        if (Objects.isNull(repository)) {
            throw new IllegalArgumentException("repository 不能为空");
        }
        this.repository = repository;
    }

    public JdbcHolidayRepository getRepository() {
        return repository;
    }

    @Override
    public List<DaysEntity> load(CountryEnums country, int year) {
        String cacheKey = country.name() + ":" + year;
        return cache.computeIfAbsent(cacheKey, key -> repository.findDaysByCountryAndYear(country.name(), year));
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
