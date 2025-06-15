package com.stellarburgers.utils;

import com.stellarburgers.models.User;
import java.util.Random;

public class TestDataGenerator {
    
    private static final Random random = new Random();
    
    public static User generateUniqueUser() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String email = "test" + timestamp + "@test.com";
        String password = "password123";
        String name = "TestUser" + timestamp;
        
        return new User(email, password, name);
    }
    
    public static User generateUserWithMissingEmail() {
        return new User(null, "password123", "TestUser");
    }
    
    public static User generateUserWithMissingPassword() {
        return new User("test@test.com", null, "TestUser");
    }
    
    public static User generateUserWithMissingName() {
        return new User("test@test.com", "password123", null);
    }
}
