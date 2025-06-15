package com.stellarburgers;

import com.stellarburgers.client.ApiClient;
import com.stellarburgers.models.User;
import com.stellarburgers.utils.TestDataGenerator;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserUpdateTest {
    
    private User testUser;
    private String accessToken;
    
    @Before
    public void setUp() {
        testUser = TestDataGenerator.generateUniqueUser();
        createTestUser();
    }
    
    @Test
    @DisplayName("Update user email with authorization")
    public void testUpdateEmailWithAuth() {
        String newEmail = "new" + System.currentTimeMillis() + "@test.com";
        User updateUser = new User(newEmail, null, null);
        
        Response response = updateUserWithAuth(updateUser);
        
        checkSuccessfulUpdate(response, newEmail, testUser.getName());
    }
    
    @Test
    @DisplayName("Update user name with authorization")
    public void testUpdateNameWithAuth() {
        String newName = "NewName" + System.currentTimeMillis();
        User updateUser = new User(null, null, newName);
        
        Response response = updateUserWithAuth(updateUser);
        
        checkSuccessfulUpdate(response, testUser.getEmail(), newName);
    }
    
    @Test
    @DisplayName("Update user password with authorization")
    public void testUpdatePasswordWithAuth() {
        String newPassword = "newpassword123";
        User updateUser = new User(null, newPassword, null);
        
        Response response = updateUserWithAuth(updateUser);
        
        checkSuccessfulUpdate(response, testUser.getEmail(), testUser.getName());
    }
    
    @Test
    @DisplayName("Update user without authorization")
    public void testUpdateUserWithoutAuth() {
        String newName = "NewName" + System.currentTimeMillis();
        User updateUser = new User(null, null, newName);
        
        Response response = updateUserWithoutAuth(updateUser);
        
        checkUnauthorizedError(response);
    }
    
    @Step("Create test user")
    private void createTestUser() {
        Response response = ApiClient.getRequestSpec()
                .body(testUser)
                .when()
                .post("/auth/register");
        
        accessToken = response.jsonPath().getString("accessToken");
    }
    
    @Step("Update user with authorization")
    private Response updateUserWithAuth(User user) {
        return ApiClient.getRequestSpecWithAuth(accessToken)
                .body(user)
                .when()
                .patch("/auth/user");
    }
    
    @Step("Update user without authorization")
    private Response updateUserWithoutAuth(User user) {
        return ApiClient.getRequestSpec()
                .body(user)
                .when()
                .patch("/auth/user");
    }
    
    @Step("Check successful update")
    private void checkSuccessfulUpdate(Response response, String expectedEmail, String expectedName) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(expectedEmail))
                .body("user.name", equalTo(expectedName));
    }
    
    @Step("Check unauthorized error")
    private void checkUnauthorizedError(Response response) {
        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
    
    @After
    public void tearDown() {
        deleteTestUser();
    }
    
    @Step("Delete test user")
    private void deleteTestUser() {
        if (accessToken != null) {
            given()
                    .spec(ApiClient.getRequestSpecWithAuth(accessToken))
                    .when()
                    .delete("/auth/user")
                    .then()
                    .statusCode(anyOf(is(202), is(404)));
        }
    }
}
