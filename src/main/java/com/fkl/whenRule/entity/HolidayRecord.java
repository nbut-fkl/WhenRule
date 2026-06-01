package com.fkl.whenRule.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 节假日数据库记录（含主键，供管理端 CRUD 使用）。
 *
 * @author fkl
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HolidayRecord {
    private Long id;
    private String name;
    private String country;
    private LocalDate date;
    private Boolean isOffDay;
}
