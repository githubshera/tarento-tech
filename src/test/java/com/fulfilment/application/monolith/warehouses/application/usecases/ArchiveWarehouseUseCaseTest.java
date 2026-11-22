
package com.fulfilment.application.monolith.warehouses.application.usecases;

import com.fulfilment.application.monolith.warehouses.application.ports.out.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveWarehouseUseCaseTest {

    @Mock
    WarehouseStore warehouseStore;

    @InjectMocks
    ArchiveWarehouseUseCase archiveWarehouseUseCase;

    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.id = 1L;
        warehouse.businessUnitCode = "MWH.001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 10;
    }

    @Test
    void shouldArchiveWarehouseSuccessfully() {

        archiveWarehouseUseCase.archive(warehouse);

        assertNotNull(warehouse.archivedAt);

        verify(warehouseStore).update(warehouse);
    }

    @Test
    void shouldThrowExceptionWhenWarehouseIsNull() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> archiveWarehouseUseCase.archive(null));

        assertEquals("Warehouse not found", exception.getMessage());

        verify(warehouseStore, never()).update(any());
    }

    @Test
    void shouldThrowExceptionWhenWarehouseAlreadyArchived() {

        warehouse.archivedAt = LocalDateTime.now();

        LocalDateTime originalArchivedAt = warehouse.archivedAt;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> archiveWarehouseUseCase.archive(warehouse));

        assertEquals("Warehouse is already archived",
                exception.getMessage());

        assertEquals(originalArchivedAt, warehouse.archivedAt);

        verify(warehouseStore, never()).update(any());
    }
}
