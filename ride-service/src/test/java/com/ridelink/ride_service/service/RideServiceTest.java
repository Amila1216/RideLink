package com.ridelink.ride_service.service;

import com.ridelink.ride_service.client.DriverServiceClient;
import com.ridelink.ride_service.dto.AvailableDriverResponse;
import com.ridelink.ride_service.dto.DriverDetailsResponse;
import com.ridelink.ride_service.exception.RideAccessDeniedException;
import com.ridelink.ride_service.model.Ride;
import com.ridelink.ride_service.model.RideStatus;
import com.ridelink.ride_service.repository.RideRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @InjectMocks
    private RideService rideService;

    @Test
    @DisplayName("Positive: Passenger assigns an available driver to own REQUESTED ride")
    void assignAvailableDriver_RequestedRide_Success() {

        String rideId = "ride-101";
        String passengerId = "passenger-303";

        Ride existingRide = new Ride(
                passengerId,
                null,
                "Colombo 03",
                "Kandy",
                RideStatus.REQUESTED
        );

        existingRide.setId(rideId);

        AvailableDriverResponse availableDriver =
                new AvailableDriverResponse(
                        202L,
                        "AVAILABLE"
                );

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        when(driverServiceClient.getAvailableDrivers())
                .thenReturn(List.of(availableDriver));

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Ride updatedRide =
                rideService.assignAvailableDriver(
                        rideId,
                        passengerId,
                        "PASSENGER"
                );

        assertNotNull(updatedRide);

        assertEquals(
                RideStatus.ASSIGNED,
                updatedRide.getStatus()
        );

        assertEquals(
                "202",
                updatedRide.getDriverId()
        );

        verify(rideRepository, times(1))
                .findById(rideId);

        verify(driverServiceClient, times(1))
                .getAvailableDrivers();

        verify(rideRepository, times(1))
                .save(existingRide);
    }

    @Test
    @DisplayName("Negative: Reject REQUESTED to COMPLETED transition")
    void completeRide_RequestedRide_ThrowsIllegalStateException() {

        String rideId = "ride-101";
        String driverEmail = "driver@example.com";

        Ride existingRide = new Ride(
                "passenger-303",
                "202",
                "Colombo 03",
                "Kandy",
                RideStatus.REQUESTED
        );

        existingRide.setId(rideId);

        DriverDetailsResponse driver =
                new DriverDetailsResponse(
                        202L,
                        driverEmail,
                        "AVAILABLE"
                );

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        when(driverServiceClient.getDriverById("202"))
                .thenReturn(driver);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> rideService.completeRide(
                                rideId,
                                driverEmail
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Invalid ride status transition"
                        )
        );

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    @DisplayName("Negative: Reject cancellation of a COMPLETED ride")
    void cancelRide_CompletedRide_ThrowsIllegalStateException() {

        String rideId = "ride-101";

        Ride existingRide = new Ride(
                "passenger-303",
                "202",
                "Colombo 03",
                "Kandy",
                RideStatus.COMPLETED
        );

        existingRide.setId(rideId);

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> rideService.cancelRide(
                                rideId,
                                "admin-account",
                                "admin@example.com",
                                "ADMIN"
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Invalid ride status transition"
                        )
        );

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    @DisplayName("Positive: Assigned driver can accept own ride")
    void acceptRide_AssignedDriver_Success() {

        String rideId = "ride-101";
        String driverEmail = "driver@example.com";

        Ride existingRide = new Ride(
                "passenger-303",
                "202",
                "Colombo 03",
                "Kandy",
                RideStatus.ASSIGNED
        );

        existingRide.setId(rideId);

        DriverDetailsResponse driver =
                new DriverDetailsResponse(
                        202L,
                        driverEmail,
                        "AVAILABLE"
                );

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        when(driverServiceClient.getDriverById("202"))
                .thenReturn(driver);

        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Ride updatedRide =
                rideService.acceptRide(
                        rideId,
                        driverEmail
                );

        assertEquals(
                RideStatus.ACCEPTED,
                updatedRide.getStatus()
        );

        assertEquals(
                "202",
                updatedRide.getDriverId()
        );

        verify(rideRepository, times(1))
                .save(existingRide);
    }

    @Test
    @DisplayName("Negative: Ride cannot be accepted without assigned driver")
    void acceptRide_WithoutDriver_ThrowsIllegalStateException() {

        String rideId = "ride-101";

        Ride existingRide = new Ride(
                "passenger-303",
                null,
                "Colombo 03",
                "Kandy",
                RideStatus.ASSIGNED
        );

        existingRide.setId(rideId);

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> rideService.acceptRide(
                                rideId,
                                "driver@example.com"
                        )
                );

        assertEquals(
                "Ride does not have an assigned driver",
                exception.getMessage()
        );

        verify(driverServiceClient, never())
                .getDriverById(anyString());

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    @DisplayName("Negative: Different driver cannot accept assigned ride")
    void acceptRide_WrongDriver_ThrowsRideAccessDeniedException() {

        String rideId = "ride-101";

        Ride existingRide = new Ride(
                "passenger-303",
                "202",
                "Colombo 03",
                "Kandy",
                RideStatus.ASSIGNED
        );

        existingRide.setId(rideId);

        DriverDetailsResponse assignedDriver =
                new DriverDetailsResponse(
                        202L,
                        "assigned-driver@example.com",
                        "AVAILABLE"
                );

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        when(driverServiceClient.getDriverById("202"))
                .thenReturn(assignedDriver);

        assertThrows(
                RideAccessDeniedException.class,
                () -> rideService.acceptRide(
                        rideId,
                        "different-driver@example.com"
                )
        );

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    @DisplayName("Negative: Passenger cannot update another passenger's ride")
    void updateRide_WrongPassenger_ThrowsRideAccessDeniedException() {

        String rideId = "ride-101";

        Ride existingRide = new Ride(
                "passenger-owner",
                null,
                "Colombo 03",
                "Kandy",
                RideStatus.REQUESTED
        );

        existingRide.setId(rideId);

        Ride updateRequest = new Ride();

        updateRequest.setPickupLocation("Galle");
        updateRequest.setDropoffLocation("Matara");

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        assertThrows(
                RideAccessDeniedException.class,
                () -> rideService.updateRide(
                        rideId,
                        updateRequest,
                        "different-passenger",
                        "PASSENGER"
                )
        );

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    @DisplayName("Negative: ASSIGNED ride details cannot be updated")
    void updateRide_AssignedRide_ThrowsIllegalStateException() {

        String rideId = "ride-101";
        String passengerId = "passenger-owner";

        Ride existingRide = new Ride(
                passengerId,
                "202",
                "Colombo 03",
                "Kandy",
                RideStatus.ASSIGNED
        );

        existingRide.setId(rideId);

        Ride updateRequest = new Ride();

        updateRequest.setPickupLocation("Galle");
        updateRequest.setDropoffLocation("Matara");

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> rideService.updateRide(
                                rideId,
                                updateRequest,
                                passengerId,
                                "PASSENGER"
                        )
                );

        assertEquals(
                "Ride details can only be updated while status is REQUESTED",
                exception.getMessage()
        );

        verify(rideRepository, never())
                .save(any(Ride.class));
    }

    @Test
    @DisplayName("Positive: Passenger can view own ride")
    void getRideByIdForUser_OwnerPassenger_Success() {

        String rideId = "ride-101";
        String passengerId = "passenger-owner";

        Ride existingRide = new Ride(
                passengerId,
                null,
                "Colombo",
                "Kandy",
                RideStatus.REQUESTED
        );

        existingRide.setId(rideId);

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        Ride result =
                rideService.getRideByIdForUser(
                        rideId,
                        passengerId,
                        "passenger@example.com",
                        "PASSENGER"
                );

        assertEquals(
                rideId,
                result.getId()
        );

        assertEquals(
                passengerId,
                result.getPassengerId()
        );

        verify(rideRepository, times(1))
                .findById(rideId);
    }

    @Test
    @DisplayName("Negative: Passenger cannot view another passenger's ride")
    void getRideByIdForUser_WrongPassenger_ThrowsRideAccessDeniedException() {

        String rideId = "ride-101";

        Ride existingRide = new Ride(
                "passenger-owner",
                null,
                "Colombo",
                "Kandy",
                RideStatus.REQUESTED
        );

        existingRide.setId(rideId);

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(existingRide));

        assertThrows(
                RideAccessDeniedException.class,
                () -> rideService.getRideByIdForUser(
                        rideId,
                        "different-passenger",
                        "different@example.com",
                        "PASSENGER"
                )
        );
    }

    @Test
    @DisplayName("Negative: Driver cannot view another driver's ride list")
    void getRidesByDriverForUser_WrongDriver_ThrowsRideAccessDeniedException() {

        String driverId = "202";

        DriverDetailsResponse assignedDriver =
                new DriverDetailsResponse(
                        202L,
                        "assigned-driver@example.com",
                        "AVAILABLE"
                );

        when(driverServiceClient.getDriverById(driverId))
                .thenReturn(assignedDriver);

        assertThrows(
                RideAccessDeniedException.class,
                () -> rideService.getRidesByDriverForUser(
                        driverId,
                        "different-driver@example.com",
                        "DRIVER"
                )
        );

        verify(rideRepository, never())
                .findByDriverId(anyString());
    }
}