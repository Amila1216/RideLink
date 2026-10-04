package com.ridelink.ride_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ride_service.config.JwtAuthenticationFilter;
import com.ridelink.ride_service.config.SecurityConfig;
import com.ridelink.ride_service.model.Ride;
import com.ridelink.ride_service.model.RideStatus;
import com.ridelink.ride_service.service.JwtService;
import com.ridelink.ride_service.service.RideService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class
})
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideService rideService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("Negative: POST /api/rides without JWT returns 401")
    void createRide_WithoutJwt_Returns401Unauthorized()
            throws Exception {

        Map<String, String> payload = Map.of(
                "pickupLocation",
                "Colombo",
                "dropoffLocation",
                "Kandy"
        );

        mockMvc.perform(
                        post("/api/rides")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                payload
                                        )
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Authentication is required to access this resource"
                                )
                );
    }

    @Test
    @DisplayName("Negative: DRIVER role cannot create a ride and receives 403")
    void createRide_DriverRole_Returns403Forbidden()
            throws Exception {

        String token = "driver-token";

        mockDriverToken(
                token,
                "driver-account-456"
        );

        Map<String, String> payload = Map.of(
                "pickupLocation",
                "Colombo",
                "dropoffLocation",
                "Kandy"
        );

        mockMvc.perform(
                        post("/api/rides")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                payload
                                        )
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Forbidden")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "You do not have permission to access this resource"
                                )
                );
    }

    @Test
    @DisplayName("Negative: POST /api/rides with blank locations returns 400")
    void createRide_BlankFields_Returns400BadRequest()
            throws Exception {

        String token = "passenger-token";

        mockPassengerToken(
                token,
                "passenger-123"
        );

        Map<String, String> payload = Map.of(
                "pickupLocation", "",
                "dropoffLocation", ""
        );

        mockMvc.perform(
                        post("/api/rides")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                payload
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath("$.validationErrors.pickupLocation")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.validationErrors.dropoffLocation")
                                .exists()
                );
    }

    @Test
    @DisplayName("Positive: Passenger creates ride and passengerId comes from JWT")
    void createRide_ValidFields_Returns201Created()
            throws Exception {

        String token = "passenger-token";
        String passengerId = "passenger-123";

        mockPassengerToken(
                token,
                passengerId
        );

        Map<String, String> payload = Map.of(
                "pickupLocation",
                "Galle Road, Colombo",
                "dropoffLocation",
                "Main Street, Negombo"
        );

        Ride createdRide = new Ride(
                passengerId,
                null,
                "Galle Road, Colombo",
                "Main Street, Negombo",
                RideStatus.REQUESTED
        );

        createdRide.setId("ride-555");

        when(rideService.createRide(any(Ride.class)))
                .thenReturn(createdRide);

        mockMvc.perform(
                        post("/api/rides")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                payload
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value("ride-555")
                )
                .andExpect(
                        jsonPath("$.passengerId")
                                .value(passengerId)
                )
                .andExpect(
                        jsonPath("$.pickupLocation")
                                .value("Galle Road, Colombo")
                )
                .andExpect(
                        jsonPath("$.dropoffLocation")
                                .value("Main Street, Negombo")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("REQUESTED")
                );
    }

    @Test
    @DisplayName("Negative: Invalid lifecycle transition returns 400")
    void completeRide_InvalidTransition_Returns400BadRequest()
            throws Exception {

        String token = "driver-token";
        String rideId = "ride-123";
        String driverEmail = "driver@example.com";

        mockDriverToken(
                token,
                "driver-account-456"
        );

        when(
                rideService.completeRide(
                        eq(rideId),
                        eq(driverEmail)
                )
        )
                .thenThrow(
                        new IllegalStateException(
                                "Invalid ride status transition: REQUESTED -> COMPLETED"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/rides/{id}/complete",
                                rideId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid ride status transition: REQUESTED -> COMPLETED"
                                )
                );
    }

    private void mockPassengerToken(
            String token,
            String accountId) {

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractAccountId(token))
                .thenReturn(accountId);

        when(jwtService.extractEmail(token))
                .thenReturn("passenger@example.com");

        when(jwtService.extractRole(token))
                .thenReturn("PASSENGER");
    }

    private void mockDriverToken(
            String token,
            String accountId) {

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractAccountId(token))
                .thenReturn(accountId);

        when(jwtService.extractEmail(token))
                .thenReturn("driver@example.com");

        when(jwtService.extractRole(token))
                .thenReturn("DRIVER");
    }
}