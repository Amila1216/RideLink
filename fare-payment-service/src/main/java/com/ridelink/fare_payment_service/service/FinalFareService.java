package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.model.FinalFare;
import com.ridelink.fare_payment_service.repository.FinalFareRepository;
import org.springframework.stereotype.Service;

@Service
public class FinalFareService {

    private static final double BASE_FARE = 100.0;
    private static final double DISTANCE_RATE_PER_KM = 30.0;
    private static final double TIME_RATE_PER_MINUTE = 5.0;

    private final FinalFareRepository finalFareRepository;

    public FinalFareService(FinalFareRepository finalFareRepository) {
        this.finalFareRepository = finalFareRepository;
    }

    public FinalFare calculateFinalFare(FinalFareRequest request) {

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