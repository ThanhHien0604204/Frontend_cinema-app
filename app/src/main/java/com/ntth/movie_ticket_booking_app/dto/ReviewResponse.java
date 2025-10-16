package com.ntth.movie_ticket_booking_app.dto;

import com.google.gson.annotations.SerializedName;

public class ReviewResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("movieId")
    private String movieId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("rating")
    private double rating; // Phải là double/float vì JSON là 5.0

    @SerializedName("content")
    private String content;

    @SerializedName("reviewTime")
    private String reviewTime; // Hoặc Date nếu là timestamp

    // Getter/setter cho tất cả fields
    public String getId() { return id; }
    public String getMovieId() { return movieId; }
    public String getUserId() { return userId; }
    public double getRating() { return rating; }
    public String getContent() { return content; }
    public String getReviewTime() { return reviewTime; }
}
