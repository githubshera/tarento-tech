
package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.application.ports.out.FulfillmentStore;
import com.fulfilment.application.monolith.fulfilment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class WarehouseFulfillmentRepository
        implements PanacheRepository<WarehouseFulfillmentEntity>, FulfillmentStore {

    private final EntityManager entityManager;

    public WarehouseFulfillmentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean existsAssignment(
            Long storeId, Long warehouseId, Long productId) {

        return count(
                "store.id = ?1 and warehouse.id = ?2 and product.id = ?3",
                storeId, warehouseId, productId) > 0;
    }

    @Override
    public long countWarehousesForProductAndStore(
            Long storeId, Long productId) {

        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT f.warehouse.id) " +
                                "FROM WarehouseFulfillmentEntity f " +
                                "WHERE f.store.id = :storeId " +
                                "AND f.product.id = :productId",
                        Long.class)
                .setParameter("storeId", storeId)
                .setParameter("productId", productId)
                .getSingleResult();
    }

    @Override
    public long countWarehousesForStore(Long storeId) {

        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT f.warehouse.id) " +
                                "FROM WarehouseFulfillmentEntity f " +
                                "WHERE f.store.id = :storeId",
                        Long.class)
                .setParameter("storeId", storeId)
                .getSingleResult();
    }

    @Override
    public boolean existsWarehouseForStore(
            Long storeId, Long warehouseId) {

        return count(
                "store.id = ?1 and warehouse.id = ?2",
                storeId, warehouseId) > 0;
    }

    @Override
    public long countProductsForWarehouse(Long warehouseId) {

        return entityManager.createQuery(
                        "SELECT COUNT(DISTINCT f.product.id) " +
                                "FROM WarehouseFulfillmentEntity f " +
                                "WHERE f.warehouse.id = :warehouseId",
                        Long.class)
                .setParameter("warehouseId", warehouseId)
                .getSingleResult();
    }

    @Override
    public boolean existsProductForWarehouse(
            Long warehouseId, Long productId) {

        return count(
                "warehouse.id = ?1 and product.id = ?2",
                warehouseId, productId) > 0;
    }

    @Override
    public void save(WarehouseFulfillment fulfillment) {

        WarehouseFulfillmentEntity entity =
                new WarehouseFulfillmentEntity();

        entity.store = entityManager.getReference(Store.class, fulfillment.storeId);
        entity.warehouse = entityManager.getReference(
                DbWarehouse.class, fulfillment.warehouseId);
        entity.product = entityManager.getReference(
                Product.class, fulfillment.productId);

        entityManager.persist(entity);
    }
}