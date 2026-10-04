package com.ridelink.drivervehicle.driver;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Document(collection = "drivers")
public class Driver {

    @Id
    private Long driverId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must be valid")
    private String phoneNumber;

    @NotBlank(message = "License number is required")
    @Indexed(unique = true)
    private String licenseNumber;

    private DriverAvailability driverAvailability = DriverAvailability.AVAILABLE;

    private String serviceArea;

    private String serviceAreaDetails;

    private Double currentLatitude;

    private Double currentLongitude;

    public Driver() {
    }

    public Driver(String firstName, String lastName, String email, String phoneNumber, String licenseNumber,
                  DriverAvailability driverAvailability) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.licenseNumber = licenseNumber;
        this.driverAvailability = driverAvailability != null ? driverAvailability : DriverAvailability.AVAILABLE;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public DriverAvailability getDriverAvailability() {
        return driverAvailability;
    }

    public void setDriverAvailability(DriverAvailability driverAvailability) {
        this.driverAvailability = driverAvailability != null ? driverAvailability : DriverAvailability.AVAILABLE;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getServiceAreaDetails() {
        return serviceAreaDetails;
    }

    public void setServiceAreaDetails(String serviceAreaDetails) {
        this.serviceAreaDetails = serviceAreaDetails;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

}
