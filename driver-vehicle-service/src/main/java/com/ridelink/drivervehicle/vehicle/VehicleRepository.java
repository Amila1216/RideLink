package com.ridelink.drivervehicle.vehicle;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByDriverIdIn(List<Long> driverIds);
}
