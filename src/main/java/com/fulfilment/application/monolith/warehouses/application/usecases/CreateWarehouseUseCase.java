
package com.fulfilment.application.monolith.warehouses.application.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import com.fulfilment.application.monolith.warehouses.application.ports.in.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.application.ports.out.LocationResolver;
import com.fulfilment.application.monolith.warehouses.application.ports.out.WarehouseStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseValidator warehouseValidator =
          new WarehouseValidator();

  public CreateWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver) {

    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {

    // 1. Validate required values
    warehouseValidator.validateRequiredFields(warehouse);

    // 2. Validate stock against warehouse capacity
    warehouseValidator.validateStockCapacity(warehouse);

    // 3. Validate whether location exists
    Location location =
            locationResolver.resolveByIdentifier(warehouse.location);

    warehouseValidator.validateLocationExists(location);

    // 4. Get only active warehouses
    List<Warehouse> activeWarehouses = warehouseStore.getAll()
            .stream()
            .filter(w -> w.archivedAt == null)
            .toList();

    // 5. Validate unique business unit code
    warehouseValidator.validateUniqueBusinessUnitCode(
            activeWarehouses, warehouse);

    // 6. Get warehouses for the requested location
    List<Warehouse> locationWarehouses = activeWarehouses.stream()
            .filter(w -> w.location.equals(warehouse.location))
            .toList();

    // 7. Validate maximum warehouse count for location
    warehouseValidator.validateLocationWarehouseLimit(
            locationWarehouses, location);

    // 8. Validate total location capacity
    warehouseValidator.validateLocationCapacity(
            locationWarehouses, warehouse, location);

    // 9. Set creation timestamp
    warehouse.createdAt = LocalDateTime.now();

    // 10. Save warehouse
    warehouseStore.create(warehouse);
  }
}