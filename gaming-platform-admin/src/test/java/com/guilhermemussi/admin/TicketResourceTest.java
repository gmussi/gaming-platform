package com.guilhermemussi.admin;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TicketResourceTest extends ContainerMongoTest {
    static String username = UUID.randomUUID().toString();
    static String password = UUID.randomUUID().toString();

    @Test
    public void testGetTicketAuthenticated() {
        // first, register a new user
        String token = given()
                .formParam("username", username)
                .formParam("password", password)
                .when().post("auth/register")
                .then()
                .statusCode(200)
                .extract().asString();

        // use token to get
        String ticket = given()
                .header("Authorization", "Bearer " + token)
                .when().get("/ticket")
                .then()
                .statusCode(200)
                .extract().asString();
    }

    @Test
    public void testGetTicketUnauthenticated() {
        // trying to obtain a ticket without authentication must fail
        given()
                .when().get("/ticket")
                .then()
                .statusCode(401);
    }
}
