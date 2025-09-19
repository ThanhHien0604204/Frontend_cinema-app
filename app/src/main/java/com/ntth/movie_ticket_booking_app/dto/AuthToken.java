package com.ntth.movie_ticket_booking_app.dto;

import com.google.gson.annotations.SerializedName;

public class AuthToken {
    @SerializedName("token")
    private String token;

    public String getAccessToken() {
        return token; // Trả về giá trị của trường "token"
    }

    public void setAccessToken(String token) {
        this. token = token;
    }
}
