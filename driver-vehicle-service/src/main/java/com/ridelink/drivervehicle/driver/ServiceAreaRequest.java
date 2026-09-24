package com.ridelink.drivervehicle.driver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceAreaRequest(
        @NotBlank(message = "Service area is required")
        @Size(max = 100, message = "Service area must not exceed 100 characters")
        String serviceArea,

        @Size(max = 255, message = "Service area details must not exceed 255 characters")
        String serviceAreaDetails
) {
}