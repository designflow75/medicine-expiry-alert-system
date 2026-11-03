package com.mediguard.ui;

import com.mediguard.dao.MedicineDAO;
import com.mediguard.dao.MedicineDAOImpl;
import com.mediguard.model.ExpiryStatus;
import com.mediguard.model.Medicine;
import com.mediguard.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Application Dashboard Frame for logged-in students.
 * Manages the Sidebar Navigation, Dashboard, Add Medicine, My Medicines,
 * Expiry Alerts, Medicine Search, Reports, and Logout views via CardLayout.
 * Free of unicode emoji box glitches, with anti-aliased non-shivering buttons.
 * Demonstrates Inheritance (extends BaseFrame) and OOP principles.
 */
public class DashboardFrame extends BaseFrame {

    private final User currentUser;
    private final MedicineDAO medicineDAO = new MedicineDAOImpl();

    // Navigation & Layout
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private final Map<String, ModernButton> sidebarButtons = new HashMap<>();

    // Dashboard KPI Cards
    private JLabel totalCountLabel;
    private JLabel safeCountLabel;
    private JLabel expiringCountLabel;
    private JLabel expiredCountLabel;
    private JPanel bannerWarningPanel;
    private JLabel bannerWarningLabel;
    private JTable dashOverviewTable;
    private DefaultTableModel dashOverviewTableModel;

    // My Medicines View Components
    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;
    private List<Medicine> currentInventoryList = new ArrayList<>();

    // Add Medicine Form Components
    private JTextField addNameField;
    private JComboBox<String> addCategoryCombo;
    private JTextField addBatchField;
    private JSpinner addQuantitySpinner;
    private JTextField addPurchaseDateField;
    private JTextField addExpiryDateField;
    private JTextField addLocationField;
    private JTextArea addNotesArea;

    // Search View Components
    private JTextField searchInputField;
    private JComboBox<String> searchCategoryFilter;
    private JComboBox<String> searchStatusFilter;
    private JTable searchTable;
    private DefaultTableModel searchTableModel;
    private List<Medicine> searchResultList = new ArrayList<>();

    // Expiry Alerts View Components
    private DefaultTableModel expiredAlertModel;
    private DefaultTableModel todayAlertModel;
    private DefaultTableModel criticalAlertModel;
    private DefaultTableModel soonAlertModel;
    private JTable expiredAlertTable;
    private JTable todayAlertTable;
    private JTable criticalAlertTable;
    private JTable soonAlertTable;

    // Reports View Components
    private JLabel rptTotalLabel;
    private JLabel rptSafeLabel;
    private JLabel rptSoonLabel;
    private JLabel rptCriticalLabel;
    private JLabel rptTodayLabel;
    private JLabel rptExpiredLabel;
    private JComboBox<String> rptStatusFilterCombo;
    private JTable rptTable;
    private DefaultTableModel rptTableModel;

    public DashboardFrame(User user) {
        super("Medicine Expiry Alert System - " + user.getFullName());
        this.currentUser = user;

        setSize(1180, 780);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        buildLayout();

        // Refresh all view data from MySQL
        refreshDashboardData();

        // Show welcome popup reminder if urgent medicines exist
        checkAndShowInitialAlerts();
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG_LIGHT);

        // 1. Top Header Bar
        root.add(createTopBar(), BorderLayout.NORTH);

        // 2. Left Sidebar Navigation
        root.add(createSidebar(), BorderLayout.WEST);

