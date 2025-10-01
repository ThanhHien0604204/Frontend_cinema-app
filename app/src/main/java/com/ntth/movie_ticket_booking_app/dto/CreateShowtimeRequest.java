package com.ntth.movie_ticket_booking_app.dto;

import java.util.List;

public class CreateShowtimeRequest {
    public String movieId;
    public String roomId;
    public String sessionName; // Thêm field bắt buộc
    public List<Integer> date; // Thay LocalDate bằng List<Integer> [year, month, day]
    public double startAt;     // Thay String bằng double (timestamp giây)
    public double endAt;       // Thay String bằng double (timestamp giây)
    public Long price;

    // Constructors
    public CreateShowtimeRequest() {}

    // Getters and Setters
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }
    public List<Integer> getDate() { return date; }
    public void setDate(List<Integer> date) { this.date = date; }
    public double getStartAt() { return startAt; }
    public void setStartAt(double startAt) { this.startAt = startAt; }
    public double getEndAt() { return endAt; }
    public void setEndAt(double endAt) { this.endAt = endAt; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
}