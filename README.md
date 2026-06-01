# WhenRule

WhenRule 是一个轻量级的 **Java 时间规则匹配库**。通过链式 Builder 组合多种时间条件，判断某个时刻是否满足业务规则。

适用于定时任务调度、营销活动生效窗口、仅在节假日/工作日执行逻辑等场景。

---

## 特性

- **链式 API**：Fluent Builder，条件组合清晰易读
- **多种时间维度**：时间区间、每日时间窗口、节假日/工作日、周末、指定年/月/日/周几
- **跨午夜支持**：每日时间窗口支持跨日（如 22:00 ~ 次日 02:00）
- **AND / OR 多段组合**：通过 `.and()` / `.or()` 组合多组条件，从左到右求值
- **中国节假日支持**：内置 2007–2027 年中国法定节假日与调休数据（JSON）
- **调休感知**：正确处理「周末补班」与「工作日放假」
- **可插拔数据源**：通过 SPI 接口自定义节假日数据来源（DB / Redis / HTTP / 自定义文件等），零侵入扩展，支持**全局配置**与**调用级覆盖**两种粒度
- **灵活匹配模式**：单段内支持 `ALL`（全部满足）与 `ANY`（任一满足）
- **时区支持**：基于 `ZonedDateTime` 与地区时区配置
- **零框架依赖**：核心库不依赖 Spring 等框架，纯 Java 项目可直接使用
- **内置模板**：`WhenRuleTemplates` 提供法定节假日、工作日上午、月末最后一天等常用场景，一行代码启用

---

## 环境要求

- Java 17+
- Maven 3.x

---

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>io.github.nbut-fkl</groupId>
    <artifactId>WhenRule</artifactId>
    <version>1.2.0</version>
</dependency>
```

> 若尚未发布到 Maven 中央仓库，可先在本项目根目录执行 `mvn install` 安装到本地仓库。

### 基本用法

```java
import com.fkl.whenRule.WhenRule;
import com.fkl.whenRule.WhenRuleBuilder;
import com.fkl.whenRule.WhenRuleTimeRange;
import com.fkl.whenRule.enums.MatchingModelEnums;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

// 判断当前时间是否为工作日上午 9:00–12:00
boolean matched = WhenRule.when(
    new WhenRuleBuilder()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
        .matchingModel(MatchingModelEnums.ALL)
        .timeRange(new WhenRuleTimeRange(
            LocalDateTime.of(2026, 3, 10, 9, 0),
            LocalDateTime.of(2026, 3, 10, 12, 0)
        ))
        .holiday(false)          // 工作日（含调休补班）
        .specifyMonth(List.of(3)) // 3 月
);

System.out.println(matched); // true 或 false
```

### 使用内置模板（推荐）

```java
import com.fkl.whenRule.WhenRule;
import com.fkl.whenRule.template.WhenRuleTemplates;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// 工作日上午 09:00–12:00
boolean matched = WhenRule.when(
    WhenRuleTemplates.workdayMorning()
        .calculateTime(ZonedDateTime.of(
            LocalDateTime.of(2026, 3, 10, 10, 0),
            ZoneId.of("Asia/Shanghai")
        ))
);

