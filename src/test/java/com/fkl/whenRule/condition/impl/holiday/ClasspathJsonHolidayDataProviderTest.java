package com.fkl.whenRule.condition.impl.holiday;

import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 默认 classpath JSON 数据源实现的单元测试。
 *
 * @author fkl
 */
class ClasspathJsonHolidayDataProviderTest {

    @Test
    void shouldLoadDataFromClasspathJson() {
        ClasspathJsonHolidayDataProvider provider = new ClasspathJsonHolidayDataProvider();

        List<DaysEntity> days = provider.load(CountryEnums.CN, 2026);

        assertNotNull(days);
        assertFalse(days.isEmpty(), "2026 年应能从 classpath 加载到节假日数据");
        // 元旦
        assertTrue(days.stream().anyMatch(d ->
                Objects.equals(d.getDate(), LocalDate.of(2026, 1, 1))
                        && Boolean.TRUE.equals(d.getIsOffDay())));
    }

    @Test
    void shouldReturnEmptyListWhenYearFileMissing() {
        ClasspathJsonHolidayDataProvider provider = new ClasspathJsonHolidayDataProvider();

        // 1900 年文件不存在，应返回空列表，且不抛异常
        List<DaysEntity> days = provider.load(CountryEnums.CN, 1900);

        assertNotNull(days);
        assertTrue(days.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenCountryNotProvided() {
        ClasspathJsonHolidayDataProvider provider = new ClasspathJsonHolidayDataProvider();

        // US 目录下没有数据文件
        List<DaysEntity> days = provider.load(CountryEnums.US, 2026);

        assertNotNull(days);
        assertTrue(days.isEmpty());
    }

    @Test
    void shouldCacheLoadedData() {
        ClasspathJsonHolidayDataProvider provider = new ClasspathJsonHolidayDataProvider();

        List<DaysEntity> first = provider.load(CountryEnums.CN, 2026);
        List<DaysEntity> second = provider.load(CountryEnums.CN, 2026);

        // 由于使用 ConcurrentHashMap.computeIfAbsent 缓存，应返回同一个引用
        assertSame(first, second);
    }

    @Test
    void shouldClearCache() {
        ClasspathJsonHolidayDataProvider provider = new ClasspathJsonHolidayDataProvider();

        List<DaysEntity> first = provider.load(CountryEnums.CN, 2026);
        provider.clearCache();
        List<DaysEntity> second = provider.load(CountryEnums.CN, 2026);

        // clearCache 后应重新加载，返回新的引用
        assertNotSame(first, second);
        assertEquals(first.size(), second.size());
    }
}
