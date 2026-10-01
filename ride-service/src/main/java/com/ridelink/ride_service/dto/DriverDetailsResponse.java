package com.ridelink.ride_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DriverDetailsResponse(
        Long driverId,
        String email,
        String driverAvailability
) {
}