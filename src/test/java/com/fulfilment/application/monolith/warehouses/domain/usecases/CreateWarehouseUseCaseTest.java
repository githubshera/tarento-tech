
package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWarehouseUseCaseTest {

    @Mock
    WarehouseStore warehouseStore;

    @Mock
    LocationResolver locationResolver;

    @InjectMocks
    CreateWarehouseUseCase createWarehouseUseCase;

    private Warehouse warehouse;
    private Location location;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.TEST01";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 20;
        warehouse.stock = 10;

        location = new Location("ZWOLLE-001", 2, 40);
    }

    @Test
    void shouldCreateWarehouseSuccessfully() {

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(Collections.emptyList());

        createWarehouseUseCase.create(warehouse);

        assertNotNull(warehouse.createdAt);

        verify(warehouseStore).create(warehouse);
    }

    @Test
    void shouldThrowExceptionWhenRequiredFieldsAreMissing() {

        warehouse.businessUnitCode = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        assertEquals("Required warehouse fields are missing",
                exception.getMessage());

        verifyNoInteractions(warehouseStore, locationResolver);
    }

    @Test
    void shouldThrowExceptionWhenStockExceedsCapacity() {

        warehouse.stock = 25;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        assertEquals("Stock cannot exceed warehouse capacity",
                exception.getMessage());

        verifyNoInteractions(warehouseStore, locationResolver);
    }

    @Test
    void shouldThrowExceptionWhenLocationDoesNotExist() {

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        assertEquals("Location does not exist", exception.getMessage());

        verify(warehouseStore, never()).create(any());
    }

    @Test
    void shouldThrowExceptionWhenBusinessUnitCodeAlreadyExists() {

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        Warehouse existingWarehouse = new Warehouse();
        existingWarehouse.businessUnitCode = "MWH.TEST01";
        existingWarehouse.location = "ZWOLLE-001";
        existingWarehouse.capacity = 10;
        existingWarehouse.stock = 5;

        when(warehouseStore.getAll())
                .thenReturn(List.of(existingWarehouse));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        assertEquals("Business unit code already exists",
                exception.getMessage());

        verify(warehouseStore, never()).create(any());
    }

    @Test
    void shouldThrowExceptionWhenMaximumWarehouseCountReached() {

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        Warehouse first = createExistingWarehouse("MWH.OLD01", 10);
        Warehouse second = createExistingWarehouse("MWH.OLD02", 10);

        when(warehouseStore.getAll())
                .thenReturn(List.of(first, second));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        assertEquals(
                "Maximum number of warehouses reached for this location",
                exception.getMessage());

        verify(warehouseStore, never()).create(any());
    }

    @Test
    void shouldThrowExceptionWhenLocationCapacityExceeded() {

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        Warehouse existing = createExistingWarehouse("MWH.OLD01", 30);

        when(warehouseStore.getAll())
                .thenReturn(List.of(existing));

        warehouse.capacity = 20;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWarehouseUseCase.create(warehouse));

        assertEquals("Location maximum capacity exceeded",
                exception.getMessage());

        verify(warehouseStore, never()).create(any());
    }

    @Test
    void shouldIgnoreArchivedWarehousesDuringValidation() {

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        Warehouse archived = createExistingWarehouse("MWH.OLD01", 30);
        archived.archivedAt = java.time.LocalDateTime.now();

        when(warehouseStore.getAll())
                .thenReturn(List.of(archived));

        createWarehouseUseCase.create(warehouse);

        verify(warehouseStore).create(warehouse);
    }

    private Warehouse createExistingWarehouse(String code, int capacity) {

        Warehouse existing = new Warehouse();
        existing.businessUnitCode = code;
        existing.location = "ZWOLLE-001";
        existing.capacity = capacity;
        existing.stock = 5;

        return existing;
    }
}
