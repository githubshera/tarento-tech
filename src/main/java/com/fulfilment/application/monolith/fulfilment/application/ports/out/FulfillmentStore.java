
package com.fulfilment.application.monolith.fulfilment.application.ports.out;

import com.fulfilment.application.monolith.fulfilment.domain.models.WarehouseFulfillment;

public interface FulfillmentStore {

    boolean existsAssignment(Long storeId, Long warehouseId, Long productId);

    long countWarehousesForProductAndStore(Long storeId, Long productId);

    long countWarehousesForStore(Long storeId);

    boolean existsWarehouseForStore(Long storeId, Long warehouseId);

    long countProductsForWarehouse(Long warehouseId);

    boolean existsProductForWarehouse(Long warehouseId, Long productId);

    void save(WarehouseFulfillment fulfillment);
}