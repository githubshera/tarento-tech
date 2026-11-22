
package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.application.ports.out.StoreResolver;
import com.fulfilment.application.monolith.stores.Store;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@ApplicationScoped
public class StoreResolverAdapter implements StoreResolver {

    private final EntityManager entityManager;

    public StoreResolverAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean existsById(Long storeId) {
        return entityManager.find(Store.class, storeId) != null;
    }

    @Override
    public void lockById(Long storeId) {

        Store store = entityManager.find(
                Store.class,
                storeId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }
    }
}