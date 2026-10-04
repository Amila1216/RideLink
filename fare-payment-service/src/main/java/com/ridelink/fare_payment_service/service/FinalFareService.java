package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.model.FinalFare;
import com.ridelink.fare_payment_service.repository.FinalFareRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FinalFareService {

    private static final double BASE_FARE = 100.0;
    private static final double DISTANCE_RATE_PER_KM = 30.0;
    private static final double TIME_RATE_PER_MINUTE = 5.0;

    private final FinalFareRepository finalFareRepository;
    private final RideClient rideClient;

    public FinalFareService(
            FinalFareRepository finalFareRepository,
            RideClient rideClient) {

        this.finalFareRepository = finalFareRepository;
        this.rideClient = rideClient;
    }

    public FinalFare calculateFinalFare(FinalFareRequest request) {

        // Get ride details from Ride Management Service
        Map<String, Object> ride =
                rideClient.getRideById(request.getRideId());

        if (ride == null) {
            throw new IllegalArgumentException(
                    "Ride not found: " + request.getRideId()
            );
        }

        Object statusValue = ride.get("status");

        if (statusValue == null ||
                !"COMPLETED".equalsIgnoreCase(statusValue.toString())) {

            throw new IllegalStateException(
                    "Final fare can only be calculated for a completed ride"
            );
        }

        double distanceFare =
                request.getActualDistanceKm() * DISTANCE_RATE_PER_KM;

        double timeFare =
                request.getActualDurationMinutes() * TIME_RATE_PER_MINUTE;

        double finalFare =
                BASE_FARE + distanceFare + timeFare;

        FinalFare fare = new FinalFare(
                request.getRideId(),
                request.getActualDistanceKm(),
                request.getActualDurationMinutes(),
                BASE_FARE,
                distanceFare,
                timeFare,
                finalFare,
                "LKR"
        );

        return finalFareRepository.save(fare);
    }
}