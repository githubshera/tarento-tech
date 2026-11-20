
package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();

  @Test
  void shouldReturnLocationWhenIdentifierExists() {

    // When
    Location location =
            locationGateway.resolveByIdentifier("ZWOLLE-001");

    // Then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  void shouldReturnNullWhenIdentifierDoesNotExist() {

    // When
    Location location =
            locationGateway.resolveByIdentifier("INVALID-001");

    // Then
    assertNull(location);
  }

  @Test
  void shouldReturnNullWhenIdentifierIsNull() {

    // When
    Location location =
            locationGateway.resolveByIdentifier(null);

    // Then
    assertNull(location);
  }
}
