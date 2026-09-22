package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class FareEstimateRequest {

    @NotNull
    @DecimalMin(value = "0.1", message = "Distance must be greater than 0")
    private Double distanceKm;

    @NotNull
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer estimatedDurationMinutes;

    public FareEstimateRequest() {
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }
}