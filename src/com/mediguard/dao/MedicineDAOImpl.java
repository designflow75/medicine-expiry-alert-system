package com.mediguard.dao;

import com.mediguard.database.DatabaseConnection;
import com.mediguard.model.ExpiryStatus;
import com.mediguard.model.Medicine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of MedicineDAO using JDBC PreparedStatements and MySQL.
 * Demonstrates Polymorphism and Method Overriding in OOP.
 */
public class MedicineDAOImpl implements MedicineDAO {

    private final DatabaseConnection dbConnection = DatabaseConnection.getInstance();

    @Override
    public boolean addMedicine(Medicine med) {
        if (med == null) return false;

        String sql = """
            INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, med.getUserId());
            ps.setString(2, med.getMedicineName());
            ps.setString(3, med.getCategory());
            ps.setString(4, med.getBatchNumber());
            ps.setInt(5, med.getQuantity());
            ps.setDate(6, Date.valueOf(med.getPurchaseDate()));
            ps.setDate(7, Date.valueOf(med.getExpiryDate()));
            ps.setString(8, med.getStorageLocation());
            ps.setString(9, med.getNotes());
            LocalDateTime created = med.getCreatedAt() != null ? med.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(10, Timestamp.valueOf(created));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        med.setMedicineId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding medicine: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateMedicine(Medicine med) {
        if (med == null) return false;

        String sql = """
            UPDATE medicines
            SET medicine_name = ?, category = ?, batch_number = ?, quantity = ?, purchase_date = ?, expiry_date = ?, storage_location = ?, notes = ?
            WHERE medicine_id = ? AND user_id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, med.getMedicineName());
            ps.setString(2, med.getCategory());
            ps.setString(3, med.getBatchNumber());
            ps.setInt(4, med.getQuantity());
            ps.setDate(5, Date.valueOf(med.getPurchaseDate()));
            ps.setDate(6, Date.valueOf(med.getExpiryDate()));
            ps.setString(7, med.getStorageLocation());
            ps.setString(8, med.getNotes());
            ps.setInt(9, med.getMedicineId());
            ps.setInt(10, med.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medicine: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteMedicine(int medicineId, int userId) {
        String sql = "DELETE FROM medicines WHERE medicine_id = ? AND user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medicineId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting medicine: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Medicine getMedicineById(int medicineId, int userId) {
        String sql = "SELECT * FROM medicines WHERE medicine_id = ? AND user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medicineId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMedicine(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding medicine: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Medicine> getMedicinesByUser(int userId) {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE user_id = ? ORDER BY expiry_date ASC, medicine_name ASC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medicines: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Medicine> searchAndFilterMedicines(int userId, String keyword, String category, ExpiryStatus statusFilter) {
        List<Medicine> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM medicines WHERE user_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(medicine_name) LIKE ? OR LOWER(category) LIKE ? OR LOWER(batch_number) LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All Categories")) {
            sql.append("AND LOWER(category) = ? ");
            params.add(category.trim().toLowerCase());
        }

        if (statusFilter != null) {
            switch (statusFilter) {
                case EXPIRED -> sql.append("AND expiry_date < CURRENT_DATE ");
                case EXPIRES_TODAY -> sql.append("AND expiry_date = CURRENT_DATE ");
                case CRITICAL -> sql.append("AND expiry_date > CURRENT_DATE AND expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY) ");
                case EXPIRING_SOON -> sql.append("AND expiry_date > DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY) AND expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) ");
                case SAFE -> sql.append("AND expiry_date > DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) ");
            }
        }

        sql.append("ORDER BY expiry_date ASC, medicine_name ASC");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in search/filter: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Medicine> getMedicinesByStatus(int userId, ExpiryStatus status) {
        return searchAndFilterMedicines(userId, null, null, status);
    }

    @Override
    public List<Medicine> getMedicinesExpiringWithinDays(int userId, int minDays, int maxDays) {
        List<Medicine> list = new ArrayList<>();
        String sql = """
            SELECT * FROM medicines
            WHERE user_id = ?
              AND expiry_date >= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
              AND expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
            ORDER BY expiry_date ASC
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, minDays);
            ps.setInt(3, maxDays);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching expiring medicines: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Medicine> getExpiredMedicines(int userId) {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE user_id = ? AND expiry_date < CURRENT_DATE ORDER BY expiry_date ASC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching expired medicines: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Medicine> getMedicinesExpiringToday(int userId) {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE user_id = ? AND expiry_date = CURRENT_DATE ORDER BY medicine_name ASC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medicines expiring today: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Map<ExpiryStatus, Integer> getStatusCounts(int userId) {
        Map<ExpiryStatus, Integer> map = new EnumMap<>(ExpiryStatus.class);
        for (ExpiryStatus st : ExpiryStatus.values()) {
            map.put(st, 0);
        }

        String sql = """
            SELECT
                COUNT(CASE WHEN expiry_date < CURRENT_DATE THEN 1 END) AS expired_cnt,
                COUNT(CASE WHEN expiry_date = CURRENT_DATE THEN 1 END) AS today_cnt,
                COUNT(CASE WHEN expiry_date > CURRENT_DATE AND expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY) THEN 1 END) AS critical_cnt,
                COUNT(CASE WHEN expiry_date > DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY) AND expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) THEN 1 END) AS expiring_soon_cnt,
                COUNT(CASE WHEN expiry_date > DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) THEN 1 END) AS safe_cnt
            FROM medicines
            WHERE user_id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    map.put(ExpiryStatus.EXPIRED, rs.getInt("expired_cnt"));
                    map.put(ExpiryStatus.EXPIRES_TODAY, rs.getInt("today_cnt"));
                    map.put(ExpiryStatus.CRITICAL, rs.getInt("critical_cnt"));
                    map.put(ExpiryStatus.EXPIRING_SOON, rs.getInt("expiring_soon_cnt"));
                    map.put(ExpiryStatus.SAFE, rs.getInt("safe_cnt"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting status counts: " + e.getMessage());
        }
        return map;
    }

    @Override
    public int getTotalMedicineCount(int userId) {
        String sql = "SELECT COUNT(*) FROM medicines WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total count: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<String> getCategories(int userId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM medicines WHERE user_id = ? AND category IS NOT NULL AND category <> '' ORDER BY category ASC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("category"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
        }
        return list;
    }

    private Medicine mapRowToMedicine(ResultSet rs) throws SQLException {
        int id = rs.getInt("medicine_id");
        int uid = rs.getInt("user_id");
        String name = rs.getString("medicine_name");
        String cat = rs.getString("category");
        String batch = rs.getString("batch_number");
        int qty = rs.getInt("quantity");

        Date pDate = rs.getDate("purchase_date");
        LocalDate purchaseDate = (pDate != null) ? pDate.toLocalDate() : LocalDate.now();

        Date eDate = rs.getDate("expiry_date");
        LocalDate expiryDate = (eDate != null) ? eDate.toLocalDate() : LocalDate.now();

        String loc = rs.getString("storage_location");
        String notes = rs.getString("notes");

        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

        return new Medicine(id, uid, name, cat, batch, qty, purchaseDate, expiryDate, loc, notes, created);
    }
}

