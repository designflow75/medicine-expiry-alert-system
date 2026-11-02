package com.mediguard.dao;

import com.mediguard.model.User;

/**
 * Data Access Object (DAO) interface for User authentication and profile management.
 * Demonstrates the Abstraction concept of OOP.
 */
public interface UserDAO {

    /**
     * Registers a new user into MySQL with a hashed password.
     * @param user user details
     * @return true if successful, false otherwise
     */
    boolean registerUser(User user);

    /**
     * Authenticates user against MySQL using email and plaintext password.
     * @param email user's email
     * @param plainPassword plaintext password
     * @return User object if credentials are valid, null otherwise
     */
    User authenticate(String email, String plainPassword);

    /**
     * Checks if an email address is already registered in the database.
     * @param email email to check
     * @return true if email exists, false otherwise
     */
    boolean emailExists(String email);

    /**
     * Finds a user by their unique email.
     * @param email user email
     * @return User or null
     */
    User findByEmail(String email);

    /**
     * Finds a user by their primary key user_id.
     * @param userId user ID
     * @return User or null
     */
    User findById(int userId);
}

