package com.ridelink.ride_service.exception;

public class DriverServiceUnavailableException extends RuntimeException {

    public DriverServiceUnavailableException(String message) {
        super(message);
    }

    public DriverServiceUnavailableException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}