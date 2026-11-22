package com.fulfilment.application.monolith.fulfilment.domain.exceptions;

public class DuplicateFulfillmentException extends RuntimeException {

    public DuplicateFulfillmentException(String message) {
        super(message);
    }
}