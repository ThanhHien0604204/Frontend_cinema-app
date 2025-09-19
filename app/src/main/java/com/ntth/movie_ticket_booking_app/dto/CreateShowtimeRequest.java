package com.ntth.movie_ticket_booking_app.dto;

import java.time.LocalDate;

public class CreateShowtimeRequest {
    public String movieId;
    public String roomId;
    public LocalDate date;
    public String startAt;
    public String endAt;
    public Long price;

    // Constructors, getters, setters
    public CreateShowtimeRequest() {}

    // Add getters and setters
}