package com.ridelink.fare_payment_service.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class RideClient {

    private final RestClient restClient;
    private final HttpServletRequest request;

    public RideClient(
            @Value("${ride.service.base-url}") String rideServiceBaseUrl,
            HttpServletRequest request) {

        this.restClient = RestClient.builder()
                .baseUrl(rideServiceBaseUrl)
                .build();

        this.request = request;
    }

    public Map<String, Object> getRideById(String rideId) {

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            return restClient.get()
                    .uri("/api/rides/{id}", rideId)
                    .headers(headers -> {
                        if (authorizationHeader != null) {
                            headers.set(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            );
                        }
                    })
                    .retrieve()
                    .body(Map.class);

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ride not found: " + rideId
            );

        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Ride Service authentication failed"
            );
        }
    }
}