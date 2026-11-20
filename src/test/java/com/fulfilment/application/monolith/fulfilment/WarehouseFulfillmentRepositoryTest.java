
package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class WarehouseFulfillmentRepositoryTest {

    @Inject
    WarehouseFulfillmentRepository repository;

    @Inject
    EntityManager entityManager;

    @Test
    @TestTransaction
    void shouldCountDistinctWarehousesAndProducts() {

        // Create Store
        Store store = new Store("TEST.FULFILLMENT.STORE");
        entityManager.persist(store);

        // Create Products
        Product product1 = new Product("TEST.PRODUCT.1");
        Product product2 = new Product("TEST.PRODUCT.2");

        entityManager.persist(product1);
        entityManager.persist(product2);

        // Create Warehouses
        DbWarehouse warehouse1 = createWarehouse("TEST.WH.1");
        DbWarehouse warehouse2 = createWarehouse("TEST.WH.2");
        DbWarehouse warehouse3 = createWarehouse("TEST.WH.3");

        entityManager.persist(warehouse1);
        entityManager.persist(warehouse2);
        entityManager.persist(warehouse3);

        entityManager.flush();

        // Product 1 -> Warehouse 1 and Warehouse 2
        createFulfillment(store, warehouse1, product1);
        createFulfillment(store, warehouse2, product1);

        // Product 2 -> Warehouse 1 and Warehouse 3
        createFulfillment(store, warehouse1, product2);
        createFulfillment(store, warehouse3, product2);

        entityManager.flush();

        // Verify distinct warehouse count for Product 1 and Store
        assertEquals(
                2L,
                repository.countWarehousesForProductAndStore(
                        store.id, product1.id)
        );

        // Store uses 3 different warehouses
        assertEquals(
                3L,
                repository.countWarehousesForStore(store.id)
        );

        // Warehouse 1 contains 2 different products
        assertEquals(
                2L,
                repository.countProductsForWarehouse(warehouse1.id)
        );
    }

    private DbWarehouse createWarehouse(String code) {
        DbWarehouse warehouse = new DbWarehouse();
        warehouse.businessUnitCode = code;
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 20;
        return warehouse;
    }

    private void createFulfillment(
            Store store,
            DbWarehouse warehouse,
            Product product) {

        WarehouseFulfillment fulfillment = new WarehouseFulfillment();
        fulfillment.store = store;
        fulfillment.warehouse = warehouse;
        fulfillment.product = product;
        entityManager.persist(fulfillment);
    }
}