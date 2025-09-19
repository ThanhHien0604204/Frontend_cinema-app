package com.ntth.movie_ticket_booking_app.Class;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class Showtime {
    @SerializedName("id")
    private String id;
    @SerializedName("movieId")
    private String movieId;
    @SerializedName("roomId")
    private String roomId;
    @SerializedName("sessionName")
    private String sessionName;
    @SerializedName("date")
    private List<Integer> date; // Sửa thành List<Integer> để nhận mảng [year, month, day]
    @SerializedName("startAt")
    private double startAt; // API trả double (timestamp với .0)
    @SerializedName("endAt")
    private double endAt; // API trả double
    @SerializedName("price")
    private Integer price;
    @SerializedName("takenSeats")
    private List<String> takenSeats;
    @SerializedName("totalSeats")
    private int totalSeats;
    @SerializedName("availableSeats")
    private int availableSeats;

    public Showtime() {
    }

    // Getter/Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }
    public List<Integer> getDate() { return date; }
    public void setDate(List<Integer> date) { this.date = date; }
    public double getStartAt() { return startAt; }
    public void setStartAt(double startAt) { this.startAt = startAt; }
    public double getEndAt() { return endAt; }
    public void setEndAt(double endAt) { this.endAt = endAt; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public List<String> getTakenSeats() { return takenSeats; }
    public void setTakenSeats(List<String> takenSeats) { this.takenSeats = takenSeats; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    // Thêm phương thức format
    public String getFormattedDate() {
        if (date != null && date.size() == 3) {
            return String.format(Locale.US, "%d-%02d-%02d", date.get(0), date.get(1), date.get(2));
        }
        return "N/A";
    }

    public String getFormattedStartTime() {
        long timestampMillis = (long) (startAt * 1000); // Chuyển từ giây sang millis
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new java.util.Date(timestampMillis));
    }

    public String getFormattedEndTime() {
        long timestampMillis = (long) (endAt * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new java.util.Date(timestampMillis));
    }
}