
package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.application.ports.in.AssignFulfillmentOperation;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/fulfillments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WarehouseFulfillmentResource {

    @Inject
    AssignFulfillmentOperation assignFulfillmentOperation;

    @POST
    public Response assign(WarehouseFulfillmentRequest request) {

        assignFulfillmentOperation.assign(
                request.storeId,
                request.warehouseId,
                request.productId
        );

        return Response.status(Response.Status.CREATED).build();
    }
}