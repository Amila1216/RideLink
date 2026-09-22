package com.ridelink.drivervehicle.vehicle;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotBlank(message = "Vehicle make is required")
        @Size(min = 2, max = 50, message = "Make must be between 2 and 50 characters")
        String make,

        @NotBlank(message = "Vehicle model is required")
        @Size(min = 2, max = 50, message = "Model must be between 2 and 50 characters")
        String model,

        @NotNull(message = "Vehicle year is required")
        @Min(value = 1900, message = "Vehicle year must be 1900 or later")
        Integer year,

        @NotBlank(message = "License plate is required")
        @Size(min = 2, max = 20, message = "License plate must be between 2 and 20 characters")
        String licensePlate,

        @NotBlank(message = "Vehicle color is required")
        @Size(min = 2, max = 30, message = "Color must be between 2 and 30 characters")
        String color,

        @NotNull(message = "Vehicle type is required")
        VehicleType vehicleType,

        @NotBlank(message = "Registration number is required")
        @Size(min = 2, max = 30, message = "Registration number must be between 2 and 30 characters")
        String registrationNumber
) {
}
