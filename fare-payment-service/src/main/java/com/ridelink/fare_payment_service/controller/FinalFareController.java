package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.model.FinalFare;
import com.ridelink.fare_payment_service.service.FinalFareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fares")
@Tag(
    name = "Final Fare",
    description = "APIs for calculating the final ride fare"
)
public class FinalFareController {

    private final FinalFareService finalFareService;

    public FinalFareController(FinalFareService finalFareService) {
        this.finalFareService = finalFareService;
    }

    @Operation(
        summary = "Calculate final ride fare",
        description = "Calculates the final fare using the actual ride distance and duration."
    )
    @PostMapping("/final")
    public ResponseEntity<FinalFare> calculateFinalFare(
            @Valid @RequestBody FinalFareRequest request) {

        FinalFare finalFare = finalFareService.calculateFinalFare(request);

        return ResponseEntity.ok(finalFare);
    }
}