// 法定节假日
WhenRule.when(
    WhenRuleTemplates.officialHoliday()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
);
```

---

## 核心概念

### 入口

```java
boolean result = WhenRule.when(builder);
```

- 未设置任何条件时，默认返回 `true`
- 传入 `null` builder 时，会使用默认 builder（当前时间、中国时区）

### 匹配模式

| 模式 | 说明 |
|------|------|
| `MatchingModelEnums.ALL` | 所有已配置条件均满足时返回 `true`（默认） |
| `MatchingModelEnums.ANY` | 任一条件满足即返回 `true` |

### 基础配置

| 方法 | 说明 | 默认值 |
|------|------|--------|
| `country(CountryEnums)` | 国家 | `CN` |
| `region(RegionEnums)` | 地区/时区 | 随国家默认（CN → `Asia/Shanghai`） |
| `calculateTime(ZonedDateTime)` | 待判断的时间 | 当前时间 |
| `matchingModel(MatchingModelEnums)` | 匹配模式 | `ALL` |

> 切换 `country()` 时会自动同步该国家的默认 `region`。

---

## 条件说明

同一类条件重复调用时会**替换**而非叠加（例如多次调用 `holiday()` 只保留最后一次）。

### 1. 时间区间 `timeRange()`

```java
.timeRange(new WhenRuleTimeRange(startTime, endTime))
```

| 参数 | 说明 |
|------|------|
| `startTime` | 开始时间，`null` 表示无下限 |
| `endTime` | 结束时间，`null` 表示无上限 |

边界为**闭区间** `[start, end]`，起止时刻本身也算匹配。

---

### 2. 节假日 / 工作日 `holiday()`

```java
.holiday(true)   // 匹配节假日
.holiday(false)  // 匹配工作日（含调休补班）
```

**目前仅支持中国（`CountryEnums.CN`）**，数据来自 `src/main/resources/holiday/cn/{year}.json`。

| `flag` | 包含 | 排除 |
|--------|------|------|
| `true`（节假日） | 周六、周日、法定放假日期 | 调休补班日（`isOffDay: false`） |
| `false`（工作日） | 周一至周五、调休补班日 | 法定放假日期（`isOffDay: true`） |

示例（2026 年）：

| 日期 | 说明 | `holiday(true)` | `holiday(false)` |
|------|------|:---------------:|:----------------:|
| 2026-01-01 | 元旦（周四） | ✅ | ❌ |
| 2026-02-14 | 春节调休补班（周六） | ❌ | ✅ |
| 2026-02-15 | 春节（周日） | ✅ | ❌ |
| 2026-03-10 | 普通周二 | ❌ | ✅ |

---

### 3. 周末 `weekend()`

```java
.weekend(true)   // 匹配周六、周日
.weekend(false)  // 匹配周一至周五
```

> **注意**：`weekend()` 仅按自然周判断，**不感知调休**。若需准确的中国工作日/节假日判断，请优先使用 `holiday()`，避免与 `weekend()` 同时使用时产生逻辑冲突。

---

### 4. 指定周几 `specifyWeekday()`

```java
.specifyWeekday(List.of(1, 5))  // 周一、周五
```

- 取值范围：`1`（周一）– `7`（周日）
- 传 `null`：不限制（不加入该条件）
- 传空列表 `List.of()`：始终不匹配

---

### 5. 指定日期 `specifyDay()`

```java
.specifyDay(List.of(1, 15, 31))  // 每月 1、15、31 号
```

- 取值范围：`1`–`31`
- `null` / 空列表语义同 `specifyWeekday()`

---

### 6. 指定月份 `specifyMonth()`

```java
.specifyMonth(List.of(1, 6, 12))  // 1 月、6 月、12 月
```

- 取值范围：`1`（一月）– `12`（十二月）

---

### 7. 指定年份 `specifyYear()`

```java
.specifyYear(List.of(2025, 2026))
```

- 四位年份，如 `2026`

---

### 8. 每日时间窗口 `dailyTimeRange()`

```java
.dailyTimeRange(new WhenRuleDailyTimeRange(LocalTime.of(14, 0), LocalTime.of(17, 0)))
```

与 `timeRange()` 的区别：**只关心时分秒，不绑定具体日期**，适合表达「每天/每个工作日的某个固定时段」。

| 参数 | 说明 |
|------|------|
| `startTime` | 开始时间（`LocalTime`），`null` 表示无下限 |
| `endTime` | 结束时间（`LocalTime`），`null` 表示无上限 |

- 边界为**闭区间** `[start, end]`
- **跨午夜**：当 `startTime` 晚于 `endTime` 时（例如 `22:00 ~ 02:00`）自动按跨日处理

```java
// 每天晚上 22:00 ~ 次日 02:00
.dailyTimeRange(new WhenRuleDailyTimeRange(LocalTime.of(22, 0), LocalTime.of(2, 0)))
```

---

## 多段条件组合（AND / OR）

当一个条件组（单个 Builder 内的 ALL/ANY）无法表达需求时，可以用 `.and()` / `.or()` 把多组条件串起来。**保留基础配置**（`country` / `region` / `calculateTime` / `matchingModel`），后续段无需重复设置。

```java
// 「工作日下午 2~5 点」 OR 「周末上午 9~12 点」
boolean matched = WhenRule.when(
    new WhenRuleBuilder()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
        .holiday(false)
        .dailyTimeRange(new WhenRuleDailyTimeRange(LocalTime.of(14, 0), LocalTime.of(17, 0)))
        .or()
        .weekend(true)
        .dailyTimeRange(new WhenRuleDailyTimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)))
);
```

### 求值规则

- **从左到右顺序求值**，不区分 AND/OR 优先级
  - `A .and() B .or() C` 等价于 `(A AND B) OR C`，**而非** `A AND (B OR C)`
- 每个段内仍由 `matchingModel(ALL/ANY)` 控制条件之间的关系
- 段间的 `.and()` / `.or()` 只用于段与段之间
- `calculateTime`、`country`、`region`、`matchingModel` 这些基础配置在所有段间**自动保留**
- 单段 Builder（不调用 `.and()` / `.or()`）行为与之前完全一致，向后兼容

---

## 使用示例

### 仅在工作日 9:00–18:00 执行

```java
WhenRule.when(
    new WhenRuleBuilder()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
        .timeRange(new WhenRuleTimeRange(
            LocalDateTime.of(2026, 1, 1, 9, 0),
            LocalDateTime.of(2026, 1, 1, 18, 0)
        ))
        .holiday(false)
);
```

### 仅在法定节假日执行

```java
WhenRule.when(
    new WhenRuleBuilder()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
        .holiday(true)
);
```

### 每月 1 号和 15 号

```java
WhenRule.when(
    new WhenRuleBuilder()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
        .specifyDay(List.of(1, 15))
);
```

### 2026 年 Q1 的周一或周五（任一满足）

```java
WhenRule.when(
    new WhenRuleBuilder()
        .matchingModel(MatchingModelEnums.ANY)
        .calculateTime(ZonedDateTime.of(
            LocalDateTime.of(2026, 2, 2, 10, 0),
            ZoneId.of("Asia/Shanghai")
        ))
        .specifyYear(List.of(2026))
        .specifyMonth(List.of(1, 2, 3))
        .specifyWeekday(List.of(1, 5))
);
```

### 每个工作日下午 2:00–5:00（每日重复）

```java
WhenRule.when(
    new WhenRuleBuilder()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
        .holiday(false)
        .dailyTimeRange(new WhenRuleDailyTimeRange(LocalTime.of(14, 0), LocalTime.of(17, 0)))
);
```

### 工作日下午 OR 周末上午（多段组合）

```java
WhenRule.when(
    new WhenRuleBuilder()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
        .holiday(false)
        .dailyTimeRange(new WhenRuleDailyTimeRange(LocalTime.of(14, 0), LocalTime.of(17, 0)))
        .or()
        .weekend(true)
        .dailyTimeRange(new WhenRuleDailyTimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)))
);
```

---

## 内置模板 `WhenRuleTemplates`

`WhenRuleTemplates` 集中提供常用规则模板，每个静态方法返回预配置好的 `WhenRuleBuilder`，可继续链式追加条件。

> 模板**不会**自动设置 `calculateTime`，使用前请调用 `.calculateTime(...)` 指定待判断时间。

| 方法 | 说明 |
|------|------|
| **节假日 / 工作日** | |
| `officialHoliday()` | 法定节假日（含自然周末，排除调休补班） |
| `officialHoliday(HolidayDataProvider)` | 法定节假日 + 自定义数据源 |
| `workday()` | 工作日（含调休补班，排除法定放假） |
| `workday(HolidayDataProvider)` | 工作日 + 自定义数据源 |
| `weekend()` | 自然周末（周六、周日，不感知调休） |
| `weekday()` / `mondayToFriday()` | 自然周一至周五 |
| **工作时段** | |
| `workdayMorning()` / `(start, end)` | 工作日上午，默认 09:00–12:00 |
| `workdayAfternoon()` / `(start, end)` | 工作日下午，默认 14:00–18:00 |
| `workdayEvening()` / `(start, end)` | 工作日晚上，默认 18:00–22:00 |
| `businessHours()` / `(start, end)` | 工作时段，默认 09:00–18:00 |
| `workdayNightShift()` / `(start, end)` | 工作日夜班，默认 22:00–次日 06:00 |
| `lunchBreak()` / `(start, end)` | 午休，默认 12:00–13:30 |
| `nightShift()` / `(start, end)` | 跨午夜时段，默认 22:00–06:00 |
| **周末 / 节假日 + 时段** | |
| `weekendMorning()` / `(start, end)` | 周末上午，默认 09:00–12:00 |
| `weekendAfternoon()` / `(start, end)` | 周末下午，默认 14:00–18:00 |
| `holidayMorning()` / `(start, end)` | 节假日上午，默认 09:00–12:00 |
| `holidayAfternoon()` / `(start, end)` | 节假日下午，默认 14:00–18:00 |
| **按月 / 按周 / 按年** | |
| `firstDayOfMonth()` | 每月 1 号 |
| `midMonth()` | 每月 15 号 |
| `lastDayOfMonth()` | 每月最后一天 |
| `payday()` | 发薪日（每月 10、15 号） |
| `monthlyOnDays(int...)` | 指定每月若干日期 |
| `everyMonday()` / `everyFriday()` / `everySunday()` | 每周固定星期 |
| `weeklyOnWeekdays(int...)` | 指定每周若干星期（1=周一，7=周日） |
| `q1()` / `q2()` / `q3()` / `q4()` | 第一至第四季度 |
| `quarterEnd()` | 季度末最后一天（3/6/9/12 月） |
| `inMonths(int...)` | 指定若干月份 |
| `inYear(int)` / `inYears(int...)` | 指定年份 |

### 示例

```java
// 发薪日（10、15 号）的工作日上午
WhenRule.when(
    WhenRuleTemplates.workdayMorning()
        .calculateTime(ZonedDateTime.of(
            LocalDateTime.of(2026, 3, 10, 10, 0),
            ZoneId.of("Asia/Shanghai")
        ))
        .specifyDay(List.of(10, 15))
);

