package com.mediguard.model;

import java.time.LocalDateTime;

/**
 * Domain entity representing a student/user account.
 * Demonstrates OOP Encapsulation with private fields and public accessor methods.
 */
public class User {

    private int userId;
    private String fullName;
    private String studentId;
    private String collegeName;
    private String email;
    private String password;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String fullName, String studentId, String collegeName, String email, String password) {
        this.fullName = fullName;
        this.studentId = studentId;
        this.collegeName = collegeName;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    public User(int userId, String fullName, String studentId, String collegeName, String email, String password, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.studentId = studentId;
        this.collegeName = collegeName;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", studentId='" + studentId + '\'' +
                ", collegeName='" + collegeName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

