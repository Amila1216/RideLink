package com.ridelink.ride_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ride_service.config.SecurityConfig;
import com.ridelink.ride_service.model.Ride;
import com.ridelink.ride_service.model.RideStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
@Import(SecurityConfig.class)
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideService rideService;

    @Test
    @DisplayName("POST /api/rides with blank fields returns 400 Bad Request")
    void testCreateRide_BlankFields_Returns400BadRequest() throws Exception {
        Ride invalidRide = new Ride("", null, "", "", null);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/rides with valid fields returns 201 Created")
    void testCreateRide_ValidFields_Returns201Created() throws Exception {
        Ride validRide = new Ride("passenger-123", null, "Galle Road, Colombo", "Main Street, Negombo", null);
        Ride createdRide = new Ride("passenger-123", null, "Galle Road, Colombo", "Main Street, Negombo", RideStatus.REQUESTED.name());
        createdRide.setId("ride-555");

        when(rideService.createRide(any(Ride.class))).thenReturn(createdRide);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRide)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ride-555"))
                .andExpect(jsonPath("$.passengerId").value("passenger-123"))
                .andExpect(jsonPath("$.pickupLocation").value("Galle Road, Colombo"))
                .andExpect(jsonPath("$.dropoffLocation").value("Main Street, Negombo"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @DisplayName("PATCH /api/rides/{id}/status with invalid transition returns 400 Bad Request and error json")
    void testUpdateStatus_InvalidTransition_Returns400BadRequestWithErrorJson() throws Exception {
        String rideId = "ride-123";
        Map<String, String> payload = Map.of("status", "COMPLETED");

        when(rideService.updateRideStatus(eq(rideId), eq("COMPLETED"), eq(null)))
                .thenThrow(new IllegalStateException("Invalid status transition from REQUESTED to COMPLETED"));

        mockMvc.perform(patch("/api/rides/{id}/status", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status transition from REQUESTED to COMPLETED"));
    }
}
