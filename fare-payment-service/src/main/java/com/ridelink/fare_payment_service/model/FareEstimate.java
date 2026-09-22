package com.ridelink.fare_payment_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fare_estimates")
public class FareEstimate {

    @Id
    private String id;

    private double distanceKm;
    private int estimatedDurationMinutes;

    private double baseFare;
    private double distanceFare;
    private double timeFare;
    private double totalEstimatedFare;

    private String currency;

    public FareEstimate() {
    }

    public FareEstimate(
            double distanceKm,
            int estimatedDurationMinutes,
            double baseFare,
            double distanceFare,
            double timeFare,
            double totalEstimatedFare,
            String currency) {

        this.distanceKm = distanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.baseFare = baseFare;
        this.distanceFare = distanceFare;
        this.timeFare = timeFare;
        this.totalEstimatedFare = totalEstimatedFare;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getDistanceFare() {
        return distanceFare;
    }

    public double getTimeFare() {
        return timeFare;
    }

    public double getTotalEstimatedFare() {
        return totalEstimatedFare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public void setDistanceFare(double distanceFare) {
        this.distanceFare = distanceFare;
    }

    public void setTimeFare(double timeFare) {
        this.timeFare = timeFare;
    }

    public void setTotalEstimatedFare(double totalEstimatedFare) {
        this.totalEstimatedFare = totalEstimatedFare;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}