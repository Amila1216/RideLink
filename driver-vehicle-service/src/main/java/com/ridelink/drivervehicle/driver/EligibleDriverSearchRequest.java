package com.ridelink.drivervehicle.driver;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ParameterObject
public record EligibleDriverSearchRequest(
        @NotBlank(message = "Service area is required")
        @Size(max = 100, message = "Service area must not exceed 100 characters")
        @Schema(description = "Requested service area; compared without case sensitivity", example = "Colombo", requiredMode = Schema.RequiredMode.REQUIRED)
        String serviceArea,

        @Schema(description = "When true, only include drivers with a complete simulated location", defaultValue = "false")
        Boolean requireLocation
) {
    public EligibleDriverSearchRequest {
        if (requireLocation == null) {
            requireLocation = false;
        }
    }
}