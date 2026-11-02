package com.mediguard.dao;

import com.mediguard.database.DatabaseConnection;
import com.mediguard.model.User;
import com.mediguard.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Concrete implementation of UserDAO using JDBC and MySQL PreparedStatements.
 * Demonstrates Polymorphism and Method Overriding.
 */
public class UserDAOImpl implements UserDAO {

    private final DatabaseConnection dbConnection = DatabaseConnection.getInstance();

    @Override
    public boolean registerUser(User user) {
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            return false;
        }

        String sql = "INSERT INTO users (full_name, student_id, college_name, email, password, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getStudentId());
            ps.setString(3, user.getCollegeName());
            ps.setString(4, user.getEmail().trim().toLowerCase());

            // Hash password before saving to database
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            ps.setString(5, hashedPassword);

            LocalDateTime created = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(6, Timestamp.valueOf(created));

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
        return false;
    }

    @Override
    public User authenticate(String email, String plainPassword) {
        if (email == null || plainPassword == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT user_id, full_name, student_id, college_name, email, password, created_at FROM users WHERE LOWER(email) = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                        return mapRowToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE LOWER(email) = ? LIMIT 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT user_id, full_name, student_id, college_name, email, password, created_at FROM users WHERE LOWER(email) = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findById(int userId) {
        String sql = "SELECT user_id, full_name, student_id, college_name, email, password, created_at FROM users WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by id: " + e.getMessage());
        }
        return null;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String name = rs.getString("full_name");
        String studentId = rs.getString("student_id");
        String college = rs.getString("college_name");
        String mail = rs.getString("email");
        String pwd = rs.getString("password");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        return new User(id, name, studentId, college, mail, pwd, created);
    }
}

