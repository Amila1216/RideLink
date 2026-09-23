package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.model.FareEstimate;
import com.ridelink.fare_payment_service.service.FareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    public ResponseEntity<FareEstimate> estimateFare(
            @Valid @RequestBody FareEstimateRequest request) {

        FareEstimate fareEstimate = fareService.calculateFare(request);

        return ResponseEntity.ok(fareEstimate);
    }
}