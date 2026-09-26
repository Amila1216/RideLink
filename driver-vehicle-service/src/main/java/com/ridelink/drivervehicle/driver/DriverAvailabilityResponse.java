package com.ridelink.drivervehicle.driver;

public record DriverAvailabilityResponse(
        Long driverId,
        DriverAvailability status
) {
    public static DriverAvailabilityResponse from(Driver driver) {
        return new DriverAvailabilityResponse(driver.getDriverId(), driver.getDriverAvailability());
    }
}