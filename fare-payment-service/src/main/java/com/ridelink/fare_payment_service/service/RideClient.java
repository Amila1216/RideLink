package com.ridelink.fare_payment_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class RideClient {

    private final RestClient restClient;

    public RideClient(
            @Value("${ride.service.base-url}") String rideServiceBaseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(rideServiceBaseUrl)
                .build();
    }

    public Map<String, Object> getRideById(String rideId) {

        return restClient.get()
                .uri("/api/rides/{id}", rideId)
                .retrieve()
                .body(Map.class);
    }
}