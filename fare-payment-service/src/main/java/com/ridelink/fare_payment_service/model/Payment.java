package com.ridelink.fare_payment_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String rideId;
    private double amount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime paymentDate;

    public Payment() {
    }

    public Payment(
            String rideId,
            double amount,
            String paymentMethod,
            String paymentStatus,
            String transactionId,
            LocalDateTime paymentDate) {

        this.rideId = rideId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
        this.paymentDate = paymentDate;
    }

    public String getId() {
        return id;
    }

    public String getRideId() {
        return rideId;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}