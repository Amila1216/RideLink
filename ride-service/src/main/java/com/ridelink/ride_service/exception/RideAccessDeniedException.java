package com.ridelink.ride_service.exception;

public class RideAccessDeniedException extends RuntimeException {

    public RideAccessDeniedException(String message) {
        super(message);
    }
}