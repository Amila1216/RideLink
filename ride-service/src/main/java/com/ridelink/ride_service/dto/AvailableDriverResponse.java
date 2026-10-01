package com.ridelink.ride_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AvailableDriverResponse(
        Long driverId,
        String driverAvailability
) {
}