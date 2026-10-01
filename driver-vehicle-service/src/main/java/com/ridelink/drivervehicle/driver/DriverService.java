package com.ridelink.drivervehicle.driver;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ridelink.drivervehicle.common.DuplicateResourceException;
import com.ridelink.drivervehicle.common.ResourceNotFoundException;
import com.ridelink.drivervehicle.vehicle.Vehicle;

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
                request.driverAvailability() != null
                        ? request.driverAvailability()
                        : DriverAvailability.AVAILABLE
        );

        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with id: " + driverId
                        )
                );

        return DriverResponse.from(driver);
    }

    // Get all drivers who are currently AVAILABLE.
    // Ride Service will use this for driver assignment.
    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDrivers() {

        return driverRepository
                .findByDriverAvailability(DriverAvailability.AVAILABLE)
                .stream()
                .map(DriverResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EligibleDriverSearchResponse findEligibleDrivers(
            EligibleDriverSearchRequest request) {

        String requestedArea = request.serviceArea().trim();

        List<EligibleDriverResponse> eligibleDrivers = driverRepository
                .findByDriverAvailability(DriverAvailability.AVAILABLE)
                .stream()
                .filter(driver -> hasOperationalProfile(driver)
                        && StringUtils.hasText(driver.getServiceArea())
                        && requestedArea.equalsIgnoreCase(driver.getServiceArea().trim())
                        && (!request.requireLocation() || hasValidSimulatedLocation(driver)))
                .map(driver -> new EligibleDriverResponse(
                        driver.getDriverId(),
                        driver.getDriverAvailability(),
                        driver.getVehicles().stream()
                                .filter(this::hasVehicleDetails)
                                .map(vehicle -> new EligibleVehicleResponse(
                                        vehicle.getVehicleId(),
                                        vehicle.getVehicleType()))
                                .toList()))
                .filter(driver -> !driver.vehicles().isEmpty())
                .toList();

        return new EligibleDriverSearchResponse(eligibleDrivers);
    }

    @Transactional
    public DriverAvailabilityResponse updateAvailability(
            Long driverId,
            DriverAvailabilityRequest request) {

        Driver driver = findDriver(driverId);

        if (!"AVAILABLE".equals(request.status())
                && !"UNAVAILABLE".equals(request.status())) {

            throw new IllegalArgumentException(
                    "Availability status must be AVAILABLE or UNAVAILABLE"
            );
        }

        driver.setDriverAvailability(
                DriverAvailability.valueOf(request.status())
        );

        return DriverAvailabilityResponse.from(
                driverRepository.save(driver)
        );
    }

    @Transactional(readOnly = true)
    public DriverAvailabilityResponse getAvailability(Long driverId) {
        return DriverAvailabilityResponse.from(
                findDriver(driverId)
        );
    }

    @Transactional
    public DriverResponse updateDriver(
            Long driverId,
            DriverRequest request) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with id: " + driverId
                        )
                );

        if (!driver.getEmail().equalsIgnoreCase(request.email())) {

            driverRepository.findByEmail(request.email())
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException(
                                "Driver already exists with email: "
                                        + request.email()
                        );
                    });
        }

        if (!driver.getLicenseNumber()
                .equalsIgnoreCase(request.licenseNumber())) {

            driverRepository.findByLicenseNumber(
                            request.licenseNumber()
                    )
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException(
                                "Driver already exists with license number: "
                                        + request.licenseNumber()
                        );
                    });
        }

        driver.setFirstName(request.firstName());
        driver.setLastName(request.lastName());
        driver.setEmail(request.email());
        driver.setPhoneNumber(request.phoneNumber());
        driver.setLicenseNumber(request.licenseNumber());

        driver.setDriverAvailability(
                request.driverAvailability() != null
                        ? request.driverAvailability()
                        : DriverAvailability.AVAILABLE
        );

        return DriverResponse.from(
                driverRepository.save(driver)
        );
    }

    @Transactional
    public ServiceAreaResponse updateServiceArea(
            Long driverId,
            ServiceAreaRequest request) {

        Driver driver = findDriver(driverId);

        driver.setServiceArea(
                request.serviceArea().trim()
        );

        driver.setServiceAreaDetails(
                request.serviceAreaDetails()
        );

        return ServiceAreaResponse.from(
                driverRepository.save(driver)
        );
    }

    @Transactional(readOnly = true)
    public ServiceAreaResponse getServiceArea(Long driverId) {
        return ServiceAreaResponse.from(
                findDriver(driverId)
        );
    }

    @Transactional
    public SimulatedLocationResponse updateSimulatedLocation(
            Long driverId,
            SimulatedLocationRequest request) {

        Driver driver = findDriver(driverId);

        driver.setCurrentLatitude(
                request.latitude()
        );

        driver.setCurrentLongitude(
                request.longitude()
        );

        return SimulatedLocationResponse.from(
                driverRepository.save(driver)
        );
    }

    @Transactional(readOnly = true)
    public SimulatedLocationResponse getSimulatedLocation(
            Long driverId) {

        return SimulatedLocationResponse.from(
                findDriver(driverId)
        );
    }

    private Driver findDriver(Long driverId) {

        return driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with id: " + driverId
                        )
                );
    }

    private boolean hasOperationalProfile(Driver driver) {
        return StringUtils.hasText(driver.getFirstName())
                && StringUtils.hasText(driver.getLastName())
                && StringUtils.hasText(driver.getEmail())
                && StringUtils.hasText(driver.getPhoneNumber())
                && StringUtils.hasText(driver.getLicenseNumber());
    }

    private boolean hasValidSimulatedLocation(Driver driver) {
        Double latitude = driver.getCurrentLatitude();
        Double longitude = driver.getCurrentLongitude();
        return latitude != null && longitude != null
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }

    private boolean hasVehicleDetails(Vehicle vehicle) {
        return vehicle.getVehicleId() != null
                && StringUtils.hasText(vehicle.getMake())
                && StringUtils.hasText(vehicle.getModel())
                && vehicle.getYear() != null && vehicle.getYear() >= 1900
                && StringUtils.hasText(vehicle.getLicensePlate())
                && StringUtils.hasText(vehicle.getColor())
                && vehicle.getVehicleType() != null
                && StringUtils.hasText(vehicle.getRegistrationNumber());
    }

    private void validateUniqueFields(
            String email,
            String licenseNumber) {

        driverRepository.findByEmail(email)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Driver already exists with email: " + email
                    );
                });

        driverRepository.findByLicenseNumber(licenseNumber)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Driver already exists with license number: "
                                    + licenseNumber
                    );
                });
    }
}