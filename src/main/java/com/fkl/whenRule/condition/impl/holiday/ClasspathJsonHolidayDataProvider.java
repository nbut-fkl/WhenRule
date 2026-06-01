package com.fkl.whenRule.condition.impl.holiday;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认数据源实现：从 jar 内置的 classpath JSON 文件读取节假日数据。
 * <p>
 * 文件路径约定：{@code holiday/{country}/{year}.json}，例如 {@code holiday/cn/2026.json}。
 *
 * @author fkl
 */
@Slf4j
public class ClasspathJsonHolidayDataProvider implements HolidayDataProvider {

    private final Map<String, List<DaysEntity>> cache = new ConcurrentHashMap<>();

    @Override
    public List<DaysEntity> load(CountryEnums country, int year) {
        String path = String.format("holiday/%s/%s.json", country.name().toLowerCase(), year);
        return cache.computeIfAbsent(path, this::doLoad);
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    private List<DaysEntity> doLoad(String classpathPath) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(classpathPath)) {
            if (Objects.isNull(input)) {
                log.error("节假日数据文件不存在: {}", classpathPath);
                return new ArrayList<>();
            }
            JSONObject json = JSON.parseObject(input, StandardCharsets.UTF_8);
            List<DaysEntity> days = json.getList("days", DaysEntity.class);
            return Objects.nonNull(days) ? days : new ArrayList<>();
        } catch (Exception e) {
            log.error("读取节假日数据失败: {}", classpathPath, e);
            return new ArrayList<>();
        }
    }
}
