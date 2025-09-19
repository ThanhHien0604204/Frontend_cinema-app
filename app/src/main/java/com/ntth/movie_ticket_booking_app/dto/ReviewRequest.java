package com.ntth.movie_ticket_booking_app.dto;

public class ReviewRequest {
    public String movieId;
    public Double rating;
    public String content;

    public ReviewRequest(String movieId, double rating, String content) {
        this.movieId = movieId; this.rating = rating; this.content = content;
    }
    public String getMovieId() { return movieId; }
    public Double getRating() { return rating; }
    public String getContent() { return content; }
}
