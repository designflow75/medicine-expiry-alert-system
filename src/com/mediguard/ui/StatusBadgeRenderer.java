package com.mediguard.ui;

import com.mediguard.model.ExpiryStatus;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Custom TableCellRenderer that renders medicine expiry statuses as colored rounded pill badges.
 * Demonstrates Polymorphism and Method Overriding in Java Swing.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    private ExpiryStatus currentStatus = ExpiryStatus.SAFE;

    public StatusBadgeRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setFont(BaseFrame.FONT_BODY_BOLD);

        if (value instanceof ExpiryStatus status) {
            this.currentStatus = status;
            label.setText(status.getDisplayName());
            label.setForeground(status.getForeground());
        } else if (value != null) {
            label.setText(value.toString());
            label.setForeground(BaseFrame.COLOR_TEXT_DARK);
        }

        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padX = 10;
        int padY = 5;
        int width = getWidth() - (padX * 2);
        int height = getHeight() - (padY * 2);

        if (width > 0 && height > 0 && currentStatus != null) {
            // Draw soft pill background
            g2.setColor(currentStatus.getBackground());
            g2.fillRoundRect(padX, padY, width, height, height, height);

            // Draw border matching status foreground
            g2.setColor(currentStatus.getForeground());
            g2.drawRoundRect(padX, padY, width, height, height, height);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}

