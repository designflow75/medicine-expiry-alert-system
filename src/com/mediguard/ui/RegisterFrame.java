package com.mediguard.ui;

import com.mediguard.dao.UserDAO;
import com.mediguard.dao.UserDAOImpl;
import com.mediguard.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Student Registration Window.
 * Validates student details and genuinely saves new accounts to MySQL.
 * Clean, modern soft-green aesthetic with zero button shivering.
 * Demonstrates Inheritance (extends BaseFrame).
 */
public class RegisterFrame extends BaseFrame {

    private final UserDAO userDAO = new UserDAOImpl();
    private final JFrame parentLoginFrame;

    private JTextField fullNameField;
    private JTextField studentIdField;
    private JTextField collegeNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterFrame(JFrame parentLoginFrame) {
        super("Medicine Expiry Alert System - Create Student Account");
        this.parentLoginFrame = parentLoginFrame;

        setSize(520, 650);
        setMinimumSize(new Dimension(480, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG_LIGHT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(COLOR_BG_LIGHT);

        JLabel logoLabel = new JLabel("Create Student Account");
        logoLabel.setFont(FONT_TITLE);
        logoLabel.setForeground(COLOR_PRIMARY);
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("KTU S3 B.Tech AI/ML - Medicine Expiry Alert System");
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(COLOR_TEXT_MUTED);
        subLabel.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subLabel);
        headerPanel.add(Box.createVerticalStrut(15));

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card
        JPanel formCard = createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Full Name
        fullNameField = createStyledTextField();
        addFormField(formCard, gbc, row++, "Full Name *", fullNameField);

        // Student ID
        studentIdField = createStyledTextField();
        addFormField(formCard, gbc, row++, "Student ID / Roll No *", studentIdField);

        // College Name
        collegeNameField = createStyledTextField();
        addFormField(formCard, gbc, row++, "College / Institution *", collegeNameField);

        // Email Address
        emailField = createStyledTextField();
        addFormField(formCard, gbc, row++, "Email Address *", emailField);

        // Password
        passwordField = createStyledPasswordField();
        addFormField(formCard, gbc, row++, "Password *", passwordField);

        // Confirm Password
        confirmPasswordField = createStyledPasswordField();
        addFormField(formCard, gbc, row++, "Confirm Password *", confirmPasswordField);

        mainPanel.add(formCard, BorderLayout.CENTER);

        // Bottom Actions Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(COLOR_BG_LIGHT);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JButton registerBtn = createPrimaryButton("Create Account");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> handleRegistration());

        JButton cancelBtn = createSecondaryButton("Back to Sign In");
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cancelBtn.setAlignmentX(CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> returnToLogin());

        bottomPanel.add(registerBtn);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(cancelBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField comp) {
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_BODY_BOLD);
        lbl.setForeground(COLOR_TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridy = (row * 2) + 1;
        comp.setPreferredSize(new Dimension(260, 34));
        panel.add(comp, gbc);
    }

    private void handleRegistration() {
        String fullName = fullNameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String collegeName = collegeNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (fullName.isEmpty() || studentId.isEmpty() || collegeName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showWarning("All fields marked with * are required.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }

        if (password.length() < 4) {
            showWarning("Password must be at least 4 characters long.");
            passwordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Password and Confirm Password do not match!");
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }

        if (userDAO.emailExists(email)) {
            showError("An account with email '" + email + "' already exists.\nPlease sign in or use another email.");
            emailField.requestFocus();
            return;
        }

        User newUser = new User(fullName, studentId, collegeName, email, password);
        boolean success = userDAO.registerUser(newUser);

        if (success) {
            showSuccess("Registration Successful!\nWelcome, " + fullName + ".\nYou can now log in with your credentials.");
            returnToLogin();
        } else {
            showError("Failed to register account. Please check your MySQL database connection.");
        }
    }

    private void returnToLogin() {
        dispose();
        if (parentLoginFrame != null) {
            parentLoginFrame.setVisible(true);
        }
    }
}
