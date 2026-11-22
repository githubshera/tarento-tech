package com.fulfilment.application.monolith.warehouses.application.ports.out;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;

public interface LocationResolver {
  Location resolveByIdentifier(String identifier);
}
