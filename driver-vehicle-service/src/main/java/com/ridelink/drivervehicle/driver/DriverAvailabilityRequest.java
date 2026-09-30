package com.ridelink.drivervehicle.driver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DriverAvailabilityRequest(
        @NotBlank(message = "Availability status is required")
        @Pattern(regexp = "AVAILABLE|UNAVAILABLE",
                message = "Availability status must be AVAILABLE or UNAVAILABLE")
        String status
) {
}