// 季度末最后一天
WhenRule.when(
    WhenRuleTemplates.quarterEnd()
        .calculateTime(ZonedDateTime.of(
            LocalDateTime.of(2026, 3, 31, 10, 0),
            ZoneId.of("Asia/Shanghai")
        ))
);

// 工作日上午
WhenRule.when(
    WhenRuleTemplates.workdayMorning()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
);

// 工作时段 08:30–17:30
WhenRule.when(
    WhenRuleTemplates.businessHours(LocalTime.of(8, 30), LocalTime.of(17, 30))
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
);

// 月末最后一天 + 继续追加条件
WhenRule.when(
    WhenRuleTemplates.lastDayOfMonth()
        .calculateTime(ZonedDateTime.of(
            LocalDateTime.of(2026, 1, 31, 10, 0),
            ZoneId.of("Asia/Shanghai")
        ))
        .specifyMonth(List.of(1, 4, 7, 10))  // 仅 Q 季末月
);

// 模板 + 多段组合
WhenRule.when(
    WhenRuleTemplates.workdayMorning()
        .calculateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")))
        .or()
        .weekend(true)
        .dailyTimeRange(new WhenRuleDailyTimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)))
);
```

---

## 节假日数据格式

数据文件路径：`src/main/resources/holiday/cn/{year}.json`

```json
{
    "days": [
        {
            "name": "元旦",
            "date": "2026-01-01",
            "isOffDay": true
        },
        {
            "name": "春节",
            "date": "2026-02-14",
            "isOffDay": false
        }
    ]
}
```

| 字段 | 说明 |
|------|------|
| `name` | 节假日名称 |
| `date` | 日期，格式 `yyyy-MM-dd` |
| `isOffDay` | `true` = 放假；`false` = 调休补班 |

---

## 自定义节假日数据源

WhenRule 提供了一个 **SPI（服务提供接口）** 机制 —— `HolidayDataProvider`，让你不必使用内置的 JSON 文件，而是接入**任意数据源**：自有数据库、Redis、HTTP 接口、配置中心等。

### 设计概览

| 组件 | 类型 | 作用 |
|------|------|------|
| [`HolidayDataProvider`](src/main/java/com/fkl/whenRule/condition/impl/holiday/HolidayDataProvider.java) | 接口 | 节假日数据源 SPI，用户实现该接口接入自己的数据源 |
| [`ClasspathJsonHolidayDataProvider`](src/main/java/com/fkl/whenRule/condition/impl/holiday/ClasspathJsonHolidayDataProvider.java) | 默认实现 | 从 jar 内置 `holiday/{country}/{year}.json` 读取，带本地缓存 |
| [`JdbcHolidayDataProvider`](src/main/java/com/fkl/whenRule/condition/impl/holiday/JdbcHolidayDataProvider.java) | 官方 JDBC 实现 | 用户传入 `DataSource`，从 `when_rule_holidays` 表按国家/年份查询 |
| [`HolidayAdminService`](src/main/java/com/fkl/whenRule/condition/impl/holiday/HolidayAdminService.java) | 管理 Service | 节假日数据增删改查（**仅 JDBC 模式可用**） |
| [`WhenRuleConfig`](src/main/java/com/fkl/whenRule/config/WhenRuleConfig.java) | 全局配置中心 | 在程序启动时注册自定义实现，未注册则使用默认 JSON |

> 不做任何配置时使用默认实现，开箱即用。

### SPI 接口定义

```java
public interface HolidayDataProvider {

