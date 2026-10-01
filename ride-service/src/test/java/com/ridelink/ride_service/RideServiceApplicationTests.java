package com.ridelink.ride_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "spring.data.mongodb.uri=mongodb://localhost:27017/ridelink_test"
        }
)
class RideServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}