package com.mediguard.dao;

import com.mediguard.model.ExpiryStatus;
import com.mediguard.model.Medicine;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) interface for Medicine management operations.
 * Enforces per-user privacy so each student only accesses their own inventory.
 * Demonstrates Abstraction in OOP.
 */
public interface MedicineDAO {

    /**
     * Inserts a new medicine into MySQL.
     */
    boolean addMedicine(Medicine medicine);

    /**
     * Updates an existing medicine in MySQL.
     */
    boolean updateMedicine(Medicine medicine);

    /**
     * Deletes a medicine by ID, ensuring user ownership.
     */
    boolean deleteMedicine(int medicineId, int userId);

    /**
     * Retrieves a single medicine by its ID.
     */
    Medicine getMedicineById(int medicineId, int userId);

    /**
     * Retrieves all medicines belonging to the specified user.
     */
    List<Medicine> getMedicinesByUser(int userId);

    /**
     * Searches medicines by name, category, or batch number, with optional status filter.
     */
    List<Medicine> searchAndFilterMedicines(int userId, String keyword, String category, ExpiryStatus statusFilter);

    /**
     * Retrieves medicines with a specific expiry status.
     */
    List<Medicine> getMedicinesByStatus(int userId, ExpiryStatus status);

    /**
     * Retrieves medicines whose remaining days are within the specified range [minDays, maxDays].
     */
    List<Medicine> getMedicinesExpiringWithinDays(int userId, int minDays, int maxDays);

    /**
     * Retrieves already expired medicines (remaining days < 0).
     */
    List<Medicine> getExpiredMedicines(int userId);

    /**
     * Retrieves medicines expiring today (remaining days == 0).
     */
    List<Medicine> getMedicinesExpiringToday(int userId);

    /**
     * Retrieves counts for each expiry status tier directly from the database.
     */
    Map<ExpiryStatus, Integer> getStatusCounts(int userId);

    /**
     * Retrieves total medicine count for the user.
     */
    int getTotalMedicineCount(int userId);

    /**
     * Retrieves distinct categories present in the user's inventory.
     */
    List<String> getCategories(int userId);
}

