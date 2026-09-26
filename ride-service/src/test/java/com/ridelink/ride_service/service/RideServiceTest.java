package com.ridelink.ride_service.service;

import com.ridelink.ride_service.model.Ride;
import com.ridelink.ride_service.model.RideStatus;
import com.ridelink.ride_service.repository.RideRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private RideService rideService;

    @Test
    @DisplayName("Positive: Successful transition from REQUESTED to ASSIGNED with driverId set")
    void testUpdateRideStatus_RequestedToAssigned_Success() {
        String rideId = "ride-101";
        String driverId = "driver-202";
        Ride existingRide = new Ride("passenger-303", null, "Colombo 03", "Kandy", RideStatus.REQUESTED.name());
        existingRide.setId(rideId);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride updatedRide = rideService.updateRideStatus(rideId, "ASSIGNED", driverId);

        assertNotNull(updatedRide);
        assertEquals(RideStatus.ASSIGNED.name(), updatedRide.getStatus());
        assertEquals(driverId, updatedRide.getDriverId());
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, times(1)).save(existingRide);
    }

    @Test
    @DisplayName("Negative: Invalid transition from REQUESTED directly to COMPLETED expecting IllegalStateException")
    void testUpdateRideStatus_RequestedToCompleted_ThrowsIllegalStateException() {
        String rideId = "ride-101";
        Ride existingRide = new Ride("passenger-303", null, "Colombo 03", "Kandy", RideStatus.REQUESTED.name());
        existingRide.setId(rideId);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(existingRide));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rideService.updateRideStatus(rideId, "COMPLETED", null)
        );

        assertTrue(exception.getMessage().contains("Invalid status transition"));
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("Negative: Attempt to cancel a COMPLETED ride expecting IllegalStateException")
    void testUpdateRideStatus_CancelCompletedRide_ThrowsIllegalStateException() {
        String rideId = "ride-101";
        Ride existingRide = new Ride("passenger-303", "driver-202", "Colombo 03", "Kandy", RideStatus.COMPLETED.name());
        existingRide.setId(rideId);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(existingRide));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rideService.updateRideStatus(rideId, "CANCELLED", null)
        );

        assertTrue(exception.getMessage().contains("Invalid status transition"));
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any());
    }
}
