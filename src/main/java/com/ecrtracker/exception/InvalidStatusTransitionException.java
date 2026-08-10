package com.ecrtracker.exception;

public class InvalidStatusTransitionException
        extends RuntimeException {

    public InvalidStatusTransitionException(
            String currentStatus,
            String newStatus) {

        super(
            "Invalid status transition: "
            + currentStatus
            + " -> "
            + newStatus
        );
    }
}