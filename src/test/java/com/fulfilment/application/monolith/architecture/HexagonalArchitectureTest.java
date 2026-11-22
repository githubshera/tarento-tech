
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
        ArchRule rule = noClasses()
                .that().resideInAPackage("..warehouses.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..warehouses.adapters..");

        rule.check(classes);
    }

    @Test
    void warehouseDomainShouldNotDependOnApplication() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..warehouses.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..warehouses.application..");

        rule.check(classes);
    }

    @Test
    void fulfillmentDomainShouldNotDependOnAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..fulfilment.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..fulfilment.adapters..");

        rule.check(classes);
    }

    @Test
    void fulfillmentDomainShouldNotDependOnApplication() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..fulfilment.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..fulfilment.application..");

        rule.check(classes);
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