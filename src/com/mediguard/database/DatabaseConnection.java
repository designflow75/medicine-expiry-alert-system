package com.mediguard.database;

import com.mediguard.util.PasswordUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Singleton database connection manager handling MySQL JDBC connectivity,
 * configuration loading, automatic table creation, and demo account seeding.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private String host = "localhost";
    private int port = 3306;
    private String database = "medicine_expiry_db";
    private String username = "root";
    private String password = "";

    private boolean driverLoaded = false;
    private String lastError = "";

    private DatabaseConnection() {
        loadConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            lastError = "MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in classpath.";
            System.err.println(lastError);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Loads connection properties from config/db.properties if available.
     */
    private void loadConfig() {
        File configFile = new File("config/db.properties");
        Properties props = new Properties();
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Could not read config/db.properties: " + e.getMessage());
            }
        }

        this.host = props.getProperty("db.host", "localhost").trim();
        try {
            this.port = Integer.parseInt(props.getProperty("db.port", "3306").trim());
        } catch (NumberFormatException e) {
            this.port = 3306;
        }
        this.database = props.getProperty("db.name", "medicine_expiry_db").trim();
        this.username = props.getProperty("db.user", "root").trim();
        this.password = props.getProperty("db.password", "").trim();
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    /**
     * Obtains an active connection to MySQL.
     */
    public Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found in classpath.");
            }
        }
        return DriverManager.getConnection(getJdbcUrl(), username, password);
    }

    /**
     * Tests connectivity to MySQL server.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            lastError = "";
            return rs.next();
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public String getLastError() {
        return lastError;
    }

    /**
     * Automatically initializes tables and seeds the demo account.
     */
    public synchronized boolean initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            String createUsersSql = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INT AUTO_INCREMENT PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    student_id VARCHAR(50),
                    college_name VARCHAR(150),
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_email (email)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.execute(createUsersSql);

            // 2. Medicines Table
            String createMedicinesSql = """
                CREATE TABLE IF NOT EXISTS medicines (
                    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    medicine_name VARCHAR(150) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    batch_number VARCHAR(50) NOT NULL,
                    quantity INT NOT NULL CHECK (quantity >= 0),
                    purchase_date DATE NOT NULL,
                    expiry_date DATE NOT NULL,
                    storage_location VARCHAR(100),
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    INDEX idx_expiry_date (expiry_date),
                    INDEX idx_category (category),
                    INDEX idx_batch (batch_number)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;
            stmt.execute(createMedicinesSql);

            // 3. Ensure Demo Account Exists
            seedDemoAccount(stmt);

            return true;
        } catch (SQLException e) {
            lastError = e.getMessage();
            System.err.println("Database auto-initialization error: " + e.getMessage());
            return false;
        }
    }

    private void seedDemoAccount(Statement stmt) throws SQLException {
        String demoEmail = "demo@medicine.com";
        String demoHashedPassword = PasswordUtil.hashPassword("Demo@123");

        // Insert or update demo user
        String seedUserSql = String.format("""
            INSERT INTO users (user_id, full_name, student_id, college_name, email, password)
            VALUES (1, 'Demo Student', 'KTU-AIML-042', 'KTU College of Engineering', '%s', '%s')
            ON DUPLICATE KEY UPDATE password='%s', full_name='Demo Student';
            """, demoEmail, demoHashedPassword, demoHashedPassword);
        stmt.execute(seedUserSql);

        // Check if demo user has medicines
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM medicines WHERE user_id = 1")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Seed sample medicines for demo
                String seedMedicinesSql = """
                    INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes) VALUES
                    (1, 'Amoxicillin 500mg', 'Antibiotics', 'AMX-2024-09', 12, DATE_SUB(CURRENT_DATE, INTERVAL 180 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), 'Medicine Cabinet A', 'Prescription antibiotic. Expired past shelf-life.'),
                    (1, 'Cough Syrup Dextromethorphan', 'Syrups', 'DXM-2024-11', 1, DATE_SUB(CURRENT_DATE, INTERVAL 90 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), 'First Aid Box', 'Bottle opened 3 months ago. Do not use.'),
                    (1, 'Hydrocortisone Cream 1%', 'Ointments', 'HYD-TODAY-01', 2, DATE_SUB(CURRENT_DATE, INTERVAL 60 DAY), CURRENT_DATE, 'Top Drawer', 'Expires today! Use or discard immediately.'),
                    (1, 'Insulin Glargine 100 IU/ml', 'Injections', 'INS-7889-C', 3, DATE_SUB(CURRENT_DATE, INTERVAL 45 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Refrigerator (2-8 C)', 'Keep refrigerated. Expiring in 3 days.'),
                    (1, 'Salbutamol Inhaler 100mcg', 'Respiratory', 'SAL-2025-04', 1, DATE_SUB(CURRENT_DATE, INTERVAL 60 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Emergency Bag', 'Rescue inhaler. Reorder soon.'),
                    (1, 'Cetirizine 10mg', 'Antihistamines', 'CET-8812-E', 20, DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'Medicine Cabinet A', 'Allergy relief tablets.'),
                    (1, 'Ibuprofen 400mg', 'Pain Relief', 'IBU-9921-X', 15, DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 25 DAY), 'Medicine Cabinet B', 'Anti-inflammatory tablets.'),
                    (1, 'Paracetamol 650mg', 'Analgesics', 'PCM-650-SAFE', 30, DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 180 DAY), 'Medicine Cabinet A', 'Common fever and pain relief.'),
                    (1, 'Vitamin C 500mg Chewable', 'Supplements', 'VTC-500-SAFE', 50, DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 365 DAY), 'Kitchen Shelf', 'Immunity booster vitamins.'),
                    (1, 'Metformin 500mg', 'Diabetes', 'MET-500-SAFE', 60, DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 240 DAY), 'Bedroom Drawer', 'Daily blood sugar control medication.');
                    """;
                stmt.execute(seedMedicinesSql);
            }
        }
    }
}

