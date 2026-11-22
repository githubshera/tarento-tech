
package com.fulfilment.application.monolith.fulfilment.domain.validators;

import com.fulfilment.application.monolith.fulfilment.domain.exceptions.DuplicateFulfillmentException;

public class FulfillmentValidator {

    public void validateNoDuplicateAssignment(boolean alreadyExists) {
        if (alreadyExists) {
            throw new DuplicateFulfillmentException(
                    "This fulfillment association already exists");
        }
    }

    public void validateWarehousesForProduct(long existingWarehouses) {
        if (existingWarehouses >= 2) {
            throw new IllegalArgumentException(
                    "A product can be fulfilled by maximum 2 warehouses per store");
        }
    }

    public void validateWarehousesForStore(
            boolean newWarehouse,
            long existingWarehouses) {

        if (newWarehouse && existingWarehouses >= 3) {
            throw new IllegalArgumentException(
                    "A store can be fulfilled by maximum 3 warehouses");
        }
    }

    public void validateProductsForWarehouse(
            boolean newProduct,
            long existingProducts) {

        if (newProduct && existingProducts >= 5) {
            throw new IllegalArgumentException(
                    "A warehouse can store maximum 5 product types");
        }
    }
}