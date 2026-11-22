
package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.application.ports.in.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.application.ports.in.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.application.ports.in.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.application.ports.out.WarehouseStore;
import com.warehouse.api.beans.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseResourceImplTest {

    @Mock
    WarehouseStore warehouseStore;

    @Mock
    CreateWarehouseOperation createWarehouseOperation;

    @Mock
    ReplaceWarehouseOperation replaceWarehouseOperation;

    @Mock
    ArchiveWarehouseOperation archiveWarehouseOperation;

    @InjectMocks
    WarehouseResourceImpl warehouseResource;

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
    createDomainWarehouse(Long id, String buCode) {

        var warehouse =
                new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

        warehouse.id = id;
        warehouse.businessUnitCode = buCode;
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;

        return warehouse;
    }

    private Warehouse createRequest() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH.TEST");
        warehouse.setLocation("ZWOLLE-001");
        warehouse.setCapacity(100);
        warehouse.setStock(50);
        return warehouse;
    }

    @Test
    void shouldListAllWarehouses() {
        when(warehouseStore.getAll()).thenReturn(List.of(
                createDomainWarehouse(1L, "MWH001"),
                createDomainWarehouse(2L, "MWH002")
        ));

        List<Warehouse> result =
                warehouseResource.listAllWarehousesUnits();

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("MWH001", result.get(0).getBusinessUnitCode());
        assertEquals("2", result.get(1).getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoWarehousesExist() {
        when(warehouseStore.getAll()).thenReturn(List.of());

        List<Warehouse> result =
                warehouseResource.listAllWarehousesUnits();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCreateWarehouse() {
        doAnswer(invocation -> {
            var warehouse = invocation
                    .getArgument(0,
                            com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);
            warehouse.id = 10L;
            return null;
        }).when(createWarehouseOperation).create(any());

        Warehouse result =
                warehouseResource.createANewWarehouseUnit(createRequest());

        assertEquals("10", result.getId());
        assertEquals("MWH.TEST", result.getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.getLocation());
        assertEquals(100, result.getCapacity());
        assertEquals(50, result.getStock());

        verify(createWarehouseOperation).create(any());
    }

    @Test
    void shouldGetWarehouseById() {
        when(warehouseStore.findWarehouseById(1L))
                .thenReturn(createDomainWarehouse(1L, "MWH001"));

        Warehouse result =
                warehouseResource.getAWarehouseUnitByID("1");

        assertEquals("1", result.getId());
        assertEquals("MWH001", result.getBusinessUnitCode());
    }

    @Test
    void shouldThrow404ForInvalidWarehouseId() {
        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> warehouseResource.getAWarehouseUnitByID("ABC")
        );

        assertEquals(404, exception.getResponse().getStatus());
        verifyNoInteractions(warehouseStore);
    }

    @Test
    void shouldThrow404WhenWarehouseDoesNotExist() {
        when(warehouseStore.findWarehouseById(999L)).thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> warehouseResource.getAWarehouseUnitByID("999")
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void shouldArchiveWarehouse() {
        var warehouse = createDomainWarehouse(1L, "MWH001");

        when(warehouseStore.findWarehouseById(1L))
                .thenReturn(warehouse);

        warehouseResource.archiveAWarehouseUnitByID("1");

        verify(archiveWarehouseOperation).archive(warehouse);
    }

    @Test
    void shouldThrow404WhenArchivingInvalidId() {
        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> warehouseResource.archiveAWarehouseUnitByID("ABC")
        );

        assertEquals(404, exception.getResponse().getStatus());
        verifyNoInteractions(warehouseStore);
        verifyNoInteractions(archiveWarehouseOperation);
    }

    @Test
    void shouldThrow404WhenArchivingMissingWarehouse() {
        when(warehouseStore.findWarehouseById(999L)).thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> warehouseResource.archiveAWarehouseUnitByID("999")
        );

        assertEquals(404, exception.getResponse().getStatus());
        verifyNoInteractions(archiveWarehouseOperation);
    }

    @Test
    void shouldReplaceWarehouseUsingPathBusinessUnitCode() {
        Warehouse request = createRequest();
        request.setBusinessUnitCode("REQUEST.CODE");
        request.setId("100");

        doAnswer(invocation -> {
            var warehouse = invocation.getArgument(
                    0,
                    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class
            );

            // ID must be null before the replacement operation creates it.
            assertNull(warehouse.id);

            warehouse.id = 20L;
            return null;
        }).when(replaceWarehouseOperation).replace(any());

        Warehouse result =
                warehouseResource.replaceTheCurrentActiveWarehouse(
                        "PATH.CODE", request);

        ArgumentCaptor<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse>
                captor = ArgumentCaptor.forClass(
                com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);

        verify(replaceWarehouseOperation).replace(captor.capture());

        var passedWarehouse = captor.getValue();

        assertEquals("PATH.CODE", passedWarehouse.businessUnitCode);
        assertEquals("20", result.getId());
        assertEquals("PATH.CODE", result.getBusinessUnitCode());
    }
}