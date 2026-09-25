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

    public Ride createRide(Ride ride) {
        ride.setStatus(RideStatus.REQUESTED.name());
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

    public Ride updateRideStatus(String id, String newStatusStr, String driverId) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found with id: " + id));

        RideStatus currentStatus = RideStatus.valueOf(ride.getStatus());
        RideStatus newStatus;
        try {
            newStatus = RideStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + newStatusStr);
        }

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        if (newStatus == RideStatus.ASSIGNED && driverId != null && !driverId.isBlank()) {
            ride.setDriverId(driverId);
        }

        ride.setStatus(newStatus.name());
        return rideRepository.save(ride);
    }

    private boolean isValidTransition(RideStatus current, RideStatus next) {
        if (current == next) return true;
        if (next == RideStatus.CANCELLED) {
            return current != RideStatus.COMPLETED && current != RideStatus.CANCELLED;
        }

        return switch (current) {
            case REQUESTED -> next == RideStatus.ASSIGNED;
            case ASSIGNED -> next == RideStatus.ACCEPTED;
            case ACCEPTED -> next == RideStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == RideStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    public Ride updateRide(String id, Ride updatedRide) {
        return rideRepository.findById(id).map(ride -> {
            ride.setPickupLocation(updatedRide.getPickupLocation());
            ride.setDropoffLocation(updatedRide.getDropoffLocation());
            return rideRepository.save(ride);
        }).orElse(null);
    }

    public void deleteRide(String id) {
        rideRepository.deleteById(id);
    }
}