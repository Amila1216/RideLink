package com.ridelink.drivervehicle.driver;

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

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

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
}