package com.ridelink.drivervehicle.driver;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ridelink.drivervehicle.common.ResourceNotFoundException;
import com.ridelink.drivervehicle.vehicle.Vehicle;
import com.ridelink.drivervehicle.vehicle.VehicleType;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

        @Test
        void findEligibleDrivers_shouldReturnAvailableMatchingDriverAndMinimalVehicleInfo() {
        Driver eligible = eligibleDriver(1L, "Colombo");
        Driver unavailable = eligibleDriver(2L, "Colombo");
        unavailable.setDriverAvailability(DriverAvailability.UNAVAILABLE);
        Driver otherArea = eligibleDriver(3L, "Kandy");
        when(driverRepository.findByDriverAvailability(DriverAvailability.AVAILABLE))
            .thenReturn(List.of(eligible, otherArea));

        EligibleDriverSearchResponse response = new DriverService(driverRepository)
            .findEligibleDrivers(new EligibleDriverSearchRequest(" colombo ", false));

        assertEquals(1, response.drivers().size());
        assertEquals(1L, response.drivers().get(0).driverId());
        assertEquals(DriverAvailability.AVAILABLE, response.drivers().get(0).availability());
        assertEquals(21L, response.drivers().get(0).vehicles().get(0).vehicleId());
        assertEquals(VehicleType.SEDAN, response.drivers().get(0).vehicles().get(0).vehicleType());
        }

        @Test
        void findEligibleDrivers_shouldExcludeMissingProfileOrVehicleDetails() {
        Driver missingProfile = eligibleDriver(1L, "Colombo");
        missingProfile.setFirstName(" ");
        Driver missingVehicle = driver(2L);
        missingVehicle.setServiceArea("Colombo");
        Driver incompleteVehicle = eligibleDriver(3L, "Colombo");
        incompleteVehicle.getVehicles().get(0).setMake("");
        when(driverRepository.findByDriverAvailability(DriverAvailability.AVAILABLE))
            .thenReturn(List.of(missingProfile, missingVehicle, incompleteVehicle));

        EligibleDriverSearchResponse response = new DriverService(driverRepository)
            .findEligibleDrivers(new EligibleDriverSearchRequest("Colombo", false));

        assertEquals(List.of(), response.drivers());
        }

        @Test
        void findEligibleDrivers_shouldRequireCompleteLocationOnlyWhenRequested() {
        Driver driver = eligibleDriver(1L, "Colombo");
        when(driverRepository.findByDriverAvailability(DriverAvailability.AVAILABLE))
            .thenReturn(List.of(driver));
        DriverService service = new DriverService(driverRepository);

        assertEquals(1, service.findEligibleDrivers(
            new EligibleDriverSearchRequest("Colombo", false)).drivers().size());
        assertEquals(List.of(), service.findEligibleDrivers(
            new EligibleDriverSearchRequest("Colombo", true)).drivers());

        driver.setCurrentLatitude(6.9271);
        driver.setCurrentLongitude(79.8612);
        assertEquals(1, service.findEligibleDrivers(
            new EligibleDriverSearchRequest("Colombo", true)).drivers().size());
        }

        @Test
        void findEligibleDrivers_shouldReturnEmptyWhenNoDriverMatches() {
        when(driverRepository.findByDriverAvailability(DriverAvailability.AVAILABLE))
            .thenReturn(List.of());

        EligibleDriverSearchResponse response = new DriverService(driverRepository)
            .findEligibleDrivers(new EligibleDriverSearchRequest("Colombo", false));

        assertEquals(List.of(), response.drivers());
        }

    @Test
    void updateServiceArea_shouldSaveAreaForDriver() {
        Driver driver = driver(1L);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        ServiceAreaResponse response = new DriverService(driverRepository)
                .updateServiceArea(1L, new ServiceAreaRequest(" Colombo ", "City limits"));

        assertEquals("Colombo", response.serviceArea());
        assertEquals("City limits", response.serviceAreaDetails());
        verify(driverRepository).save(driver);
    }

    @Test
    void getServiceArea_shouldReturnStoredArea() {
        Driver driver = driver(1L);
        driver.setServiceArea("Kandy");
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        ServiceAreaResponse response = new DriverService(driverRepository).getServiceArea(1L);

        assertEquals("Kandy", response.serviceArea());
    }

    @Test
    void updateSimulatedLocation_shouldSaveCoordinates() {
        Driver driver = driver(1L);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        SimulatedLocationResponse response = new DriverService(driverRepository)
                .updateSimulatedLocation(1L, new SimulatedLocationRequest(6.9271, 79.8612));

        assertEquals(6.9271, response.latitude());
        assertEquals(79.8612, response.longitude());
        verify(driverRepository).save(driver);
    }

    @Test
    void getSimulatedLocation_shouldReturnStoredCoordinates() {
        Driver driver = driver(1L);
        driver.setCurrentLatitude(6.9271);
        driver.setCurrentLongitude(79.8612);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        SimulatedLocationResponse response = new DriverService(driverRepository).getSimulatedLocation(1L);

        assertEquals(6.9271, response.latitude());
        assertEquals(79.8612, response.longitude());
    }

    @Test
    void updateAvailability_shouldSetAvailable() {
        Driver driver = driver(1L);
        driver.setDriverAvailability(DriverAvailability.UNAVAILABLE);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        DriverAvailabilityResponse response = new DriverService(driverRepository)
                .updateAvailability(1L, new DriverAvailabilityRequest("AVAILABLE"));

        assertEquals(DriverAvailability.AVAILABLE, response.status());
        verify(driverRepository).save(driver);
    }

    @Test
    void updateAvailability_shouldSetUnavailable() {
        Driver driver = driver(1L);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        DriverAvailabilityResponse response = new DriverService(driverRepository)
                .updateAvailability(1L, new DriverAvailabilityRequest("UNAVAILABLE"));

        assertEquals(DriverAvailability.UNAVAILABLE, response.status());
        verify(driverRepository).save(driver);
    }

    @Test
    void getAvailability_shouldReturnStoredStatus() {
        Driver driver = driver(1L);
        driver.setDriverAvailability(DriverAvailability.UNAVAILABLE);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        DriverAvailabilityResponse response = new DriverService(driverRepository).getAvailability(1L);

        assertEquals(DriverAvailability.UNAVAILABLE, response.status());
    }

    @Test
    void updateAvailability_shouldRejectUnsupportedStatus() {
        Driver driver = driver(1L);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        assertThrows(IllegalArgumentException.class,
                () -> new DriverService(driverRepository)
                        .updateAvailability(1L, new DriverAvailabilityRequest("BUSY")));
    }

    @Test
    void getAvailability_shouldThrowWhenDriverDoesNotExist() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> new DriverService(driverRepository).getAvailability(99L));
    }

    @Test
    void getServiceArea_shouldThrowWhenDriverDoesNotExist() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> new DriverService(driverRepository).getServiceArea(99L));
    }

    private Driver driver(Long id) {
        Driver driver = new Driver("Test", "Driver", "test" + id + "@example.com", "+1234567890",
                "DL-" + id, DriverAvailability.AVAILABLE);
        driver.setDriverId(id);
        return driver;
    }

    private Driver eligibleDriver(Long id, String serviceArea) {
        Driver driver = driver(id);
        driver.setServiceArea(serviceArea);
        Vehicle vehicle = new Vehicle("Toyota", "Corolla", 2022, "ABC-" + id,
                "Silver", VehicleType.SEDAN, "REG-" + id, driver);
        vehicle.setVehicleId(id + 20);
        driver.setVehicles(List.of(vehicle));
        return driver;
    }
}