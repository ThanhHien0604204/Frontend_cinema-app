package com.ntth.movie_ticket_booking_app.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateShowtimeRequest {
    @SerializedName("movieId")
    private String movieId;

    @SerializedName("roomId")
    private String roomId;

    @SerializedName("sessionName")
    private String sessionName;

    @SerializedName("date")
    private String date; // "yyyy-MM-dd"

    @SerializedName("startTime")
    private String startTime; // "HH:mm"

    @SerializedName("endTime")
    private String endTime; // "HH:mm"

    @SerializedName("price")
    private Integer price;

    @SerializedName("totalSeats")
    private Integer totalSeats;

    @SerializedName("availableSeats")
    private Integer availableSeats;

    // Constructor mặc định
    public UpdateShowtimeRequest() {}

    // Constructor đầy đủ
    public UpdateShowtimeRequest(String movieId, String roomId, String sessionName, String date,
                                 String startTime, String endTime, Integer price,
                                 Integer totalSeats, Integer availableSeats) {
        this.movieId = movieId;
        this.roomId = roomId;
        this.sessionName = sessionName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }

    // Getter (bắt buộc cho Gson/Retrofit)
    public String getMovieId() {
        return movieId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }
}