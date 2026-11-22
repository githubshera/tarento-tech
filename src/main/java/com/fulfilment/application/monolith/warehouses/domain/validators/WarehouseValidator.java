
package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

import java.util.List;
import java.util.Objects;

public class WarehouseValidator {

    // CREATE VALIDATIONS

    public void validateRequiredFields(Warehouse warehouse) {

        if (warehouse.businessUnitCode == null ||
                warehouse.location == null ||
                warehouse.capacity == null ||
                warehouse.stock == null) {

            throw new IllegalArgumentException(
                    "Required warehouse fields are missing");
        }
    }

    public void validateStockCapacity(Warehouse warehouse) {

        if (warehouse.stock > warehouse.capacity) {
            throw new IllegalArgumentException(
                    "Stock cannot exceed warehouse capacity");
        }
    }


    public void validateUniqueBusinessUnitCode(
            List<Warehouse> activeWarehouses,
            Warehouse warehouse) {

        boolean alreadyExists = activeWarehouses.stream()
                .anyMatch(w -> w.businessUnitCode.equals(
                        warehouse.businessUnitCode));

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "Business unit code already exists");
        }
    }

    public void validateLocationWarehouseLimit(
            List<Warehouse> locationWarehouses,
            Location location) {

        if (locationWarehouses.size() >= location.maxNumberOfWarehouses) {
            throw new IllegalArgumentException(
                    "Maximum number of warehouses reached for this location");
        }
    }

    public void validateLocationCapacity(
            List<Warehouse> locationWarehouses,
            Warehouse warehouse,
            Location location) {

        int totalCapacity = locationWarehouses.stream()
                .mapToInt(w -> w.capacity)
                .sum();

        if (totalCapacity + warehouse.capacity > location.maxCapacity) {
            throw new IllegalArgumentException(
                    "Location maximum capacity exceeded");
        }
    }

    // REPLACE VALIDATIONS

    public void validateReplacementRequiredFields(Warehouse warehouse) {

        if (warehouse.location == null ||
                warehouse.capacity == null ||
                warehouse.stock == null) {

            throw new IllegalArgumentException(
                    "Required warehouse fields are missing");
        }
    }

    public void validateReplacementCapacity(
            Warehouse newWarehouse,
            Warehouse oldWarehouse) {

        if (newWarehouse.capacity < oldWarehouse.stock) {
            throw new IllegalArgumentException(
                    "New capacity cannot be less than existing stock");
        }
    }

    public void validateLocationExists(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location does not exist");
        }
    }

    public void validateWarehouseExists(Warehouse warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("Warehouse not found");
        }
    }

    public void validateWarehouseNotArchived(Warehouse warehouse) {
        if (warehouse.archivedAt != null) {
            throw new IllegalArgumentException("Warehouse is already archived");
        }
    }

    public void validateReplacementStock(
            Warehouse newWarehouse,
            Warehouse oldWarehouse) {

        if (!Objects.equals(newWarehouse.stock, oldWarehouse.stock)) {
            throw new IllegalArgumentException(
                    "Replacement stock must match existing stock");
        }
    }
}