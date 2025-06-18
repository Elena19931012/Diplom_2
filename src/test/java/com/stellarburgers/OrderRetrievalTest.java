package com.stellarburgers;

import com.stellarburgers.api.OrderApi;
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

public class OrderRetrievalTest {
    
    private User testUser;
    private String accessToken;
    
    @Before
    public void setUp() {
        testUser = TestDataGenerator.generateUniqueUser();
        createTestUser();
    }
    
    @Test
    @DisplayName("Get orders for authorized user")
    @Description("Тест проверяет, что авторизованный пользователь может успешно получить историю своих заказов со всеми ожидаемыми полями данных")
    public void testGetOrdersForAuthorizedUser() {
        Response response = OrderApi.getOrders(accessToken);
        
        checkSuccessfulOrdersRetrieval(response);
    }
    
    @Test
    @DisplayName("Get orders for unauthorized user")
    @Description("Тест проверяет, что попытка получить заказы без авторизации приводит к ошибке 401 с соответствующим сообщением об ошибке")
    public void testGetOrdersForUnauthorizedUser() {
        Response response = OrderApi.getOrdersWithoutAuth();
        
        checkUnauthorizedError(response);
    }
    
    @Step("Создание тестового пользователя")
    private void createTestUser() {
        Response response = UserApi.createUser(testUser);
        accessToken = response.jsonPath().getString("accessToken");
    }
    
    @Step("Проверка успешного получения заказов")
    private void checkSuccessfulOrdersRetrieval(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("total", notNullValue())
                .body("totalToday", notNullValue());
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
