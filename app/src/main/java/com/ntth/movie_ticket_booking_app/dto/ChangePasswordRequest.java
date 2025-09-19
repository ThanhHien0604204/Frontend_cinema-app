package com.ntth.movie_ticket_booking_app.dto;


import android.support.annotation.Size;

public class ChangePasswordRequest {
    public String currentPassword;
    public String newPassword;
    public String confirmNewPassword;

    public ChangePasswordRequest() {}
    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmNewPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }
}
