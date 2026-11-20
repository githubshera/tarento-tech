
package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class WarehouseFulfillmentService {

    private final WarehouseFulfillmentRepository fulfillmentRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    private final EntityManager entityManager;

    public WarehouseFulfillmentService(
            WarehouseFulfillmentRepository fulfillmentRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            EntityManager entityManager) {

        this.fulfillmentRepository = fulfillmentRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void assign(Long storeId, Long warehouseId, Long productId) {

        // 1. Validate Store
        Store store = entityManager.find(Store.class, storeId);

        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }

        // 2. Validate Product
        Product product = productRepository.findById(productId);

        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        // 3. Validate Warehouse
        DbWarehouse warehouse = warehouseRepository.findById(warehouseId);

        if (warehouse == null || warehouse.archivedAt != null) {
            throw new IllegalArgumentException("Active warehouse not found");
        }

        // 4. Acquire database locks to protect concurrent assignments
        entityManager.lock(store, LockModeType.PESSIMISTIC_WRITE);
        entityManager.lock(warehouse, LockModeType.PESSIMISTIC_WRITE);

        // 5. Prevent duplicate associations
        long existingAssignment = fulfillmentRepository.count(
                "store.id = ?1 and warehouse.id = ?2 and product.id = ?3",
                storeId, warehouseId, productId);

        if (existingAssignment > 0) {
            throw new IllegalArgumentException(
                    "This fulfillment association already exists");
        }

        // 6. Maximum 2 warehouses per Product per Store
        long warehousesForProduct =
                fulfillmentRepository.countWarehousesForProductAndStore(
                        storeId, productId);

        if (warehousesForProduct >= 2) {
            throw new IllegalArgumentException(
                    "A product can be fulfilled by maximum 2 warehouses per store");
        }

        // 7. Maximum 3 warehouses per Store
        long warehousesForStore =
                fulfillmentRepository.countWarehousesForStore(storeId);

        boolean newWarehouseForStore =
                fulfillmentRepository.count(
                        "store.id = ?1 and warehouse.id = ?2",
                        storeId, warehouseId) == 0;

        if (newWarehouseForStore && warehousesForStore >= 3) {
            throw new IllegalArgumentException(
                    "A store can be fulfilled by maximum 3 warehouses");
        }

        // 8. Maximum 5 product types per Warehouse
        long productsForWarehouse =
                fulfillmentRepository.countProductsForWarehouse(warehouseId);

        boolean newProductForWarehouse =
                fulfillmentRepository.count(
                        "warehouse.id = ?1 and product.id = ?2",
                        warehouseId, productId) == 0;

        if (newProductForWarehouse && productsForWarehouse >= 5) {
            throw new IllegalArgumentException(
                    "A warehouse can store maximum 5 product types");
        }

        // 9. Save fulfillment association
        WarehouseFulfillment fulfillment = new WarehouseFulfillment();

        fulfillment.store = store;
        fulfillment.warehouse = warehouse;
        fulfillment.product = product;

        fulfillmentRepository.persist(fulfillment);
    }
}