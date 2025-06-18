package com.stellarburgers;

import com.stellarburgers.api.UserApi;
import com.stellarburgers.models.User;
import com.stellarburgers.utils.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class UserRegistrationTest {
    
    private String accessToken;
    
    @Test
    @DisplayName("Register a unique user")
    @Description("Тест проверяет, что новый пользователь может быть успешно зарегистрирован с действительными учетными данными и получает правильные токены аутентификации")
    public void testRegisterUniqueUser() {
        User user = TestDataGenerator.generateUniqueUser();
        
        Response response = UserApi.createUser(user);
        
        checkSuccessfulRegistration(response);
        extractAccessToken(response);
    }
    
    @Test
    @DisplayName("Register a user that already exists")
    @Description("Тест проверяет, что попытка регистрации пользователя с уже существующим адресом электронной почты возвращает соответствующую ошибку")
    public void testRegisterExistingUser() {
        User user = TestDataGenerator.generateUniqueUser();
        
        Response firstResponse = UserApi.createUser(user);
        extractAccessToken(firstResponse);
        
        Response secondResponse = UserApi.createUser(user);
        
        checkUserAlreadyExistsError(secondResponse);
    }
    
    @Test
    @DisplayName("Register a user with missing email")
    @Description("Тест проверяет, что попытка регистрации пользователя без указания адреса электронной почты возвращает ошибку валидации")
    public void testRegisterUserWithMissingEmail() {
        User user = TestDataGenerator.generateUserWithMissingEmail();
        
        Response response = UserApi.createUser(user);
        
        checkMissingFieldsError(response);
    }
    
    @Test
    @DisplayName("Register a user with missing password")
    @Description("Тест проверяет, что попытка регистрации пользователя без указания пароля возвращает ошибку валидации")
    public void testRegisterUserWithMissingPassword() {
        User user = TestDataGenerator.generateUserWithMissingPassword();
        
        Response response = UserApi.createUser(user);
        
        checkMissingFieldsError(response);
    }
    
    @Test
    @DisplayName("Register a user with missing name")
    @Description("Тест проверяет, что попытка регистрации пользователя без указания имени возвращает ошибку валидации")
    public void testRegisterUserWithMissingName() {
        User user = TestDataGenerator.generateUserWithMissingName();
        
        Response response = UserApi.createUser(user);
        
        checkMissingFieldsError(response);
    }
    
    @Step("Проверка успешной регистрации")
    private void checkSuccessfulRegistration(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", notNullValue())
                .body("user.name", notNullValue())
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }
    
    @Step("Проверка ошибки существующего пользователя")
    private void checkUserAlreadyExistsError(Response response) {
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }
    
    @Step("Проверка ошибки отсутствия обязательных полей")
    private void checkMissingFieldsError(Response response) {
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
    
    @Step("Извлечение токена доступа")
    private void extractAccessToken(Response response) {
        if (response.getStatusCode() == 200) {
            accessToken = response.jsonPath().getString("accessToken");
        }
    }
    
    @After
    public void tearDown() {
        deleteUser();
    }
    
    @Step("Удаление пользователя")
    private void deleteUser() {
        if (accessToken != null) {
            UserApi.deleteUser(accessToken).then()
                   .statusCode(anyOf(is(SC_ACCEPTED), is(SC_NOT_FOUND)));
        }
    }
}
