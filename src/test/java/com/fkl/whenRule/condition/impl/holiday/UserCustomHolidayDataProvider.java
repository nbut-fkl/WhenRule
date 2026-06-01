package com.fkl.whenRule.condition.impl.holiday;

import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * @author fkl
 * @since 2026/6/1 12:15
 */
public class UserCustomHolidayDataProvider implements HolidayDataProvider {
    @Override
    public List<DaysEntity> load(CountryEnums country, int year) {
        return Arrays.asList(DaysEntity.builder()
                .name("user custom")
                .isOffDay(true)
                .date(LocalDate.of(year, 5, 1))
                .build());
    }
}
