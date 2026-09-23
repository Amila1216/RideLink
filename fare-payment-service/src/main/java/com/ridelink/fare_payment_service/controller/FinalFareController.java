package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.model.FinalFare;
import com.ridelink.fare_payment_service.service.FinalFareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fares")
public class FinalFareController {

    private final FinalFareService finalFareService;

    public FinalFareController(FinalFareService finalFareService) {
        this.finalFareService = finalFareService;
    }

    @PostMapping("/final")
    public ResponseEntity<FinalFare> calculateFinalFare(
            @Valid @RequestBody FinalFareRequest request) {

        FinalFare finalFare = finalFareService.calculateFinalFare(request);

        return ResponseEntity.ok(finalFare);
    }
}