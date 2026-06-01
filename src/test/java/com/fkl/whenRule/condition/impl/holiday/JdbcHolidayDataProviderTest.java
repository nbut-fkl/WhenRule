package com.fkl.whenRule.condition.impl.holiday;

import com.fkl.whenRule.WhenRule;
import com.fkl.whenRule.WhenRuleBuilder;
import com.fkl.whenRule.config.WhenRuleConfig;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.entity.HolidayRecord;
import com.fkl.whenRule.enums.CountryEnums;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcHolidayDataProviderTest {

    private DataSource dataSource;
    private JdbcHolidayDataProvider provider;

    @BeforeEach
    void setUp() {
        dataSource = JdbcHolidayTestSupport.createDataSource();
        provider = new JdbcHolidayDataProvider(dataSource);
    }

    @AfterEach
    void tearDown() {
        WhenRuleConfig.reset();
    }

    @Test
    void shouldLoadByCountryAndYear() {
        HolidayAdminService admin = new HolidayAdminService(provider);
        admin.importDays(CountryEnums.CN, 2026, List.of(
                DaysEntity.builder().name("元旦").date(LocalDate.of(2026, 1, 1)).isOffDay(true).build(),
                DaysEntity.builder().name("春节").date(LocalDate.of(2026, 2, 14)).isOffDay(false).build()
        ));

        List<DaysEntity> days = provider.load(CountryEnums.CN, 2026);

        assertEquals(2, days.size());
        assertEquals(LocalDate.of(2026, 1, 1), days.get(0).getDate());
    }

    @Test
    void shouldUseCacheUntilCleared() {
        HolidayAdminService admin = new HolidayAdminService(provider);
        admin.importDays(CountryEnums.CN, 2026, List.of(
                DaysEntity.builder().name("元旦").date(LocalDate.of(2026, 1, 1)).isOffDay(true).build()
        ));

        assertEquals(1, provider.load(CountryEnums.CN, 2026).size());

        admin.save(HolidayRecord.builder()
                .name("测试")
                .country("CN")
                .date(LocalDate.of(2026, 5, 1))
                .isOffDay(true)
                .build());

        // save 会自动 clearCache，下次 load 从 DB 读取最新数据
        assertEquals(2, provider.load(CountryEnums.CN, 2026).size());

        provider.clearCache();
        assertEquals(2, provider.load(CountryEnums.CN, 2026).size());
    }

    @Test
    void shouldWorkWithGlobalConfig() {
        WhenRuleConfig.setHolidayDataSource(dataSource);
        HolidayAdminService.requireFromConfig().importDays(CountryEnums.CN, 2026, List.of(
                DaysEntity.builder().name("元旦").date(LocalDate.of(2026, 1, 1)).isOffDay(true).build()
        ));

        boolean matched = WhenRule.when(new WhenRuleBuilder()
                .calculateTime(ZonedDateTime.of(LocalDateTime.of(2026, 1, 1, 10, 0), ZoneId.of("Asia/Shanghai")))
                .holiday(true));

        assertTrue(matched);
    }

    @Test
    void shouldWorkWithBuilderLevelDataSource() {
        HolidayAdminService admin = new HolidayAdminService(dataSource);
        admin.importDays(CountryEnums.CN, 2026, List.of(
                DaysEntity.builder().name("调休").date(LocalDate.of(2026, 2, 14)).isOffDay(false).build()
        ));

        WhenRuleConfig.setHolidayDataProvider((country, year) -> List.of());

        boolean matched = WhenRule.when(new WhenRuleBuilder()
                .calculateTime(ZonedDateTime.of(LocalDateTime.of(2026, 2, 14, 10, 0), ZoneId.of("Asia/Shanghai")))
                .holiday(false)
                .holidayDataSource(dataSource));

        assertTrue(matched);
    }
}
