package com.ridelink.drivervehicle.driver;

public record SimulatedLocationResponse(
        Long driverId,
        Double latitude,
        Double longitude
) {
    public static SimulatedLocationResponse from(Driver driver) {
        return new SimulatedLocationResponse(
                driver.getDriverId(),
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude()
        );
    }
}