package com.ridelink.ride_service.service;

import com.ridelink.ride_service.client.DriverServiceClient;
import com.ridelink.ride_service.dto.AvailableDriverResponse;
import com.ridelink.ride_service.dto.DriverDetailsResponse;
import com.ridelink.ride_service.exception.NoAvailableDriverException;
import com.ridelink.ride_service.exception.RideAccessDeniedException;
import com.ridelink.ride_service.model.Ride;
import com.ridelink.ride_service.model.RideStatus;
import com.ridelink.ride_service.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;

    public RideService(
            RideRepository rideRepository,
            DriverServiceClient driverServiceClient) {

        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
    }

    public Ride createRide(Ride ride) {

        ride.setId(null);
        ride.setDriverId(null);
        ride.setStatus(RideStatus.REQUESTED);

        return rideRepository.save(ride);
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    public Ride getRideByIdForUser(
            String id,
            String accountId,
            String email,
            String role) {

        Ride ride = getRequiredRide(id);

        if ("ADMIN".equalsIgnoreCase(role)) {
            return ride;
        }

        if ("PASSENGER".equalsIgnoreCase(role)) {

            validatePassengerOwnership(
                    ride,
                    accountId
            );

            return ride;
        }

        if ("DRIVER".equalsIgnoreCase(role)) {

            validateDriverOwnership(
                    ride,
                    email
            );

            return ride;
        }

        throw new RideAccessDeniedException(
                "You do not have permission to access this ride"
        );
    }

    public List<Ride> getRidesByPassengerForUser(
            String passengerId,
            String accountId,
            String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {

            if (!"PASSENGER".equalsIgnoreCase(role)
                    || accountId == null
                    || !accountId.equals(passengerId)) {

                throw new RideAccessDeniedException(
                        "You do not have permission to access these rides"
                );
            }
        }

        return rideRepository.findByPassengerId(
                passengerId
        );
    }

    public List<Ride> getRidesByDriverForUser(
            String driverId,
            String email,
            String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {

            if (!"DRIVER".equalsIgnoreCase(role)) {

                throw new RideAccessDeniedException(
                        "You do not have permission to access these rides"
                );
            }

            validateDriverIdentity(
                    driverId,
                    email
            );
        }

        return rideRepository.findByDriverId(
                driverId
        );
    }

    public Ride updateRide(
            String id,
            Ride updatedRide,
            String accountId,
            String role) {

        Ride ride = getRequiredRide(id);

        if (!"ADMIN".equalsIgnoreCase(role)) {

            validatePassengerOwnership(
                    ride,
                    accountId
            );
        }

        if (ride.getStatus() != RideStatus.REQUESTED) {

            throw new IllegalStateException(
                    "Ride details can only be updated while status is REQUESTED"
            );
        }

        ride.setPickupLocation(
                updatedRide.getPickupLocation()
        );

        ride.setDropoffLocation(
                updatedRide.getDropoffLocation()
        );

        return rideRepository.save(ride);
    }

    public Ride assignAvailableDriver(
            String id,
            String accountId,
            String role) {

        Ride ride = getRequiredRide(id);

        if (!"ADMIN".equalsIgnoreCase(role)) {

            validatePassengerOwnership(
                    ride,
                    accountId
            );
        }

        validateTransition(
                ride.getStatus(),
                RideStatus.ASSIGNED
        );

        List<AvailableDriverResponse> availableDrivers =
                driverServiceClient.getAvailableDrivers();

        AvailableDriverResponse selectedDriver =
                availableDrivers.stream()
                        .filter(driver ->
                                driver.driverId() != null
                        )
                        .filter(driver ->
                                "AVAILABLE".equalsIgnoreCase(
                                        driver.driverAvailability()
                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new NoAvailableDriverException(
                                        "No available drivers found"
                                )
                        );

        ride.setDriverId(
                String.valueOf(
                        selectedDriver.driverId()
                )
        );

        ride.setStatus(
                RideStatus.ASSIGNED
        );

        return rideRepository.save(ride);
    }

    public Ride acceptRide(
            String id,
            String driverEmail) {

        Ride ride = getRequiredRide(id);

        validateDriverOwnership(
                ride,
                driverEmail
        );

        changeStatus(
                ride,
                RideStatus.ACCEPTED
        );

        return rideRepository.save(ride);
    }

    public Ride startRide(
            String id,
            String driverEmail) {

        Ride ride = getRequiredRide(id);

        validateDriverOwnership(
                ride,
                driverEmail
        );

        changeStatus(
                ride,
                RideStatus.IN_PROGRESS
        );

        return rideRepository.save(ride);
    }

    public Ride completeRide(
            String id,
            String driverEmail) {

        Ride ride = getRequiredRide(id);

        validateDriverOwnership(
                ride,
                driverEmail
        );

        changeStatus(
                ride,
                RideStatus.COMPLETED
        );

        return rideRepository.save(ride);
    }

    public Ride cancelRide(
            String id,
            String accountId,
            String email,
            String role) {

        Ride ride = getRequiredRide(id);

        if ("ADMIN".equalsIgnoreCase(role)) {

            // Admin may cancel without ownership restriction.

        } else if ("PASSENGER".equalsIgnoreCase(role)) {

            validatePassengerOwnership(
                    ride,
                    accountId
            );

        } else if ("DRIVER".equalsIgnoreCase(role)) {

            validateDriverOwnership(
                    ride,
                    email
            );

        } else {

            throw new RideAccessDeniedException(
                    "You do not have permission to cancel this ride"
            );
        }

        changeStatus(
                ride,
                RideStatus.CANCELLED
        );

        return rideRepository.save(ride);
    }

    private void validatePassengerOwnership(
            Ride ride,
            String accountId) {

        if (accountId == null
                || accountId.isBlank()
                || ride.getPassengerId() == null
                || !ride.getPassengerId().equals(accountId)) {

            throw new RideAccessDeniedException(
                    "You do not have permission to access this ride"
            );
        }
    }

    private void validateDriverOwnership(
            Ride ride,
            String driverEmail) {

        if (ride.getDriverId() == null
                || ride.getDriverId().isBlank()) {

            throw new IllegalStateException(
                    "Ride does not have an assigned driver"
            );
        }

        validateDriverIdentity(
                ride.getDriverId(),
                driverEmail
        );
    }

    private void validateDriverIdentity(
            String driverId,
            String driverEmail) {

        if (driverEmail == null
                || driverEmail.isBlank()) {

            throw new RideAccessDeniedException(
                    "Authenticated driver email is missing"
            );
        }

        DriverDetailsResponse driver =
                driverServiceClient.getDriverById(
                        driverId
                );

        if (driver.email() == null
                || !driver.email()
                .equalsIgnoreCase(driverEmail)) {

            throw new RideAccessDeniedException(
                    "You are not the assigned driver for this ride"
            );
        }
    }

    private void changeStatus(
            Ride ride,
            RideStatus newStatus) {

        validateTransition(
                ride.getStatus(),
                newStatus
        );

        ride.setStatus(newStatus);
    }

    private void validateTransition(
            RideStatus currentStatus,
            RideStatus newStatus) {

        if (currentStatus == null) {

            throw new IllegalStateException(
                    "Current ride status is missing"
            );
        }

        boolean validTransition =
                switch (currentStatus) {

                    case REQUESTED ->
                            newStatus == RideStatus.ASSIGNED
                                    || newStatus == RideStatus.CANCELLED;

                    case ASSIGNED ->
                            newStatus == RideStatus.ACCEPTED
                                    || newStatus == RideStatus.CANCELLED;

                    case ACCEPTED ->
                            newStatus == RideStatus.IN_PROGRESS
                                    || newStatus == RideStatus.CANCELLED;

                    case IN_PROGRESS ->
                            newStatus == RideStatus.COMPLETED;

                    case COMPLETED, CANCELLED ->
                            false;
                };

        if (!validTransition) {

            throw new IllegalStateException(
                    "Invalid ride status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }
    }

    private Ride getRequiredRide(String id) {

        return rideRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ride not found: " + id
                        )
                );
    }

    public void deleteRide(String id) {

        getRequiredRide(id);

        rideRepository.deleteById(id);
    }
}