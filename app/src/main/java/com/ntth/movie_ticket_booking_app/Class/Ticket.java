package com.ntth.movie_ticket_booking_app.Class;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Ticket {
    private String id;
    private String bookingCode;
    private String userId;
    private String showtimeId;
    private List<String> seats;
    private long amount;
    private String status;
    private String holdId;
    private PaymentInfo payment;
    private double createdAt;

    // Thêm hai trường mới để hiển thị (populate sau khi fetch)
    private String movieName;
    private String movieImageUrl;

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }
    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHoldId() { return holdId; }
    public void setHoldId(String holdId) { this.holdId = holdId; }
    public PaymentInfo getPayment() { return payment; }
    public void setPayment(PaymentInfo payment) { this.payment = payment; }
    public double getCreatedAt() { return createdAt; }
    public void setCreatedAt(double createdAt) { this.createdAt = createdAt; }
    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    public String getMovieImageUrl() { return movieImageUrl; }
    public void setMovieImageUrl(String movieImageUrl) { this.movieImageUrl = movieImageUrl; }

    public static class PaymentInfo {
        private String gateway;
        private String intentId;
        private double paidAt; // Thay Instant bằng double
        private String txId;
        private Map<String, Object> raw;
        private String zpTransId;

        public String getGateway() { return gateway; }
        public void setGateway(String gateway) { this.gateway = gateway; }
        public String getIntentId() { return intentId; }
        public void setIntentId(String intentId) { this.intentId = intentId; }
        public double getPaidAt() { return paidAt; }
        public void setPaidAt(double paidAt) { this.paidAt = paidAt; }
        public String getTxId() { return txId; }
        public void setTxId(String txId) { this.txId = txId; }
        public Map<String, Object> getRaw() { return raw; }
        public void setRaw(Map<String, Object> raw) { this.raw = raw; }
        public String getZpTransId() { return zpTransId; }
        public void setZpTransId(String zpTransId) { this.zpTransId = zpTransId; }
    }
}