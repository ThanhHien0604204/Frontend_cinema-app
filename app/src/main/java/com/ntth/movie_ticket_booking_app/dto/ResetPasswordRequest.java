package com.ntth.movie_ticket_booking_app.dto;

public class ResetPasswordRequest {
    private String Otp;
    private String newPassword;
    private String confirmNewPassword;

    public ResetPasswordRequest(String Otp, String newPassword, String confirmNewPassword) {
        this.Otp =Otp;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

    // Getters & Setters

    public String getOtp() {
        return Otp;
    }
    public void setOtp(String otp) {
        Otp = otp;
    }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}