        // 3. Center Multi-View Content Panel using CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_BG_LIGHT);

        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createAddMedicinePanel(), "ADD_MEDICINE");
        contentPanel.add(createMyMedicinesPanel(), "MY_MEDICINES");
        contentPanel.add(createExpiryAlertsPanel(), "EXPIRY_ALERTS");
        contentPanel.add(createMedicineSearchPanel(), "MEDICINE_SEARCH");
        contentPanel.add(createReportsPanel(), "REPORTS");

        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);
        selectNav("DASHBOARD");
    }

    // ==========================================
    // 1. TOP HEADER BAR
    // ==========================================
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Brand & System title
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel appTitle = new JLabel("Medicine Expiry Alert System");
        appTitle.setFont(FONT_HEADER);
        appTitle.setForeground(COLOR_PRIMARY);

        JLabel subTitle = new JLabel("KTU S3 B.Tech AI/ML OOP Practical Project");
        subTitle.setFont(FONT_SMALL);
        subTitle.setForeground(COLOR_TEXT_MUTED);

        titlePanel.add(appTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subTitle);
        topBar.add(titlePanel, BorderLayout.WEST);

        // User Greeting & Date Info
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightInfo.setBackground(Color.WHITE);

        JLabel dateLabel = new JLabel("Date: " + LocalDate.now());
        dateLabel.setFont(FONT_BODY_BOLD);
        dateLabel.setForeground(COLOR_TEXT_MUTED);

        String greeting = "Welcome, " + currentUser.getFullName();
        if (currentUser.getCollegeName() != null && !currentUser.getCollegeName().isBlank()) {
            greeting += " (" + currentUser.getCollegeName() + ")";
        }
        JLabel userLabel = new JLabel(greeting);
        userLabel.setFont(FONT_BODY_BOLD);
        userLabel.setForeground(COLOR_PRIMARY);

        rightInfo.add(dateLabel);
        rightInfo.add(userLabel);
        topBar.add(rightInfo, BorderLayout.EAST);

        return topBar;
    }

    // ==========================================
    // 2. SIDEBAR NAVIGATION
    // ==========================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 86, 49)); // Professional deep forest green
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));

        JLabel menuHeading = new JLabel("  NAVIGATION MENU");
        menuHeading.setFont(FONT_SMALL_BOLD);
        menuHeading.setForeground(new Color(200, 230, 201)); // Soft light green
        sidebar.add(menuHeading);
        sidebar.add(Box.createVerticalStrut(12));

        addNavButton(sidebar, "Dashboard", "DASHBOARD");
        addNavButton(sidebar, "Add Medicine", "ADD_MEDICINE");
        addNavButton(sidebar, "My Medicines", "MY_MEDICINES");
        addNavButton(sidebar, "Expiry Alerts", "EXPIRY_ALERTS");
        addNavButton(sidebar, "Medicine Search", "MEDICINE_SEARCH");
        addNavButton(sidebar, "Reports", "REPORTS");

        sidebar.add(Box.createVerticalGlue());

        // Logout Button using ModernButton (guaranteed red background, zero shivering)
        ModernButton logoutBtn = new ModernButton("Logout", new Color(183, 28, 28), new Color(142, 20, 20), Color.WHITE, null);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.addActionListener(e -> handleLogout());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String text, String cardName) {
        ModernButton btn = new ModernButton(text, new Color(46, 125, 50), new Color(27, 94, 32), Color.WHITE, null);
        btn.setAlignLeft(true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.addActionListener(e -> selectNav(cardName));

        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(5));
        sidebarButtons.put(cardName, btn);
    }

    private void selectNav(String cardName) {
        cardLayout.show(contentPanel, cardName);

        for (Map.Entry<String, ModernButton> entry : sidebarButtons.entrySet()) {
            boolean isSelected = entry.getKey().equals(cardName);
            entry.getValue().setSelectedState(isSelected);
        }

        // Trigger view-specific reloads
        switch (cardName) {
            case "DASHBOARD" -> refreshDashboardData();
            case "MY_MEDICINES" -> reloadInventoryTable();
            case "EXPIRY_ALERTS" -> reloadExpiryAlerts();
            case "MEDICINE_SEARCH" -> executeSearch();
            case "REPORTS" -> reloadReports();
        }
    }

    // ==========================================
    // 3. DASHBOARD PANEL
    // ==========================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Welcome & Banner section
        JPanel headerArea = new JPanel();
        headerArea.setLayout(new BoxLayout(headerArea, BoxLayout.Y_AXIS));
        headerArea.setBackground(COLOR_BG_LIGHT);

        JLabel welcomeTitle = new JLabel("Welcome back, " + currentUser.getFullName() + "!");
        welcomeTitle.setFont(FONT_TITLE);
        welcomeTitle.setForeground(COLOR_TEXT_DARK);

        JLabel welcomeSub = new JLabel("Real-time cabinet safety metrics and expiry status monitoring.");
        welcomeSub.setFont(FONT_BODY);
        welcomeSub.setForeground(COLOR_TEXT_MUTED);

        headerArea.add(welcomeTitle);
        headerArea.add(Box.createVerticalStrut(4));
        headerArea.add(welcomeSub);
        headerArea.add(Box.createVerticalStrut(10));

        // Warning banner (only shown if expired / critical items exist)
        bannerWarningPanel = new JPanel(new BorderLayout(10, 0));
        bannerWarningPanel.setBackground(new Color(255, 235, 238));
        bannerWarningPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_DANGER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        bannerWarningLabel = new JLabel("WARNING: Expired or Critical medications detected in your cabinet!");
        bannerWarningLabel.setFont(FONT_BODY_BOLD);
        bannerWarningLabel.setForeground(COLOR_DANGER);

        JButton reviewAlertsBtn = createDangerButton("Review Expiry Alerts");
        reviewAlertsBtn.addActionListener(e -> selectNav("EXPIRY_ALERTS"));

        bannerWarningPanel.add(bannerWarningLabel, BorderLayout.CENTER);
        bannerWarningPanel.add(reviewAlertsBtn, BorderLayout.EAST);
        bannerWarningPanel.setVisible(false);

        headerArea.add(bannerWarningPanel);
        panel.add(headerArea, BorderLayout.NORTH);

        // 4 KPI Summary Cards using GridBagLayout
        JPanel kpiContainer = new JPanel(new GridBagLayout());
        kpiContainer.setBackground(COLOR_BG_LIGHT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        totalCountLabel = new JLabel("0", SwingConstants.CENTER);
        safeCountLabel = new JLabel("0", SwingConstants.CENTER);
        expiringCountLabel = new JLabel("0", SwingConstants.CENTER);
        expiredCountLabel = new JLabel("0", SwingConstants.CENTER);

        gbc.gridx = 0;
        kpiContainer.add(createKpiCard("TOTAL MEDICINES", totalCountLabel, "In Cabinet Inventory", COLOR_PRIMARY, COLOR_ACCENT_LIGHT), gbc);

        gbc.gridx = 1;
        kpiContainer.add(createKpiCard("SAFE MEDICINES", safeCountLabel, "> 30 Days Remaining", new Color(27, 94, 32), new Color(232, 245, 233)), gbc);

        gbc.gridx = 2;
        kpiContainer.add(createKpiCard("EXPIRING SOON", expiringCountLabel, "8-30 Days Remaining", new Color(230, 81, 0), new Color(255, 243, 224)), gbc);

        gbc.gridx = 3;
        kpiContainer.add(createKpiCard("EXPIRED MEDICINES", expiredCountLabel, "Do Not Consume!", new Color(183, 28, 28), new Color(255, 235, 238)), gbc);

        // Center Quick Action / Overview section
        JPanel centerArea = createCardPanel();
        centerArea.setLayout(new BorderLayout(0, 10));

        JPanel quickHeader = new JPanel(new BorderLayout());
        quickHeader.setBackground(Color.WHITE);
        JLabel quickTitle = new JLabel("Cabinet Medicines Overview");
        quickTitle.setFont(FONT_HEADER);
        quickTitle.setForeground(COLOR_PRIMARY);

        JPanel quickBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickBtns.setBackground(Color.WHITE);

        JButton addMedBtn = createPrimaryButton("[+] Add Medicine");
        addMedBtn.setPreferredSize(new Dimension(140, 34));
        addMedBtn.addActionListener(e -> selectNav("ADD_MEDICINE"));

        JButton viewAllBtn = createSecondaryButton("View Full Inventory");
        viewAllBtn.setPreferredSize(new Dimension(150, 34));
        viewAllBtn.addActionListener(e -> selectNav("MY_MEDICINES"));

        JButton reportsBtn = createSecondaryButton("Open Reports");
        reportsBtn.setPreferredSize(new Dimension(110, 34));
        reportsBtn.addActionListener(e -> selectNav("REPORTS"));

        quickBtns.add(addMedBtn);
        quickBtns.add(viewAllBtn);
        quickBtns.add(reportsBtn);

        quickHeader.add(quickTitle, BorderLayout.WEST);
        quickHeader.add(quickBtns, BorderLayout.EAST);
        centerArea.add(quickHeader, BorderLayout.NORTH);

        // Dashboard Live Overview Table
        String[] dashCols = {"Medicine Name", "Category", "Batch No", "Quantity", "Expiry Date", "Days Remaining", "Status"};
        dashOverviewTableModel = new DefaultTableModel(dashCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        dashOverviewTable = new JTable(dashOverviewTableModel);
        dashOverviewTable.setFont(FONT_BODY);
        dashOverviewTable.setRowHeight(32);
        dashOverviewTable.setShowVerticalLines(false);
        dashOverviewTable.getTableHeader().setFont(FONT_BODY_BOLD);
        dashOverviewTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane dashScroll = new JScrollPane(dashOverviewTable);
        dashScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        centerArea.add(dashScroll, BorderLayout.CENTER);

        JPanel centerWrap = new JPanel(new BorderLayout(0, 12));
        centerWrap.setBackground(COLOR_BG_LIGHT);
        centerWrap.add(kpiContainer, BorderLayout.NORTH);
        centerWrap.add(centerArea, BorderLayout.CENTER);

        panel.add(centerWrap, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, String subtitle, Color textColor, Color bgColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setPreferredSize(new Dimension(200, 105));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(FONT_SMALL_BOLD);
        tLbl.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(FONT_METRIC);
        valueLabel.setForeground(textColor);

        JLabel sLbl = new JLabel(subtitle);
        sLbl.setFont(FONT_SMALL);
        sLbl.setForeground(COLOR_TEXT_MUTED);

        card.add(tLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(sLbl);

        return card;
    }

    // ==========================================
    // 4. ADD MEDICINE PANEL
    // ==========================================
    private JPanel createAddMedicinePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Register New Medicine");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY);

        JLabel sub = new JLabel("Add medicine records to the database. Expiry status is calculated automatically.");
        sub.setFont(FONT_BODY);
        sub.setForeground(COLOR_TEXT_MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(COLOR_BG_LIGHT);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(sub);
        panel.add(titleBox, BorderLayout.NORTH);

        // Form Card Panel
        JPanel formCard = createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Medicine Name
        addNameField = createStyledTextField();
        addFormFieldRow(formCard, gbc, row++, "Medicine Name *", addNameField);

        // Category
        String[] categories = {
                "Tablets", "Capsules", "Syrups", "Injections", "Ointments",
                "Drops", "Antibiotics", "Pain Relief", "Analgesics", "Antihistamines",
                "Respiratory", "Diabetes", "Supplements", "First Aid", "General"
        };
        addCategoryCombo = new JComboBox<>(categories);
        addCategoryCombo.setEditable(true);
        addCategoryCombo.setFont(FONT_BODY);
        addCategoryCombo.setPreferredSize(new Dimension(280, 34));
        addFormFieldRow(formCard, gbc, row++, "Medicine Category *", addCategoryCombo);

        // Batch Number
        addBatchField = createStyledTextField();
        addFormFieldRow(formCard, gbc, row++, "Batch Number *", addBatchField);

        // Quantity
        addQuantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        addQuantitySpinner.setFont(FONT_BODY);
        addQuantitySpinner.setPreferredSize(new Dimension(280, 34));
        addFormFieldRow(formCard, gbc, row++, "Quantity (Units) *", addQuantitySpinner);

        // Purchase Date
        JPanel pDatePanel = new JPanel(new BorderLayout(6, 0));
        pDatePanel.setBackground(Color.WHITE);
        addPurchaseDateField = createStyledTextField();
        addPurchaseDateField.setText(LocalDate.now().toString());
        JButton pTodayBtn = createSecondaryButton("Today");
        pTodayBtn.setPreferredSize(new Dimension(75, 34));
        pTodayBtn.addActionListener(e -> addPurchaseDateField.setText(LocalDate.now().toString()));
        pDatePanel.add(addPurchaseDateField, BorderLayout.CENTER);
        pDatePanel.add(pTodayBtn, BorderLayout.EAST);
        addFormFieldRow(formCard, gbc, row++, "Purchase Date (YYYY-MM-DD) *", pDatePanel);

        // Expiry Date with quick buttons (+1m, +3m, +6m, +1y)
        JPanel eDatePanel = new JPanel(new BorderLayout(6, 0));
        eDatePanel.setBackground(Color.WHITE);
        addExpiryDateField = createStyledTextField();
        addExpiryDateField.setText(LocalDate.now().plusMonths(6).toString());

        JPanel quickAddBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        quickAddBtns.setBackground(Color.WHITE);
        addQuickMonthBtn(quickAddBtns, "+1 Mo", 1);
        addQuickMonthBtn(quickAddBtns, "+3 Mo", 3);
        addQuickMonthBtn(quickAddBtns, "+6 Mo", 6);
        addQuickMonthBtn(quickAddBtns, "+1 Yr", 12);

        eDatePanel.add(addExpiryDateField, BorderLayout.CENTER);
        eDatePanel.add(quickAddBtns, BorderLayout.EAST);
        addFormFieldRow(formCard, gbc, row++, "Expiry Date (YYYY-MM-DD) *", eDatePanel);

        // Storage Location
        addLocationField = createStyledTextField();
        addLocationField.setText("Medicine Cabinet A");
        addFormFieldRow(formCard, gbc, row++, "Storage Location", addLocationField);

        // Notes
        addNotesArea = new JTextArea(3, 20);
        addNotesArea.setFont(FONT_BODY);
        addNotesArea.setLineWrap(true);
        addNotesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(addNotesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));

        gbc.gridx = 0;
        gbc.gridy = row * 2;
        JLabel notesLbl = new JLabel("Notes / Usage Instructions");
        notesLbl.setFont(FONT_BODY_BOLD);
        notesLbl.setForeground(COLOR_TEXT_DARK);
        formCard.add(notesLbl, gbc);

        gbc.gridy = (row * 2) + 1;
        formCard.add(notesScroll, gbc);

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(formScroll, BorderLayout.CENTER);

        // Form Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setBackground(COLOR_BG_LIGHT);

        JButton clearBtn = createSecondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearAddMedicineForm());

        JButton submitBtn = createPrimaryButton("Save Medicine Record");
        submitBtn.setPreferredSize(new Dimension(190, 38));
        submitBtn.addActionListener(e -> handleAddMedicine());

        actionRow.add(clearBtn);
        actionRow.add(submitBtn);
        panel.add(actionRow, BorderLayout.SOUTH);

        return panel;
    }

    private void addQuickMonthBtn(JPanel parent, String label, int months) {
        JButton btn = createSecondaryButton(label);
        btn.setPreferredSize(new Dimension(65, 34));
        btn.setFont(FONT_SMALL_BOLD);
        btn.addActionListener(e -> {
            LocalDate d = LocalDate.now().plusMonths(months);
            addExpiryDateField.setText(d.toString());
        });
        parent.add(btn);
    }

    private void addFormFieldRow(JPanel panel, GridBagConstraints gbc, int row, String label, javax.swing.JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BODY_BOLD);
        lbl.setForeground(COLOR_TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridy = (row * 2) + 1;
        comp.setPreferredSize(new Dimension(300, 34));
        panel.add(comp, gbc);
    }

    private void handleAddMedicine() {
        String name = addNameField.getText().trim();
        String cat = addCategoryCombo.getSelectedItem() != null ? addCategoryCombo.getSelectedItem().toString().trim() : "General";
        String batch = addBatchField.getText().trim();
        int qty = (Integer) addQuantitySpinner.getValue();
        String pDateStr = addPurchaseDateField.getText().trim();
        String eDateStr = addExpiryDateField.getText().trim();
        String loc = addLocationField.getText().trim();
        String notes = addNotesArea.getText().trim();

        if (name.isEmpty() || batch.isEmpty() || pDateStr.isEmpty() || eDateStr.isEmpty()) {
            showWarning("Please fill in all required fields (*).");
            return;
        }

        LocalDate pDate;
        LocalDate eDate;
        try {
            pDate = LocalDate.parse(pDateStr);
            eDate = LocalDate.parse(eDateStr);
        } catch (DateTimeParseException ex) {
            showError("Invalid date format. Please use YYYY-MM-DD (e.g. 2026-11-01).");
            return;
        }

        // Strict Requirement: Expiry date must not be before purchase date
        if (eDate.isBefore(pDate)) {
            showError("Expiry date cannot be before purchase date!");
            return;
        }

        // Create Medicine domain object
        Medicine newMed = new Medicine(
                currentUser.getUserId(),
                name,
                cat,
                batch,
                qty,
                pDate,
                eDate,
                loc,
                notes
        );

        boolean success = medicineDAO.addMedicine(newMed);
        if (success) {
            showSuccess("Medicine '" + name + "' added successfully!\nCalculated Expiry Status: " + newMed.getStatus().getDisplayName());
            clearAddMedicineForm();
            refreshDashboardData();
            selectNav("MY_MEDICINES");
        } else {
            showError("Failed to save medicine record to database.");
        }
    }

    private void clearAddMedicineForm() {
        addNameField.setText("");
        addCategoryCombo.setSelectedIndex(0);
        addBatchField.setText("");
        addQuantitySpinner.setValue(1);
        addPurchaseDateField.setText(LocalDate.now().toString());
        addExpiryDateField.setText(LocalDate.now().plusMonths(6).toString());
        addLocationField.setText("Medicine Cabinet A");
        addNotesArea.setText("");
    }

    // ==========================================
    // 5. MY MEDICINES (INVENTORY TABLE)
    // ==========================================
    private JPanel createMyMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Header with Refresh & Actions
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG_LIGHT);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(COLOR_BG_LIGHT);

        JLabel title = new JLabel("My Medicines Inventory");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY);

        JLabel sub = new JLabel("Complete inventory of medicines saved in database for " + currentUser.getFullName());
        sub.setFont(FONT_BODY);
        sub.setForeground(COLOR_TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(sub);
        header.add(titleBox, BorderLayout.WEST);

        // Action Toolbar (Buttons with crisp contrast and anti-aliasing)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        toolbar.setBackground(COLOR_BG_LIGHT);

        JButton addBtn = createPrimaryButton("[+] Add Medicine");
        addBtn.setPreferredSize(new Dimension(140, 36));
        addBtn.addActionListener(e -> selectNav("ADD_MEDICINE"));

        JButton editBtn = createSecondaryButton("Edit");
        editBtn.setPreferredSize(new Dimension(85, 36));
        editBtn.addActionListener(e -> handleEditSelectedMedicine());

        JButton delBtn = createDangerButton("Delete");
        delBtn.setPreferredSize(new Dimension(90, 36));
        delBtn.addActionListener(e -> handleDeleteSelectedMedicine());

        JButton viewBtn = createSecondaryButton("View Details");
        viewBtn.setPreferredSize(new Dimension(110, 36));
        viewBtn.addActionListener(e -> handleViewSelectedMedicine());

        JButton refreshBtn = createSecondaryButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(90, 36));
        refreshBtn.addActionListener(e -> reloadInventoryTable());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(delBtn);
        toolbar.add(viewBtn);
        toolbar.add(refreshBtn);
        header.add(toolbar, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // JTable Table Model
        String[] columns = {
                "ID", "Medicine Name", "Category", "Batch No",
                "Qty", "Purchase Date", "Expiry Date", "Expiry Status", "Storage Location"
        };
        inventoryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.setFont(FONT_BODY);
        inventoryTable.setRowHeight(34);
        inventoryTable.setShowVerticalLines(false);
        inventoryTable.setGridColor(COLOR_BORDER);
        inventoryTable.getTableHeader().setFont(FONT_BODY_BOLD);
        inventoryTable.getTableHeader().setBackground(new Color(241, 245, 249));
        inventoryTable.getTableHeader().setForeground(COLOR_TEXT_DARK);
        inventoryTable.getTableHeader().setPreferredSize(new Dimension(0, 34));

        // Column widths
        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(45);  // ID
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Name
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(110); // Category
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(110); // Batch
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(50);  // Qty
        inventoryTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Purchase
        inventoryTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Expiry
        inventoryTable.getColumnModel().getColumn(7).setPreferredWidth(130); // Status
        inventoryTable.getColumnModel().getColumn(8).setPreferredWidth(140); // Storage

        // Status Badge Pill Renderer
        inventoryTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        // Double-click to view details
        inventoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && inventoryTable.getSelectedRow() != -1) {
                    handleViewSelectedMedicine();
                }
            }
        });

        // Context Menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem viewItem = new JMenuItem("View Details");
        viewItem.addActionListener(e -> handleViewSelectedMedicine());
        JMenuItem editItem = new JMenuItem("Edit Medicine");
        editItem.addActionListener(e -> handleEditSelectedMedicine());
        JMenuItem delItem = new JMenuItem("Delete Record");
        delItem.addActionListener(e -> handleDeleteSelectedMedicine());

        contextMenu.add(viewItem);
        contextMenu.add(editItem);
        contextMenu.addSeparator();
        contextMenu.add(delItem);
        inventoryTable.setComponentPopupMenu(contextMenu);

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void reloadInventoryTable() {
        currentInventoryList = medicineDAO.getMedicinesByUser(currentUser.getUserId());
        inventoryTableModel.setRowCount(0);

        for (Medicine m : currentInventoryList) {
            inventoryTableModel.addRow(new Object[]{
                    m.getMedicineId(),
                    m.getMedicineName(),
                    m.getCategory(),
                    m.getBatchNumber(),
                    m.getQuantity(),
                    m.getPurchaseDate().toString(),
                    m.getExpiryDate().toString(),
                    m.getStatus(),
                    m.getStorageLocation() != null ? m.getStorageLocation() : ""
            });
        }
    }

    private Medicine getSelectedMedicineFromTable(JTable table, List<Medicine> list) {
        int row = table.getSelectedRow();
        if (row != -1 && row < list.size()) {
            return list.get(row);
        }
        return null;
    }

    private void handleEditSelectedMedicine() {
        Medicine med = getSelectedMedicineFromTable(inventoryTable, currentInventoryList);
        if (med == null) {
            showWarning("Please select a medicine from the table to edit.");
            return;
        }

        EditMedicineDialog dialog = new EditMedicineDialog(this, med);
        dialog.setVisible(true);

        if (dialog.isUpdated()) {
            reloadInventoryTable();
            refreshDashboardData();
        }
    }

    private void handleDeleteSelectedMedicine() {
        Medicine med = getSelectedMedicineFromTable(inventoryTable, currentInventoryList);
        if (med == null) {
            showWarning("Please select a medicine from the table to delete.");
            return;
        }

        // Strict Requirement: Ask for confirmation before deleting
        boolean confirm = showConfirm(
                "Are you sure you want to permanently delete '" + med.getMedicineName() + "' (Batch: " + med.getBatchNumber() + ")?",
                "Confirm Deletion"
        );

        if (confirm) {
            boolean success = medicineDAO.deleteMedicine(med.getMedicineId(), currentUser.getUserId());
            if (success) {
                showSuccess("Medicine deleted successfully!");
                reloadInventoryTable();
                refreshDashboardData();
            } else {
                showError("Failed to delete record from database.");
            }
        }
    }

    private void handleViewSelectedMedicine() {
        Medicine med = getSelectedMedicineFromTable(inventoryTable, currentInventoryList);
        if (med == null) {
            showWarning("Please select a medicine to view details.");
            return;
        }

        ViewMedicineDialog dialog = new ViewMedicineDialog(this, med);
        dialog.setVisible(true);
    }

    // ==========================================
    // 6. EXPIRY ALERTS PANEL
    // ==========================================
    private JPanel createExpiryAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(COLOR_BG_LIGHT);

        JLabel title = new JLabel("Dedicated Expiry Alerts Center");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_DANGER);

        JLabel sub = new JLabel("Medications categorized into strict expiry risk groups. Never consume expired drugs!");
        sub.setFont(FONT_BODY);
        sub.setForeground(COLOR_TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(sub);
        panel.add(titleBox, BorderLayout.NORTH);

        // Sub-tabs without emoji glyphs that cause box [] glitches
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_BODY_BOLD);

        String[] alertCols = {"Medicine Name", "Batch No", "Expiry Date", "Days Remaining", "Current Qty", "Storage Location"};

        // Tab 1: Expired
        expiredAlertModel = new DefaultTableModel(alertCols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        expiredAlertTable = new JTable(expiredAlertModel);
        styleAlertTable(expiredAlertTable);
        tabbedPane.addTab("Already Expired", new JScrollPane(expiredAlertTable));

        // Tab 2: Today
        todayAlertModel = new DefaultTableModel(alertCols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        todayAlertTable = new JTable(todayAlertModel);
        styleAlertTable(todayAlertTable);
        tabbedPane.addTab("Expires Today", new JScrollPane(todayAlertTable));

        // Tab 3: Critical (1-7 days)
        criticalAlertModel = new DefaultTableModel(alertCols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        criticalAlertTable = new JTable(criticalAlertModel);
        styleAlertTable(criticalAlertTable);
        tabbedPane.addTab("Critical (<= 7 Days)", new JScrollPane(criticalAlertTable));

        // Tab 4: Soon (8-30 days)
        soonAlertModel = new DefaultTableModel(alertCols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        soonAlertTable = new JTable(soonAlertModel);
        styleAlertTable(soonAlertTable);
        tabbedPane.addTab("Expiring Soon (8-30 Days)", new JScrollPane(soonAlertTable));

        panel.add(tabbedPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomBar.setBackground(COLOR_BG_LIGHT);

        JButton refreshAlertsBtn = createSecondaryButton("Refresh Alerts");
        refreshAlertsBtn.addActionListener(e -> reloadExpiryAlerts());
        bottomBar.add(refreshAlertsBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void styleAlertTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.getTableHeader().setFont(FONT_BODY_BOLD);
        table.setShowVerticalLines(false);
        table.setGridColor(COLOR_BORDER);
    }

    private void reloadExpiryAlerts() {
        int uid = currentUser.getUserId();

        // 1. Expired
        List<Medicine> expired = medicineDAO.getExpiredMedicines(uid);
        populateAlertTable(expiredAlertModel, expired);

        // 2. Today
        List<Medicine> today = medicineDAO.getMedicinesExpiringToday(uid);
        populateAlertTable(todayAlertModel, today);

        // 3. Critical (1 to 7 days)
        List<Medicine> critical = medicineDAO.getMedicinesExpiringWithinDays(uid, 1, 7);
        populateAlertTable(criticalAlertModel, critical);

        // 4. Expiring Soon (8 to 30 days)
        List<Medicine> soon = medicineDAO.getMedicinesExpiringWithinDays(uid, 8, 30);
        populateAlertTable(soonAlertModel, soon);
    }

    private void populateAlertTable(DefaultTableModel model, List<Medicine> list) {
        model.setRowCount(0);
        for (Medicine m : list) {
            model.addRow(new Object[]{
                    m.getMedicineName(),
                    m.getBatchNumber(),
                    m.getExpiryDate().toString(),
                    m.getDaysLeftLabel(),
                    m.getQuantity() + " Units",
                    m.getStorageLocation() != null ? m.getStorageLocation() : ""
            });
        }
    }

    // ==========================================
    // 7. MEDICINE SEARCH & FILTERING PANEL
    // ==========================================
    private JPanel createMedicineSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top Filter Bar Card
        JPanel filterCard = createCardPanel();
        filterCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search Input
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        JLabel searchLbl = new JLabel("Search (Name, Category, Batch)");
        searchLbl.setFont(FONT_BODY_BOLD);
        filterCard.add(searchLbl, gbc);

        gbc.gridy = 1;
        searchInputField = createStyledTextField();
        searchInputField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { executeSearch(); }
            public void removeUpdate(DocumentEvent e) { executeSearch(); }
            public void changedUpdate(DocumentEvent e) { executeSearch(); }
        });
        filterCard.add(searchInputField, gbc);

        // Category Filter
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        JLabel catLbl = new JLabel("Category Filter");
        catLbl.setFont(FONT_BODY_BOLD);
        filterCard.add(catLbl, gbc);

        gbc.gridy = 1;
        searchCategoryFilter = new JComboBox<>(new String[]{"All Categories"});
        searchCategoryFilter.setFont(FONT_BODY);
        searchCategoryFilter.setPreferredSize(new Dimension(160, 34));
        searchCategoryFilter.addActionListener(e -> executeSearch());
        filterCard.add(searchCategoryFilter, gbc);

        // Status Filter
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        JLabel statusLbl = new JLabel("Expiry Status Filter");
        statusLbl.setFont(FONT_BODY_BOLD);
        filterCard.add(statusLbl, gbc);

        gbc.gridy = 1;
        searchStatusFilter = new JComboBox<>(new String[]{
                "All Medicines", "Safe", "Expiring Soon", "Critical", "Expires Today", "Expired"
        });
        searchStatusFilter.setFont(FONT_BODY);
        searchStatusFilter.setPreferredSize(new Dimension(160, 34));
        searchStatusFilter.addActionListener(e -> executeSearch());
        filterCard.add(searchStatusFilter, gbc);

        panel.add(filterCard, BorderLayout.NORTH);

        // Results Table
        String[] columns = {
                "ID", "Medicine Name", "Category", "Batch No",
                "Qty", "Expiry Date", "Days Remaining", "Status", "Storage Location"
        };
        searchTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        searchTable = new JTable(searchTableModel);
        searchTable.setFont(FONT_BODY);
        searchTable.setRowHeight(34);
        searchTable.setShowVerticalLines(false);
        searchTable.getTableHeader().setFont(FONT_BODY_BOLD);
        searchTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(searchTable);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void executeSearch() {
        String keyword = searchInputField != null ? searchInputField.getText().trim() : "";
        String cat = searchCategoryFilter != null ? (String) searchCategoryFilter.getSelectedItem() : "All Categories";
        String statusStr = searchStatusFilter != null ? (String) searchStatusFilter.getSelectedItem() : "All Medicines";

        ExpiryStatus statusEnum = null;
        if ("Safe".equalsIgnoreCase(statusStr)) statusEnum = ExpiryStatus.SAFE;
        else if ("Expiring Soon".equalsIgnoreCase(statusStr)) statusEnum = ExpiryStatus.EXPIRING_SOON;
        else if ("Critical".equalsIgnoreCase(statusStr)) statusEnum = ExpiryStatus.CRITICAL;
        else if ("Expires Today".equalsIgnoreCase(statusStr)) statusEnum = ExpiryStatus.EXPIRES_TODAY;
        else if ("Expired".equalsIgnoreCase(statusStr)) statusEnum = ExpiryStatus.EXPIRED;

        // Query MySQL directly
        searchResultList = medicineDAO.searchAndFilterMedicines(currentUser.getUserId(), keyword, cat, statusEnum);

        searchTableModel.setRowCount(0);
        for (Medicine m : searchResultList) {
            searchTableModel.addRow(new Object[]{
                    m.getMedicineId(),
                    m.getMedicineName(),
                    m.getCategory(),
                    m.getBatchNumber(),
                    m.getQuantity(),
                    m.getExpiryDate().toString(),
                    m.getDaysLeftLabel(),
                    m.getStatus(),
                    m.getStorageLocation() != null ? m.getStorageLocation() : ""
            });
        }
    }

    // ==========================================
    // 8. REPORTS PANEL
    // ==========================================
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(COLOR_BG_LIGHT);

        JLabel title = new JLabel("Medicine Expiry Reports & Summary");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY);

        JLabel sub = new JLabel("Live analytical counts calculated directly from database queries.");
        sub.setFont(FONT_BODY);
        sub.setForeground(COLOR_TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(sub);
        panel.add(titleBox, BorderLayout.NORTH);

        // KPI Counts Grid
        JPanel kpis = new JPanel(new GridLayout(2, 3, 10, 10));
        kpis.setBackground(COLOR_BG_LIGHT);

        rptTotalLabel = new JLabel("0", SwingConstants.CENTER);
        rptSafeLabel = new JLabel("0", SwingConstants.CENTER);
        rptSoonLabel = new JLabel("0", SwingConstants.CENTER);
        rptCriticalLabel = new JLabel("0", SwingConstants.CENTER);
        rptTodayLabel = new JLabel("0", SwingConstants.CENTER);
        rptExpiredLabel = new JLabel("0", SwingConstants.CENTER);

        kpis.add(createKpiCard("TOTAL MEDICINES", rptTotalLabel, "All Added Records", COLOR_PRIMARY, COLOR_ACCENT_LIGHT));
        kpis.add(createKpiCard("SAFE MEDICINES", rptSafeLabel, "> 30 Days Left", new Color(27, 94, 32), new Color(232, 245, 233)));
        kpis.add(createKpiCard("EXPIRING SOON", rptSoonLabel, "8-30 Days Left", new Color(230, 81, 0), new Color(255, 243, 224)));
        kpis.add(createKpiCard("CRITICAL", rptCriticalLabel, "1-7 Days Left", new Color(216, 67, 21), new Color(251, 233, 231)));
        kpis.add(createKpiCard("EXPIRES TODAY", rptTodayLabel, "Due Today", new Color(74, 20, 140), new Color(243, 229, 245)));
        kpis.add(createKpiCard("EXPIRED", rptExpiredLabel, "Do Not Use!", new Color(183, 28, 28), new Color(255, 235, 238)));

        // Filterable detailed report table
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setBackground(Color.WHITE);
        JLabel fLbl = new JLabel("Filter Report by Status:");
        fLbl.setFont(FONT_BODY_BOLD);

        rptStatusFilterCombo = new JComboBox<>(new String[]{
                "All Statuses", "Safe", "Expiring Soon", "Critical", "Expires Today", "Expired"
        });
        rptStatusFilterCombo.setFont(FONT_BODY);
        rptStatusFilterCombo.setPreferredSize(new Dimension(160, 32));
        rptStatusFilterCombo.addActionListener(e -> reloadReportsTableOnly());

        filterRow.add(fLbl);
        filterRow.add(rptStatusFilterCombo);
        tableCard.add(filterRow, BorderLayout.NORTH);

        String[] cols = {"Medicine Name", "Category", "Batch", "Quantity", "Expiry Date", "Days Remaining", "Status"};
        rptTableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        rptTable = new JTable(rptTableModel);
        rptTable.setFont(FONT_BODY);
        rptTable.setRowHeight(32);
        rptTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(rptTable);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel centerArea = new JPanel(new BorderLayout(0, 12));
        centerArea.setBackground(COLOR_BG_LIGHT);
        centerArea.add(kpis, BorderLayout.NORTH);
        centerArea.add(tableCard, BorderLayout.CENTER);

        panel.add(centerArea, BorderLayout.CENTER);

        return panel;
    }

    private void reloadReports() {
        int uid = currentUser.getUserId();
        Map<ExpiryStatus, Integer> counts = medicineDAO.getStatusCounts(uid);
        int total = medicineDAO.getTotalMedicineCount(uid);

        rptTotalLabel.setText(String.valueOf(total));
        rptSafeLabel.setText(String.valueOf(counts.getOrDefault(ExpiryStatus.SAFE, 0)));
        rptSoonLabel.setText(String.valueOf(counts.getOrDefault(ExpiryStatus.EXPIRING_SOON, 0)));
        rptCriticalLabel.setText(String.valueOf(counts.getOrDefault(ExpiryStatus.CRITICAL, 0)));
        rptTodayLabel.setText(String.valueOf(counts.getOrDefault(ExpiryStatus.EXPIRES_TODAY, 0)));
        rptExpiredLabel.setText(String.valueOf(counts.getOrDefault(ExpiryStatus.EXPIRED, 0)));

        reloadReportsTableOnly();
    }

    private void reloadReportsTableOnly() {
        int uid = currentUser.getUserId();
        String sel = (String) rptStatusFilterCombo.getSelectedItem();

        ExpiryStatus statusEnum = null;
        if ("Safe".equalsIgnoreCase(sel)) statusEnum = ExpiryStatus.SAFE;
        else if ("Expiring Soon".equalsIgnoreCase(sel)) statusEnum = ExpiryStatus.EXPIRING_SOON;
        else if ("Critical".equalsIgnoreCase(sel)) statusEnum = ExpiryStatus.CRITICAL;
        else if ("Expires Today".equalsIgnoreCase(sel)) statusEnum = ExpiryStatus.EXPIRES_TODAY;
        else if ("Expired".equalsIgnoreCase(sel)) statusEnum = ExpiryStatus.EXPIRED;

        List<Medicine> list = medicineDAO.searchAndFilterMedicines(uid, null, null, statusEnum);
        rptTableModel.setRowCount(0);
        for (Medicine m : list) {
            rptTableModel.addRow(new Object[]{
                    m.getMedicineName(),
                    m.getCategory(),
                    m.getBatchNumber(),
                    m.getQuantity(),
                    m.getExpiryDate().toString(),
                    m.getDaysLeftLabel(),
                    m.getStatus()
            });
        }
    }

    // ==========================================
    // DATA SYNC & GLOBAL REFRESH
    // ==========================================
    public void refreshDashboardData() {
        int uid = currentUser.getUserId();
        Map<ExpiryStatus, Integer> counts = medicineDAO.getStatusCounts(uid);
        int total = medicineDAO.getTotalMedicineCount(uid);

        int safe = counts.getOrDefault(ExpiryStatus.SAFE, 0);
        int soon = counts.getOrDefault(ExpiryStatus.EXPIRING_SOON, 0);
        int critical = counts.getOrDefault(ExpiryStatus.CRITICAL, 0);
        int today = counts.getOrDefault(ExpiryStatus.EXPIRES_TODAY, 0);
        int expired = counts.getOrDefault(ExpiryStatus.EXPIRED, 0);

        totalCountLabel.setText(String.valueOf(total));
        safeCountLabel.setText(String.valueOf(safe));
        expiringCountLabel.setText(String.valueOf(soon + critical + today));
        expiredCountLabel.setText(String.valueOf(expired));

        // Update alert banner
        if (expired > 0 || critical > 0 || today > 0) {
            bannerWarningLabel.setText(String.format("WARNING: You have %d expired and %d critical/due-today medicines!", expired, critical + today));
            bannerWarningPanel.setVisible(true);
        } else {
            bannerWarningPanel.setVisible(false);
        }

        // Update categories in search combo
        updateSearchCategories();

        // Populate dashboard live overview table
        if (dashOverviewTableModel != null) {
            List<Medicine> allMeds = medicineDAO.getMedicinesByUser(uid);
            dashOverviewTableModel.setRowCount(0);
            for (Medicine m : allMeds) {
                dashOverviewTableModel.addRow(new Object[]{
                        m.getMedicineName(),
                        m.getCategory(),
                        m.getBatchNumber(),
                        m.getQuantity() + " Units",
                        m.getExpiryDate().toString(),
                        m.getDaysLeftLabel(),
                        m.getStatus()
                });
            }
        }
    }

    private void updateSearchCategories() {
        if (searchCategoryFilter == null) return;
        String cur = (String) searchCategoryFilter.getSelectedItem();
        List<String> cats = medicineDAO.getCategories(currentUser.getUserId());

        searchCategoryFilter.removeAllItems();
        searchCategoryFilter.addItem("All Categories");
        for (String c : cats) {
            searchCategoryFilter.addItem(c);
        }

        if (cur != null) {
            searchCategoryFilter.setSelectedItem(cur);
        }
    }

    private void checkAndShowInitialAlerts() {
        int uid = currentUser.getUserId();
        List<Medicine> expired = medicineDAO.getExpiredMedicines(uid);
        List<Medicine> critical = medicineDAO.getMedicinesExpiringWithinDays(uid, 0, 7);

        if (!expired.isEmpty() || !critical.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("ATTENTION: Immediate Cabinet Review Needed!\n\n");
            if (!expired.isEmpty()) {
                sb.append("* ").append(expired.size()).append(" Expired Medicine(s) detected (Do NOT consume!)\n");
            }
            if (!critical.isEmpty()) {
                sb.append("* ").append(critical.size()).append(" Medicine(s) expiring within 7 days or today!\n");
            }
            sb.append("\nPlease review the 'Expiry Alerts' section to safely inspect or discard them.");

            JOptionPane.showMessageDialog(this, sb.toString(), "Cabinet Safety Alert", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleLogout() {
        boolean confirm = showConfirm("Are you sure you want to log out of the system?", "Confirm Logout");
        if (confirm) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
