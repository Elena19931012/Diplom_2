package com.stellarburgers;

import com.stellarburgers.client.ApiClient;
import com.stellarburgers.models.User;
import com.stellarburgers.utils.TestDataGenerator;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserRegistrationTest {
    
    private String accessToken;
    
    @Test
    @DisplayName("Register a unique user")
    public void testRegisterUniqueUser() {
        User user = TestDataGenerator.generateUniqueUser();
        
        Response response = createUser(user);
        
        checkSuccessfulRegistration(response);
        extractAccessToken(response);
    }
    
    @Test
    @DisplayName("Register a user that already exists")
    public void testRegisterExistingUser() {
        User user = TestDataGenerator.generateUniqueUser();
        
        // Create user first time
        Response firstResponse = createUser(user);
        extractAccessToken(firstResponse);
        
        // Try to create the same user again
        Response secondResponse = createUser(user);
        
        checkUserAlreadyExistsError(secondResponse);
    }
    
    @Test
    @DisplayName("Register a user with missing email")
    public void testRegisterUserWithMissingEmail() {
        User user = TestDataGenerator.generateUserWithMissingEmail();
        
        Response response = createUser(user);
        
        checkMissingFieldsError(response);
    }
    
    @Test
    @DisplayName("Register a user with missing password")
    public void testRegisterUserWithMissingPassword() {
        User user = TestDataGenerator.generateUserWithMissingPassword();
        
        Response response = createUser(user);
        
        checkMissingFieldsError(response);
    }
    
    @Test
    @DisplayName("Register a user with missing name")
    public void testRegisterUserWithMissingName() {
        User user = TestDataGenerator.generateUserWithMissingName();
        
        Response response = createUser(user);
        
        checkMissingFieldsError(response);
    }
    
    @Step("Create user")
    private Response createUser(User user) {
        return ApiClient.getRequestSpec()
                .body(user)
                .when()
                .post("/auth/register");
    }
    
    @Step("Check successful registration")
    private void checkSuccessfulRegistration(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", notNullValue())
                .body("user.name", notNullValue())
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }
    
    @Step("Check user already exists error")
    private void checkUserAlreadyExistsError(Response response) {
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }
    
    @Step("Check missing fields error")
    private void checkMissingFieldsError(Response response) {
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
    
    @Step("Extract access token")
    private void extractAccessToken(Response response) {
        if (response.getStatusCode() == 200) {
            accessToken = response.jsonPath().getString("accessToken");
        }
    }
    
    @After
    public void tearDown() {
        deleteUser();
    }
    
    @Step("Delete user")
    private void deleteUser() {
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
