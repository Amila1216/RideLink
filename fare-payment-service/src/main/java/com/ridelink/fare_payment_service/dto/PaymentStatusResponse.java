package com.ridelink.fare_payment_service.dto;

public class PaymentStatusResponse {

    private String rideId;
    private String paymentStatus;
    private String transactionId;

    public PaymentStatusResponse() {}

    public PaymentStatusResponse(
            String rideId,
            String paymentStatus,
            String transactionId) {
        this.rideId = rideId;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}