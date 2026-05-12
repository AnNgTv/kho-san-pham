package com.anngtv.kho.database;

import com.anngtv.kho.MineralWarehouse;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private final MineralWarehouse plugin;
    private Connection connection;

    public DatabaseManager(MineralWarehouse plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/warehouse.db";
            connection = DriverManager.getConnection(url);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String sql1 = "CREATE TABLE IF NOT EXISTS warehouse (" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "mineral_type TEXT NOT NULL," +
                "amount BIGINT DEFAULT 0," +
                "PRIMARY KEY (player_uuid, mineral_type)" +
                ")";
        String sql2 = "CREATE TABLE IF NOT EXISTS player_settings (" +
                "player_uuid VARCHAR(36) PRIMARY KEY," +
                "auto_deposit BOOLEAN DEFAULT 0" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql1);
            stmt.execute(sql2);
        }
    }

    public boolean isAutoDeposit(UUID uuid) {
        String sql = "SELECT auto_deposit FROM player_settings WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("auto_deposit");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setAutoDeposit(UUID uuid, boolean enabled) {
        String sql = "INSERT OR REPLACE INTO player_settings (player_uuid, auto_deposit) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setBoolean(2, enabled);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAmount(UUID uuid, String type, long amount) {
        String sql = "INSERT OR REPLACE INTO warehouse (player_uuid, mineral_type, amount) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, type);
            pstmt.setLong(3, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Long> getWarehouse(UUID uuid) {
        Map<String, Long> data = new HashMap<>();
        String sql = "SELECT mineral_type, amount FROM warehouse WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("mineral_type"), rs.getLong("amount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public long getAmount(UUID uuid, String type) {
        String sql = "SELECT amount FROM warehouse WHERE player_uuid = ? AND mineral_type = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("amount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
