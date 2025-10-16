package com.ntth.movie_ticket_booking_app.dto;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("otp") public String otp;
    @SerializedName("newPassword") public String newPassword;
    @SerializedName("confirmNewPassword") public String confirmNewPassword;

    public ResetPasswordRequest(String Otp, String newPassword, String confirmNewPassword) {
        this.otp =Otp;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

    // Getters & Setters

    public String getOtp() {
        return otp;
    }
    public void setOtp(String otp) {
        otp = otp;
    }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}