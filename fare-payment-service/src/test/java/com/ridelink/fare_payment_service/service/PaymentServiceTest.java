package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.dto.PaymentStatusResponse;
import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.model.Payment;
import com.ridelink.fare_payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Test
    void shouldProcessPaymentSuccessfully() {

        // Arrange
        PaymentRepository repository = mock(PaymentRepository.class);
        PaymentService service = new PaymentService(repository);

        PaymentRequest request = new PaymentRequest();
        request.setRideId("ride-101");
        request.setAmount(585.0);
        request.setPaymentMethod("CARD");

        when(repository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = service.processPayment(request);

        // Assert
        assertEquals("ride-101", result.getRideId());
        assertEquals(585.0, result.getAmount());
        assertEquals("CARD", result.getPaymentMethod());
        assertEquals("SUCCESS", result.getPaymentStatus());

        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("TXN-"));

        assertNotNull(result.getPaymentDate());

        verify(repository, times(1))
                .save(any(Payment.class));
    }

    @Test
    void shouldReturnPaymentStatusByRideId() {

        // Arrange
        PaymentRepository repository = mock(PaymentRepository.class);
        PaymentService service = new PaymentService(repository);

        Payment payment = new Payment(
                "ride-101",
                585.0,
                "CARD",
                "SUCCESS",
                "TXN-12345",
                LocalDateTime.now()
        );

        when(repository.findByRideId("ride-101"))
                .thenReturn(Optional.of(payment));

        // Act
        PaymentStatusResponse result =
                service.getPaymentStatus("ride-101");

        // Assert
        assertEquals("ride-101", result.getRideId());
        assertEquals("SUCCESS", result.getPaymentStatus());
        assertEquals("TXN-12345", result.getTransactionId());

        verify(repository, times(1))
                .findByRideId("ride-101");
    }

    @Test
    void shouldReturnReceiptByRideId() {

        // Arrange
        PaymentRepository repository = mock(PaymentRepository.class);
        PaymentService service = new PaymentService(repository);

        LocalDateTime paymentDate = LocalDateTime.now();

        Payment payment = new Payment(
                "ride-101",
                585.0,
                "CARD",
                "SUCCESS",
                "TXN-12345",
                paymentDate
        );

        when(repository.findByRideId("ride-101"))
            .thenReturn(Optional.of(payment));

        // Act
        ReceiptResponse result =
                service.getReceipt("ride-101");

        // Assert
        assertEquals("ride-101", result.getRideId());
        assertEquals(585.0, result.getAmount());
        assertEquals("CARD", result.getPaymentMethod());
        assertEquals("SUCCESS", result.getPaymentStatus());
        assertEquals("TXN-12345", result.getTransactionId());
        assertEquals(paymentDate, result.getPaymentDate());

        verify(repository, times(1))
                .findByRideId("ride-101");
    }
}