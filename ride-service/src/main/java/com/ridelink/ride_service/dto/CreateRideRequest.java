package com.ridelink.ride_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRideRequest(

        @NotBlank(message = "Pickup location is required")
        String pickupLocation,

        @NotBlank(message = "Dropoff location is required")
        String dropoffLocation

) {
}