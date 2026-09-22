package com.ridelink.drivervehicle.vehicle;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Driver & Vehicle Service", description = "Vehicle-related operations")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Operation(summary = "Add a vehicle to a driver")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehicle added"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Driver not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate vehicle record")
    })
    @PostMapping("/drivers/{driverId}/vehicles")
    public ResponseEntity<VehicleResponse> createVehicle(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(driverId, request));
    }

    @Operation(summary = "Get vehicle details by vehicle ID for a driver")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle or driver not found")
    })
    @GetMapping("/drivers/{driverId}/vehicles/{vehicleId}")
    public ResponseEntity<VehicleResponse> getVehicle(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getVehicle(driverId, vehicleId));
    }

    @Operation(summary = "Update vehicle details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Vehicle or driver not found")
    })
    @PutMapping("/drivers/{driverId}/vehicles/{vehicleId}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(driverId, vehicleId, request));
    }
}
