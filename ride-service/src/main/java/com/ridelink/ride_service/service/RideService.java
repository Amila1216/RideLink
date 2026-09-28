package com.ridelink.ride_service.service;

import com.ridelink.ride_service.model.Ride;
import com.ridelink.ride_service.model.RideStatus;
import com.ridelink.ride_service.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RideService {

    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    // Create a new ride.
    // New rides must always start as REQUESTED.
    public Ride createRide(Ride ride) {
        ride.setId(null);
        ride.setDriverId(null);
        ride.setStatus(RideStatus.REQUESTED);

        return rideRepository.save(ride);
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    public Optional<Ride> getRideById(String id) {
        return rideRepository.findById(id);
    }

    public List<Ride> getRidesByPassenger(String passengerId) {
        return rideRepository.findByPassengerId(passengerId);
    }

    public List<Ride> getRidesByDriver(String driverId) {
        return rideRepository.findByDriverId(driverId);
    }

    /*
     * General update is deliberately restricted.
     *
     * passengerId, driverId and status cannot be changed here.
     * Driver assignment and lifecycle changes must use dedicated methods.
     */
    public Ride updateRide(String id, Ride updatedRide) {

        Ride ride = getRequiredRide(id);

        ride.setPickupLocation(updatedRide.getPickupLocation());
        ride.setDropoffLocation(updatedRide.getDropoffLocation());

        return rideRepository.save(ride);
    }

    /*
     * Assign a driver to a REQUESTED ride.
     *
     * Later, this method will be connected to the
     * Driver & Vehicle Service so the driver is selected
     * from eligible available drivers.
     */
    public Ride assignDriver(String id, String driverId) {

        if (driverId == null || driverId.isBlank()) {
            throw new IllegalArgumentException("Driver ID is required");
        }

        Ride ride = getRequiredRide(id);

        validateTransition(
                ride.getStatus(),
                RideStatus.ASSIGNED
        );

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ASSIGNED);

        return rideRepository.save(ride);
    }

    // ASSIGNED -> ACCEPTED
    public Ride acceptRide(String id) {

        Ride ride = getRequiredRide(id);

        if (ride.getDriverId() == null
                || ride.getDriverId().isBlank()) {
            throw new IllegalStateException(
                    "Ride cannot be accepted without an assigned driver"
            );
        }

        changeStatus(ride, RideStatus.ACCEPTED);

        return rideRepository.save(ride);
    }

    // ACCEPTED -> IN_PROGRESS
    public Ride startRide(String id) {

        Ride ride = getRequiredRide(id);

        changeStatus(ride, RideStatus.IN_PROGRESS);

        return rideRepository.save(ride);
    }

    // IN_PROGRESS -> COMPLETED
    public Ride completeRide(String id) {

        Ride ride = getRequiredRide(id);

        changeStatus(ride, RideStatus.COMPLETED);

        return rideRepository.save(ride);
    }

    // Cancellation is permitted only from supported active states.
    public Ride cancelRide(String id) {

        Ride ride = getRequiredRide(id);

        changeStatus(ride, RideStatus.CANCELLED);

        return rideRepository.save(ride);
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

    /*
     * Valid lifecycle:
     *
     * REQUESTED -> ASSIGNED
     * ASSIGNED -> ACCEPTED
     * ACCEPTED -> IN_PROGRESS
     * IN_PROGRESS -> COMPLETED
     *
     * Cancellation:
     * REQUESTED -> CANCELLED
     * ASSIGNED -> CANCELLED
     * ACCEPTED -> CANCELLED
     *
     * COMPLETED and CANCELLED are terminal states.
     */
    private void validateTransition(
            RideStatus currentStatus,
            RideStatus newStatus) {

        if (currentStatus == null) {
            throw new IllegalStateException(
                    "Current ride status is missing"
            );
        }

        boolean validTransition = switch (currentStatus) {

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

            case COMPLETED, CANCELLED -> false;
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
        rideRepository.deleteById(id);
    }
}