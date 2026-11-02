package com.mediguard.ui;

import com.mediguard.model.ExpiryStatus;
import com.mediguard.model.Medicine;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Read-only modal dialog to inspect full medicine details and expiry status.
 */
public class ViewMedicineDialog extends JDialog {

    public ViewMedicineDialog(JFrame parent, Medicine medicine) {
        super(parent, "Medicine Details — " + medicine.getMedicineName(), true);

        setSize(520, 560);
        setMinimumSize(new Dimension(460, 480));
        setLocationRelativeTo(parent);

        buildUI(medicine);
    }

    private void buildUI(Medicine med) {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(BaseFrame.COLOR_BG_LIGHT);
        main.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BaseFrame.COLOR_BG_LIGHT);

        JLabel titleLabel = new JLabel(med.getMedicineName());
        titleLabel.setFont(BaseFrame.FONT_HEADER);
        titleLabel.setForeground(BaseFrame.COLOR_PRIMARY);

        ExpiryStatus status = med.getStatus();
        JLabel statusBadge = new JLabel("  " + status.getDisplayName() + "  ");
        statusBadge.setFont(BaseFrame.FONT_BODY_BOLD);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(status.getBackground());
        statusBadge.setForeground(status.getForeground());
        statusBadge.setBorder(BorderFactory.createLineBorder(status.getForeground(), 1, true));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(statusBadge, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        // Content Card
        JPanel card = BaseFrame.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        addDetailRow(card, gbc, row++, "Medicine ID:", String.valueOf(med.getMedicineId()));
        addDetailRow(card, gbc, row++, "Category:", med.getCategory());
        addDetailRow(card, gbc, row++, "Batch Number:", med.getBatchNumber());
        addDetailRow(card, gbc, row++, "Current Quantity:", med.getQuantity() + " Units");
        addDetailRow(card, gbc, row++, "Storage Location:", (med.getStorageLocation() != null && !med.getStorageLocation().isBlank()) ? med.getStorageLocation() : "Not specified");
        addDetailRow(card, gbc, row++, "Purchase Date:", med.getPurchaseDate().toString());
        addDetailRow(card, gbc, row++, "Expiry Date:", med.getExpiryDate().toString());
        addDetailRow(card, gbc, row++, "Shelf Life:", med.getDaysLeftLabel());

        // Notes
        gbc.gridx = 0;
        gbc.gridy = row++;
        JLabel notesTitle = new JLabel("Notes / Directions:");
        notesTitle.setFont(BaseFrame.FONT_BODY_BOLD);
        notesTitle.setForeground(BaseFrame.COLOR_TEXT_DARK);
        card.add(notesTitle, gbc);

        gbc.gridy = row++;
        JTextArea notesArea = new JTextArea(4, 25);
        notesArea.setText((med.getNotes() != null && !med.getNotes().isBlank()) ? med.getNotes() : "No additional notes provided.");
        notesArea.setEditable(false);
        notesArea.setFont(BaseFrame.FONT_BODY);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(BaseFrame.COLOR_BG_LIGHT);
        JScrollPane scroll = new JScrollPane(notesArea);
        scroll.setBorder(BorderFactory.createLineBorder(BaseFrame.COLOR_BORDER, 1));
        card.add(scroll, gbc);

        main.add(card, BorderLayout.CENTER);

        // Bottom Close Button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setBackground(BaseFrame.COLOR_BG_LIGHT);
        JButton closeBtn = BaseFrame.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
        rowPanel.setBackground(BaseFrame.COLOR_CARD_BG);

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(BaseFrame.FONT_BODY_BOLD);
        lblKey.setForeground(BaseFrame.COLOR_TEXT_MUTED);
        lblKey.setPreferredSize(new Dimension(140, 22));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(BaseFrame.FONT_BODY);
        lblVal.setForeground(BaseFrame.COLOR_TEXT_DARK);

        rowPanel.add(lblKey, BorderLayout.WEST);
        rowPanel.add(lblVal, BorderLayout.CENTER);
        panel.add(rowPanel, gbc);
    }
}

