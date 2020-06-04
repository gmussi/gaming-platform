package com.guilhermemussi.gameplay;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class AuthResourceTest extends ContainerMongoTest {
    public static final Logger LOGGER = Logger.getLogger(AuthResourceTest.class.getName());

    static String username = UUID.randomUUID().toString();
    static String password = UUID.randomUUID().toString();

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
        // accessing an authenticated resource without token must fail
        given()
            .when().get("auth/me")
            .then()
                .statusCode(401); //unauthorized
    }
}
