package com.fkl.whenRule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * @author fkl
 * @since 2026/5/28 11:43
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WhenRuleTimeRange {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}