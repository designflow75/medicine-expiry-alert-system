package com.mediguard.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Base abstract class providing common UI theme, styling, helper components,
 * and standard window configurations.
 * Demonstrates the OOP concept of Inheritance.
 */
public abstract class BaseFrame extends JFrame {

    // --- Color Palette (White background with soft green accents) ---
    public static final Color COLOR_PRIMARY = new Color(46, 125, 50);        // #2E7D32 Deep Green
    public static final Color COLOR_PRIMARY_HOVER = new Color(27, 94, 32);  // #1B5E20 Forest Green
    public static final Color COLOR_ACCENT = new Color(76, 175, 80);         // #4CAF50 Soft Vibrant Green
    public static final Color COLOR_ACCENT_LIGHT = new Color(232, 245, 233); // #E8F5E9 Soft Green Tint

    public static final Color COLOR_BG_LIGHT = new Color(248, 250, 249);     // #F8FAF9 Clean off-white
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(224, 224, 224);       // #E0E0E0
    public static final Color COLOR_BORDER_GREEN = new Color(200, 230, 201); // #C8E6C9

    public static final Color COLOR_TEXT_DARK = new Color(33, 33, 33);       // #212121
    public static final Color COLOR_TEXT_MUTED = new Color(117, 117, 117);   // #757575

    public static final Color COLOR_DANGER = new Color(198, 40, 40);         // #C62828 Red
    public static final Color COLOR_DANGER_HOVER = new Color(183, 28, 28);   // #B71C1C Darker Red
    public static final Color COLOR_WARNING = new Color(239, 108, 0);        // #EF6C00 Amber

    // --- Typography (Clean Segoe UI / System Sans, NO missing-glyph emojis) ---
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_METRIC = new Font("Segoe UI", Font.BOLD, 26);

    public BaseFrame(String title) {
        super(title);
        initializeBaseWindow();
    }

    private void initializeBaseWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_BG_LIGHT);
    }

    /**
     * Custom Modern Button with anti-aliasing, fixed non-shivering borders,
     * and guaranteed color rendering on any Look and Feel.
     */
    public static class ModernButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color normalFg;
        private final Color borderColor;
        private boolean isHovered = false;
        private boolean isSelectedState = false;
        private boolean alignLeft = false;

        public ModernButton(String text, Color normalBg, Color hoverBg, Color normalFg, Color borderColor) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.normalFg = normalFg;
            this.borderColor = borderColor;

            setFont(FONT_BODY_BOLD);
            setForeground(normalFg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setSelectedState(boolean selected) {
            this.isSelectedState = selected;
            repaint();
        }

        public void setAlignLeft(boolean left) {
            this.alignLeft = left;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Background color
            Color currentBg = isSelectedState ? hoverBg : (isHovered ? hoverBg : normalBg);
            if (!isEnabled()) {
                currentBg = new Color(220, 220, 220);
            }

            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, width, height, 8, 8);

            // Border color
            if (borderColor != null && !isSelectedState) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
            }

            // Left highlight bar for active sidebar items
            if (isSelectedState) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 2, 4, height - 4, 3, 3);
            }

            // Text
            g2.setFont(getFont());
            g2.setColor(isEnabled() ? normalFg : COLOR_TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();

            if (alignLeft) {
                int textX = 16;
                g2.drawString(getText(), textX, textY);
            } else {
                int textX = (width - fm.stringWidth(getText())) / 2;
                g2.drawString(getText(), textX, textY);
            }

            g2.dispose();
        }
    }

    public static JButton createPrimaryButton(String text) {
        ModernButton btn = new ModernButton(text, COLOR_PRIMARY, COLOR_PRIMARY_HOVER, Color.WHITE, COLOR_PRIMARY_HOVER);
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        ModernButton btn = new ModernButton(text, Color.WHITE, new Color(241, 248, 249), COLOR_TEXT_DARK, COLOR_BORDER);
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        ModernButton btn = new ModernButton(text, COLOR_DANGER, COLOR_DANGER_HOVER, Color.WHITE, COLOR_DANGER_HOVER);
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    public static JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        return card;
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public boolean showConfirm(String message, String title) {
        int opt = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return opt == JOptionPane.YES_OPTION;
    }
}
