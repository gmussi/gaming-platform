package com.guilhermemussi.admin;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@Testcontainers
@QuarkusTest
public class AuthResourceTest {
    public static final Logger LOGGER = Logger.getLogger(AuthResourceTest.class.getName());

    static GenericContainer mongodb = new GenericContainer<>("mongo:4.2").withExposedPorts(27017);
    static {
        mongodb.start();
        System.setProperty("quarkus.mongodb.connection-string",
                "mongodb://" + mongodb.getContainerIpAddress() + ":" + mongodb.getFirstMappedPort());
    }
    static String username = UUID.randomUUID().toString();
    static String password = UUID.randomUUID().toString();

    String token;

    @Test
    public void testUsernameExists() {
        // no player created yet, this must return false and success
        given()
            .when().get("auth/available/" + username)
            .then()
                .statusCode(200)
                .body(is("false"));

    }

    @Test
    public void testRegister() {
        // registering the first user must always work
        String token = given()
            .formParam("username", username)
            .formParam("password", password)
            .when().post("auth/register")
            .then()
                .statusCode(200)
                .extract().asString();
        LOGGER.info(token);

        // this token must work on authenticating resource
        given()
            .header("Authorization", "Bearer " + token)
            .when().get("auth/me")
            .then()
                .statusCode(200)
                .body(is(username));

        // login with username must work
        token = given()
            .formParam("username", username)
            .formParam("password", password)
                .when().post("auth/login")
                .then()
                    .statusCode(200)
                    .extract().asString();

        // this token must work on authenticating resource
        given()
            .header("Authorization", "Bearer " + token)
            .when().get("auth/me")
            .then()
                .statusCode(200)
                .body(is(username));
    }

    @Test
    public void testGetPlayer() {
        // accessing an authenticated resource must fail
        given()
            .when().get("auth/me")
            .then()
                .statusCode(401); //unauthorized
    }
}
