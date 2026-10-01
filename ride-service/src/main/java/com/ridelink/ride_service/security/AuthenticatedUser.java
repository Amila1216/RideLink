package com.ridelink.ride_service.security;

public record AuthenticatedUser(
        String accountId,
        String email,
        String role
) {
}