
package com.fulfilment.application.monolith.fulfilment.application.usecases;

import com.fulfilment.application.monolith.fulfilment.application.ports.out.FulfillmentStore;
import com.fulfilment.application.monolith.fulfilment.application.ports.out.ProductResolver;
import com.fulfilment.application.monolith.fulfilment.application.ports.out.StoreResolver;
import com.fulfilment.application.monolith.fulfilment.application.ports.out.WarehouseResolver;
import com.fulfilment.application.monolith.fulfilment.domain.exceptions.DuplicateFulfillmentException;
import com.fulfilment.application.monolith.fulfilment.domain.models.WarehouseFulfillment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignFulfillmentUseCaseTest {

    @Mock
    FulfillmentStore fulfillmentStore;

    @Mock
    ProductResolver productResolver;

    @Mock
    StoreResolver storeResolver;

    @Mock
    WarehouseResolver warehouseResolver;

    private AssignFulfillmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AssignFulfillmentUseCase(
                fulfillmentStore,
                productResolver,
                storeResolver,
                warehouseResolver
        );
    }

    // Common mocks: only entity existence checks
    private void mockValidEntities() {
        when(storeResolver.existsById(1L)).thenReturn(true);
        when(productResolver.existsById(1L)).thenReturn(true);
        when(warehouseResolver.existsActiveById(1L)).thenReturn(true);
    }

    // Common mocks for a valid assignment
    private void mockValidAssignment() {
        when(fulfillmentStore.existsAssignment(1L, 1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countWarehousesForProductAndStore(1L, 1L))
                .thenReturn(0L);

        when(fulfillmentStore.existsWarehouseForStore(1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countWarehousesForStore(1L))
                .thenReturn(0L);

        when(fulfillmentStore.existsProductForWarehouse(1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countProductsForWarehouse(1L))
                .thenReturn(0L);
    }

    @Test
    void shouldAssignWarehouseSuccessfully() {
        mockValidEntities();
        mockValidAssignment();

        useCase.assign(1L, 1L, 1L);

        ArgumentCaptor<WarehouseFulfillment> captor =
                ArgumentCaptor.forClass(WarehouseFulfillment.class);

        verify(fulfillmentStore).save(captor.capture());

        WarehouseFulfillment saved = captor.getValue();

        assertEquals(1L, saved.storeId);
        assertEquals(1L, saved.warehouseId);
        assertEquals(1L, saved.productId);

        verify(storeResolver).lockById(1L);
        verify(warehouseResolver).lockById(1L);
    }

    @Test
    void shouldThrowExceptionWhenStoreNotFound() {
        when(storeResolver.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.assign(1L, 1L, 1L));

        assertEquals("Store not found", exception.getMessage());

        verifyNoInteractions(fulfillmentStore);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        when(storeResolver.existsById(1L)).thenReturn(true);
        when(productResolver.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.assign(1L, 1L, 1L));

        assertEquals("Product not found", exception.getMessage());

        verifyNoInteractions(fulfillmentStore);
    }

    @Test
    void shouldThrowExceptionWhenWarehouseNotFound() {
        when(storeResolver.existsById(1L)).thenReturn(true);
        when(productResolver.existsById(1L)).thenReturn(true);
        when(warehouseResolver.existsActiveById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.assign(1L, 1L, 1L));

        assertEquals(
                "Active warehouse not found",
                exception.getMessage());

        verifyNoInteractions(fulfillmentStore);
    }

    @Test
    void shouldThrowExceptionForDuplicateAssignment() {
        mockValidEntities();

        when(fulfillmentStore.existsAssignment(1L, 1L, 1L))
                .thenReturn(true);

        DuplicateFulfillmentException exception = assertThrows(
                DuplicateFulfillmentException.class,
                () -> useCase.assign(1L, 1L, 1L));

        assertEquals(
                "This fulfillment association already exists",
                exception.getMessage());

        verify(fulfillmentStore, never()).save(any());
    }

    @Test
    void shouldRejectMoreThanTwoWarehousesForProductInStore() {
        mockValidEntities();

        when(fulfillmentStore.existsAssignment(1L, 1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countWarehousesForProductAndStore(1L, 1L))
                .thenReturn(2L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.assign(1L, 1L, 1L));

        assertEquals(
                "A product can be fulfilled by maximum 2 warehouses per store",
                exception.getMessage());

        verify(fulfillmentStore, never()).save(any());
    }

    @Test
    void shouldRejectMoreThanThreeWarehousesForStore() {
        mockValidEntities();

        when(fulfillmentStore.existsAssignment(1L, 1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countWarehousesForProductAndStore(1L, 1L))
                .thenReturn(0L);

        when(fulfillmentStore.existsWarehouseForStore(1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countWarehousesForStore(1L))
                .thenReturn(3L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.assign(1L, 1L, 1L));

        assertEquals(
                "A store can be fulfilled by maximum 3 warehouses",
                exception.getMessage());

        verify(fulfillmentStore, never()).save(any());
    }

    @Test
    void shouldRejectMoreThanFiveProductsForWarehouse() {
        mockValidEntities();

        when(fulfillmentStore.existsAssignment(1L, 1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countWarehousesForProductAndStore(1L, 1L))
                .thenReturn(0L);

        when(fulfillmentStore.existsWarehouseForStore(1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countWarehousesForStore(1L))
                .thenReturn(0L);

        when(fulfillmentStore.existsProductForWarehouse(1L, 1L))
                .thenReturn(false);

        when(fulfillmentStore.countProductsForWarehouse(1L))
                .thenReturn(5L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.assign(1L, 1L, 1L));

        assertEquals(
                "A warehouse can store maximum 5 product types",
                exception.getMessage());

        verify(fulfillmentStore, never()).save(any());
    }
}