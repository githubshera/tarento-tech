
package com.fulfilment.application.monolith.products;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductUsingDefaultConstructor() {
        Product product = new Product();

        assertNull(product.id);
        assertNull(product.name);
        assertNull(product.description);
        assertNull(product.price);
        assertEquals(0, product.stock);
    }

    @Test
    void shouldCreateProductUsingNameConstructor() {
        Product product = new Product("Laptop");

        assertNull(product.id);
        assertEquals("Laptop", product.name);
        assertNull(product.description);
        assertNull(product.price);
        assertEquals(0, product.stock);
    }

    @Test
    void shouldAllowSettingProductFields() {
        Product product = new Product("Mobile");

        product.description = "Android mobile";
        product.price = new BigDecimal("25000.00");
        product.stock = 20;

        assertEquals("Mobile", product.name);
        assertEquals("Android mobile", product.description);
        assertEquals(new BigDecimal("25000.00"), product.price);
        assertEquals(20, product.stock);
    }
}
