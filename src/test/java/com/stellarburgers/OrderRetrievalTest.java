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
    public void testGetOrdersForAuthorizedUser() {
        Response response = getOrdersWithAuth();
        
        checkSuccessfulOrdersRetrieval(response);
    }
    
    @Test
    @DisplayName("Get orders for unauthorized user")
    public void testGetOrdersForUnauthorizedUser() {
        Response response = getOrdersWithoutAuth();
        
        checkUnauthorizedError(response);
    }
    
    @Step("Create test user")
    private void createTestUser() {
        Response response = ApiClient.getRequestSpec()
                .body(testUser)
                .when()
                .post("/auth/register");
        
        accessToken = response.jsonPath().getString("accessToken");
    }
    
    @Step("Get orders with authorization")
    private Response getOrdersWithAuth() {
        return ApiClient.getRequestSpecWithAuth(accessToken)
                .when()
                .get("/orders");
    }
    
    @Step("Get orders without authorization")
    private Response getOrdersWithoutAuth() {
        return ApiClient.getRequestSpec()
                .when()
                .get("/orders");
    }
    
    @Step("Check successful orders retrieval")
    private void checkSuccessfulOrdersRetrieval(Response response) {
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("total", notNullValue())
                .body("totalToday", notNullValue());
    }
    
    @Step("Check unauthorized error")
    private void checkUnauthorizedError(Response response) {
        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
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
