package com.ridelink.ride_service.client;

import com.ridelink.ride_service.dto.AvailableDriverResponse;
import com.ridelink.ride_service.dto.DriverDetailsResponse;
import com.ridelink.ride_service.exception.DriverServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

@Component
public class DriverServiceClient {

    private final RestClient restClient;
    private final String username;
    private final String password;

    public DriverServiceClient(
            @Value("${driver-vehicle-service.base-url}")
            String driverServiceBaseUrl,

            @Value("${driver-vehicle-service.auth.username}")
            String username,

            @Value("${driver-vehicle-service.auth.password}")
            String password) {

        this.restClient = RestClient.builder()
                .baseUrl(driverServiceBaseUrl)
                .build();

        this.username = username;
        this.password = password;
    }

    public List<AvailableDriverResponse> getAvailableDrivers() {

        try {
            String accessToken = getAccessToken();

            List<AvailableDriverResponse> drivers = restClient
                    .get()
                    .uri("/drivers/available")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken
                    )
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
            String accessToken = getAccessToken();

            DriverDetailsResponse driver = restClient
                    .get()
                    .uri(
                            "/drivers/{driverId}",
                            driverId
                    )
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken
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

    private String getAccessToken() {

        try {
            TokenResponse tokenResponse = restClient
                    .post()
                    .uri("/auth/token")
                    .body(
                            new LoginRequest(
                                    username,
                                    password
                            )
                    )
                    .retrieve()
                    .body(TokenResponse.class);

            if (tokenResponse == null
                    || tokenResponse.accessToken() == null
                    || tokenResponse.accessToken().isBlank()) {

                throw new DriverServiceUnavailableException(
                        "Driver & Vehicle Service returned an empty authentication token"
                );
            }

            return tokenResponse.accessToken();

        } catch (DriverServiceUnavailableException exception) {

            throw exception;

        } catch (RestClientException exception) {

            throw new DriverServiceUnavailableException(
                    "Unable to authenticate with Driver & Vehicle Service",
                    exception
            );
        }
    }

    private record LoginRequest(
            String username,
            String password) {
    }

    private record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresIn) {
    }
}