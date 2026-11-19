package com.fulfilment.application.monolith.fulfilment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class WarehouseFulfillmentRepository
        implements PanacheRepository<WarehouseFulfillment> {

    @PersistenceContext
    EntityManager entityManager;

    public long countWarehousesForProductAndStore(Long storeId, Long productId) {
        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT f.warehouse.id) " +
                                "FROM WarehouseFulfillment f " +
                                "WHERE f.store.id = :storeId " +
                                "AND f.product.id = :productId",
                        Long.class)
                .setParameter("storeId", storeId)
                .setParameter("productId", productId)
                .getSingleResult();
    }

    public long countWarehousesForStore(Long storeId) {
        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT f.warehouse.id) " +
                                "FROM WarehouseFulfillment f " +
                                "WHERE f.store.id = :storeId",
                        Long.class)
                .setParameter("storeId", storeId)
                .getSingleResult();
    }

    public long countProductsForWarehouse(Long warehouseId) {
        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT f.product.id) " +
                                "FROM WarehouseFulfillment f " +
                                "WHERE f.warehouse.id = :warehouseId",
                        Long.class)
                .setParameter("warehouseId", warehouseId)
                .getSingleResult();
    }
}


