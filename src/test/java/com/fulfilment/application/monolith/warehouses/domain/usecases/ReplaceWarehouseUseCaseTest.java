
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplaceWarehouseUseCaseTest {

    @Mock
    WarehouseStore warehouseStore;

    @Mock
    LocationResolver locationResolver;

    @InjectMocks
    ReplaceWarehouseUseCase replaceWarehouseUseCase;

    private Warehouse oldWarehouse;
    private Warehouse newWarehouse;
    private Location location;

    @BeforeEach
    void setUp() {
        oldWarehouse = new Warehouse();
        oldWarehouse.id = 1L;
        oldWarehouse.businessUnitCode = "MWH.001";
        oldWarehouse.location = "ZWOLLE-001";
        oldWarehouse.capacity = 100;
        oldWarehouse.stock = 10;

        newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "MWH.001";
        newWarehouse.location = "AMSTERDAM-001";
        newWarehouse.capacity = 50;
        newWarehouse.stock = 10;

        location = new Location("AMSTERDAM-001", 5, 100);
    }

    private void mockValidScenario() {
        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(oldWarehouse);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(List.of(oldWarehouse));
    }

    @Test
    void shouldReplaceWarehouseSuccessfully() {

        mockValidScenario();

        replaceWarehouseUseCase.replace(newWarehouse);

        assertNotNull(oldWarehouse.archivedAt);
        assertNotNull(newWarehouse.createdAt);
        assertNull(newWarehouse.archivedAt);

        verify(warehouseStore).update(oldWarehouse);
        verify(warehouseStore).create(newWarehouse);
    }

    @Test
    void shouldThrowExceptionWhenActiveWarehouseNotFound() {

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        assertEquals("Active warehouse not found: MWH.001",
                exception.getMessage());

        verify(warehouseStore, never()).create(any());
        verify(warehouseStore, never()).update(any());
    }

    @Test
    void shouldThrowExceptionWhenRequiredFieldsMissing() {

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(oldWarehouse);

        newWarehouse.location = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        assertEquals("Required warehouse fields are missing",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNewCapacityIsLessThanExistingStock() {

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(oldWarehouse);

        newWarehouse.capacity = 5;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        assertEquals("New capacity cannot be less than existing stock",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenReplacementStockIsDifferent() {

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(oldWarehouse);

        newWarehouse.stock = 20;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        assertEquals("Replacement stock must match existing stock",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLocationDoesNotExist() {

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(oldWarehouse);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        assertEquals("Location does not exist", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMaximumWarehouseCountReached() {

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(oldWarehouse);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);

        Warehouse first = createWarehouse("MWH.002", "AMSTERDAM-001", 10);
        Warehouse second = createWarehouse("MWH.003", "AMSTERDAM-001", 10);
        Warehouse third = createWarehouse("MWH.004", "AMSTERDAM-001", 10);
        Warehouse fourth = createWarehouse("MWH.005", "AMSTERDAM-001", 10);
        Warehouse fifth = createWarehouse("MWH.006", "AMSTERDAM-001", 10);

        when(warehouseStore.getAll())
                .thenReturn(List.of(
                        oldWarehouse, first, second, third, fourth, fifth));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        assertEquals(
                "Maximum number of warehouses reached for this location",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLocationCapacityExceeded() {

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(oldWarehouse);

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(location);

        Warehouse existing = createWarehouse(
                "MWH.002", "AMSTERDAM-001", 60);

        when(warehouseStore.getAll())
                .thenReturn(List.of(oldWarehouse, existing));

        newWarehouse.capacity = 50;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> replaceWarehouseUseCase.replace(newWarehouse));

        assertEquals("Location maximum capacity exceeded",
                exception.getMessage());
    }

    @Test
    void shouldIgnoreArchivedWarehousesDuringValidation() {

        mockValidScenario();

        Warehouse archived = createWarehouse(
                "MWH.ARCHIVED", "AMSTERDAM-001", 90);

        archived.archivedAt = java.time.LocalDateTime.now();

        when(warehouseStore.getAll())
                .thenReturn(List.of(oldWarehouse, archived));

        replaceWarehouseUseCase.replace(newWarehouse);

        verify(warehouseStore).update(oldWarehouse);
        verify(warehouseStore).create(newWarehouse);
    }

    private Warehouse createWarehouse(
            String code, String location, int capacity) {

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = code;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = 5;

        return warehouse;
    }
}
