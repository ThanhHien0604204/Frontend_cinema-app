package com.ntth.movie_ticket_booking_app.dto;

public class HoldSeatsResponse {
    public String holdId;
    public Number amount;   // Number để nhận cả 120000 hoặc 120000.0
    public String expiresAt;

    public String getHoldId() {
        return holdId;
    }

    public void setHoldId(String holdId) {
        this.holdId = holdId;
    }

    public Number getAmount() {
        return amount;
    }

    public void setAmount(Number amount) {
        this.amount = amount;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}

