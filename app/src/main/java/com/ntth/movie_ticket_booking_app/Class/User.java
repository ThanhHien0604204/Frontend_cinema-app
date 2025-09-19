package com.ntth.movie_ticket_booking_app.Class;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("userName")
    private String userName;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;
//    @SerializedName("diemTV")
//    private int diemTV;
//
//    @SerializedName("rankId")
//    private String rankId;
//
//    @SerializedName("rankName")
//    private String rankName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
