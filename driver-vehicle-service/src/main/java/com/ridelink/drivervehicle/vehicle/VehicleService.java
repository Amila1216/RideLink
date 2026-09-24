package com.ridelink.drivervehicle.vehicle;

import com.ridelink.drivervehicle.common.DuplicateResourceException;
import com.ridelink.drivervehicle.common.ResourceNotFoundException;
import com.ridelink.drivervehicle.driver.Driver;
import com.ridelink.drivervehicle.driver.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleService(VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public VehicleResponse createVehicle(Long driverId, VehicleRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        validateUniqueFields(request.licensePlate(), request.registrationNumber());

        Vehicle vehicle = new Vehicle(
                request.make(),
                request.model(),
                request.year(),
                request.licensePlate(),
                request.color(),
                request.vehicleType(),
                request.registrationNumber(),
                driver
        );

        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(Long driverId, Long vehicleId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if (!vehicle.getDriver().getDriverId().equals(driver.getDriverId())) {
            throw new ResourceNotFoundException("Vehicle not found for driver id: " + driverId);
        }

        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public VehicleResponse updateVehicle(Long driverId, Long vehicleId, VehicleRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if (!vehicle.getDriver().getDriverId().equals(driver.getDriverId())) {
            throw new ResourceNotFoundException("Vehicle not found for driver id: " + driverId);
        }

        if (!vehicle.getLicensePlate().equalsIgnoreCase(request.licensePlate())) {
            vehicleRepository.findByLicensePlate(request.licensePlate())
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException("Vehicle already exists with license plate: " + request.licensePlate());
                    });
        }

        if (!vehicle.getRegistrationNumber().equalsIgnoreCase(request.registrationNumber())) {
            vehicleRepository.findByRegistrationNumber(request.registrationNumber())
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException("Vehicle already exists with registration number: " + request.registrationNumber());
                    });
        }

        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setLicensePlate(request.licensePlate());
        vehicle.setColor(request.color());
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setRegistrationNumber(request.registrationNumber());

        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    private void validateUniqueFields(String licensePlate, String registrationNumber) {
        vehicleRepository.findByLicensePlate(licensePlate)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Vehicle already exists with license plate: " + licensePlate);
                });

        vehicleRepository.findByRegistrationNumber(registrationNumber)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Vehicle already exists with registration number: " + registrationNumber);
                });
    }
}
