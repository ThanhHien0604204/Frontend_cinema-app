package com.ntth.movie_ticket_booking_app.dto;

import java.util.List;

public class CreateShowtimeRequest {
    public String movieId;
    public String roomId;
    public String sessionName; // Thêm field bắt buộc
    private String date;        // "yyyy-MM-dd"
    private String startTime;   // "HH:mm"
    private String endTime;
    public Long price;
    private Integer totalSeats;      // nếu backend cần
    private Integer availableSeats;

    // Constructors
    public CreateShowtimeRequest() {}

    // Getters and Setters
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
}