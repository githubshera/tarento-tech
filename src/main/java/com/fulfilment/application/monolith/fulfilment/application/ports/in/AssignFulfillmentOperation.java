package com.fulfilment.application.monolith.fulfilment.application.ports.in;

public interface AssignFulfillmentOperation {

    void assign(Long storeId, Long warehouseId, Long productId);
}

