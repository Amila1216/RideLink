package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.1", message = "Payment amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    public PaymentRequest() {
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}