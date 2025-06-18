package com.stellarburgers.api;

import com.stellarburgers.client.ApiClient;
import com.stellarburgers.models.Order;
import io.qameta.allure.Step;
import io.restassured.response.Response;

public class OrderApi {
    
    @Step("Создание заказа с авторизацией")
    public static Response createOrder(Order order, String accessToken) {
        return ApiClient.getRequestSpecWithAuth(accessToken)
                .body(order)
                .when()
                .post("/orders");
    }
    
    @Step("Создание заказа без авторизации")
    public static Response createOrderWithoutAuth(Order order) {
        return ApiClient.getRequestSpec()
                .body(order)
                .when()
                .post("/orders");
    }
    
    @Step("Получение заказов с авторизацией")
    public static Response getOrders(String accessToken) {
        return ApiClient.getRequestSpecWithAuth(accessToken)
                .when()
                .get("/orders");
    }
    
    @Step("Получение заказов без авторизации")
    public static Response getOrdersWithoutAuth() {
        return ApiClient.getRequestSpec()
                .when()
                .get("/orders");
    }
}
