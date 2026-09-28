package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.model.FareEstimate;
import com.ridelink.fare_payment_service.service.FareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fares")
@Tag(
    name = "Fare Estimation",
    description = "APIs for estimating ride fares"
)
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @Operation(
        summary = "Estimate ride fare",
        description = "Calculates an estimated fare using the ride distance and duration."
    )
    @PostMapping("/estimate")
    public ResponseEntity<FareEstimate> estimateFare(
            @Valid @RequestBody FareEstimateRequest request) {

        FareEstimate fareEstimate = fareService.calculateFare(request);

        return ResponseEntity.ok(fareEstimate);
    }
}