package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FinalFareRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotNull
    @DecimalMin(value = "0.1", message = "Actual distance must be greater than 0")
    private Double actualDistanceKm;

    @NotNull
    @Min(value = 1, message = "Actual duration must be at least 1 minute")
    private Integer actualDurationMinutes;

    public FinalFareRequest() {
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public Double getActualDistanceKm() {
        return actualDistanceKm;
    }

    public void setActualDistanceKm(Double actualDistanceKm) {
        this.actualDistanceKm = actualDistanceKm;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }
}