    /**
     * 加载指定国家、指定年份的"非常规日"数据（节假日 + 调休工作日）。
     *
     * @param country 国家
     * @param year    年份
     * @return 当年节假日数据集合，无数据时返回空集合（不要返回 null）
     */
    List<DaysEntity> load(CountryEnums country, int year);

    /** 清空内部缓存，默认空实现，实现类如有缓存可重写 */
    default void clearCache() { }
}
```

返回的 `DaysEntity` 字段与 JSON 中的字段保持一致：

| 字段 | 说明 |
|------|------|
| `name` | 节假日名称 |
| `date` | 日期（`LocalDate`） |
| `isOffDay` | `true` = 放假；`false` = 调休补班 |

### 全局注册入口

`WhenRuleConfig` 是一个线程安全的静态配置中心，对外暴露三个方法：

| 方法 | 说明 |
|------|------|
| `getHolidayDataProvider()` | 获取当前 provider（默认 `ClasspathJsonHolidayDataProvider`） |
| `setHolidayDataProvider(provider)` | 注册自定义实现，传 `null` 会被忽略 |
| `setHolidayDataSource(dataSource)` | 注册 JDBC 数据源（内部封装为 `JdbcHolidayDataProvider`） |
| `isJdbcHolidayEnabled()` | 当前全局是否为 JDBC 模式 |
| `holidayAdminService()` | 获取 AdminService（非 JDBC 返回 `Optional.empty()`） |
| `requireHolidayAdminService()` | 获取 AdminService（非 JDBC 抛出异常） |
| `reset()` | 重置为默认 JSON 实现，便于单元测试 |

### 优先级模型

WhenRule 的节假日数据源解析采用 **调用级 → 全局** 的优先级，**调用级覆盖全局**：

```
WhenRuleBuilder.holidayDataProvider(...)   ← 调用级 provider（最高）
WhenRuleBuilder.holidayDataSource(...)     ← 调用级 DataSource（等价于 new JdbcHolidayDataProvider(ds)）
                  ↓ 未设置时回退
