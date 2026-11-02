package com.mediguard.ui;

import com.mediguard.dao.UserDAO;
import com.mediguard.dao.UserDAOImpl;
import com.mediguard.database.DatabaseConnection;
import com.mediguard.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main User Login Interface.
 * Modern light theme with soft green accents.
 * Demo credentials displayed in clear text (as requested).
 * Demonstrates Inheritance (extends BaseFrame).
 */
public class LoginFrame extends BaseFrame {

    private final UserDAO userDAO = new UserDAOImpl();

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel dbStatusLabel;

    public LoginFrame() {
        super("Medicine Expiry Alert System - Sign In");

        setSize(480, 600);
        setMinimumSize(new Dimension(440, 560));
        setLocationRelativeTo(null);

        // Ensure database tables and demo user exist in MySQL
        DatabaseConnection.getInstance().initializeDatabase();

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG_LIGHT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 32, 25, 32));

        // Header Panel with Title
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(COLOR_BG_LIGHT);

        JLabel logoLabel = new JLabel("Medicine Expiry Alert System");
        logoLabel.setFont(FONT_TITLE);
        logoLabel.setForeground(COLOR_PRIMARY);
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Household & Hospital Medicine Expiry Tracker");
        subLabel.setFont(FONT_BODY);
        subLabel.setForeground(COLOR_TEXT_MUTED);
        subLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel collegeTag = new JLabel("KTU S3 B.Tech AI/ML - OOP Practical Project");
        collegeTag.setFont(FONT_SMALL);
        collegeTag.setForeground(COLOR_PRIMARY);
        collegeTag.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLabel);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(collegeTag);
        headerPanel.add(Box.createVerticalStrut(18));

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card Panel
        JPanel formCard = createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Email Field
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel emailLabel = new JLabel("Email / Username");
        emailLabel.setFont(FONT_BODY_BOLD);
        emailLabel.setForeground(COLOR_TEXT_DARK);
        formCard.add(emailLabel, gbc);

        gbc.gridy = row++;
        emailField = createStyledTextField();
        emailField.setPreferredSize(new Dimension(260, 36));
        formCard.add(emailField, gbc);

        // Password Field
        gbc.gridy = row++;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(FONT_BODY_BOLD);
        passLabel.setForeground(COLOR_TEXT_DARK);
        formCard.add(passLabel, gbc);

        gbc.gridy = row++;
        passwordField = createStyledPasswordField();
        passwordField.setPreferredSize(new Dimension(260, 36));
        formCard.add(passwordField, gbc);

        // Sign In Button (Uses ModernButton with crisp forest green and zero shivering)
        gbc.gridy = row++;
        gbc.insets = new Insets(14, 4, 10, 4);
        JButton signInBtn = createPrimaryButton("Sign In");
        signInBtn.setPreferredSize(new Dimension(260, 40));
        signInBtn.addActionListener(e -> handleSignIn());
        formCard.add(signInBtn, gbc);

        // Demo Credentials Text Card (Replaced the button with clean text credentials)
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 4, 4, 4);
        JPanel demoBox = new JPanel();
        demoBox.setLayout(new BoxLayout(demoBox, BoxLayout.Y_AXIS));
        demoBox.setBackground(new Color(241, 248, 233)); // Soft light green card
        demoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER_GREEN, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        demoBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel demoHeader = new JLabel("DEMO ACCOUNT CREDENTIALS");
        demoHeader.setFont(FONT_SMALL_BOLD);
        demoHeader.setForeground(COLOR_PRIMARY);

        JLabel demoEmail = new JLabel("Email: demo@medicine.com");
        demoEmail.setFont(FONT_SMALL);
        demoEmail.setForeground(COLOR_TEXT_DARK);

        JLabel demoPwd = new JLabel("Password: Demo@123");
        demoPwd.setFont(FONT_SMALL);
        demoPwd.setForeground(COLOR_TEXT_DARK);

        JLabel demoAutofill = new JLabel("(Click here to autofill credentials)");
        demoAutofill.setFont(FONT_SMALL);
        demoAutofill.setForeground(COLOR_PRIMARY);

        demoBox.add(demoHeader);
        demoBox.add(Box.createVerticalStrut(3));
        demoBox.add(demoEmail);
        demoBox.add(demoPwd);
        demoBox.add(Box.createVerticalStrut(4));
        demoBox.add(demoAutofill);

        // Clicking the demo box automatically fills in the demo credentials
        demoBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                emailField.setText("demo@medicine.com");
                passwordField.setText("Demo@123");
            }
        });

        formCard.add(demoBox, gbc);

        mainPanel.add(formCard, BorderLayout.CENTER);

        // Bottom Navigation (Create Account link & DB Status)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(COLOR_BG_LIGHT);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerRow.setBackground(COLOR_BG_LIGHT);
        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(FONT_BODY);
        noAccountLabel.setForeground(COLOR_TEXT_DARK);

        JButton createAccountBtn = new JButton("Create New Account");
        createAccountBtn.setFont(FONT_BODY_BOLD);
        createAccountBtn.setForeground(COLOR_PRIMARY);
        createAccountBtn.setContentAreaFilled(false);
        createAccountBtn.setBorderPainted(false);
        createAccountBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createAccountBtn.addActionListener(e -> openRegisterWindow());

        registerRow.add(noAccountLabel);
        registerRow.add(createAccountBtn);

        dbStatusLabel = new JLabel("Database: Connected to MySQL (medicine_expiry_db)", SwingConstants.CENTER);
        dbStatusLabel.setFont(FONT_SMALL);
        dbStatusLabel.setForeground(COLOR_PRIMARY);
        dbStatusLabel.setAlignmentX(CENTER_ALIGNMENT);

        bottomPanel.add(registerRow);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(dbStatusLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Verify live connection
        if (!DatabaseConnection.getInstance().testConnection()) {
            dbStatusLabel.setText("Warning: MySQL Not Connected. Check config/db.properties");
            dbStatusLabel.setForeground(COLOR_DANGER);
        }
    }

    private void handleSignIn() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty()) {
            showWarning("Please enter your email address.");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showWarning("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        User authenticatedUser = userDAO.authenticate(email, password);
        if (authenticatedUser != null) {
            openDashboard(authenticatedUser);
        } else {
            showError("Invalid email or password.\nPlease check your credentials or create a new account.");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void openRegisterWindow() {
        RegisterFrame regFrame = new RegisterFrame(this);
        regFrame.setVisible(true);
        setVisible(false);
    }

    private void openDashboard(User user) {
        dispose();
        DashboardFrame dashboard = new DashboardFrame(user);
        dashboard.setVisible(true);
    }
}
