
package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductResourceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductResource productResource;

    private Product createProduct(String name) {
        Product product = new Product();
        product.name = name;
        product.description = "Test product";
        product.stock = 10;
        return product;
    }

    @Test
    void shouldGetAllProducts() {
        List<Product> products = List.of(
                createProduct("Product A"),
                createProduct("Product B")
        );

        when(productRepository.listAll(any(Sort.class)))
                .thenReturn(products);

        List<Product> result = productResource.get();

        assertEquals(2, result.size());
        assertEquals("Product A", result.get(0).name);
        verify(productRepository).listAll(any(Sort.class));
    }

    @Test
    void shouldGetSingleProduct() {
        Product product = createProduct("Product A");

        when(productRepository.findById(1L)).thenReturn(product);

        Product result = productResource.getSingle(1L);

        assertEquals("Product A", result.name);
    }

    @Test
    void shouldThrow404WhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> productResource.getSingle(999L)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void shouldCreateProduct() {
        Product product = createProduct("New Product");

        Response response = productResource.create(product);

        assertEquals(201, response.getStatus());
        verify(productRepository).persist(product);
    }

    @Test
    void shouldRejectProductCreationWhenIdIsProvided() {
        Product product = createProduct("New Product");
        product.id = 10L;

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> productResource.create(product)
        );

        assertEquals(422, exception.getResponse().getStatus());
        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldUpdateProduct() {
        Product existing = createProduct("Old Product");
        Product updated = createProduct("Updated Product");

        updated.description = "Updated description";
        updated.stock = 50;

        when(productRepository.findById(1L)).thenReturn(existing);

        Product result = productResource.update(1L, updated);

        assertEquals("Updated Product", result.name);
        assertEquals("Updated description", result.description);
        assertEquals(50, result.stock);

        verify(productRepository).persist(existing);
    }

    @Test
    void shouldRejectUpdateWhenProductNameIsMissing() {
        Product updated = createProduct("Ignored");
        updated.name = null;

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> productResource.update(1L, updated)
        );

        assertEquals(422, exception.getResponse().getStatus());
        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldRejectUpdateWhenProductDoesNotExist() {
        Product updated = createProduct("Updated Product");

        when(productRepository.findById(999L)).thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> productResource.update(999L, updated)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void shouldDeleteProduct() {
        Product product = createProduct("Product A");

        when(productRepository.findById(1L)).thenReturn(product);

        Response response = productResource.delete(1L);

        assertEquals(204, response.getStatus());
        verify(productRepository).delete(product);
    }

    @Test
    void shouldRejectDeleteWhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> productResource.delete(999L)
        );

        assertEquals(404, exception.getResponse().getStatus());
        verify(productRepository, never()).delete(any());
    }

    @Test
    void shouldMapWebApplicationException() {
        ProductResource.ErrorMapper mapper =
                new ProductResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        Response response = mapper.toResponse(
                new WebApplicationException("Not found", 404)
        );

        assertEquals(404, response.getStatus());
    }

    @Test
    void shouldMapUnexpectedExceptionTo500() {
        ProductResource.ErrorMapper mapper =
                new ProductResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        Response response = mapper.toResponse(
                new RuntimeException("Unexpected error")
        );

        assertEquals(500, response.getStatus());
    }
}
