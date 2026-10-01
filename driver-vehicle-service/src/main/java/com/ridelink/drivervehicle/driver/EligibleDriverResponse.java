package com.ridelink.drivervehicle.driver;

import java.util.List;

public record EligibleDriverResponse(
        Long driverId,
        DriverAvailability availability,
        List<EligibleVehicleResponse> vehicles
) {
}