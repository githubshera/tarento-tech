package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject
  private WarehouseStore warehouseStore;
  @Inject
  private CreateWarehouseOperation createWarehouseOperation;

  @Inject
  private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Inject
  private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseStore.getAll()
            .stream()
            .map(this::toWarehouseResponse)
            .toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {

    var warehouse = toWarehouseDomain(data);

    createWarehouseOperation.create(warehouse);

    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {

    Long warehouseId;

    try {
      warehouseId = Long.valueOf(id);
    } catch (NumberFormatException e) {
      throw new WebApplicationException(
              "Invalid warehouse ID: " + id, 404);
    }

    var warehouse = warehouseStore.findWarehouseById(warehouseId);

    if (warehouse == null) {
      throw new WebApplicationException(
              "Warehouse not found with ID: " + id, 404);
    }

    return toWarehouseResponse(warehouse);
  }


  @Override
  public void archiveAWarehouseUnitByID(String id) {

    Long warehouseId;

    try {
      warehouseId = Long.valueOf(id);
    } catch (NumberFormatException e) {
      throw new WebApplicationException(
              "Invalid warehouse ID: " + id, 404);
    }

    var warehouse = warehouseStore.findWarehouseById(warehouseId);

    if (warehouse == null) {
      throw new WebApplicationException(
              "Warehouse not found with ID: " + id, 404);
    }

    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode,
          @NotNull Warehouse data) {

    var warehouse = toWarehouseDomain(data);

    // Path parameter is the authoritative business unit code
    warehouse.businessUnitCode = businessUnitCode;

    // Database should generate a new ID
    warehouse.id = null;

    replaceWarehouseOperation.replace(warehouse);

    return toWarehouseResponse(warehouse);
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
  toWarehouseDomain(Warehouse data) {

    var warehouse =
            new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();

    return warehouse;
  }

  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {

    var response = new Warehouse();

    response.setId(warehouse.id == null ? null : warehouse.id.toString());
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
