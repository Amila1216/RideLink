package com.ridelink.drivervehicle.driver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Driver & Vehicle Service", description = "Driver, vehicle, availability, service-area, and simulated-location operations")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @Operation(summary = "Create a driver operational profile")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Driver created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Duplicate driver record")
    })
    @PostMapping("/drivers")
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.createDriver(request));
    }

    @Operation(summary = "Get driver operational profile by driver ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver found"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<DriverResponse> getDriver(
            @Parameter(description = "Driver ID") @PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getDriver(driverId));
    }

    @Operation(summary = "Update driver operational information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PutMapping("/drivers/{driverId}")
    public ResponseEntity<DriverResponse> updateDriver(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Valid @RequestBody DriverRequest request) {
        return ResponseEntity.ok(driverService.updateDriver(driverId, request));
    }

    @Operation(summary = "Set a driver's availability",
            description = "Accepts AVAILABLE or UNAVAILABLE. This service does not currently configure authentication or role-based authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver availability updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, driver ID, or availability status"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PutMapping("/drivers/{driverId}/availability")
    public ResponseEntity<DriverAvailabilityResponse> updateAvailability(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Valid @RequestBody DriverAvailabilityRequest request) {
        return ResponseEntity.ok(driverService.updateAvailability(driverId, request));
    }

    @Operation(summary = "Get a driver's current availability",
            description = "This service does not currently configure authentication or role-based authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver availability found"),
            @ApiResponse(responseCode = "400", description = "Invalid driver ID"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/drivers/{driverId}/availability")
    public ResponseEntity<DriverAvailabilityResponse> getAvailability(
            @Parameter(description = "Driver ID") @PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getAvailability(driverId));
    }

    @Operation(summary = "Set or update a driver's service area")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service area saved"),
            @ApiResponse(responseCode = "400", description = "Invalid service-area request"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PutMapping("/drivers/{driverId}/service-area")
    public ResponseEntity<ServiceAreaResponse> updateServiceArea(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Valid @RequestBody ServiceAreaRequest request) {
        return ResponseEntity.ok(driverService.updateServiceArea(driverId, request));
    }

    @Operation(summary = "Get a driver's service area")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service area found"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/drivers/{driverId}/service-area")
    public ResponseEntity<ServiceAreaResponse> getServiceArea(
            @Parameter(description = "Driver ID") @PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getServiceArea(driverId));
    }

    @Operation(summary = "Set or update a driver's simulated current location")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulated location saved"),
            @ApiResponse(responseCode = "400", description = "Invalid latitude or longitude"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PutMapping("/drivers/{driverId}/location")
    public ResponseEntity<SimulatedLocationResponse> updateSimulatedLocation(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Valid @RequestBody SimulatedLocationRequest request) {
        return ResponseEntity.ok(driverService.updateSimulatedLocation(driverId, request));
    }

    @Operation(summary = "Get a driver's simulated current location")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulated location found"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/drivers/{driverId}/location")
    public ResponseEntity<SimulatedLocationResponse> getSimulatedLocation(
            @Parameter(description = "Driver ID") @PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getSimulatedLocation(driverId));
    }
}
