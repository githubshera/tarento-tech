
package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseFulfillmentServiceTest {

    @Mock
    WarehouseFulfillmentRepository fulfillmentRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    WarehouseRepository warehouseRepository;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    WarehouseFulfillmentService fulfillmentService;

    private Store store;
    private Product product;
    private DbWarehouse warehouse;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.id = 1L;

        product = new Product();
        product.id = 1L;

        warehouse = new DbWarehouse();
        warehouse.id = 1L;
        warehouse.businessUnitCode = "MWH.001";
        warehouse.capacity = 100;
        warehouse.stock = 10;
    }

    private void mockValidEntities() {
        when(entityManager.find(Store.class, 1L))
                .thenReturn(store);

        when(productRepository.findById(1L))
                .thenReturn(product);

        when(warehouseRepository.findById(1L))
                .thenReturn(warehouse);
    }

    @Test
    void shouldAssignWarehouseSuccessfully() {

        mockValidEntities();

        fulfillmentService.assign(1L, 1L, 1L);

        ArgumentCaptor<WarehouseFulfillment> captor =
                ArgumentCaptor.forClass(WarehouseFulfillment.class);

        verify(fulfillmentRepository).persist(captor.capture());

        WarehouseFulfillment saved = captor.getValue();

        assertEquals(store, saved.store);
        assertEquals(warehouse, saved.warehouse);
        assertEquals(product, saved.product);

        verify(entityManager).lock(store, LockModeType.PESSIMISTIC_WRITE);
        verify(entityManager).lock(warehouse, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void shouldThrowExceptionWhenStoreNotFound() {

        when(entityManager.find(Store.class, 1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.assign(1L, 1L, 1L));

        assertEquals("Store not found", exception.getMessage());

        verify(fulfillmentRepository, never())
                .persist(any(WarehouseFulfillment.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        when(entityManager.find(Store.class, 1L)).thenReturn(store);
        when(productRepository.findById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.assign(1L, 1L, 1L));

        assertEquals("Product not found", exception.getMessage());

        verify(fulfillmentRepository, never())
        .persist(any(WarehouseFulfillment.class));
    }

    @Test
    void shouldThrowExceptionWhenWarehouseNotFound() {

        when(entityManager.find(Store.class, 1L)).thenReturn(store);
        when(productRepository.findById(1L)).thenReturn(product);
        when(warehouseRepository.findById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.assign(1L, 1L, 1L));

        assertEquals("Active warehouse not found", exception.getMessage());

        verify(fulfillmentRepository, never())
                .persist(any(WarehouseFulfillment.class));
    }

    @Test
    void shouldThrowExceptionWhenWarehouseIsArchived() {

        warehouse.archivedAt = LocalDateTime.now();
        mockValidEntities();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.assign(1L, 1L, 1L));

        assertEquals("Active warehouse not found", exception.getMessage());

        verify(fulfillmentRepository, never())
        .persist(any(WarehouseFulfillment.class));
    }

    @Test
    void shouldThrowExceptionForDuplicateAssignment() {

        mockValidEntities();

        when(fulfillmentRepository.count(
                "store.id = ?1 and warehouse.id = ?2 and product.id = ?3",
                1L, 1L, 1L)).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.assign(1L, 1L, 1L));

        assertEquals(
                "This fulfillment association already exists",
                exception.getMessage());

        verify(fulfillmentRepository, never())
        .persist(any(WarehouseFulfillment.class));
    }

    @Test
    void shouldRejectMoreThanTwoWarehousesForProductInStore() {

        mockValidEntities();

        when(fulfillmentRepository.countWarehousesForProductAndStore(1L, 1L))
                .thenReturn(2L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.assign(1L, 1L, 1L));

        assertEquals(
                "A product can be fulfilled by maximum 2 warehouses per store",
                exception.getMessage());

        verify(fulfillmentRepository, never())
        .persist(any(WarehouseFulfillment.class));
    }

    @Test
    void shouldRejectMoreThanThreeWarehousesForStore() {

        mockValidEntities();
        when(fulfillmentRepository.count(
                "store.id = ?1 and warehouse.id = ?2 and product.id = ?3",
                1L, 1L, 1L)).thenReturn(0L);

        when(fulfillmentRepository.countWarehousesForStore(1L))
                .thenReturn(3L);

        when(fulfillmentRepository.count(
                "store.id = ?1 and warehouse.id = ?2",
                1L, 1L)).thenReturn(0L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.assign(1L, 1L, 1L));

        assertEquals(
                "A store can be fulfilled by maximum 3 warehouses",
                exception.getMessage());

        verify(fulfillmentRepository, never())
        .persist(any(WarehouseFulfillment.class));
    }

    @Test
    void shouldRejectMoreThanFiveProductsForWarehouse() {

        mockValidEntities();
        when(fulfillmentRepository.count(
                "store.id = ?1 and warehouse.id = ?2",
                1L, 1L)).thenReturn(0L);
        when(fulfillmentRepository.count(
                "store.id = ?1 and warehouse.id = ?2 and product.id = ?3",
                1L, 1L, 1L)).thenReturn(0L);

        when(fulfillmentRepository.countProductsForWarehouse(1L))
                .thenReturn(5L);

        when(fulfillmentRepository.count(
                "warehouse.id = ?1 and product.id = ?2",
                1L, 1L)).thenReturn(0L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.assign(1L, 1L, 1L));

        assertEquals(
                "A warehouse can store maximum 5 product types",
                exception.getMessage());

        verify(fulfillmentRepository, never())
        .persist(any(WarehouseFulfillment.class));
    }
}
