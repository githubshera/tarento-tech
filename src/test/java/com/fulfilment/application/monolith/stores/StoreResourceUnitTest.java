
package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreResourceUnitTest {

    @Mock
    LegacyStoreManagerGateway legacyGateway;

    @Mock
    TransactionSynchronizationRegistry transactionRegistry;

    @InjectMocks
    StoreResource storeResource;

    @Test
    void shouldNotifyLegacySystemOnlyAfterCommit() {
        Store store = new Store("UnitTestStore");

        storeResource.executeAfterCommit(
                () -> legacyGateway.createStoreOnLegacySystem(store)
        );

        ArgumentCaptor<Synchronization> captor =
                ArgumentCaptor.forClass(Synchronization.class);

        verify(transactionRegistry)
                .registerInterposedSynchronization(captor.capture());

        verify(legacyGateway, never())
                .createStoreOnLegacySystem(store);

        captor.getValue().afterCompletion(Status.STATUS_COMMITTED);

        verify(legacyGateway)
                .createStoreOnLegacySystem(store);
    }

    @Test
    void shouldNotNotifyLegacySystemWhenTransactionRollsBack() {
        Store store = new Store("UnitTestStore");

        storeResource.executeAfterCommit(
                () -> legacyGateway.createStoreOnLegacySystem(store)
        );

        ArgumentCaptor<Synchronization> captor =
                ArgumentCaptor.forClass(Synchronization.class);

        verify(transactionRegistry)
                .registerInterposedSynchronization(captor.capture());

        captor.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);

        verify(legacyGateway, never())
                .createStoreOnLegacySystem(store);
    }

    @Test
    void shouldMapWebApplicationExceptionCorrectly() {

        StoreResource.ErrorMapper mapper =
                new StoreResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        Response response = mapper.toResponse(
                new WebApplicationException("Not found", 404));

        assertEquals(404, response.getStatus());
    }

    @Test
    void shouldMapIllegalArgumentExceptionTo400() {

        StoreResource.ErrorMapper mapper =
                new StoreResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        Response response = mapper.toResponse(
                new IllegalArgumentException("Invalid input"));

        assertEquals(400, response.getStatus());
    }

    @Test
    void shouldMapUnexpectedExceptionTo500() {

        StoreResource.ErrorMapper mapper =
                new StoreResource.ErrorMapper();

        mapper.objectMapper = new ObjectMapper();

        Response response = mapper.toResponse(
                new RuntimeException("Unexpected error"));

        assertEquals(500, response.getStatus());
    }
}
