package com.jobhunter.db;

import com.jobhunter.cli.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JobRepository {
    private final String dbUrl;

    public JobRepository() {
        String dbPath = Main.config.getString("jobhunter.db.path");
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initSchema();
    }

    public boolean hasBeenSeen(String url) {
        String sql = "SELECT 1 FROM seen_urls WHERE url = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, url);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("DB error checking URL: " + e.getMessage());
            return false;
        }
    }

    public void markAsSeen(String url) {
        String sql = "INSERT OR IGNORE INTO seen_urls (url) VALUES (?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB error marking URL as seen: " + e.getMessage());
        }
    }

    private void initSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS seen_urls (
                    url      TEXT PRIMARY KEY,
                    seen_at  TEXT DEFAULT (datetime('now'))
                )
                """;
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}
