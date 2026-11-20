
package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository warehouseRepository;

    private Warehouse createWarehouse(String buCode) {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = buCode;
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        warehouse.createdAt = LocalDateTime.now();
        return warehouse;
    }

    @Test
    @TestTransaction
    void shouldCreateWarehouseAndGenerateId() {
        Warehouse warehouse = createWarehouse("TEST.CREATE");

        warehouseRepository.create(warehouse);

        assertNotNull(warehouse.id);
        assertEquals("TEST.CREATE",
                warehouseRepository.findByBusinessUnitCode("TEST.CREATE")
                        .businessUnitCode);
    }

    @Test
    @TestTransaction
    void shouldGetAllWarehouses() {
        warehouseRepository.create(createWarehouse("TEST.ALL1"));
        warehouseRepository.create(createWarehouse("TEST.ALL2"));

        List<Warehouse> warehouses = warehouseRepository.getAll();

        assertTrue(warehouses.stream()
                .anyMatch(w -> "TEST.ALL1".equals(w.businessUnitCode)));

        assertTrue(warehouses.stream()
                .anyMatch(w -> "TEST.ALL2".equals(w.businessUnitCode)));
    }

    @Test
    @TestTransaction
    void shouldUpdateWarehouseById() {
        Warehouse warehouse = createWarehouse("TEST.UPDATE.ID");
        warehouseRepository.create(warehouse);

        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 200;
        warehouse.stock = 80;

        warehouseRepository.update(warehouse);

        Warehouse updated = warehouseRepository
                .findWarehouseById(warehouse.id);

        assertEquals("AMSTERDAM-001", updated.location);
        assertEquals(200, updated.capacity);
        assertEquals(80, updated.stock);
    }

    @Test
    @TestTransaction
    void shouldUpdateWarehouseByBusinessUnitCode() {
        Warehouse warehouse = createWarehouse("TEST.UPDATE.BU");
        warehouseRepository.create(warehouse);

        Warehouse update = createWarehouse("TEST.UPDATE.BU");
        update.capacity = 250;
        update.stock = 100;

        warehouseRepository.update(update);

        Warehouse updated = warehouseRepository
                .findByBusinessUnitCode("TEST.UPDATE.BU");

        assertEquals(250, updated.capacity);
        assertEquals(100, updated.stock);
    }

    @Test
    @TestTransaction
    void shouldThrowExceptionWhenUpdatingMissingWarehouse() {
        Warehouse warehouse = createWarehouse("TEST.MISSING");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> warehouseRepository.update(warehouse)
        );

        assertTrue(exception.getMessage().contains("Warehouse not found"));
    }

    @Test
    @TestTransaction
    void shouldRemoveWarehouse() {
        Warehouse warehouse = createWarehouse("TEST.REMOVE");
        warehouseRepository.create(warehouse);

        warehouseRepository.remove(warehouse);

        assertNull(warehouseRepository
                .findByBusinessUnitCode("TEST.REMOVE"));
    }

    @Test
    @TestTransaction
    void shouldFindWarehouseByBusinessUnitCode() {
        Warehouse warehouse = createWarehouse("TEST.FIND.BU");
        warehouseRepository.create(warehouse);

        Warehouse result = warehouseRepository
                .findByBusinessUnitCode("TEST.FIND.BU");

        assertNotNull(result);
        assertEquals(warehouse.id, result.id);
    }

    @Test
    @TestTransaction
    void shouldNotFindArchivedWarehouseByBusinessUnitCode() {
        Warehouse warehouse = createWarehouse("TEST.ARCHIVED");
        warehouseRepository.create(warehouse);

        warehouse.archivedAt = LocalDateTime.now();
        warehouseRepository.update(warehouse);

        assertNull(warehouseRepository
                .findByBusinessUnitCode("TEST.ARCHIVED"));
    }

    @Test
    @TestTransaction
    void shouldFindWarehouseById() {
        Warehouse warehouse = createWarehouse("TEST.FIND.ID");
        warehouseRepository.create(warehouse);

        Warehouse result = warehouseRepository
                .findWarehouseById(warehouse.id);

        assertNotNull(result);
        assertEquals("TEST.FIND.ID", result.businessUnitCode);
    }

    @Test
    @TestTransaction
    void shouldReturnNullWhenWarehouseIdDoesNotExist() {
        assertNull(warehouseRepository.findWarehouseById(-999L));
    }
}