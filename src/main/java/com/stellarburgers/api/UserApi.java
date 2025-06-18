package com.stellarburgers.api;

import com.stellarburgers.client.ApiClient;
import com.stellarburgers.models.User;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class UserApi {
    
    @Step("Регистрация нового пользователя")
    public static Response createUser(User user) {
        // Log the request details to Allure
        Allure.addAttachment("Request Body", "application/json", 
                "{ \"email\": \"" + user.getEmail() + 
                "\", \"password\": \"" + user.getPassword() + 
                "\", \"name\": \"" + user.getName() + "\" }", ".json");
        
        Response response = ApiClient.getRequestSpec()
                .body(user)
                .when()
                .post("/auth/register");
        
        // Log the response details to Allure
        Allure.addAttachment("Response Body", "application/json", response.getBody().asString(), ".json");
        
        return response;
    }
    
    @Step("Вход пользователя")
    public static Response loginUser(User user) {
        return ApiClient.getRequestSpec()
                .body(user)
                .when()
                .post("/auth/login");
    }
    
    @Step("Обновление пользователя с авторизацией")
    public static Response updateUser(User user, String accessToken) {
        // Log the request details to Allure
        Allure.addAttachment("Update Request", "application/json", 
                "{ \"email\": \"" + user.getEmail() + 
                "\", \"password\": \"" + user.getPassword() + 
                "\", \"name\": \"" + user.getName() + "\" }", ".json");
        
        Response response = ApiClient.getRequestSpecWithAuth(accessToken)
                .body(user)
                .when()
                .patch("/auth/user");
        
        // Log the response details to Allure
        Allure.addAttachment("Update Response", "application/json", response.getBody().asString(), ".json");
        
        return response;
    }
    
    @Step("Обновление пользователя без авторизации")
    public static Response updateUserWithoutAuth(User user) {
        return ApiClient.getRequestSpec()
                .body(user)
                .when()
                .patch("/auth/user");
    }
    
    @Step("Удаление пользователя")
    public static Response deleteUser(String accessToken) {
        return ApiClient.getRequestSpecWithAuth(accessToken)
                .when()
                .delete("/auth/user");
    }
}
