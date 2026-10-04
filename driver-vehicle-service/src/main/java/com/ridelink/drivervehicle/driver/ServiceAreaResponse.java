package com.ridelink.drivervehicle.driver;

public record ServiceAreaResponse(
        Long driverId,
        String serviceArea,
        String serviceAreaDetails
) {
    public static ServiceAreaResponse from(Driver driver) {
        return new ServiceAreaResponse(driver.getDriverId(), driver.getServiceArea(), driver.getServiceAreaDetails());
    }
}