WhenRuleConfig.setHolidayDataProvider(...) ← 全局 provider
WhenRuleConfig.setHolidayDataSource(...)   ← 全局 DataSource
                  ↓ 未设置时回退
ClasspathJsonHolidayDataProvider            ← 内置 JSON 默认
```

这样设计的好处：

- **全局配置足够简单**：90% 场景只需启动时调用一次 `WhenRuleConfig.setHolidayDataProvider(...)`
- **多场景互不干扰**：不同业务线/不同请求可以通过 `builder.holidayDataProvider(...)` 各自指定，**无线程安全问题、无 ThreadLocal 泄漏风险**
- **调用级不污染全局**：builder 上的 provider 仅作用于本次规则计算，不会修改 `WhenRuleConfig`

### 用法 1：使用默认实现（零配置）

什么都不做即可。`HolidayCondition` 会自动调用内置 `ClasspathJsonHolidayDataProvider`，读取 jar 内置的 2007–2027 年中国节假日 JSON 数据。

### 用法 2：官方 JDBC 数据源（推荐）

先建表并导入数据（参见 [`sql/when_rule_holidays_init.sql`](sql/when_rule_holidays_init.sql)）：

```sql
CREATE TABLE when_rule_holidays(
    id bigint primary key auto_increment,
    name varchar(255),
    country varchar(255),
    `date` date,
    is_off_day tinyint default 0
);
```

**全局注册**（程序启动时一次）：

```java
import com.fkl.whenRule.config.WhenRuleConfig;
import com.fkl.whenRule.condition.impl.holiday.HolidayAdminService;
import javax.sql.DataSource;

