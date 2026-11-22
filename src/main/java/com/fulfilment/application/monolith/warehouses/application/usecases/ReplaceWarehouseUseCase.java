
package com.fulfilment.application.monolith.warehouses.application.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import com.fulfilment.application.monolith.warehouses.application.ports.in.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.application.ports.out.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.application.ports.out.LocationResolver;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  private final WarehouseValidator warehouseValidator =
          new WarehouseValidator();

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
    warehouseValidator.validateReplacementRequiredFields(
            newWarehouse);

    // 3. New capacity must accommodate old stock
    warehouseValidator.validateReplacementCapacity(
            newWarehouse, oldWarehouse);

    // 4. Replacement stock must remain unchanged
    warehouseValidator.validateReplacementStock(
            newWarehouse, oldWarehouse);

    // 5. Validate new location
    Location location =
            locationResolver.resolveByIdentifier(
                    newWarehouse.location);

    warehouseValidator.validateLocationExists(location);

    // 6. Get active warehouses, excluding the old warehouse
    List<Warehouse> activeWarehouses =
            warehouseStore.getAll().stream()
                    .filter(w -> w.archivedAt == null)
                    .filter(w -> !w.businessUnitCode.equals(
                            oldWarehouse.businessUnitCode))
                    .toList();

    // 7. Get warehouses for the new location
    List<Warehouse> locationWarehouses =
            activeWarehouses.stream()
                    .filter(w -> w.location.equals(
                            newWarehouse.location))
                    .toList();

    // 8. Validate maximum warehouse count
    warehouseValidator.validateLocationWarehouseLimit(
            locationWarehouses, location);

    // 9. Validate total location capacity
    warehouseValidator.validateLocationCapacity(
            locationWarehouses, newWarehouse, location);

    // 10. Archive old warehouse
    LocalDateTime now = LocalDateTime.now();

    oldWarehouse.archivedAt = now;
    warehouseStore.update(oldWarehouse);

    // 11. Create new warehouse record
    newWarehouse.createdAt = now;
    newWarehouse.archivedAt = null;

    warehouseStore.create(newWarehouse);
  }
}