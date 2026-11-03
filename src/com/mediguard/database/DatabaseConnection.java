package com.mediguard.database;

import com.mediguard.util.PasswordUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Properties;

/**
 * Singleton database connection manager supporting both Supabase (Cloud PostgreSQL)
 * and local MySQL databases via JDBC.
 *
 * Provides:
 * - Dynamic JDBC driver loading (PostgreSQL or MySQL)
 * - Transparent connection string parsing and SSL configuration
 * - Automatic schema creation (tables and indexes) tailored for both PostgreSQL & MySQL
 * - Automatic demo user and sample medicines seeding
 * - Connection diagnostic and test utilities
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private String dbType = "supabase"; // "supabase", "postgresql", or "mysql"
    private String customUrl = "";
    private String host = "aws-0-ap-southeast-1.pooler.supabase.com";
    private int port = 5432;
    private String database = "postgres";
    private String username = "postgres.ahfygicztfdszpxtohsg";
    private String password = "";
    private boolean ssl = true;

    private boolean driverLoaded = false;
    private String lastError = "";
    private String activeDatabaseProductName = "Unknown";

    private DatabaseConnection() {
        loadConfig();
        ensureDriverLoaded();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Loads connection properties from config/db.properties.
     */
    public void loadConfig() {
        File configFile = new File("config/db.properties");
        Properties props = new Properties();
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Could not read config/db.properties: " + e.getMessage());
            }
        }

        this.customUrl = props.getProperty("db.url", "").trim();
        this.dbType = props.getProperty("db.type", "auto").trim().toLowerCase();

        // Auto-detect db type if unspecified or auto
        if ("auto".equals(this.dbType) || this.dbType.isEmpty()) {
            if (!customUrl.isEmpty()) {
                if (customUrl.startsWith("jdbc:postgresql:") || customUrl.contains("supabase")) {
                    this.dbType = "supabase";
                } else {
                    this.dbType = "mysql";
                }
            } else {
                String rawHost = props.getProperty("db.host", "").toLowerCase();
                if (rawHost.contains("supabase") || rawHost.contains("postgres")) {
                    this.dbType = "supabase";
                } else {
                    // Default to supabase if host is set to supabase pooler, otherwise fallback to mysql
                    this.dbType = rawHost.contains("localhost") ? "mysql" : "supabase";
                }
            }
        }

        boolean isPostgres = isPostgreSQL();
        String defaultHost = isPostgres ? "aws-0-ap-southeast-1.pooler.supabase.com" : "localhost";
        int defaultPort = isPostgres ? 5432 : 3306;
        String defaultDb = isPostgres ? "postgres" : "medicine_expiry_db";
        String defaultUser = isPostgres ? "postgres.ahfygicztfdszpxtohsg" : "root";

        if (!isPostgres && props.containsKey("db.mysql.host")) {
            this.host = props.getProperty("db.mysql.host", defaultHost).trim();
            try {
                this.port = Integer.parseInt(props.getProperty("db.mysql.port", "3306").trim());
            } catch (NumberFormatException e) {
                this.port = 3306;
            }
            this.database = props.getProperty("db.mysql.name", defaultDb).trim();
            this.username = props.getProperty("db.mysql.user", defaultUser).trim();
            this.password = props.getProperty("db.mysql.password", "").trim();
            this.ssl = false;
        } else {
            this.host = props.getProperty("db.host", defaultHost).trim();
            try {
                this.port = Integer.parseInt(props.getProperty("db.port", String.valueOf(defaultPort)).trim());
            } catch (NumberFormatException e) {
                this.port = defaultPort;
            }
            this.database = props.getProperty("db.name", defaultDb).trim();
            this.username = props.getProperty("db.user", defaultUser).trim();
            this.password = props.getProperty("db.password", "").trim();
            this.ssl = Boolean.parseBoolean(props.getProperty("db.ssl", isPostgres ? "true" : "false").trim());
        }
    }

    public boolean isPostgreSQL() {
        return "supabase".equals(dbType) || "postgres".equals(dbType) || "postgresql".equals(dbType);
    }

    private void ensureDriverLoaded() {
        try {
            if (isPostgreSQL()) {
                Class.forName("org.postgresql.Driver");
                driverLoaded = true;
            } else {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
            }
        } catch (ClassNotFoundException e) {
            driverLoaded = false;
            lastError = (isPostgreSQL() ? "PostgreSQL JDBC Driver" : "MySQL JDBC Driver") + " not found in classpath.";
            System.err.println(lastError);
        }
    }

    public String getJdbcUrl() {
        if (!customUrl.isEmpty()) {
            return customUrl;
        }

        if (isPostgreSQL()) {
            // Supabase / PostgreSQL JDBC URL
            String sslParam = ssl ? "?sslmode=require" : "";
            return "jdbc:postgresql://" + host + ":" + port + "/" + database + sslParam;
        } else {
            // MySQL JDBC URL
            return "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
        }
    }

    /**
     * Obtains an active connection to the configured database.
     */
    public Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            ensureDriverLoaded();
        }

        try {
            Connection conn = DriverManager.getConnection(getJdbcUrl(), username, password);
            this.activeDatabaseProductName = conn.getMetaData().getDatabaseProductName();
            return conn;
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw e;
        }
    }

    /**
     * Tests connectivity to the database.
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

    public String getDatabaseTypeDisplay() {
        if (isPostgreSQL()) {
            return "Supabase (Cloud PostgreSQL)";
        }
        return "MySQL (Local/Server)";
    }

    public String getActiveDatabaseProductName() {
        return activeDatabaseProductName;
    }

    /**
     * Automatically initializes tables, constraints, indexes, and seeds the demo account.
     * Selects appropriate DDL syntax based on PostgreSQL or MySQL.
     */
    public synchronized boolean initializeDatabase() {
        try (Connection conn = getConnection()) {
            boolean isPg = conn.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");

            if (isPg) {
                initPostgreSQLSchema(conn);
            } else {
                initMySQLSchema(conn);
            }

            seedDemoAccount(conn, isPg);
            return true;
        } catch (SQLException e) {
            lastError = e.getMessage();
            System.err.println("Database auto-initialization error: " + e.getMessage());
            return false;
        }
    }

    private void initPostgreSQLSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Users table (PostgreSQL)
            String createUsersSql = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id SERIAL PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    student_id VARCHAR(50),
                    college_name VARCHAR(150),
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
                """;
            stmt.execute(createUsersSql);

            // Users indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);");

            // 2. Medicines table (PostgreSQL)
            String createMedicinesSql = """
                CREATE TABLE IF NOT EXISTS medicines (
                    medicine_id SERIAL PRIMARY KEY,
                    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                    medicine_name VARCHAR(150) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    batch_number VARCHAR(50) NOT NULL,
                    quantity INT NOT NULL CHECK (quantity >= 0),
                    purchase_date DATE NOT NULL,
                    expiry_date DATE NOT NULL,
                    storage_location VARCHAR(100),
                    notes TEXT,
                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                );
                """;
            stmt.execute(createMedicinesSql);

            // Medicines indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_med_expiry_date ON medicines (expiry_date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_med_category ON medicines (category);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_med_batch ON medicines (batch_number);");
        }
    }

    private void initMySQLSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Users Table (MySQL)
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

            // 2. Medicines Table (MySQL)
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
        }
    }

    private void seedDemoAccount(Connection conn, boolean isPg) throws SQLException {
        String demoEmail = "demo@medicine.com";
        String demoHashedPassword = PasswordUtil.hashPassword("Demo@123");
        int demoUserId = 1;

        // Upsert demo user
        if (isPg) {
            String seedUserPg = """
                INSERT INTO users (user_id, full_name, student_id, college_name, email, password)
                VALUES (1, 'Demo Student', 'KTU-AIML-042', 'KTU College of Engineering', ?, ?)
                ON CONFLICT (email) DO UPDATE SET full_name = EXCLUDED.full_name, password = EXCLUDED.password
                RETURNING user_id;
                """;
            try (PreparedStatement ps = conn.prepareStatement(seedUserPg)) {
                ps.setString(1, demoEmail);
                ps.setString(2, demoHashedPassword);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        demoUserId = rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                // If conflict on user_id, fall back to lookup
                try (PreparedStatement psLookup = conn.prepareStatement("SELECT user_id FROM users WHERE LOWER(email) = ?")) {
                    psLookup.setString(1, demoEmail);
                    try (ResultSet rs = psLookup.executeQuery()) {
                        if (rs.next()) demoUserId = rs.getInt(1);
                    }
                }
            }

            // Sync sequence to avoid collision on next registration
            try (Statement s = conn.createStatement()) {
                s.execute("SELECT setval(pg_get_serial_sequence('users', 'user_id'), COALESCE((SELECT MAX(user_id) FROM users), 1));");
            } catch (Exception ignored) {
            }
        } else {
            String seedUserSql = String.format("""
                INSERT INTO users (user_id, full_name, student_id, college_name, email, password)
                VALUES (1, 'Demo Student', 'KTU-AIML-042', 'KTU College of Engineering', '%s', '%s')
                ON DUPLICATE KEY UPDATE password='%s', full_name='Demo Student';
                """, demoEmail, demoHashedPassword, demoHashedPassword);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(seedUserSql);
            }
            demoUserId = 1;
        }

        // Check if demo user has medicines
        try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM medicines WHERE user_id = ?")) {
            checkStmt.setInt(1, demoUserId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertDemoMedicines(conn, demoUserId, isPg);
                }
            }
        }
    }

    private void insertDemoMedicines(Connection conn, int userId, boolean isPg) throws SQLException {
        LocalDate today = LocalDate.now();

        Object[][] demoItems = {
            {"Amoxicillin 500mg", "Antibiotics", "AMX-2024-09", 12, today.minusDays(180), today.minusDays(15), "Medicine Cabinet A", "Prescription antibiotic. Expired past shelf-life."},
            {"Cough Syrup Dextromethorphan", "Syrups", "DXM-2024-11", 1, today.minusDays(90), today.minusDays(3), "First Aid Box", "Bottle opened 3 months ago. Do not use."},
            {"Hydrocortisone Cream 1%", "Ointments", "HYD-TODAY-01", 2, today.minusDays(60), today, "Top Drawer", "Expires today! Use or discard immediately."},
            {"Insulin Glargine 100 IU/ml", "Injections", "INS-7889-C", 3, today.minusDays(45), today.plusDays(3), "Refrigerator (2-8 C)", "Keep refrigerated. Expiring in 3 days."},
            {"Salbutamol Inhaler 100mcg", "Respiratory", "SAL-2025-04", 1, today.minusDays(60), today.plusDays(6), "Emergency Bag", "Rescue inhaler. Reorder soon."},
            {"Cetirizine 10mg", "Antihistamines", "CET-8812-E", 20, today.minusDays(30), today.plusDays(14), "Medicine Cabinet A", "Allergy relief tablets."},
            {"Ibuprofen 400mg", "Pain Relief", "IBU-9921-X", 15, today.minusDays(30), today.plusDays(25), "Medicine Cabinet B", "Anti-inflammatory tablets."},
            {"Paracetamol 650mg", "Analgesics", "PCM-650-SAFE", 30, today.minusDays(15), today.plusDays(180), "Medicine Cabinet A", "Common fever and pain relief."},
            {"Vitamin C 500mg Chewable", "Supplements", "VTC-500-SAFE", 50, today.minusDays(10), today.plusDays(365), "Kitchen Shelf", "Immunity booster vitamins."},
            {"Metformin 500mg", "Diabetes", "MET-500-SAFE", 60, today.minusDays(20), today.plusDays(240), "Bedroom Drawer", "Daily blood sugar control medication."}
        };

        String sql = """
            INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] item : demoItems) {
                ps.setInt(1, userId);
                ps.setString(2, (String) item[0]);
                ps.setString(3, (String) item[1]);
                ps.setString(4, (String) item[2]);
                ps.setInt(5, (Integer) item[3]);
                ps.setDate(6, Date.valueOf((LocalDate) item[4]));
                ps.setDate(7, Date.valueOf((LocalDate) item[5]));
                ps.setString(8, (String) item[6]);
                ps.setString(9, (String) item[7]);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        if (isPg) {
            try (Statement s = conn.createStatement()) {
                s.execute("SELECT setval(pg_get_serial_sequence('medicines', 'medicine_id'), COALESCE((SELECT MAX(medicine_id) FROM medicines), 1));");
            } catch (Exception ignored) {
            }
        }
    }
}
