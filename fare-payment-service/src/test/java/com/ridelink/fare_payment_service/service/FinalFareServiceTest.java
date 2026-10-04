package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.model.FinalFare;
import com.ridelink.fare_payment_service.repository.FinalFareRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FinalFareServiceTest {

    @Test
    void shouldCalculateFinalFareCorrectly() {

        // Arrange
        FinalFareRepository repository =
                mock(FinalFareRepository.class);

        RideClient rideClient =
                mock(RideClient.class);

        FinalFareService service =
                new FinalFareService(repository, rideClient);

        FinalFareRequest request =
                new FinalFareRequest();

        request.setRideId("ride-101");
        request.setActualDistanceKm(12.0);
        request.setActualDurationMinutes(25);

        Map<String, Object> completedRide =
                Map.of(
                        "id", "ride-101",
                        "status", "COMPLETED"
                );

        when(rideClient.getRideById("ride-101"))
                .thenReturn(completedRide);

        FinalFare savedFare =
                new FinalFare(
                        "ride-101",
                        12.0,
                        25,
                        100.0,
                        360.0,
                        125.0,
                        585.0,
                        "LKR"
                );

        when(repository.save(any(FinalFare.class)))
                .thenReturn(savedFare);

        // Act
        FinalFare result =
                service.calculateFinalFare(request);

        // Assert
        assertEquals(100.0, result.getBaseFare());
        assertEquals(360.0, result.getDistanceFare());
        assertEquals(125.0, result.getTimeFare());
        assertEquals(585.0, result.getFinalFare());
        assertEquals("LKR", result.getCurrency());

        verify(rideClient, times(1))
                .getRideById("ride-101");

        verify(repository, times(1))
                .save(any(FinalFare.class));
    }

    @Test
    void shouldRejectFinalFareWhenRideIsNotCompleted() {

        // Arrange
        FinalFareRepository repository =
                mock(FinalFareRepository.class);

        RideClient rideClient =
                mock(RideClient.class);

        FinalFareService service =
                new FinalFareService(repository, rideClient);

        FinalFareRequest request =
                new FinalFareRequest();

        request.setRideId("ride-102");
        request.setActualDistanceKm(10.0);
        request.setActualDurationMinutes(20);

        Map<String, Object> inProgressRide =
                Map.of(
                        "id", "ride-102",
                        "status", "IN_PROGRESS"
                );

        when(rideClient.getRideById("ride-102"))
                .thenReturn(inProgressRide);

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> service.calculateFinalFare(request)
        );

        verify(rideClient, times(1))
                .getRideById("ride-102");

        verify(repository, never())
                .save(any(FinalFare.class));
    }
}