// 用户应用自己准备 DataSource（HikariCP / Spring 等）
DataSource dataSource = ...;

WhenRuleConfig.setHolidayDataSource(dataSource);

// 管理数据（仅 JDBC 模式可用）
HolidayAdminService admin = WhenRuleConfig.requireHolidayAdminService();
admin.save(HolidayRecord.builder()
    .name("元旦")
    .country("CN")
    .date(LocalDate.of(2026, 1, 1))
    .isOffDay(true)
    .build());
```

**Builder 级注册**（单次规则使用独立数据源）：

```java
WhenRule.when(new WhenRuleBuilder()
    .calculateTime(...)
    .holiday(true)
    .holidayDataSource(dataSource));   // 或 .holidayDataProvider(new JdbcHolidayDataProvider(dataSource))
```

> 未调用 `setHolidayDataSource` 时，默认仍使用 jar 内 JSON，**AdminService 不可用**（`isJdbcHolidayEnabled()` 为 `false`）。

### 用法 3：自定义 Provider（完全自行实现）

若需接入 Redis / HTTP 等非 JDBC 存储，仍可实现 SPI：

```java
import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;
import com.fkl.whenRule.config.WhenRuleConfig;
import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.enums.CountryEnums;

import java.util.List;

public class MyHolidayDataProvider implements HolidayDataProvider {

    @Override
    public List<DaysEntity> load(CountryEnums country, int year) {
        // 你的查询逻辑：Redis / HTTP / MyBatis 等
        return holidayDao.queryByCountryAndYear(country.name(), year);
    }
}

WhenRuleConfig.setHolidayDataProvider(new MyHolidayDataProvider());
```

### 用法 4：混合 / 装饰模式（默认数据 + 自定义补丁）

例如想在内置数据基础上叠加公司内部的特殊调休：

```java
import com.fkl.whenRule.condition.impl.holiday.ClasspathJsonHolidayDataProvider;
import com.fkl.whenRule.condition.impl.holiday.HolidayDataProvider;
import com.fkl.whenRule.config.WhenRuleConfig;

import java.util.ArrayList;
import java.util.List;

WhenRuleConfig.setHolidayDataProvider(new HolidayDataProvider() {
    private final HolidayDataProvider fallback = new ClasspathJsonHolidayDataProvider();

    @Override
    public List<DaysEntity> load(CountryEnums country, int year) {
        List<DaysEntity> list = new ArrayList<>(fallback.load(country, year));
        list.addAll(myDao.queryCompanyHolidays(year));   // 追加公司内部调休
        return list;
    }
});
```

### 用法 5：Spring Boot 项目集成

在 Spring 项目中注入 `DataSource` 后注册：

```java
@Component
public class WhenRuleInitializer {

