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

public class UserLoginTest {
    
    private User testUser;
    private String accessToken;
    
    @Before
    public void setUp() {
        testUser = TestDataGenerator.generateUniqueUser();
        createTestUser();
    }
    
    @Test
    @DisplayName("Login with valid credentials")
    public void testLoginWithValidCredentials() {
        User loginUser = new User(testUser.getEmail(), testUser.getPassword(), null);
        
        Response response = loginUser(loginUser);
        
        checkSuccessfulLogin(response);
    }
    
    @Test
    @DisplayName("Login with invalid email")
    public void testLoginWithInvalidEmail() {
        User loginUser = new User("invalid@test.com", testUser.getPassword(), null);
        
        Response response = loginUser(loginUser);
        
        checkInvalidCredentialsError(response);
    }
    
    @Test
    @DisplayName("Login with invalid password")
    public void testLoginWithInvalidPassword() {
        User loginUser = new User(testUser.getEmail(), "wrongpassword", null);
        
        Response response = loginUser(loginUser);
        
        checkInvalidCredentialsError(response);
    }
    
    @Step("Create test user")
    private void createTestUser() {
        Response response = ApiClient.getRequestSpec()
                .body(testUser)
                .when()
                .post("/auth/register");
        
        accessToken = response.jsonPath().getString("accessToken");
    }
    
    @Step("Login user")
    private Response loginUser(User user) {
        return ApiClient.getRequestSpec()
                .body(user)
                .when()
                .post("/auth/login");
    }
    
    @Step("Check successful login")
    private void checkSuccessfulLogin(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(testUser.getEmail()))
                .body("user.name", equalTo(testUser.getName()));
    }
    
    @Step("Check invalid credentials error")
    private void checkInvalidCredentialsError(Response response) {
        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
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
