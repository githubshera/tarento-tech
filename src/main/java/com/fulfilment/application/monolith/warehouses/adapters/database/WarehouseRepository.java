package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.application.ports.out.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = DbWarehouse.fromWarehouse(warehouse);

    this.persistAndFlush(dbWarehouse);

    warehouse.id = dbWarehouse.id;
  }

  @Override
  public void update(Warehouse warehouse) {

    DbWarehouse dbWarehouse;

    if (warehouse.id != null) {
      dbWarehouse = this.findById(warehouse.id);
    } else {
      dbWarehouse = this
              .find("businessUnitCode = ?1 and archivedAt is null",
                      warehouse.businessUnitCode)
              .firstResult();
    }

    if (dbWarehouse == null) {
      throw new IllegalArgumentException(
              "Warehouse not found: " + warehouse.businessUnitCode);
    }

    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse dbWarehouse = this
            .find("businessUnitCode = ?1 and archivedAt is null",
                    warehouse.businessUnitCode)
            .firstResult();

    if (dbWarehouse != null) {
      this.delete(dbWarehouse);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = this
            .find("businessUnitCode = ?1 and archivedAt is null", buCode)
            .firstResult();

    return dbWarehouse == null ? null : dbWarehouse.toWarehouse();
  }

  @Override
  public Warehouse findWarehouseById(Long id) {
    DbWarehouse dbWarehouse = this.findByIdOptional(id).orElse(null);

    return dbWarehouse == null ? null : dbWarehouse.toWarehouse();
  }
}
