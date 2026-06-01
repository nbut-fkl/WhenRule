package com.fkl.whenRule.condition.impl.holiday;

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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HolidayAdminServiceTest {

    private DataSource dataSource;
    private HolidayAdminService admin;

    @BeforeEach
    void setUp() {
        dataSource = JdbcHolidayTestSupport.createDataSource();
        admin = new HolidayAdminService(dataSource);
    }

    @AfterEach
    void tearDown() {
        WhenRuleConfig.reset();
    }

    @Test
    void shouldInsertUpdateDelete() {
        long id = admin.save(HolidayRecord.builder()
                .name("元旦")
                .country("CN")
                .date(LocalDate.of(2026, 1, 1))
                .isOffDay(true)
                .build());

        HolidayRecord saved = admin.findById(id).orElseThrow();
        assertEquals("元旦", saved.getName());

        admin.update(HolidayRecord.builder()
                .id(id)
                .name("元旦（修订）")
                .country("CN")
                .date(LocalDate.of(2026, 1, 1))
                .isOffDay(true)
                .build());

        assertEquals("元旦（修订）", admin.findById(id).orElseThrow().getName());

        admin.deleteById(id);
        assertTrue(admin.findById(id).isEmpty());
    }

    @Test
    void shouldReplaceYearAndImportDays() {
        admin.importDays(CountryEnums.CN, 2026, List.of(
                DaysEntity.builder().name("A").date(LocalDate.of(2026, 1, 1)).isOffDay(true).build(),
                DaysEntity.builder().name("B").date(LocalDate.of(2026, 2, 1)).isOffDay(false).build()
        ));

        List<HolidayRecord> records = admin.listByCountryAndYear(CountryEnums.CN, 2026);
        assertEquals(2, records.size());

        admin.replaceYear(CountryEnums.CN, 2026, List.of(
                HolidayRecord.builder().name("C").country("CN").date(LocalDate.of(2026, 3, 1)).isOffDay(true).build()
        ));

        records = admin.listByCountryAndYear(CountryEnums.CN, 2026);
        assertEquals(1, records.size());
        assertEquals("C", records.get(0).getName());
    }

    @Test
    void shouldFilterByCountry() {
        admin.save(HolidayRecord.builder()
                .name("CN")
                .country("CN")
                .date(LocalDate.of(2026, 1, 1))
                .isOffDay(true)
                .build());
        admin.save(HolidayRecord.builder()
                .name("US")
                .country("US")
                .date(LocalDate.of(2026, 1, 1))
                .isOffDay(true)
                .build());

        assertEquals(1, admin.listByCountryAndYear(CountryEnums.CN, 2026).size());
    }

    @Test
    void fromConfigShouldBeEmptyWhenUsingJsonProvider() {
        WhenRuleConfig.reset();
        assertFalse(HolidayAdminService.isAvailable());
        assertTrue(HolidayAdminService.fromConfig().isEmpty());
        assertThrows(UnsupportedOperationException.class, HolidayAdminService::requireFromConfig);
        assertThrows(UnsupportedOperationException.class, WhenRuleConfig::requireHolidayAdminService);
    }

    @Test
    void fromConfigShouldWorkWhenJdbcEnabled() {
        WhenRuleConfig.setHolidayDataSource(dataSource);

        assertTrue(HolidayAdminService.isAvailable());
        Optional<HolidayAdminService> optional = WhenRuleConfig.holidayAdminService();
        assertTrue(optional.isPresent());

        long id = optional.get().save(HolidayRecord.builder()
                .name("测试")
                .country("CN")
                .date(LocalDate.of(2026, 6, 1))
                .isOffDay(true)
                .build());
        assertTrue(optional.get().findById(id).isPresent());
    }

    @Test
    void ofShouldRejectNonJdbcProvider() {
        assertThrows(UnsupportedOperationException.class,
                () -> HolidayAdminService.of((country, year) -> List.of()));
    }
}
