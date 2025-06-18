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
    @Description("Тест проверяет, что авторизованный пользователь может успешно обновить свой адрес электронной почты, и что изменения правильно сохраняются в системе")
    public void testUpdateEmailWithAuth() {
        String newEmail = "new" + System.currentTimeMillis() + "@test.com";
        User updateUser = new User(newEmail, null, null);
        
        Response response = UserApi.updateUser(updateUser, accessToken);
        
        checkSuccessfulEmailUpdate(response, newEmail);
    }
    
    @Test
    @DisplayName("Update user name with authorization")
    @Description("Тест проверяет, что авторизованный пользователь может успешно обновить своё имя, и что изменения правильно сохраняются в системе")
    public void testUpdateNameWithAuth() {
        String newName = "NewName" + System.currentTimeMillis();
        User updateUser = new User(testUser.getEmail(), null, newName);
        
        Response response = UserApi.updateUser(updateUser, accessToken);
        
        checkSuccessfulUpdate(response, testUser.getEmail(), newName);
    }
    
    @Test
    @DisplayName("Update user password with authorization")
    @Description("Тест проверяет, что авторизованный пользователь может успешно обновить свой пароль, и что изменения правильно сохраняются в системе")
    public void testUpdatePasswordWithAuth() {
        String newPassword = "newpassword123";
        User updateUser = new User(testUser.getEmail(), newPassword, testUser.getName());
        
        Response response = UserApi.updateUser(updateUser, accessToken);
        
        checkSuccessfulUpdate(response, testUser.getEmail(), testUser.getName());
    }
    
    @Test
    @DisplayName("Update user without authorization")
    @Description("Тест проверяет, что попытка обновить информацию пользователя без надлежащей авторизации приводит к соответствующей ошибке")
    public void testUpdateUserWithoutAuth() {
        String newName = "NewName" + System.currentTimeMillis();
        User updateUser = new User(null, null, newName);
        
        Response response = UserApi.updateUserWithoutAuth(updateUser);
        
        checkUnauthorizedError(response);
    }
    
    @Step("Создание тестового пользователя")
    private void createTestUser() {
        Response response = UserApi.createUser(testUser);
        accessToken = response.jsonPath().getString("accessToken");
    }
    
    @Step("Проверка успешного обновления электронной почты")
    private void checkSuccessfulEmailUpdate(Response response, String expectedEmail) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(expectedEmail));
    }
    
    @Step("Проверка ошибки доступа")
    private void checkPermissionError(Response response) {
        response.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false));
    }
    
    @Step("Проверка успешного обновления")
    private void checkSuccessfulUpdate(Response response, String expectedEmail, String expectedName) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(expectedEmail))
                .body("user.name", equalTo(expectedName));
    }
    
    @Step("Проверка ошибки неавторизованного доступа")
    private void checkUnauthorizedError(Response response) {
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
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
