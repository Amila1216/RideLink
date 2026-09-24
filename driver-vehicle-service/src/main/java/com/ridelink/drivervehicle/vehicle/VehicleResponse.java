package com.ridelink.drivervehicle.vehicle;

public record VehicleResponse(
        Long vehicleId,
        Long driverId,
        String make,
        String model,
        Integer year,
        String licensePlate,
        String color,
        VehicleType vehicleType,
        String registrationNumber
) {
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getVehicleId(),
                vehicle.getDriver().getDriverId(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getLicensePlate(),
                vehicle.getColor(),
                vehicle.getVehicleType(),
                vehicle.getRegistrationNumber()
        );
    }
}
