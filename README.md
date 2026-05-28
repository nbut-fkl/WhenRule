# WhenRule

WhenRule 是一个轻量级的 **Java 时间规则匹配库**。通过链式 Builder 组合多种时间条件，判断某个时刻是否满足业务规则。

适用于定时任务调度、营销活动生效窗口、仅在节假日/工作日执行逻辑等场景。

---

## 特性

- **链式 API**：Fluent Builder，条件组合清晰易读
- **多种时间维度**：时间区间、节假日/工作日、周末、指定年/月/日/周几
- **中国节假日支持**：内置 2007–2027 年中国法定节假日与调休数据（JSON）
- **调休感知**：正确处理「周末补班」与「工作日放假」
- **灵活匹配模式**：支持 `ALL`（全部满足）与 `ANY`（任一满足）
- **时区支持**：基于 `ZonedDateTime` 与地区时区配置

---

## 环境要求

- Java 17+
- Maven 3.x

---

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>com.fkl.whenRyle</groupId>
    <artifactId>WhenRule</artifactId>
    <version>1.0.0</version>
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

## 运行测试

```bash
mvn test
```

---

## 路线图

### 短期计划

- [ ] **更强的自定义时间配置**
  - 支持 cron 表达式
  - 支持每日/每周重复时间段（如「每个工作日下午 2–5 点」）
  - 支持跨天时间段与更灵活的组合规则
- [ ] **节假日数据存储升级**
  - 从 classpath JSON 迁移至数据库
  - 支持运行时动态更新，无需重新发布
  - 提供数据管理接口（增删改查、批量导入）
- [ ] **更多内置模板**
  - 预置常用规则模板（如「工作日上午」「月末最后一天」「法定节假日」）
  - 一行代码快速启用常见场景

### 长期计划

- [ ] **多国家 / 多地区支持**
  - 当前节假日逻辑仅完整支持中国（`CN`）
  - 扩展美国、欧盟等主要国家/地区的节假日与工作历
  - 支持按地区自定义工作日历
- [ ] **国际化与扩展性**
  - 插件化条件注册机制
  - 更丰富的时区与夏令时处理
  - Spring Boot Starter 集成

---

## 项目结构

```
WhenRule/
├── src/main/java/com/fkl/whenRule/
│   ├── WhenRule.java              # 入口
│   ├── WhenRuleBuilder.java       # 链式构建器
│   ├── WhenRuleTimeRange.java     # 时间区间
│   ├── condition/                 # 条件接口与实现
│   ├── entity/                    # 数据实体
│   └── enums/                     # 枚举（国家、地区、匹配模式）
├── src/main/resources/holiday/cn/ # 中国节假日 JSON 数据
└── src/test/java/                 # 单元测试
```

---

## 许可证

待定（License TBD）
