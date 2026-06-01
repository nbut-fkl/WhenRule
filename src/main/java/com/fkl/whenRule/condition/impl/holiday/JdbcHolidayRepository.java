package com.fkl.whenRule.condition.impl.holiday;

import com.fkl.whenRule.entity.DaysEntity;
import com.fkl.whenRule.entity.HolidayRecord;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code when_rule_holidays} 表的 JDBC 访问层。
 *
 * @author fkl
 */
@Slf4j
public class JdbcHolidayRepository {

    public static final String DEFAULT_TABLE_NAME = "when_rule_holidays";

    private final DataSource dataSource;
    private final String tableName;

    public JdbcHolidayRepository(DataSource dataSource) {
        this(dataSource, DEFAULT_TABLE_NAME);
    }

    public JdbcHolidayRepository(DataSource dataSource, String tableName) {
        if (Objects.isNull(dataSource)) {
            throw new IllegalArgumentException("dataSource 不能为空");
        }
        if (Objects.isNull(tableName) || tableName.isBlank()) {
            throw new IllegalArgumentException("tableName 不能为空");
        }
        this.dataSource = dataSource;
        this.tableName = tableName;
    }

    public List<DaysEntity> findDaysByCountryAndYear(String country, int year) {
        String sql = "SELECT name, `date`, is_off_day FROM " + tableName
                + " WHERE country = ? AND YEAR(`date`) = ? ORDER BY `date`";
        List<DaysEntity> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(DaysEntity.builder()
                            .name(rs.getString("name"))
                            .date(rs.getDate("date").toLocalDate())
                            .isOffDay(rs.getInt("is_off_day") == 1)
                            .build());
                }
            }
        } catch (SQLException e) {
            log.error("查询节假日数据失败: country={}, year={}", country, year, e);
            throw new HolidayDataAccessException("查询节假日数据失败", e);
        }
        return result;
    }

    public List<HolidayRecord> findRecordsByCountryAndYear(String country, int year) {
        String sql = "SELECT id, name, country, `date`, is_off_day FROM " + tableName
                + " WHERE country = ? AND YEAR(`date`) = ? ORDER BY `date`";
        List<HolidayRecord> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRecord(rs));
                }
            }
        } catch (SQLException e) {
            log.error("查询节假日记录失败: country={}, year={}", country, year, e);
            throw new HolidayDataAccessException("查询节假日记录失败", e);
        }
        return result;
    }

    public Optional<HolidayRecord> findById(long id) {
        String sql = "SELECT id, name, country, `date`, is_off_day FROM " + tableName + " WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRecord(rs));
                }
            }
        } catch (SQLException e) {
            log.error("查询节假日记录失败: id={}", id, e);
            throw new HolidayDataAccessException("查询节假日记录失败", e);
        }
        return Optional.empty();
    }

    public long insert(HolidayRecord record) {
        validateRecord(record, false);
        String sql = "INSERT INTO " + tableName + " (name, country, `date`, is_off_day) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getName());
            ps.setString(2, record.getCountry());
            ps.setDate(3, Date.valueOf(record.getDate()));
            ps.setInt(4, Boolean.TRUE.equals(record.getIsOffDay()) ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new HolidayDataAccessException("插入节假日记录失败：未返回主键");
        } catch (SQLException e) {
            log.error("插入节假日记录失败: {}", record, e);
            throw new HolidayDataAccessException("插入节假日记录失败", e);
        }
    }

    public void update(HolidayRecord record) {
        validateRecord(record, true);
        String sql = "UPDATE " + tableName
                + " SET name = ?, country = ?, `date` = ?, is_off_day = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getName());
            ps.setString(2, record.getCountry());
            ps.setDate(3, Date.valueOf(record.getDate()));
            ps.setInt(4, Boolean.TRUE.equals(record.getIsOffDay()) ? 1 : 0);
            ps.setLong(5, record.getId());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new HolidayDataAccessException("更新节假日记录失败：id=" + record.getId() + " 不存在");
            }
        } catch (SQLException e) {
            log.error("更新节假日记录失败: {}", record, e);
            throw new HolidayDataAccessException("更新节假日记录失败", e);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("删除节假日记录失败: id={}", id, e);
            throw new HolidayDataAccessException("删除节假日记录失败", e);
        }
    }

    public void deleteByCountryAndYear(String country, int year) {
        String sql = "DELETE FROM " + tableName + " WHERE country = ? AND YEAR(`date`) = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country);
            ps.setInt(2, year);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("删除节假日记录失败: country={}, year={}", country, year, e);
            throw new HolidayDataAccessException("删除节假日记录失败", e);
        }
    }

    public int batchInsert(List<HolidayRecord> records) {
        if (Objects.isNull(records) || records.isEmpty()) {
            return 0;
        }
        String sql = "INSERT INTO " + tableName + " (name, country, `date`, is_off_day) VALUES (?, ?, ?, ?)";
        int count = 0;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (HolidayRecord record : records) {
                validateRecord(record, false);
                ps.setString(1, record.getName());
                ps.setString(2, record.getCountry());
                ps.setDate(3, Date.valueOf(record.getDate()));
                ps.setInt(4, Boolean.TRUE.equals(record.getIsOffDay()) ? 1 : 0);
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            for (int result : results) {
                if (result > 0) {
                    count++;
                }
            }
        } catch (SQLException e) {
            log.error("批量插入节假日记录失败", e);
            throw new HolidayDataAccessException("批量插入节假日记录失败", e);
        }
        return count;
    }

    private HolidayRecord mapRecord(ResultSet rs) throws SQLException {
        return HolidayRecord.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .country(rs.getString("country"))
                .date(rs.getDate("date").toLocalDate())
                .isOffDay(rs.getInt("is_off_day") == 1)
                .build();
    }

    private void validateRecord(HolidayRecord record, boolean requireId) {
        if (Objects.isNull(record)) {
            throw new IllegalArgumentException("record 不能为空");
        }
        if (requireId && Objects.isNull(record.getId())) {
            throw new IllegalArgumentException("id 不能为空");
        }
        if (Objects.isNull(record.getCountry()) || record.getCountry().isBlank()) {
            throw new IllegalArgumentException("country 不能为空");
        }
        if (Objects.isNull(record.getDate())) {
            throw new IllegalArgumentException("date 不能为空");
        }
    }
}
