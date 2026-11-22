
package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.application.ports.out.WarehouseResolver;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@ApplicationScoped
public class WarehouseResolverAdapter implements WarehouseResolver {

    private final EntityManager entityManager;

    public WarehouseResolverAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean existsActiveById(Long warehouseId) {
        DbWarehouse warehouse =
                entityManager.find(DbWarehouse.class, warehouseId);

        return warehouse != null && warehouse.archivedAt == null;
    }

    @Override
    public void lockById(Long warehouseId) {

        DbWarehouse warehouse = entityManager.find(
                DbWarehouse.class,
                warehouseId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (warehouse == null || warehouse.archivedAt != null) {
            throw new IllegalArgumentException(
                    "Active warehouse not found");
        }
    }
}