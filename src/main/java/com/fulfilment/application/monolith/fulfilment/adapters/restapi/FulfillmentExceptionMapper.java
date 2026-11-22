
package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.domain.exceptions.DuplicateFulfillmentException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class FulfillmentExceptionMapper
        implements ExceptionMapper<DuplicateFulfillmentException> {

    @Override
    public Response toResponse(DuplicateFulfillmentException exception) {

        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(
                        409,
                        exception.getMessage()))
                .build();
    }

    public static class ErrorResponse {
        public int code;
        public String error;

        public ErrorResponse(int code, String error) {
            this.code = code;
            this.error = error;
        }
    }
}