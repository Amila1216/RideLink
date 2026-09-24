package com.ridelink.ride_service.service;

import com.ridelink.ride_service.model.Ride;
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

    public Ride updateRide(String id, Ride updatedRide) {

        Optional<Ride> existingRide = rideRepository.findById(id);

        if (existingRide.isPresent()) {

            Ride ride = existingRide.get();

            ride.setPassengerId(updatedRide.getPassengerId());
            ride.setDriverId(updatedRide.getDriverId());
            ride.setPickupLocation(updatedRide.getPickupLocation());
            ride.setDropoffLocation(updatedRide.getDropoffLocation());
            ride.setStatus(updatedRide.getStatus());

            return rideRepository.save(ride);
        }

        return null;
    }

    public void deleteRide(String id) {
        rideRepository.deleteById(id);
    }
}