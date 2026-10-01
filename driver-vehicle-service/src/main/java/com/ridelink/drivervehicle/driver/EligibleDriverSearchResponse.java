package com.ridelink.drivervehicle.driver;

import java.util.List;

public record EligibleDriverSearchResponse(List<EligibleDriverResponse> drivers) {
}