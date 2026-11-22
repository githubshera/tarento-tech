
package com.fulfilment.application.monolith.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static final String BASE_PACKAGE =
            "com.fulfilment.application.monolith";

    private final JavaClasses classes =
            new ClassFileImporter().importPackages(BASE_PACKAGE);

    @Test
    void warehouseDomainShouldNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage(
                        "com.fulfilment.application.monolith.warehouses.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(
                        "com.fulfilment.application.monolith.warehouses.adapters..")
                .check(classes);
    }

    @Test
    void warehouseDomainShouldNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage(
                        "com.fulfilment.application.monolith.warehouses.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(
                        "com.fulfilment.application.monolith.warehouses.application..")
                .check(classes);
    }


    @Test
    void fulfillmentDomainShouldNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage(
                        "com.fulfilment.application.monolith.fulfilment.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(
                        "com.fulfilment.application.monolith.fulfilment.adapters..")
                .check(classes);
    }

    @Test
    void fulfillmentDomainShouldNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage(
                        "com.fulfilment.application.monolith.fulfilment.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(
                        "com.fulfilment.application.monolith.fulfilment.application..")
                .check(classes);
    }

    @Test
    void warehouseRestAdapterShouldNotDependOnUseCaseImplementation() {
        noClasses()
                .that().resideInAPackage("..warehouses.adapters.restapi..")
                .should().dependOnClassesThat()
                .resideInAPackage("..warehouses.application.usecases..")
                .check(classes);
    }

    @Test
    void fulfillmentRestAdapterShouldNotDependOnUseCaseImplementation() {
        noClasses()
                .that().resideInAPackage("..fulfilment.adapters.restapi..")
                .should().dependOnClassesThat()
                .resideInAPackage("..fulfilment.application.usecases..")
                .check(classes);
    }

    @Test
    void warehouseDatabaseAdapterShouldNotDependOnUseCaseImplementation() {
        noClasses()
                .that().resideInAPackage("..warehouses.adapters.database..")
                .should().dependOnClassesThat()
                .resideInAPackage("..warehouses.application.usecases..")
                .check(classes);
    }

    @Test
    void fulfillmentDatabaseAdapterShouldNotDependOnUseCaseImplementation() {
        noClasses()
                .that().resideInAPackage("..fulfilment.adapters.database..")
                .should().dependOnClassesThat()
                .resideInAPackage("..fulfilment.application.usecases..")
                .check(classes);
    }

    @Test
    void warehouseUseCasesShouldNotDependOnDatabaseAdapters() {
        noClasses()
                .that().resideInAPackage("..warehouses.application.usecases..")
                .should().dependOnClassesThat()
                .resideInAPackage("..warehouses.adapters.database..")
                .check(classes);
    }

    @Test
    void fulfillmentUseCasesShouldNotDependOnDatabaseAdapters() {
        noClasses()
                .that().resideInAPackage("..fulfilment.application.usecases..")
                .should().dependOnClassesThat()
                .resideInAPackage("..fulfilment.adapters.database..")
                .check(classes);
    }

    @Test
    void warehouseDomainShouldNotDependOnFulfillment() {
        noClasses()
                .that().resideInAPackage(
                        "com.fulfilment.application.monolith.warehouses.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(
                        "com.fulfilment.application.monolith.fulfilment..")
                .check(classes);
    }

    @Test
    void fulfillmentDomainShouldNotDependOnWarehouse() {
        noClasses()
                .that().resideInAPackage(
                        "com.fulfilment.application.monolith.fulfilment.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(
                        "com.fulfilment.application.monolith.warehouses..")
                .check(classes);
    }
}