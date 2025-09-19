package com.ntth.movie_ticket_booking_app.dto;

public class RegisterRequest {
    public String userName;
    public String email;
    public String password;
    public String role = "USER";  // Mặc định USER

    public RegisterRequest() {}
    public RegisterRequest(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    // Getters và setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
