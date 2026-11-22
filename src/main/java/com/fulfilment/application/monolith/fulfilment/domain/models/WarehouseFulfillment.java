
package com.fulfilment.application.monolith.fulfilment.domain.models;

public class WarehouseFulfillment {

    public Long id;
    public Long storeId;
    public Long warehouseId;
    public Long productId;

    public WarehouseFulfillment() {
    }

    public WarehouseFulfillment(
            Long storeId,
            Long warehouseId,
            Long productId) {
        this.storeId = storeId;
        this.warehouseId = warehouseId;
        this.productId = productId;
    }
}