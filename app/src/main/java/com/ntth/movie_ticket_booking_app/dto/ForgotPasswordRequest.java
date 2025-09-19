package com.ntth.movie_ticket_booking_app.dto;

public class ForgotPasswordRequest {
    public String email;

    public ForgotPasswordRequest() {}
    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    // Getters và setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
