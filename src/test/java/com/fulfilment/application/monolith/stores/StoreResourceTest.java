
package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class StoreResourceTest {

    private Long createStore(String name) {

        Number id = given()
                .contentType("application/json")
                .body("{\"name\":\"" + name +
                        "\",\"quantityProductsInStock\":10}")
                .when()
                .post("/store")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        return id.longValue();
    }

    @Test
    void shouldCreateStoreSuccessfully() {

        createStore("UTStoreCreate");

        given()
                .when()
                .get("/store")
                .then()
                .statusCode(200)
                .body(containsString("UTStoreCreate"));
    }

    @Test
    void shouldGetExistingStore() {

        Long id = createStore("UTStoreGet");

        given()
                .when()
                .get("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("UTStoreGet"));
    }

    @Test
    void shouldReturn404WhenStoreDoesNotExist() {

        given()
                .when()
                .get("/store/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldRejectStoreCreationWhenIdIsProvided() {

        given()
                .contentType("application/json")
                .body("{\"id\":999,\"name\":\"UTStoreInvalid\"}")
                .when()
                .post("/store")
                .then()
                .statusCode(422);
    }


    @Test
    void shouldUpdateStoreSuccessfully() {

        Long id = createStore("UTStorePut");

        given()
                .contentType("application/json")
                .body("{\"name\":\"UTStorePutUpdated\","
                        + "\"quantityProductsInStock\":25}")
                .when()
                .put("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("UTStorePutUpdated"))
                .body("quantityProductsInStock", equalTo(25));
    }

    @Test
    void shouldRejectUpdateWhenStoreNameIsMissing() {

        Long id = createStore("UTStorePutInvalid");

        given()
                .contentType("application/json")
                .body("{\"quantityProductsInStock\":20}")
                .when()
                .put("/store/" + id)
                .then()
                .statusCode(422);
    }

    @Test
    void shouldPatchStoreSuccessfully() {

        Long id = createStore("UTStorePatch");

        given()
                .contentType("application/json")
                .body("{\"name\":\"UTStorePatchUpdated\","
                        + "\"quantityProductsInStock\":30}")
                .when()
                .patch("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("UTStorePatchUpdated"))
                .body("quantityProductsInStock", equalTo(30));
    }

    @Test
    void shouldDeleteStoreSuccessfully() {

        Long id = createStore("UTStoreDelete");

        given()
                .when()
                .delete("/store/" + id)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/store/" + id)
                .then()
                .statusCode(404);
    }
}
