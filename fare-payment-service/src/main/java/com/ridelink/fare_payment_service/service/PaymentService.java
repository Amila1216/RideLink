package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.model.Payment;
import com.ridelink.fare_payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(PaymentRequest request) {

        // Generate a simulated transaction ID
        String transactionId = "TXN-" + UUID.randomUUID();

        // Simulated payment is successful
        String paymentStatus = "SUCCESS";

        Payment payment = new Payment(
                request.getRideId(),
                request.getAmount(),
                request.getPaymentMethod(),
                paymentStatus,
                transactionId,
                LocalDateTime.now()
        );

        return paymentRepository.save(payment);
    }
}