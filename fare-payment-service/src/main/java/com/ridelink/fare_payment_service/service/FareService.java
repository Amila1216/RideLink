package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.model.FareEstimate;
import com.ridelink.fare_payment_service.repository.FareEstimateRepository;
import org.springframework.stereotype.Service;

@Service
public class FareService {

    private static final double BASE_FARE = 100.0;
    private static final double DISTANCE_RATE = 30.0;
    private static final double TIME_RATE = 5.0;

    private final FareEstimateRepository fareEstimateRepository;

    public FareService(FareEstimateRepository fareEstimateRepository) {
        this.fareEstimateRepository = fareEstimateRepository;
    }

    public FareEstimate calculateFare(FareEstimateRequest request) {

        double distanceFare =
                request.getDistanceKm() * DISTANCE_RATE;

        double timeFare =
                request.getEstimatedDurationMinutes() * TIME_RATE;

        double totalEstimatedFare =
                BASE_FARE + distanceFare + timeFare;

        FareEstimate fareEstimate = new FareEstimate(
                request.getDistanceKm(),
                request.getEstimatedDurationMinutes(),
                BASE_FARE,
                distanceFare,
                timeFare,
                totalEstimatedFare,
                "LKR"
        );

        return fareEstimateRepository.save(fareEstimate);
    }
}