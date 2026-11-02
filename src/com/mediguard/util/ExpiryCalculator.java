package com.mediguard.util;

import com.mediguard.model.ExpiryStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utility class to dynamically calculate expiry status and remaining days
 * using Java Date-Time API (LocalDate, ChronoUnit).
 * Never relies on manually entered expiry statuses.
 */
public class ExpiryCalculator {

    /**
     * Calculates days remaining between today and expiry date.
     * Negative values indicate the medicine has already expired.
     */
    public static long calculateDaysRemaining(LocalDate expiryDate) {
        if (expiryDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Categorizes medicine into one of 5 strict tiers:
     * - Expired: Expiry date has passed (< 0 days)
     * - Expires Today: Expiry date is today (0 days)
     * - Critical: 1–7 days remaining
     * - Expiring Soon: 8–30 days remaining
     * - Safe: More than 30 days remaining (> 30 days)
     */
    public static ExpiryStatus calculateStatus(LocalDate expiryDate) {
        if (expiryDate == null) {
            return ExpiryStatus.SAFE;
        }

        long days = calculateDaysRemaining(expiryDate);

        if (days < 0) {
            return ExpiryStatus.EXPIRED;
        } else if (days == 0) {
            return ExpiryStatus.EXPIRES_TODAY;
        } else if (days <= 7) {
            return ExpiryStatus.CRITICAL;
        } else if (days <= 30) {
            return ExpiryStatus.EXPIRING_SOON;
        } else {
            return ExpiryStatus.SAFE;
        }
    }

    /**
     * Generates a descriptive, human-readable label for remaining shelf life.
     */
    public static String formatDaysRemaining(LocalDate expiryDate) {
        if (expiryDate == null) {
            return "No date set";
        }

        long days = calculateDaysRemaining(expiryDate);

        if (days < 0) {
            long passed = Math.abs(days);
            return "Expired " + passed + (passed == 1 ? " day ago" : " days ago");
        } else if (days == 0) {
            return "Expires Today!";
        } else if (days == 1) {
            return "1 day remaining";
        } else {
            return days + " days remaining";
        }
    }
}

