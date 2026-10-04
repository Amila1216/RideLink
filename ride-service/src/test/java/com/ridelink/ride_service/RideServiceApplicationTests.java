package com.ridelink.ride_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "spring.data.mongodb.uri=mongodb://localhost:27017/ridelink_test",
                "driver-vehicle-service.auth.username=test-admin",
                "driver-vehicle-service.auth.password=test-password"
        }
)
class RideServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}