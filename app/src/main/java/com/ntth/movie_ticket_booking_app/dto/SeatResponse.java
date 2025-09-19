package com.ntth.movie_ticket_booking_app.dto;

import com.google.gson.annotations.SerializedName;
import com.ntth.movie_ticket_booking_app.Class.Seat;

import java.util.List;

public class SeatResponse {
    public String seat;
    public String state;
}  // state: FREE | HOLD | CONFIRMED
