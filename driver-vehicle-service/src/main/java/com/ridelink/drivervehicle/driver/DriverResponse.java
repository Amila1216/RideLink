package com.ridelink.drivervehicle.driver;

public record DriverResponse(
        Long driverId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String licenseNumber,
        DriverAvailability driverAvailability
) {
    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getDriverId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getEmail(),
                driver.getPhoneNumber(),
                driver.getLicenseNumber(),
                driver.getDriverAvailability()
        );
    }
}
