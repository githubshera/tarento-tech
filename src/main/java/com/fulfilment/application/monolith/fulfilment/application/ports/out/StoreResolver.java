
package com.fulfilment.application.monolith.fulfilment.application.ports.out;

public interface StoreResolver {

    boolean existsById(Long storeId);

    void lockById(Long storeId);
}