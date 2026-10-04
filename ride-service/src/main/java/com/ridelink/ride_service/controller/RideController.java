package com.ridelink.ride_service.controller;

import com.ridelink.ride_service.dto.CreateRideRequest;
import com.ridelink.ride_service.dto.UpdateRideRequest;
import com.ridelink.ride_service.model.Ride;
import com.ridelink.ride_service.security.AuthenticatedUser;
import com.ridelink.ride_service.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Ride> createRide(
            @Valid @RequestBody CreateRideRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Ride ride = new Ride();

        ride.setPassengerId(authenticatedUser.accountId());
        ride.setPickupLocation(request.pickupLocation());
        ride.setDropoffLocation(request.dropoffLocation());

        return ResponseEntity
                .status(201)
                .body(rideService.createRide(ride));
    }

    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(
                rideService.getAllRides()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                rideService.getRideByIdForUser(
                        id,
                        authenticatedUser.accountId(),
                        authenticatedUser.email(),
                        authenticatedUser.role()
                )
        );
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Ride>> getRidesByPassenger(
            @PathVariable String passengerId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                rideService.getRidesByPassengerForUser(
                        passengerId,
                        authenticatedUser.accountId(),
                        authenticatedUser.role()
                )
        );
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Ride>> getRidesByDriver(
            @PathVariable String driverId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                rideService.getRidesByDriverForUser(
                        driverId,
                        authenticatedUser.email(),
                        authenticatedUser.role()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ride> updateRide(
            @PathVariable String id,
            @Valid @RequestBody UpdateRideRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Ride ride = new Ride();

        ride.setPickupLocation(request.pickupLocation());
        ride.setDropoffLocation(request.dropoffLocation());

        Ride updatedRide = rideService.updateRide(
                id,
                ride,
                authenticatedUser.accountId(),
                authenticatedUser.role()
        );

        return ResponseEntity.ok(updatedRide);
    }

    @PostMapping("/{id}/assign-driver")
    public ResponseEntity<Ride> assignDriver(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                rideService.assignAvailableDriver(
                        id,
                        authenticatedUser.accountId(),
                        authenticatedUser.role()
                )
        );
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Ride> acceptRide(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                rideService.acceptRide(
                        id,
                        authenticatedUser.email()
                )
        );
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Ride> startRide(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                rideService.startRide(
                        id,
                        authenticatedUser.email()
                )
        );
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Ride> completeRide(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                rideService.completeRide(
                        id,
                        authenticatedUser.email()
                )
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Ride> cancelRide(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                rideService.cancelRide(
                        id,
                        authenticatedUser.accountId(),
                        authenticatedUser.email(),
                        authenticatedUser.role()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable String id) {
        rideService.deleteRide(id);
        return ResponseEntity.noContent().build();
    }
}