package com.ridelink.ride_service.client;

import com.ridelink.ride_service.dto.AvailableDriverResponse;
import com.ridelink.ride_service.dto.DriverDetailsResponse;
import com.ridelink.ride_service.exception.DriverServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

@Component
public class DriverServiceClient {

    private final RestClient restClient;

    public DriverServiceClient(
            @Value("${driver-vehicle-service.base-url}")
            String driverServiceBaseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(driverServiceBaseUrl)
                .build();
    }

    public List<AvailableDriverResponse> getAvailableDrivers() {

        try {

            List<AvailableDriverResponse> drivers = restClient
                    .get()
                    .uri("/drivers/available")
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    List<AvailableDriverResponse>>() {
                            }
                    );

            return drivers != null
                    ? drivers
                    : Collections.emptyList();

        } catch (RestClientException exception) {

            throw new DriverServiceUnavailableException(
                    "Driver & Vehicle Service is currently unavailable",
                    exception
            );
        }
    }

    public DriverDetailsResponse getDriverById(
            String driverId) {

        try {

            DriverDetailsResponse driver = restClient
                    .get()
                    .uri(
                            "/drivers/{driverId}",
                            driverId
                    )
                    .retrieve()
                    .body(DriverDetailsResponse.class);

            if (driver == null) {
                throw new DriverServiceUnavailableException(
                        "Driver & Vehicle Service returned an empty driver response"
                );
            }

            return driver;

        } catch (DriverServiceUnavailableException exception) {

            throw exception;

        } catch (RestClientException exception) {

            throw new DriverServiceUnavailableException(
                    "Driver & Vehicle Service is currently unavailable",
                    exception
            );
        }
    }
}