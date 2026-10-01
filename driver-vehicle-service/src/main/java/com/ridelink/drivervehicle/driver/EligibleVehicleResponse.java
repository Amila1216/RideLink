package com.ridelink.drivervehicle.driver;

import com.ridelink.drivervehicle.vehicle.VehicleType;

public record EligibleVehicleResponse(Long vehicleId, VehicleType vehicleType) {
}