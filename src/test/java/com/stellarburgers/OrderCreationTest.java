package com.stellarburgers;

import com.stellarburgers.client.ApiClient;
import com.stellarburgers.models.Order;
import com.stellarburgers.models.User;
import com.stellarburgers.utils.TestDataGenerator;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderCreationTest {
    
    private User testUser;
    private String accessToken;
    private static final String VALID_INGREDIENT_1 = "60d3b41abdacab0026a733c6";
    private static final String VALID_INGREDIENT_2 = "60d3b41abdacab0026a733c7";
    private static final String INVALID_INGREDIENT = "invalid_hash";
    
    @Before
    public void setUp() {
        testUser = TestDataGenerator.generateUniqueUser();
        createTestUser();
    }
    
    @Test
    @DisplayName("Create order with authorization and valid ingredients")
    public void testCreateOrderWithAuthAndIngredients() {
        Order order = new Order(Arrays.asList(VALID_INGREDIENT_1, VALID_INGREDIENT_2));
        
        Response response = createOrderWithAuth(order);
        
        checkSuccessfulOrderCreation(response);
    }
    
    @Test
    @DisplayName("Create order without authorization")
    public void testCreateOrderWithoutAuth() {
        Order order = new Order(Arrays.asList(VALID_INGREDIENT_1, VALID_INGREDIENT_2));
        
        Response response = createOrderWithoutAuth(order);
        
        checkSuccessfulOrderCreation(response);
    }
    
    @Test
    @DisplayName("Create order without ingredients")
    public void testCreateOrderWithoutIngredients() {
        Order order = new Order(Collections.emptyList());
        
        Response response = createOrderWithAuth(order);
        
        checkMissingIngredientsError(response);
    }
    
    @Test
    @DisplayName("Create order with invalid ingredient hash")
    public void testCreateOrderWithInvalidIngredient() {
        Order order = new Order(Arrays.asList(INVALID_INGREDIENT));
        
        Response response = createOrderWithAuth(order);
        
        checkInvalidIngredientError(response);
    }
    
    @Step("Create test user")
    private void createTestUser() {
        Response response = ApiClient.getRequestSpec()
                .body(testUser)
                .when()
                .post("/auth/register");
        
        accessToken = response.jsonPath().getString("accessToken");
    }
    
    @Step("Create order with authorization")
    private Response createOrderWithAuth(Order order) {
        return ApiClient.getRequestSpecWithAuth(accessToken)
                .body(order)
                .when()
                .post("/orders");
    }
    
    @Step("Create order without authorization")
    private Response createOrderWithoutAuth(Order order) {
        return ApiClient.getRequestSpec()
                .body(order)
                .when()
                .post("/orders");
    }
    
    @Step("Check successful order creation")
    private void checkSuccessfulOrderCreation(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }
    
    @Step("Check missing ingredients error")
    private void checkMissingIngredientsError(Response response) {
        response.then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }
    
    @Step("Check invalid ingredient error")
    private void checkInvalidIngredientError(Response response) {
        response.then()
                .statusCode(500);
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
