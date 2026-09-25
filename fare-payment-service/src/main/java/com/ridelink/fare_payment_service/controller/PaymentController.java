package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.dto.PaymentStatusResponse;
import com.ridelink.fare_payment_service.model.Payment;
import com.ridelink.fare_payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> processPayment(
            @Valid @RequestBody PaymentRequest request) {

        Payment payment = paymentService.processPayment(request);

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/ride/{rideId}/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable String rideId) {

        PaymentStatusResponse status =
                paymentService.getPaymentStatus(rideId);

        return ResponseEntity.ok(status);
    }
}