package com.ansh.fintech.dto;

public class PaymentVerifyResponse {
    private boolean verified;
    private String message;
    private String status;

    public PaymentVerifyResponse() {}

    public PaymentVerifyResponse(boolean verified, String message, String status) {
        this.verified = verified;
        this.message = message;
        this.status = status;
    }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