    @Resource
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        WhenRuleConfig.setHolidayDataSource(dataSource);
    }
}
```

> 提示：未来会提供官方 `WhenRule-spring-boot-starter`，自动从 yml 配置完成注册。

### 用法 6：调用级 provider / DataSource（多场景并发隔离）⭐️

当应用内**同时存在多种业务场景**，每个场景需要不同的节假日数据源时，**不要**频繁调用 `WhenRuleConfig.setHolidayDataProvider(...)`（会引发线程安全问题），而是使用 `WhenRuleBuilder.holidayDataProvider(...)` —— **仅作用于本次规则计算**：

```java
HolidayDataProvider tenantA = (country, year) -> dao.queryTenantA(year);
HolidayDataProvider tenantB = (country, year) -> dao.queryTenantB(year);

// 线程 1
boolean r1 = WhenRule.when(new WhenRuleBuilder()
        .holidayDataProvider(tenantA)   // 仅当前 builder 使用 tenantA
        .holiday(true));

// 线程 2（同一时刻，互不影响）
boolean r2 = WhenRule.when(new WhenRuleBuilder()
        .holidayDataProvider(tenantB)   // 仅当前 builder 使用 tenantB
        .holiday(true));
```

**特性**：

- ✅ 完全线程安全，多个 builder 互不干扰
- ✅ 不修改全局 `WhenRuleConfig`，没有副作用
- ✅ 调用顺序无要求：`holidayDataProvider()` 在 `holiday()` 前后调用都生效
- ✅ `.and()` / `.or()` 多段组合时自动跨段生效
- ✅ 传 `null` 显式回退到全局配置

### `HolidayAdminService` 数据管理（仅 JDBC 模式）

当且仅当使用 `JdbcHolidayDataProvider` 时，可通过 AdminService 直接操作数据库：

```java
if (WhenRuleConfig.isJdbcHolidayEnabled()) {
    HolidayAdminService admin = WhenRuleConfig.requireHolidayAdminService();

    long id = admin.save(HolidayRecord.builder()
        .name("公司年会假").country("CN")
        .date(LocalDate.of(2026, 12, 25)).isOffDay(true).build());

    admin.update(...);
    admin.deleteById(id);
    admin.listByCountryAndYear(CountryEnums.CN, 2026);
    admin.replaceYear(CountryEnums.CN, 2027, records);
    admin.importDays(CountryEnums.CN, 2027, daysFromJson);
}
```

| 方法 | 说明 |
|------|------|
| `save` / `update` / `deleteById` | 增删改 |
| `listByCountryAndYear` | 按国家+年份查询 |
| `replaceYear` / `importDays` | 整年覆盖 / 批量导入 |

> JSON 模式下 `holidayAdminService()` 返回 empty；后台可在 `isJdbcHolidayEnabled()` 为 false 时禁用写操作。

### 单元测试中的隔离

为避免不同测试用例之间互相污染，建议：

```java
@AfterEach
void tearDown() {
    HolidayCondition.clearCache();   // 清当前 provider 缓存
    WhenRuleConfig.reset();          // 重置回默认实现
}
```

### 注意事项

- `load()` **不要返回 `null`**，无数据请返回空集合
- 自定义实现请自行处理**缓存与并发**（默认实现使用 `ConcurrentHashMap`）
- `HolidayCondition.clearCache()` 内部委托给**全局 provider** 的 `clearCache()`，不会清理通过 builder 级注入的 provider 实例缓存
- `setHolidayDataProvider(null)` / `setHolidayDataSource(null)` 不会清空当前 provider，会被静默忽略
- `HolidayAdminService` 仅在 JDBC 模式下可用；改库后会自动 `clearCache()`，builder 级私有 provider 需自行清理
- `WhenRuleConfig` 全局单例**不适合在运行时频繁切换**（多线程会互相覆盖），需要场景隔离时请使用 `WhenRuleBuilder.holidayDataProvider(...)` / `holidayDataSource(...)` 调用级注入
- 目前节假日匹配逻辑仅对 `CountryEnums.CN` 生效；自定义 provider 即便提供了其他国家数据，仍受此限制（后续会扩展）

---

## 运行测试

```bash
mvn test
```

---

## 路线图

### 短期计划

- [x] **更强的自定义时间配置**
  - [x] 支持每日/每周重复时间段（`dailyTimeRange` 已实现，参见「条件说明」第 8 节）
  - [x] 支持跨天时间段（`dailyTimeRange` 跨午夜场景，如 22:00 ~ 次日 02:00）
  - [x] 多段条件组合（`.and()` / `.or()` 语法）
- [x] **可插拔节假日数据源**
  - [x] 通过 `HolidayDataProvider` SPI 接入任意数据源（DB / Redis / HTTP / 自定义文件）
  - [x] 全局配置中心 `WhenRuleConfig`，启动时一次注册即可全局生效
  - [x] 默认实现 `ClasspathJsonHolidayDataProvider` 保持向后兼容
  - [x] 调用级 provider 注入（`WhenRuleBuilder.holidayDataProvider(...)`），支持多场景并发隔离
- [x] **节假日数据存储升级（架构层）**
  - [x] 通过 SPI 把"如何取数据"从 jar 内置 JSON 解耦出来，用户可接入任意存储
  - [x] 支持运行时动态更新（用户的 provider 直接查 DB / Redis / 配置中心即可），无需重新发布
- [x] **官方扩展实现（生态层）**
  - [x] 官方 `JdbcHolidayDataProvider`（用户传入 `DataSource`，按国家/年份查询）
  - [x] `WhenRuleConfig.setHolidayDataSource` / `WhenRuleBuilder.holidayDataSource` 全局与调用级注入
  - [x] `HolidayAdminService` 提供增删改查、整年覆盖、批量导入（仅 JDBC 模式可用）
- [x] **更多内置模板**
  - [x] `WhenRuleTemplates` 预置常用规则（法定节假日、工作日、工作日上午/下午、工作时段、月末最后一天等）
  - [x] 一行代码快速启用常见场景，返回的 Builder 可继续链式扩展

### 长期计划

- [ ] **多国家 / 多地区支持**
  - 当前节假日逻辑仅完整支持中国（`CN`）
  - 扩展美国、欧盟等主要国家/地区的节假日与工作历
  - 支持按地区自定义工作日历
- [ ] **国际化与扩展性**
  - 插件化条件注册机制
  - 更丰富的时区与夏令时处理
  - Spring Boot Starter 集成（自动从 `application.yml` 注册自定义 `HolidayDataProvider`）

---

## 项目结构

```
WhenRule/
├── src/main/java/com/fkl/whenRule/
│   ├── WhenRule.java                  # 入口
│   ├── WhenRuleBuilder.java           # 链式构建器
│   ├── WhenRuleTimeRange.java         # 绝对时间区间（LocalDateTime）
│   ├── WhenRuleDailyTimeRange.java    # 每日时间窗口（LocalTime）
│   ├── config/                        # 全局配置（WhenRuleConfig）
│   ├── template/                      # 内置模板（WhenRuleTemplates）
│   ├── condition/                     # 条件接口与实现
│   │   └── impl/holiday/              # SPI、JSON/JDBC 实现、HolidayAdminService
│   ├── entity/                        # DaysEntity、HolidayRecord 等
│   └── enums/                         # 枚举（国家、地区、匹配模式、AND/OR）
├── sql/when_rule_holidays_init.sql    # 节假日表 DDL + 初始化数据
├── src/main/resources/holiday/cn/     # 中国节假日 JSON 数据（默认数据源）
└── src/test/java/                     # 单元测试
```

---

## 许可证

本项目采用 [MIT License](LICENSE)（麻省理工学院许可证）。

您可以自由使用、修改、分发本软件，但需保留原始版权声明与许可全文。
