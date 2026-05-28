package com.fkl.whenRule.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author fkl
 * @since 2026/5/28 15:43
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DaysEntity {
    private String name;
    private LocalDate date;
    private Boolean isOffDay;
}
