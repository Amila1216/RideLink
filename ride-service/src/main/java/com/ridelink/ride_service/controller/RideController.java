package com.ridelink.ride_service.controller;

import com.ridelink.ride_service.model.Ride;
import com.ridelink.ride_service.service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<Ride> createRide(@RequestBody Ride ride) {

        if (ride.getStatus() == null || ride.getStatus().isBlank()) {
            ride.setStatus("REQUESTED");
        }

        return ResponseEntity.ok(rideService.createRide(ride));
    }

    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideService.getAllRides());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable String id) {

        return rideService.getRideById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Ride>> getRidesByPassenger(
            @PathVariable String passengerId) {

        return ResponseEntity.ok(
                rideService.getRidesByPassenger(passengerId)
        );
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Ride>> getRidesByDriver(
            @PathVariable String driverId) {

        return ResponseEntity.ok(
                rideService.getRidesByDriver(driverId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ride> updateRide(
            @PathVariable String id,
            @RequestBody Ride ride) {

        Ride updatedRide = rideService.updateRide(id, ride);

        if (updatedRide == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedRide);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable String id) {

        if (rideService.getRideById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        rideService.deleteRide(id);

        return ResponseEntity.noContent().build();
    }
}