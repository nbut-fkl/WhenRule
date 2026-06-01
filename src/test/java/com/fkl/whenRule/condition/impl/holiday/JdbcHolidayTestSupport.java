package com.fkl.whenRule.condition.impl.holiday;

import org.h2.jdbcx.JdbcDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

final class JdbcHolidayTestSupport {

    private JdbcHolidayTestSupport() {
    }

    static JdbcDataSource createDataSource() {
        try {
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:whenrule_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            dataSource.setUser("sa");
            dataSource.setPassword("");
            initSchema(dataSource);
            return dataSource;
        } catch (Exception e) {
            throw new IllegalStateException("初始化测试数据库失败", e);
        }
    }

    private static void initSchema(JdbcDataSource dataSource) throws IOException, SQLException {
        String ddl;
        try (InputStream input = JdbcHolidayTestSupport.class.getClassLoader()
                .getResourceAsStream("schema-h2.sql")) {
            if (input == null) {
                throw new IllegalStateException("schema-h2.sql 不存在");
            }
            ddl = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }
    }
}
