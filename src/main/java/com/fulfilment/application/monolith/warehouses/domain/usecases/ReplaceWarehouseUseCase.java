
package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {

    // 1. Find existing active warehouse
    Warehouse oldWarehouse =
            warehouseStore.findByBusinessUnitCode(
                    newWarehouse.businessUnitCode);

    if (oldWarehouse == null) {
      throw new IllegalArgumentException(
              "Active warehouse not found: "
                      + newWarehouse.businessUnitCode);
    }

    // 2. Validate required fields
    if (newWarehouse.location == null
            || newWarehouse.capacity == null
            || newWarehouse.stock == null) {
      throw new IllegalArgumentException(
              "Required warehouse fields are missing");
    }

    // 3. New capacity must accommodate old stock
    if (newWarehouse.capacity < oldWarehouse.stock) {
      throw new IllegalArgumentException(
              "New capacity cannot be less than existing stock");
    }

    // 4. Stock must remain unchanged
    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
      throw new IllegalArgumentException(
              "Replacement stock must match existing stock");
    }

    // 5. Validate new location
    Location location =
            locationResolver.resolveByIdentifier(
                    newWarehouse.location);

    if (location == null) {
      throw new IllegalArgumentException(
              "Location does not exist");
    }

    // 6. Get active warehouses, excluding the old warehouse
    List<Warehouse> activeWarehouses =
            warehouseStore.getAll().stream()
                    .filter(w -> w.archivedAt == null)
                    .filter(w -> !w.businessUnitCode.equals(
                            oldWarehouse.businessUnitCode))
                    .toList();

    // 7. Validate maximum warehouse count at new location
    List<Warehouse> locationWarehouses =
            activeWarehouses.stream()
                    .filter(w -> w.location.equals(
                            newWarehouse.location))
                    .toList();

    if (locationWarehouses.size()
            >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException(
              "Maximum number of warehouses reached for this location");
    }

    // 8. Validate total location capacity
    int totalCapacity = locationWarehouses.stream()
            .mapToInt(w -> w.capacity)
            .sum();

    if (totalCapacity + newWarehouse.capacity
            > location.maxCapacity) {
      throw new IllegalArgumentException(
              "Location maximum capacity exceeded");
    }

    // 9. Archive old warehouse
    LocalDateTime now = LocalDateTime.now();
    oldWarehouse.archivedAt = now;
    warehouseStore.update(oldWarehouse);

    // 10. Create new warehouse record
    newWarehouse.createdAt = now;
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}