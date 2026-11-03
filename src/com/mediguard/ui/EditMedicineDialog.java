package com.mediguard.ui;

import com.mediguard.dao.MedicineDAO;
import com.mediguard.dao.MedicineDAOImpl;
import com.mediguard.model.Medicine;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for editing existing medicine details.
 * Updates MySQL directly and asks confirmation where appropriate.
 */
public class EditMedicineDialog extends JDialog {

    private final Medicine medicine;
    private final MedicineDAO medicineDAO = new MedicineDAOImpl();
    private boolean updated = false;

    private JTextField nameField;
    private JComboBox<String> categoryCombo;
    private JTextField batchField;
    private JSpinner quantitySpinner;
    private JTextField purchaseDateField;
    private JTextField expiryDateField;
    private JTextField storageLocationField;
    private JTextArea notesArea;

    public EditMedicineDialog(JFrame parent, Medicine medicine) {
        super(parent, "Edit Medicine — " + medicine.getMedicineName(), true);
        this.medicine = medicine;

        setSize(560, 640);
        setMinimumSize(new Dimension(500, 580));
        setLocationRelativeTo(parent);

        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(BaseFrame.COLOR_BG_LIGHT);
        main.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel("Edit Medicine Details");
        titleLabel.setFont(BaseFrame.FONT_HEADER);
        titleLabel.setForeground(BaseFrame.COLOR_PRIMARY);
        main.add(titleLabel, BorderLayout.NORTH);

        JPanel formCard = BaseFrame.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Name
        nameField = BaseFrame.createStyledTextField();
        nameField.setText(medicine.getMedicineName());
        addFormField(formCard, gbc, row++, "Medicine Name *", nameField);

        // Category
        String[] categories = {
                "Tablets", "Capsules", "Syrups", "Injections", "Ointments",
                "Drops", "Antibiotics", "Pain Relief", "Analgesics", "Antihistamines",
                "Respiratory", "Diabetes", "Supplements", "First Aid", "General"
        };
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setEditable(true);
        categoryCombo.setFont(BaseFrame.FONT_BODY);
        categoryCombo.setSelectedItem(medicine.getCategory());
        addFormField(formCard, gbc, row++, "Category *", categoryCombo);

        // Batch Number
        batchField = BaseFrame.createStyledTextField();
        batchField.setText(medicine.getBatchNumber());
        addFormField(formCard, gbc, row++, "Batch Number *", batchField);

        // Quantity
        quantitySpinner = new JSpinner(new SpinnerNumberModel(medicine.getQuantity(), 0, 99999, 1));
        quantitySpinner.setFont(BaseFrame.FONT_BODY);
        addFormField(formCard, gbc, row++, "Quantity (Units) *", quantitySpinner);

        // Purchase Date
        purchaseDateField = BaseFrame.createStyledTextField();
        purchaseDateField.setText(medicine.getPurchaseDate().toString());
        addFormField(formCard, gbc, row++, "Purchase Date (YYYY-MM-DD) *", purchaseDateField);

        // Expiry Date
        expiryDateField = BaseFrame.createStyledTextField();
        expiryDateField.setText(medicine.getExpiryDate().toString());
        addFormField(formCard, gbc, row++, "Expiry Date (YYYY-MM-DD) *", expiryDateField);

        // Storage Location
        storageLocationField = BaseFrame.createStyledTextField();
        storageLocationField.setText(medicine.getStorageLocation() != null ? medicine.getStorageLocation() : "");
        addFormField(formCard, gbc, row++, "Storage Location", storageLocationField);

        // Notes
        notesArea = new JTextArea(3, 20);
        notesArea.setFont(BaseFrame.FONT_BODY);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setText(medicine.getNotes() != null ? medicine.getNotes() : "");
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(BaseFrame.COLOR_BORDER, 1, true));

        gbc.gridx = 0;
        gbc.gridy = row * 2;
        JLabel notesLbl = new JLabel("Notes / Directions");
        notesLbl.setFont(BaseFrame.FONT_BODY_BOLD);
        formCard.add(notesLbl, gbc);

        gbc.gridy = (row * 2) + 1;
        formCard.add(notesScroll, gbc);

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        main.add(formScroll, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BaseFrame.COLOR_BG_LIGHT);

        JButton cancelBtn = BaseFrame.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = BaseFrame.createPrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> handleSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        main.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, javax.swing.JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(BaseFrame.FONT_BODY_BOLD);
        lbl.setForeground(BaseFrame.COLOR_TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridy = (row * 2) + 1;
        comp.setPreferredSize(new Dimension(280, 32));
        panel.add(comp, gbc);
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String cat = categoryCombo.getSelectedItem() != null ? categoryCombo.getSelectedItem().toString().trim() : "General";
        String batch = batchField.getText().trim();
        int qty = (Integer) quantitySpinner.getValue();
        String pDateStr = purchaseDateField.getText().trim();
        String eDateStr = expiryDateField.getText().trim();
        String loc = storageLocationField.getText().trim();
        String notes = notesArea.getText().trim();

        if (name.isEmpty() || batch.isEmpty() || pDateStr.isEmpty() || eDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (*).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate pDate;
        LocalDate eDate;
        try {
            pDate = LocalDate.parse(pDateStr);
            eDate = LocalDate.parse(eDateStr);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-11-01).", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Expiry date must not be before purchase date
        if (eDate.isBefore(pDate)) {
            JOptionPane.showMessageDialog(this, "Expiry date cannot be earlier than purchase date!", "Date Conflict", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Apply changes
        medicine.setMedicineName(name);
        medicine.setCategory(cat);
        medicine.setBatchNumber(batch);
        medicine.setQuantity(qty);
        medicine.setPurchaseDate(pDate);
        medicine.setExpiryDate(eDate);
        medicine.setStorageLocation(loc);
        medicine.setNotes(notes);

        boolean success = medicineDAO.updateMedicine(medicine);
        if (success) {
            updated = true;
            JOptionPane.showMessageDialog(this, "Medicine details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update record in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isUpdated() {
        return updated;
    }
}

