package com.ntth.movie_ticket_booking_app.dto;


public class BookingRequest {
    public String holdId;
    public String paymentMethod; // "CASH" | "ZALOPAY"
    public BookingRequest(String holdId, String paymentMethod) {
        this.holdId = holdId;
        this.paymentMethod = paymentMethod;
    }
}