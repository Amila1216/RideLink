package com.ridelink.fare_payment_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "final_fares")
public class FinalFare {

    @Id
    private String id;

    private String rideId;
    private double actualDistanceKm;
    private int actualDurationMinutes;

    private double baseFare;
    private double distanceFare;
    private double timeFare;
    private double finalFare;

    private String currency;

    public FinalFare() {
    }

    public FinalFare(
            String rideId,
            double actualDistanceKm,
            int actualDurationMinutes,
            double baseFare,
            double distanceFare,
            double timeFare,
            double finalFare,
            String currency) {

        this.rideId = rideId;
        this.actualDistanceKm = actualDistanceKm;
        this.actualDurationMinutes = actualDurationMinutes;
        this.baseFare = baseFare;
        this.distanceFare = distanceFare;
        this.timeFare = timeFare;
        this.finalFare = finalFare;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public String getRideId() {
        return rideId;
    }

    public double getActualDistanceKm() {
        return actualDistanceKm;
    }

    public int getActualDurationMinutes() {
        return actualDurationMinutes;
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

    public double getFinalFare() {
        return finalFare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public void setActualDistanceKm(double actualDistanceKm) {
        this.actualDistanceKm = actualDistanceKm;
    }

    public void setActualDurationMinutes(int actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
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

    public void setFinalFare(double finalFare) {
        this.finalFare = finalFare;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}