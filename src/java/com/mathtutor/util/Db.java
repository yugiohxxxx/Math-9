package com.mathtutor.util;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

/** Tiện ích JDBC: mỗi dòng kết quả là Map (key = tên cột viết thường chữ đầu) nên JSP dùng được ngay bằng EL. */
public class Db {
    private static final String URL, USER, PASS;

    static {
        Properties p = new Properties();
        try (InputStream in = Db.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) { }
        URL = p.getProperty("url", "jdbc:sqlserver://localhost:1433;databaseName=MathTutor;encrypt=true;trustServerCertificate=true");
        USER = p.getProperty("user", "sa");
        PASS = p.getProperty("password", "123456");
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Thiếu thư viện mssql-jdbc", e);
        }
    }

    public static class DbException extends RuntimeException {
        public DbException(SQLException e) { super(e.getMessage(), e); }
    }

    public static Connection conn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** IsActive -> active, IsCorrect -> correct, UserId -> userId ... */
    static String key(String label) {
        if (label == null || label.isEmpty()) return "value"; // cột không đặt tên, vd COUNT(*)
        if (label.length() > 2 && label.startsWith("Is") && Character.isUpperCase(label.charAt(2)))
            label = label.substring(2);
        return Character.toLowerCase(label.charAt(0)) + label.substring(1);
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }

    public static List<Map<String, Object>> query(String sql, Object... params) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                List<Map<String, Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++)
                        row.put(key(md.getColumnLabel(i)), rs.getObject(i));
                    list.add(row);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public static Map<String, Object> one(String sql, Object... params) {
        List<Map<String, Object>> l = query(sql, params);
        return l.isEmpty() ? null : l.get(0);
    }

    public static int count(String sql, Object... params) {
        Map<String, Object> r = one(sql, params);
        if (r == null) return 0;
        Object v = r.values().iterator().next();
        return v == null ? 0 : ((Number) v).intValue();
    }

    public static int update(String sql, Object... params) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /** Chạy nhiều câu lệnh trong MỘT giao dịch; mỗi phần tử là {sql, tham số...}. Lỗi ở bất kỳ câu nào thì huỷ toàn bộ. */
    public static void tx(Object[]... stmts) {
        try (Connection c = conn()) {
            c.setAutoCommit(false);
            try {
                for (Object[] s : stmts) {
                    try (PreparedStatement ps = c.prepareStatement((String) s[0])) {
                        bind(ps, Arrays.copyOfRange(s, 1, s.length));
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /** INSERT và trả về khoá identity vừa tạo. */
    public static long insert(String sql, Object... params) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }
}
