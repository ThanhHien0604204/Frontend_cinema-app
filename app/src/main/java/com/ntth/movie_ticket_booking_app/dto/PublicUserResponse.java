package com.ntth.movie_ticket_booking_app.dto;

import com.ntth.movie_ticket_booking_app.Class.User;

public class PublicUserResponse {
    public String id;
    public String userName;
    public String email;      // nếu không muốn lộ email, có thể bỏ field này

    public static PublicUserResponse of(User u) {
        PublicUserResponse dto = new PublicUserResponse();
        dto.id = u.getId();
        dto.userName = u.getUserName();
        dto.email = u.getEmail();
        return dto;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
