package com.ntth.movie_ticket_booking_app.dto;

public class UpdateUserRequest {
    public String userName;   // để null nếu không đổi
    public String email;      // để null nếu không đổi

    public UpdateUserRequest() {}
    public UpdateUserRequest(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }
}