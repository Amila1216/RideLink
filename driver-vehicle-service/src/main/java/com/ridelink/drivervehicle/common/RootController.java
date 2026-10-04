package com.ridelink.drivervehicle.common;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "serviceName", "RideLink Driver & Vehicle Service",
                "status", "UP",
                "version", "1.0.0"
        );
    }
}
