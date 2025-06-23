package com.stellarburgers;

import com.stellarburgers.api.OrderApi;
import com.stellarburgers.api.UserApi;
import com.stellarburgers.models.Order;
import com.stellarburgers.models.User;
import com.stellarburgers.utils.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class OrderCreationTest {
    
    private User testUser;
    private String accessToken;
    private static final String VALID_INGREDIENT_1 = "61c0c5a71d1f82001bdaaa6c";
    private static final String VALID_INGREDIENT_2 = "61c0c5a71d1f82001bdaaa6d";
    
    private static final String INVALID_INGREDIENT = "invalid_hash";
    
    @Before
    public void setUp() {
        testUser = TestDataGenerator.generateUniqueUser();
        createTestUser();
    }
    
    @Test
    @DisplayName("Create order with authorization and valid ingredients")
    @Description("Тест проверяет возможность успешного создания заказа авторизованным пользователем с использованием действительных идентификаторов ингредиентов")
    public void testCreateOrderWithAuthAndIngredients() {
        Order order = new Order(Arrays.asList(VALID_INGREDIENT_1, VALID_INGREDIENT_2));
        
        Response response = OrderApi.createOrder(order, accessToken);
        
        checkSuccessfulOrderCreation(response);
    }
    
    @Test
    @DisplayName("Create order without authorization")
    @Description("Тест проверяет возможность создания заказа без авторизации с действительными ингредиентами")
    public void testCreateOrderWithoutAuth() {
        Order order = new Order(Arrays.asList(VALID_INGREDIENT_1, VALID_INGREDIENT_2));
        
        Response response = OrderApi.createOrderWithoutAuth(order);

        checkSuccessfulOrderCreation(response);
    }
    
    @Test
    @DisplayName("Create order without ingredients")
    @Description("Тест проверяет, что при попытке создания заказа без указания ингредиентов возвращается соответствующая ошибка")
    public void testCreateOrderWithoutIngredients() {
        Order order = new Order(Collections.emptyList());
        
        Response response = OrderApi.createOrder(order, accessToken);
        
        checkMissingIngredientsError(response);
    }
    
    @Test
    @DisplayName("Create order with invalid ingredient hash")
    @Description("Тест проверяет, что при попытке создания заказа с недействительными идентификаторами ингредиентов возвращается ошибка")
    public void testCreateOrderWithInvalidIngredient() {
        Order order = new Order(Arrays.asList(INVALID_INGREDIENT));
        
        Response response = OrderApi.createOrder(order, accessToken);
        
        checkInvalidIngredientError(response);
    }
    
    @Step("Создание тестового пользователя")
    private void createTestUser() {
        Response response = UserApi.createUser(testUser);
        accessToken = response.jsonPath().getString("accessToken");
    }
    
    @Step("Проверка успешного создания заказа")
    private void checkSuccessfulOrderCreation(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }
    
    @Step("Проверка ошибки отсутствия ингредиентов")
    private void checkMissingIngredientsError(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }
    
    @Step("Проверка ошибки недействительного ингредиента")
    private void checkInvalidIngredientError(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false));
    }
    
    @Step("Проверка ошибки некорректного запроса")
    private void checkBadRequestError(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false));
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
