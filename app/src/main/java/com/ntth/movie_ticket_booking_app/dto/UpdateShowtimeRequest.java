package com.ntth.movie_ticket_booking_app.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateShowtimeRequest {
    @SerializedName("movieId")
    private String movieId;

    @SerializedName("roomId")
    private String roomId;

    @SerializedName("sessionName")
    private String sessionName;

    @SerializedName("date")        // "yyyy-MM-dd"
    private String date;

    @SerializedName("startTime")     // ⬅️ đổi tên key đúng backend
    private String startTime;        // "HH:mm"

    @SerializedName("endTime")       // ⬅️ đổi tên key đúng backend
    private String endTime;          // "HH:mm"

    @SerializedName("price")
    private Long price;            // ⬅️ dùng Long an toàn hơn

    @SerializedName("totalSeats")
    private Integer totalSeats;

    @SerializedName("availableSeats")
    private Integer availableSeats;

    public UpdateShowtimeRequest(String movieId, String roomId, String sessionName,
                                 String date, String startAt, String endAt,
                                 Long price, Integer totalSeats, Integer availableSeats) {
        this.movieId = movieId;
        this.roomId = roomId;
        this.sessionName = sessionName;
        this.date = date;
        this.startTime = startAt;
        this.endTime = endAt;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }

    // Getters
    public String getMovieId() { return movieId; }
    public String getRoomId() { return roomId; }
    public String getSessionName() { return sessionName; }
    public String getDate() { return date; }

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
    public Integer getTotalSeats() { return totalSeats; }
    public Integer getAvailableSeats() { return availableSeats; }
}
