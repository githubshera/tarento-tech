
package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

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
    if (warehouse.businessUnitCode == null ||
            warehouse.location == null ||
            warehouse.capacity == null ||
            warehouse.stock == null) {
      throw new IllegalArgumentException("Required warehouse fields are missing");
    }

    // 2. Validate stock against warehouse capacity
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException(
              "Stock cannot exceed warehouse capacity");
    }

    // 3. Validate whether location exists
    Location location =
            locationResolver.resolveByIdentifier(warehouse.location);

    if (location == null) {
      throw new IllegalArgumentException("Location does not exist");
    }

    // 4. Get only active warehouses
    List<Warehouse> activeWarehouses = warehouseStore.getAll()
            .stream()
            .filter(w -> w.archivedAt == null)
            .toList();

    // 5. Validate unique business unit code
    boolean alreadyExists = activeWarehouses.stream()
            .anyMatch(w -> w.businessUnitCode.equals(
                    warehouse.businessUnitCode));

    if (alreadyExists) {
      throw new IllegalArgumentException(
              "Business unit code already exists");
    }

    // 6. Validate maximum warehouse count for location
    List<Warehouse> locationWarehouses = activeWarehouses.stream()
            .filter(w -> w.location.equals(warehouse.location))
            .toList();

    if (locationWarehouses.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException(
              "Maximum number of warehouses reached for this location");
    }

    // 7. Validate total location capacity
    int totalCapacity = locationWarehouses.stream()
            .mapToInt(w -> w.capacity)
            .sum();

    if (totalCapacity + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
              "Location maximum capacity exceeded");
    }

    // 8. Set creation timestamp
    warehouse.createdAt = LocalDateTime.now();

    // 9. Save warehouse
    warehouseStore.create(warehouse);
  }
}