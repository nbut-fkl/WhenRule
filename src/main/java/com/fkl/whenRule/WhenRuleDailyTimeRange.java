package com.fkl.whenRule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 每日时间窗口（仅时分秒）。
 * 当 startTime 晚于 endTime 时，表示跨午夜（例如 22:00 ~ 02:00）。
 *
 * @author fkl
 * @since 2026/5/31
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WhenRuleDailyTimeRange {
    private LocalTime startTime;
    private LocalTime endTime;
}
