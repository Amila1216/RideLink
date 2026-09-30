package com.ridelink.ride_service.exception;

public class NoAvailableDriverException extends RuntimeException {

    public NoAvailableDriverException(String message) {
        super(message);
    }
}