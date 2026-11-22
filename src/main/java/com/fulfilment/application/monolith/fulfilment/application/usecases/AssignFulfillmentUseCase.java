
package com.fulfilment.application.monolith.fulfilment.application.usecases;

import com.fulfilment.application.monolith.fulfilment.application.ports.in.AssignFulfillmentOperation;
import com.fulfilment.application.monolith.fulfilment.application.ports.out.FulfillmentStore;
import com.fulfilment.application.monolith.fulfilment.application.ports.out.ProductResolver;
import com.fulfilment.application.monolith.fulfilment.application.ports.out.StoreResolver;
import com.fulfilment.application.monolith.fulfilment.application.ports.out.WarehouseResolver;
import com.fulfilment.application.monolith.fulfilment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfilment.domain.validators.FulfillmentValidator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AssignFulfillmentUseCase implements AssignFulfillmentOperation {

    private final FulfillmentStore fulfillmentStore;
    private final ProductResolver productResolver;
    private final StoreResolver storeResolver;
    private final WarehouseResolver warehouseResolver;

    private final FulfillmentValidator fulfillmentValidator =
            new FulfillmentValidator();

    public AssignFulfillmentUseCase(
            FulfillmentStore fulfillmentStore,
            ProductResolver productResolver,
            StoreResolver storeResolver,
            WarehouseResolver warehouseResolver) {

        this.fulfillmentStore = fulfillmentStore;
        this.productResolver = productResolver;
        this.storeResolver = storeResolver;
        this.warehouseResolver = warehouseResolver;
    }

    @Override
    @Transactional
    public void assign(Long storeId, Long warehouseId, Long productId) {

        // 1. Validate Store
        if (!storeResolver.existsById(storeId)) {
            throw new IllegalArgumentException("Store not found");
        }

        // 2. Validate Product
        if (!productResolver.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }

        // 3. Validate active Warehouse
        if (!warehouseResolver.existsActiveById(warehouseId)) {
            throw new IllegalArgumentException("Active warehouse not found");
        }

        // 4. Acquire locks to protect concurrent assignments
        storeResolver.lockById(storeId);
        warehouseResolver.lockById(warehouseId);

        // Recheck warehouse status after acquiring the lock
        if (!warehouseResolver.existsActiveById(warehouseId)) {
            throw new IllegalArgumentException("Active warehouse not found");
        }

        // 5. Prevent duplicate assignments
        boolean assignmentExists =
                fulfillmentStore.existsAssignment(
                        storeId, warehouseId, productId);

        fulfillmentValidator.validateNoDuplicateAssignment(
                assignmentExists);

        // 6. Maximum 2 warehouses per Product per Store
        long warehousesForProduct =
                fulfillmentStore.countWarehousesForProductAndStore(
                        storeId, productId);

        fulfillmentValidator.validateWarehousesForProduct(
                warehousesForProduct);

        // 7. Maximum 3 warehouses per Store
        boolean newWarehouseForStore =
                !fulfillmentStore.existsWarehouseForStore(
                        storeId, warehouseId);

        long warehousesForStore =
                fulfillmentStore.countWarehousesForStore(storeId);

        fulfillmentValidator.validateWarehousesForStore(
                newWarehouseForStore, warehousesForStore);

        // 8. Maximum 5 product types per Warehouse
        boolean newProductForWarehouse =
                !fulfillmentStore.existsProductForWarehouse(
                        warehouseId, productId);

        long productsForWarehouse =
                fulfillmentStore.countProductsForWarehouse(warehouseId);

        fulfillmentValidator.validateProductsForWarehouse(
                newProductForWarehouse, productsForWarehouse);

        // 9. Save fulfillment assignment
        WarehouseFulfillment fulfillment =
                new WarehouseFulfillment(
                        storeId, warehouseId, productId);

        fulfillmentStore.save(fulfillment);
    }
}