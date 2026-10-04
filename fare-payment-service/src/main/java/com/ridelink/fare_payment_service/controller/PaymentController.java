package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.dto.PaymentStatusResponse;
import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.model.Payment;
import com.ridelink.fare_payment_service.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(
    name = "Payments",
    description = "APIs for payment processing, payment status, and receipts"
)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
        summary = "Process payment",
        description = "Processes a simulated payment for a completed ride."
    )
    @PostMapping
    public ResponseEntity<Payment> processPayment(
            @Valid @RequestBody PaymentRequest request) {

        Payment payment = paymentService.processPayment(request);

        return ResponseEntity.ok(payment);
    }

    @Operation(
        summary = "Get payment status",
        description = "Retrieves the payment status for a ride using the ride ID."
    )
    @GetMapping("/ride/{rideId}/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable String rideId) {

        PaymentStatusResponse status =
                paymentService.getPaymentStatus(rideId);

        return ResponseEntity.ok(status);
    }

    @Operation(
        summary = "Get payment receipt",
        description = "Retrieves the payment receipt for a ride using the ride ID."
    )
    @GetMapping("/ride/{rideId}/receipt")
    public ResponseEntity<ReceiptResponse> getReceipt(
            @PathVariable String rideId) {

        ReceiptResponse receipt =
                paymentService.getReceipt(rideId);

        return ResponseEntity.ok(receipt);
    }
}