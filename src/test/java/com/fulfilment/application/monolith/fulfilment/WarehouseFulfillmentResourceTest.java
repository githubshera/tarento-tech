
package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.application.ports.in.AssignFulfillmentOperation;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WarehouseFulfillmentResourceTest {

    @Mock
    AssignFulfillmentOperation assignFulfillmentOperation;

    @InjectMocks
    WarehouseFulfillmentResource fulfillmentResource;

    @Test
    void shouldAssignWarehouseFulfillmentSuccessfully() {

        WarehouseFulfillmentRequest request =
                new WarehouseFulfillmentRequest();

        request.storeId = 1L;
        request.warehouseId = 2L;
        request.productId = 3L;

        Response response = fulfillmentResource.assign(request);

        assertEquals(201, response.getStatus());

        verify(assignFulfillmentOperation).assign(
                1L,
                2L,
                3L
        );
    }
}
