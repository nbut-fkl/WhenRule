package com.fkl.whenRule.condition.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fkl.whenRule.condition.RuleCondition;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;
import com.fkl.whenRule.enums.RegionEnums;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author fkl
 * @since 2026/5/28 14:58
 */
@AllArgsConstructor
@Slf4j
public class HolidayCondition implements RuleCondition {
    private static final Map<String, List<DaysEntity>> HOLIDAY_CACHE = new ConcurrentHashMap<>();

    private boolean flag;

    public static void clearCache() {
        HOLIDAY_CACHE.clear();
    }

    @Override
    public boolean test(LocalDateTime calculateTime, CountryEnums country, RegionEnums region) {
        if (Objects.isNull(calculateTime)) {
            return false;
        }
        if (!CountryEnums.CN.equals(country)) {
            log.error("【{}】国家的节假日/工作日匹配暂不支持", country.getDesc());
            return false;
        }

        int year = calculateTime.getYear();
        List<DaysEntity> list = getHolidayData(String.format("resources/holiday/cn/%s.json", year));
        List<LocalDate> holidayList = list.stream().filter(day -> Objects.equals(day.getIsOffDay(), true))
                .map(DaysEntity::getDate).collect(Collectors.toList());
        List<LocalDate> workdayList = list.stream().filter(day -> Objects.equals(day.getIsOffDay(), false))
                .map(DaysEntity::getDate).collect(Collectors.toList());
        LocalDate date = calculateTime.toLocalDate();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        if (flag) {
            // 节假日：包含周六、周日、holidayList，排除 workdayList
            if (workdayList.contains(date)) {
                return false;
            }
            return isWeekend || holidayList.contains(date);
        } else {
            // 工作日：包含周一-周五、workdayList，排除 holidayList
            if (holidayList.contains(date)) {
                return false;
            }
            return !isWeekend || workdayList.contains(date);
        }
    }

    private List<DaysEntity> getHolidayData(String filePath) {
        String classpathPath = filePath.startsWith("resources/")
                ? filePath.substring("resources/".length())
                : filePath;
        return HOLIDAY_CACHE.computeIfAbsent(classpathPath, this::loadHolidayData);
    }

    private List<DaysEntity> loadHolidayData(String classpathPath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(classpathPath)) {
            if (Objects.isNull(inputStream)) {
                log.error("节假日数据文件不存在: {}", classpathPath);
                return new ArrayList<>();
            }
            JSONObject jsonObject = JSON.parseObject(inputStream, StandardCharsets.UTF_8);
            List<DaysEntity> days = jsonObject.getList("days", DaysEntity.class);
            return Objects.nonNull(days) ? days : new ArrayList<>();
        } catch (Exception e) {
            log.error("读取节假日数据失败: {}", classpathPath, e);
            return new ArrayList<>();
        }
    }
}
