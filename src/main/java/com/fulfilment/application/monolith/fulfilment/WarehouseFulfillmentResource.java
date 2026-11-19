package com.fulfilment.application.monolith.fulfilment;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;

@Path("/fulfillments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WarehouseFulfillmentResource {

    @Inject
    WarehouseFulfillmentService fulfillmentService;

    @POST
    @Transactional
    public Response assign(WarehouseFulfillmentRequest request) {

        fulfillmentService.assign(
                request.storeId,
                request.warehouseId,
                request.productId
        );

        return Response.status(Response.Status.CREATED).build();
    }
}

