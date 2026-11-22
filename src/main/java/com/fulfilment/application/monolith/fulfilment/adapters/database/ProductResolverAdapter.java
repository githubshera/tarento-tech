
package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.application.ports.out.ProductResolver;
import com.fulfilment.application.monolith.products.Product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class ProductResolverAdapter implements ProductResolver {

    private final EntityManager entityManager;

    public ProductResolverAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean existsById(Long productId) {
        return entityManager.find(Product.class, productId) != null;
    }
}