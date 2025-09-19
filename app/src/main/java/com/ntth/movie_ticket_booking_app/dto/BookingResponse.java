package com.ntth.movie_ticket_booking_app.dto;

import java.util.List;

public class BookingResponse {
    public String id;           // bookingId
    public String bookingId;    // phòng trường hợp BE trả tên khác
    public String bookingCode;
    public String status;
    public String showtimeId;
    public long amount;
    public List<String> seats;
    public PaymentInfo payment;
    public static class PaymentInfo { public String gateway; }
}
