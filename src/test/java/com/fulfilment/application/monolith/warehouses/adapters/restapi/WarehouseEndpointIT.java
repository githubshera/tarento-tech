
package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class WarehouseEndpointIT {

  @Test
  public void shouldListAllWarehouses() {

    given()
            .when()
            .get("/warehouse")
            .then()
            .statusCode(200)
            .body(
                    containsString("MWH.001"),
                    containsString("MWH.012"),
                    containsString("MWH.023")
            );
  }

  @Test
  public void shouldGetWarehouseById() {

    given()
            .when()
            .get("/warehouse/1")
            .then()
            .statusCode(200)
            .body(
                    containsString("MWH.001"),
                    containsString("ZWOLLE-001")
            );
  }

  @Test
  public void shouldReturn404WhenWarehouseDoesNotExist() {

    given()
            .when()
            .get("/warehouse/99999")
            .then()
            .statusCode(404);
  }

  @Test
  public void shouldReturn404WhenWarehouseIdIsInvalid() {

    given()
            .when()
            .get("/warehouse/invalid")
            .then()
            .statusCode(404);
  }
}
