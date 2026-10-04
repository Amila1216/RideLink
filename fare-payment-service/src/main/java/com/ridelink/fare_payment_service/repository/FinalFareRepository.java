package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.model.FinalFare;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FinalFareRepository extends MongoRepository<FinalFare, String> {

    Optional<FinalFare> findByRideId(String rideId);
}