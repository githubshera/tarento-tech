package com.fulfilment.application.monolith.warehouses.application.ports.in;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

public interface CreateWarehouseOperation {
  void create(Warehouse warehouse);
}
