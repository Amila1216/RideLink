package com.ridelink.ride_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        writeSecurityError(
                                                response,
                                                HttpStatus.UNAUTHORIZED,
                                                "Authentication is required to access this resource"
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        writeSecurityError(
                                                response,
                                                HttpStatus.FORBIDDEN,
                                                "You do not have permission to access this resource"
                                        )
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Passenger creates a ride
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/rides"
                        ).hasRole("PASSENGER")

                        // Automatic driver assignment
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/rides/*/assign-driver"
                        ).hasAnyRole(
                                "PASSENGER",
                                "ADMIN"
                        )

                        // Driver lifecycle operations
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/rides/*/accept",
                                "/api/rides/*/start",
                                "/api/rides/*/complete"
                        ).hasRole("DRIVER")

                        // Passenger, driver or admin can request cancellation.
                        // Ownership checks are enforced in RideService.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/rides/*/cancel"
                        ).hasAnyRole(
                                "PASSENGER",
                                "DRIVER",
                                "ADMIN"
                        )

                        // Passenger ride lookup
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rides/passenger/**"
                        ).hasAnyRole(
                                "PASSENGER",
                                "ADMIN"
                        )

                        // Driver ride lookup
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rides/driver/**"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN"
                        )

                        // General ride update
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/rides/*"
                        ).hasAnyRole(
                                "PASSENGER",
                                "ADMIN"
                        )

                        // Physical deletion is admin-only
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/rides/*"
                        ).hasRole("ADMIN")

                        // All rides list is admin-only
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rides"
                        ).hasRole("ADMIN")

                        // Individual ride lookup requires authentication.
                        // Ownership is checked in RideService.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rides/*"
                        ).authenticated()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private void writeSecurityError(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            String message) throws java.io.IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        objectMapper.writeValue(
                response.getOutputStream(),
                body
        );
    }
}