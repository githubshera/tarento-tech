package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "warehouse_fulfillment",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"store_id", "warehouse_id", "product_id"}
        )
)
@Cacheable
public class WarehouseFulfillmentEntity {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    public Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    public DbWarehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;

    public WarehouseFulfillmentEntity() {
    }
}
