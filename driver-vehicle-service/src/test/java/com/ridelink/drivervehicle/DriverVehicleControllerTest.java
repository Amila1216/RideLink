package com.ridelink.drivervehicle;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(authorities = "SCOPE_ADMIN")
class DriverVehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithAnonymousUser
    void protectedApi_shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/available"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_USER")
    void protectedApi_shouldRejectNonAdminUser() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/available"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void login_shouldIssueJwtThatCanAccessAdminApi() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "test-admin",
                                "password", "test-admin-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("accessToken").asText();
        mockMvc.perform(get("/api/v1/drivers/available")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void login_shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "test-admin",
                                "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rootEndpoint_shouldReturnServiceInfo() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("RideLink Driver & Vehicle Service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void missingEndpoint_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createDriver_shouldReturnCreated_whenValid() throws Exception {
        Map<String, Object> request = Map.of(
                "firstName", "Alice",
                "lastName", "Johnson",
                "email", "alice.johnson@example.com",
                "phoneNumber", "+1234567890",
                "licenseNumber", "DL-1001",
                "driverAvailability", "AVAILABLE"
        );

        mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.driverAvailability").value("AVAILABLE"));
    }

    @Test
    void getDriver_shouldReturnDriver_whenExists() throws Exception {
        Map<String, Object> request = Map.of(
                "firstName", "Bob",
                "lastName", "Smith",
                "email", "bob.smith@example.com",
                "phoneNumber", "+1234567891",
                "licenseNumber", "DL-2002",
                "driverAvailability", "OFFLINE"
        );

        String response = mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("driverId").asLong();

        mockMvc.perform(get("/api/v1/drivers/{driverId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(id))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void updateDriver_shouldReturnUpdatedDriver_whenValid() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "firstName", "Charlie",
                "lastName", "Brown",
                "email", "charlie.brown@example.com",
                "phoneNumber", "+1234567892",
                "licenseNumber", "DL-3003",
                "driverAvailability", "AVAILABLE"
        );

        String createResponse = mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("driverId").asLong();

        Map<String, Object> updateRequest = Map.of(
                "firstName", "Charlie",
                "lastName", "Brown",
                "email", "charlie.updated@example.com",
                "phoneNumber", "+1234567899",
                "licenseNumber", "DL-3004",
                "driverAvailability", "BUSY"
        );

        mockMvc.perform(put("/api/v1/drivers/{driverId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("charlie.updated@example.com"))
                .andExpect(jsonPath("$.driverAvailability").value("BUSY"));
    }

    @Test
    void createVehicle_shouldReturnCreated_whenValid() throws Exception {
        Map<String, Object> driverRequest = Map.of(
                "firstName", "Diana",
                "lastName", "White",
                "email", "diana.white@example.com",
                "phoneNumber", "+1234567893",
                "licenseNumber", "DL-4004",
                "driverAvailability", "AVAILABLE"
        );

        String driverResponse = mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long driverId = objectMapper.readTree(driverResponse).get("driverId").asLong();

        Map<String, Object> vehicleRequest = Map.of(
                "make", "Toyota",
                "model", "Corolla",
                "year", 2022,
                "licensePlate", "ABC-123",
                "color", "Silver",
                "vehicleType", "SEDAN",
                "registrationNumber", "REG-1001"
        );

        mockMvc.perform(post("/api/v1/drivers/{driverId}/vehicles", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.driverId").value(driverId));
    }

    @Test
    void getVehicle_shouldReturnVehicle_whenExists() throws Exception {
        Long driverId = createDriver("Eva", "Green", "eva.green@example.com", "DL-5005");

        Map<String, Object> vehicleRequest = Map.of(
                "make", "Honda",
                "model", "Civic",
                "year", 2021,
                "licensePlate", "XYZ-789",
                "color", "Blue",
                "vehicleType", "SEDAN",
                "registrationNumber", "REG-2002"
        );

        String vehicleResponse = mockMvc.perform(post("/api/v1/drivers/{driverId}/vehicles", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long vehicleId = objectMapper.readTree(vehicleResponse).get("vehicleId").asLong();

        mockMvc.perform(get("/api/v1/drivers/{driverId}/vehicles/{vehicleId}", driverId, vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(vehicleId))
                .andExpect(jsonPath("$.model").value("Civic"));
    }

    @Test
    void updateVehicle_shouldReturnUpdatedVehicle_whenValid() throws Exception {
        Long driverId = createDriver("Frank", "Blue", "frank.blue@example.com", "DL-6006");

        Map<String, Object> vehicleRequest = Map.of(
                "make", "Hyundai",
                "model", "Elantra",
                "year", 2020,
                "licensePlate", "LMN-456",
                "color", "Black",
                "vehicleType", "SEDAN",
                "registrationNumber", "REG-3003"
        );

        String vehicleResponse = mockMvc.perform(post("/api/v1/drivers/{driverId}/vehicles", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long vehicleId = objectMapper.readTree(vehicleResponse).get("vehicleId").asLong();

        Map<String, Object> updateRequest = Map.of(
                "make", "Hyundai",
                "model", "Sonata",
                "year", 2023,
                "licensePlate", "LMN-999",
                "color", "White",
                "vehicleType", "SEDAN",
                "registrationNumber", "REG-3004"
        );

        mockMvc.perform(put("/api/v1/drivers/{driverId}/vehicles/{vehicleId}", driverId, vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Sonata"))
                .andExpect(jsonPath("$.licensePlate").value("LMN-999"));
    }

    @Test
    void serviceArea_shouldSetAndRetrieve_whenValid() throws Exception {
        Long driverId = createDriver("Ivy", "Jones", "ivy.jones@example.com", "DL-9009");
        Map<String, Object> request = Map.of(
                "serviceArea", "Colombo",
                "serviceAreaDetails", "Central and northern suburbs"
        );

        mockMvc.perform(put("/api/v1/drivers/{driverId}/service-area", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(driverId))
                .andExpect(jsonPath("$.serviceArea").value("Colombo"))
                .andExpect(jsonPath("$.serviceAreaDetails").value("Central and northern suburbs"));

        mockMvc.perform(get("/api/v1/drivers/{driverId}/service-area", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceArea").value("Colombo"));
    }

    @Test
    void simulatedLocation_shouldUpdateAndRetrieve_whenValid() throws Exception {
        Long driverId = createDriver("Jack", "Perera", "jack.perera@example.com", "DL-9010");
        Map<String, Object> request = Map.of("latitude", 6.9271, "longitude", 79.8612);

        mockMvc.perform(put("/api/v1/drivers/{driverId}/location", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(driverId))
                .andExpect(jsonPath("$.latitude").value(6.9271))
                .andExpect(jsonPath("$.longitude").value(79.8612));

        mockMvc.perform(get("/api/v1/drivers/{driverId}/location", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(6.9271))
                .andExpect(jsonPath("$.longitude").value(79.8612));
    }

    @Test
    void availability_shouldUpdateAndRetrieve_whenValid() throws Exception {
        Long driverId = createDriver("Maya", "Perera", "maya.perera@example.com", "DL-9013");

        mockMvc.perform(put("/api/v1/drivers/{driverId}/availability", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "AVAILABLE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(driverId))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        mockMvc.perform(put("/api/v1/drivers/{driverId}/availability", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "UNAVAILABLE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNAVAILABLE"));

        mockMvc.perform(get("/api/v1/drivers/{driverId}/availability", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNAVAILABLE"));
    }

    @Test
    void eligibleDrivers_shouldReturnOnlyAvailableDriversInRequestedArea() throws Exception {
        Long matchingDriver = createDriver("Olivia", "Green", "olivia.green@example.com", "DL-ELIG-1");
        Long unavailableDriver = createDriver("Peter", "Gray", "peter.gray@example.com", "DL-ELIG-2");
        Long differentAreaDriver = createDriver("Quinn", "White", "quinn.white@example.com", "DL-ELIG-3");
        addVehicle(matchingDriver, "ELG-001", "ELG-REG-1");
        addVehicle(unavailableDriver, "ELG-002", "ELG-REG-2");
        addVehicle(differentAreaDriver, "ELG-003", "ELG-REG-3");
        setServiceArea(matchingDriver, "Colombo");
        setServiceArea(unavailableDriver, "Colombo");
        setServiceArea(differentAreaDriver, "Kandy");
        setLocation(matchingDriver);
        setLocation(unavailableDriver);
        setLocation(differentAreaDriver);
        mockMvc.perform(put("/api/v1/drivers/{driverId}/availability", unavailableDriver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "UNAVAILABLE"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/drivers/eligible")
                        .param("serviceArea", "colombo")
                        .param("requireLocation", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drivers.length()").value(1))
                .andExpect(jsonPath("$.drivers[0].driverId").value(matchingDriver))
                .andExpect(jsonPath("$.drivers[0].availability").value("AVAILABLE"))
                .andExpect(jsonPath("$.drivers[0].vehicles[0].vehicleId").isNumber())
                .andExpect(jsonPath("$.drivers[0].email").doesNotExist());
    }

    @Test
    void eligibleDrivers_shouldReturnEmptyListWhenNoDriversMatch() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/eligible").param("serviceArea", "Colombo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drivers").isArray())
                .andExpect(jsonPath("$.drivers").isEmpty());
    }

    @Test
    void eligibleDrivers_shouldRejectMissingOrInvalidServiceArea() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/eligible"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/v1/drivers/eligible").param("serviceArea", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/v1/drivers/eligible").param("serviceArea", "x".repeat(101)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eligibleDrivers_shouldExcludeDriverWithoutLocationWhenRequired() throws Exception {
        Long driverId = createDriver("Rae", "Blue", "rae.blue@example.com", "DL-ELIG-4");
        addVehicle(driverId, "ELG-004", "ELG-REG-4");
        setServiceArea(driverId, "Colombo");

        mockMvc.perform(get("/api/v1/drivers/eligible")
                        .param("serviceArea", "Colombo")
                        .param("requireLocation", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drivers").isEmpty());
    }

    @Test
    void updateAvailability_shouldReturnBadRequest_whenStatusIsUnsupportedOrMissing() throws Exception {
        Long driverId = createDriver("Noah", "Silva", "noah.silva@example.com", "DL-9014");

        mockMvc.perform(put("/api/v1/drivers/{driverId}/availability", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "BUSY"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(put("/api/v1/drivers/{driverId}/availability", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(put("/api/v1/drivers/{driverId}/availability", driverId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or missing request body"));
    }

    @Test
    void availability_shouldReturnBadRequestForInvalidDriverId() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/{driverId}/availability", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void availability_shouldReturnNotFound_whenDriverDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/{driverId}/availability", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateLocation_shouldReturnBadRequest_whenCoordinatesAreOutOfRange() throws Exception {
        Long driverId = createDriver("Karen", "Silva", "karen.silva@example.com", "DL-9011");
        Map<String, Object> request = Map.of("latitude", 91.0, "longitude", -181.0);

        mockMvc.perform(put("/api/v1/drivers/{driverId}/location", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateServiceArea_shouldReturnBadRequest_whenAreaIsBlank() throws Exception {
        Long driverId = createDriver("Liam", "Fernando", "liam.fernando@example.com", "DL-9012");
        Map<String, Object> request = Map.of("serviceArea", "   ");

        mockMvc.perform(put("/api/v1/drivers/{driverId}/service-area", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLocation_shouldReturnNotFound_whenDriverDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/{driverId}/location", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateServiceArea_shouldReturnNotFound_whenDriverDoesNotExist() throws Exception {
        Map<String, Object> request = Map.of("serviceArea", "Colombo");

        mockMvc.perform(put("/api/v1/drivers/{driverId}/service-area", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDriver_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        Map<String, Object> request = Map.of(
                "firstName", "",
                "lastName", "New",
                "email", "invalid-email",
                "phoneNumber", "+123",
                "licenseNumber", "",
                "driverAvailability", "AVAILABLE"
        );

        mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVehicle_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        Long driverId = createDriver("Grace", "Hall", "grace.hall@example.com", "DL-7007");

        Map<String, Object> request = Map.of(
                "make", "",
                "model", "",
                "year", 1800,
                "licensePlate", "",
                "color", "Red",
                "vehicleType", "SUV",
                "registrationNumber", ""
        );

        mockMvc.perform(post("/api/v1/drivers/{driverId}/vehicles", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDriver_shouldReturnNotFound_whenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/{driverId}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getVehicle_shouldReturnNotFound_whenMissing() throws Exception {
        Long driverId = createDriver("Harris", "King", "harris.king@example.com", "DL-8008");

        mockMvc.perform(get("/api/v1/drivers/{driverId}/vehicles/{vehicleId}", driverId, 999999L))
                .andExpect(status().isNotFound());
    }

    private Long createDriver(String firstName, String lastName, String email, String licenseNumber) throws Exception {
        Map<String, Object> request = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "phoneNumber", "+1234567890",
                "licenseNumber", licenseNumber,
                "driverAvailability", "AVAILABLE"
        );

        String response = mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("driverId").asLong();
    }

        private void addVehicle(Long driverId, String licensePlate, String registrationNumber) throws Exception {
                Map<String, Object> request = Map.of(
                                "make", "Toyota",
                                "model", "Corolla",
                                "year", 2022,
                                "licensePlate", licensePlate,
                                "color", "Silver",
                                "vehicleType", "SEDAN",
                                "registrationNumber", registrationNumber
                );
                mockMvc.perform(post("/api/v1/drivers/{driverId}/vehicles", driverId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        private void setServiceArea(Long driverId, String serviceArea) throws Exception {
                mockMvc.perform(put("/api/v1/drivers/{driverId}/service-area", driverId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(Map.of("serviceArea", serviceArea))))
                                .andExpect(status().isOk());
        }

        private void setLocation(Long driverId) throws Exception {
                mockMvc.perform(put("/api/v1/drivers/{driverId}/location", driverId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(Map.of("latitude", 6.9271, "longitude", 79.8612))))
                                .andExpect(status().isOk());
        }
}
