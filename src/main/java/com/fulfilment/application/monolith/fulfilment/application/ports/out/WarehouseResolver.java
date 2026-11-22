
package com.fulfilment.application.monolith.fulfilment.application.ports.out;

public interface WarehouseResolver {

    boolean existsActiveById(Long warehouseId);

    void lockById(Long warehouseId);
}