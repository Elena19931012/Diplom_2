package com.stellarburgers;

import com.stellarburgers.api.UserApi;
import com.stellarburgers.models.User;
import com.stellarburgers.utils.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
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
    @Description("Тест проверяет, что пользователь может успешно войти в систему с корректными учетными данными и получить правильные токены аутентификации")
    public void testLoginWithValidCredentials() {
        User loginUser = new User(testUser.getEmail(), testUser.getPassword(), null);
        
        Response response = UserApi.loginUser(loginUser);
        
        checkSuccessfulLogin(response);
    }
    
    @Test
    @DisplayName("Login with invalid email")
    @Description("Тест проверяет, что попытка входа с неправильным адресом электронной почты возвращает соответствующую ошибку аутентификации")
    public void testLoginWithInvalidEmail() {
        User loginUser = new User("invalid@test.com", testUser.getPassword(), null);
        
        Response response = UserApi.loginUser(loginUser);
        
        checkInvalidCredentialsError(response);
    }
    
    @Test
    @DisplayName("Login with invalid password")
    @Description("Тест проверяет, что попытка входа с неправильным паролем возвращает соответствующую ошибку аутентификации")
    public void testLoginWithInvalidPassword() {
        User loginUser = new User(testUser.getEmail(), "wrongpassword", null);
        
        Response response = UserApi.loginUser(loginUser);
        
        checkInvalidCredentialsError(response);
    }
    
    @Step("Создание тестового пользователя")
    private void createTestUser() {
        Response response = UserApi.createUser(testUser);
        accessToken = response.jsonPath().getString("accessToken");
    }
    
    @Step("Проверка успешного входа")
    private void checkSuccessfulLogin(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(testUser.getEmail()))
                .body("user.name", equalTo(testUser.getName()));
    }
    
    @Step("Проверка ошибки неверных учетных данных")
    private void checkInvalidCredentialsError(Response response) {
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
    
    @After
    public void tearDown() {
        deleteTestUser();
    }
    
    @Step("Удаление тестового пользователя")
    private void deleteTestUser() {
        if (accessToken != null) {
            UserApi.deleteUser(accessToken).then()
                   .statusCode(anyOf(is(SC_ACCEPTED), is(SC_NOT_FOUND)));
        }
    }
}
