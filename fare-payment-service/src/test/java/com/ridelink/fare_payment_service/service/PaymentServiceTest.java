package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.model.Payment;
import com.ridelink.fare_payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

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
}