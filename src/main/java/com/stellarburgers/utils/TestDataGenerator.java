package com.stellarburgers.utils;

import com.stellarburgers.models.User;
import net.datafaker.Faker;

public class TestDataGenerator {
    
    private static final Faker faker = new Faker();
    
    public static User generateUniqueUser() {
        String email = faker.internet().safeEmailAddress();
        String password = faker.internet().password(8, 12);
        String name = faker.name().fullName();
        
        return new User(email, password, name);
    }
    
    public static User generateUserWithMissingEmail() {
        return new User(null, faker.internet().password(8, 12), faker.name().fullName());
    }
    
    public static User generateUserWithMissingPassword() {
        return new User(faker.internet().safeEmailAddress(), null, faker.name().fullName());
    }
    
    public static User generateUserWithMissingName() {
        return new User(faker.internet().safeEmailAddress(), faker.internet().password(8, 12), null);
    }
}
