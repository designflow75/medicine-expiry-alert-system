package com.mediguard.model;

import com.mediguard.util.ExpiryCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing a medicine record in the inventory.
 * Encapsulates properties and provides dynamic expiry calculations.
 */
public class Medicine {

    private int medicineId;
    private int userId;
    private String medicineName;
    private String category;
    private String batchNumber;
    private int quantity;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private String storageLocation;
    private String notes;
    private LocalDateTime createdAt;

    public Medicine() {
        this.purchaseDate = LocalDate.now();
        this.expiryDate = LocalDate.now().plusMonths(6);
        this.quantity = 1;
        this.createdAt = LocalDateTime.now();
    }

    public Medicine(int userId, String medicineName, String category, String batchNumber,
                    int quantity, LocalDate purchaseDate, LocalDate expiryDate,
                    String storageLocation, String notes) {
        this.userId = userId;
        this.medicineName = medicineName;
        this.category = category;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.storageLocation = storageLocation;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public Medicine(int medicineId, int userId, String medicineName, String category,
                    String batchNumber, int quantity, LocalDate purchaseDate,
                    LocalDate expiryDate, String storageLocation, String notes,
                    LocalDateTime createdAt) {
        this.medicineId = medicineId;
        this.userId = userId;
        this.medicineName = medicineName;
        this.category = category;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.storageLocation = storageLocation;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // --- Dynamic OOP Business Logic ---

    /**
     * Calculates days left until expiration. Negative if already expired.
     */
    public long getDaysLeft() {
        return ExpiryCalculator.calculateDaysRemaining(expiryDate);
    }

    /**
     * Automatically evaluates the dynamic ExpiryStatus based on current date.
     */
    public ExpiryStatus getStatus() {
        return ExpiryCalculator.calculateStatus(expiryDate);
    }

    /**
     * Human-friendly shelf life string (e.g. "Expired 5 days ago", "12 days remaining").
     */
    public String getDaysLeftLabel() {
        return ExpiryCalculator.formatDaysRemaining(expiryDate);
    }

    public boolean isExpired() {
        return getStatus() == ExpiryStatus.EXPIRED;
    }

    public boolean isExpiresToday() {
        return getStatus() == ExpiryStatus.EXPIRES_TODAY;
    }

    public boolean isCritical() {
        return getStatus() == ExpiryStatus.CRITICAL;
    }

    public boolean isExpiringSoon() {
        return getStatus() == ExpiryStatus.EXPIRING_SOON;
    }

    public boolean isSafe() {
        return getStatus() == ExpiryStatus.SAFE;
    }

    // --- Getters & Setters (Encapsulation) ---

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + medicineId +
                ", name='" + medicineName + '\'' +
                ", category='" + category + '\'' +
                ", batch='" + batchNumber + '\'' +
                ", quantity=" + quantity +
                ", expiryDate=" + expiryDate +
                ", status=" + getStatus() +
                '}';
    }
}

