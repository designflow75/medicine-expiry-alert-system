package com.mediguard.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing and verifying passwords using SHA-256.
 * Fulfills the security requirement of storing hashed credentials rather than plaintext.
 */
public class PasswordUtil {

    /**
     * Hashes a plaintext password using SHA-256.
     * @param plainPassword the user's plaintext password
     * @return 64-character hexadecimal SHA-256 hash string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            plainPassword = "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies if a plaintext password matches a stored SHA-256 hash.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equalsIgnoreCase(storedHash);
    }
}

