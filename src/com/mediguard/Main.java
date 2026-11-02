package com.mediguard;

import com.mediguard.database.DatabaseConnection;
import com.mediguard.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * Main Application Entry Point.
 * Medicine Expiry Alert System — Java Swing + MySQL
 * Suitable for KTU S3 B.Tech AI/ML OOP Practical Project & Viva Demonstration.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  MEDICINE EXPIRY ALERT SYSTEM - Java Swing + MySQL (JDBC)");
        System.out.println("  KTU S3 B.Tech AI/ML - Object-Oriented Programming (OOP) Project  ");
        System.out.println("==================================================================");

        // Configure modern FlatLaf Look and Feel for crisp, anti-aliased UI
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.selectionBackground", new Color(232, 245, 233));
            UIManager.put("Table.selectionForeground", new Color(33, 33, 33));
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize database connection & ensure schema exists
        System.out.println("[*] Initializing MySQL Database Connection...");
        DatabaseConnection db = DatabaseConnection.getInstance();
        boolean initialized = db.initializeDatabase();
        if (initialized) {
            System.out.println("[SUCCESS] Connected to MySQL database 'medicine_expiry_db'!");
            System.out.println("[SUCCESS] Required tables and demo credentials verified.");
        } else {
            System.err.println("[WARNING] MySQL Connection issue: " + db.getLastError());
            System.err.println("[INFO] Please verify MySQL service is running and password matches in config/db.properties.");
        }

        // Launch Java Swing GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
                System.out.println("[SUCCESS] Desktop GUI window launched successfully.");
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to start desktop interface: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
