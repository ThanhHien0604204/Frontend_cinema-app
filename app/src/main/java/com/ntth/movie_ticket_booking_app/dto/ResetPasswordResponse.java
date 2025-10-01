package com.ntth.movie_ticket_booking_app.dto;

public class ResetPasswordResponse {
    private boolean success;
    private String message;

    // Getters & Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}