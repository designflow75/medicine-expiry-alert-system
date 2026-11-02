package com.mediguard.model;

import java.awt.Color;

/**
 * Enum representing the 5 dynamic expiry status categories
 * as specified in KTU S3 B.Tech AI/ML OOP project requirements.
 *
 * Conditions:
 * - Safe: More than 30 days remaining (> 30 days)
 * - Expiring Soon: 8–30 days remaining (8 to 30 days)
 * - Critical: 1–7 days remaining (1 to 7 days)
 * - Expires Today: Expiry date is today (0 days)
 * - Expired: Expiry date has passed (< 0 days)
 */
public enum ExpiryStatus {
    SAFE("Safe", new Color(27, 94, 32), new Color(232, 245, 233)),             // Dark Green / Soft Green
    EXPIRING_SOON("Expiring Soon", new Color(230, 81, 0), new Color(255, 243, 224)), // Amber / Soft Orange
    CRITICAL("Critical", new Color(216, 67, 21), new Color(251, 233, 231)),     // Deep Orange / Soft Pink
    EXPIRES_TODAY("Expires Today", new Color(74, 20, 140), new Color(243, 229, 245)), // Purple / Soft Lavender
    EXPIRED("Expired", new Color(183, 28, 28), new Color(255, 235, 238));       // Deep Red / Soft Red

    private final String displayName;
    private final Color foreground;
    private final Color background;

    ExpiryStatus(String displayName, Color foreground, Color background) {
        this.displayName = displayName;
        this.foreground = foreground;
        this.background = background;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

