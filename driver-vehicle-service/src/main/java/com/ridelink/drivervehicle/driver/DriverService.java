package com.ridelink.drivervehicle.driver;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ridelink.drivervehicle.common.DuplicateResourceException;
import com.ridelink.drivervehicle.common.ResourceNotFoundException;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        validateUniqueFields(request.email(), request.licenseNumber());

        Driver driver = new Driver(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber(),
                request.licenseNumber(),
                request.driverAvailability() != null ? request.driverAvailability() : DriverAvailability.AVAILABLE
        );

        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));
        return DriverResponse.from(driver);
    }

    @Transactional
    public DriverResponse updateDriver(Long driverId, DriverRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        if (!driver.getEmail().equalsIgnoreCase(request.email())) {
            driverRepository.findByEmail(request.email())
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException("Driver already exists with email: " + request.email());
                    });
        }

        if (!driver.getLicenseNumber().equalsIgnoreCase(request.licenseNumber())) {
            driverRepository.findByLicenseNumber(request.licenseNumber())
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException("Driver already exists with license number: " + request.licenseNumber());
                    });
        }

        driver.setFirstName(request.firstName());
        driver.setLastName(request.lastName());
        driver.setEmail(request.email());
        driver.setPhoneNumber(request.phoneNumber());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setDriverAvailability(request.driverAvailability() != null ? request.driverAvailability() : DriverAvailability.AVAILABLE);

        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional
    public ServiceAreaResponse updateServiceArea(Long driverId, ServiceAreaRequest request) {
        Driver driver = findDriver(driverId);
        driver.setServiceArea(request.serviceArea().trim());
        driver.setServiceAreaDetails(request.serviceAreaDetails());
        return ServiceAreaResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public ServiceAreaResponse getServiceArea(Long driverId) {
        return ServiceAreaResponse.from(findDriver(driverId));
    }

    @Transactional
    public SimulatedLocationResponse updateSimulatedLocation(Long driverId, SimulatedLocationRequest request) {
        Driver driver = findDriver(driverId);
        driver.setCurrentLatitude(request.latitude());
        driver.setCurrentLongitude(request.longitude());
        return SimulatedLocationResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public SimulatedLocationResponse getSimulatedLocation(Long driverId) {
        return SimulatedLocationResponse.from(findDriver(driverId));
    }

    private Driver findDriver(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));
    }

    private void validateUniqueFields(String email, String licenseNumber) {
        driverRepository.findByEmail(email)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Driver already exists with email: " + email);
                });

        driverRepository.findByLicenseNumber(licenseNumber)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Driver already exists with license number: " + licenseNumber);
                });
    }
}
