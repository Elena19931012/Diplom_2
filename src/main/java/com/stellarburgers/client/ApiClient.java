package com.stellarburgers.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class ApiClient {
    
    public static final String BASE_URL = "https://stellarburgers.nomoreparties.site/api";
    
    static {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new AllureRestAssured());
    }
    
    public static RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .contentType(ContentType.JSON);
    }
    
    public static RequestSpecification getRequestSpecWithAuth(String accessToken) {
        return getRequestSpec()
                .header("Authorization", accessToken);
    }
}
