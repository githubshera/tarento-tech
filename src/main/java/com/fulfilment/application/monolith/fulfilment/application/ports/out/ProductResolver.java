
package com.fulfilment.application.monolith.fulfilment.application.ports.out;

public interface ProductResolver {

    boolean existsById(Long productId);
}