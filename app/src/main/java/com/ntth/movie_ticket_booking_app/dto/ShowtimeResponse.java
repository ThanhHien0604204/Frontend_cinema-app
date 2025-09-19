package com.ntth.movie_ticket_booking_app.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.List;

public class ShowtimeResponse {
    @SerializedName("sessionId")
    public String id;
    @SerializedName("movieId")
    public String movieId;
    @SerializedName("roomId")
    public String roomId;
    @SerializedName("sessionName")
    public String sessionName;
    @SerializedName("startAt")
    public String startAt;    // HH:mm
    @SerializedName("endAt")
    public String endAt;      // HH:mm
    @SerializedName("price")
    public Long price;
    @SerializedName("totalSeats")
    public Integer totalSeats;
    @SerializedName("availableSeats")
    public Integer availableSeats;
    @SerializedName("movieTitle")
    public String movieTitle; // optional
    @SerializedName("roomName")
    public String roomName;   // optional
    @SerializedName("takenSeats")
    public List<String> takenSeats;

//    // Lưu dữ liệu thô từ JSON
//    @SerializedName("date")  // Ánh xạ trường JSON "date"
//    private JsonElement rawDate;
//
//    // Getter để parse date tương tự Movie.java
//    public LocalDate getDate() {
//        if (rawDate == null || rawDate.isJsonNull()) return null;
//        try {
//            if (rawDate.isJsonPrimitive()) { // String
//                return LocalDate.parse(rawDate.getAsString()); // "2025-08-25"
//            }
//            if (rawDate.isJsonArray()) {
//                JsonArray array = rawDate.getAsJsonArray();
//                int year = array.get(0).getAsInt();
//                int month = array.get(1).getAsInt();
//                int day = array.get(2).getAsInt();
//                return LocalDate.of(year, month, day); // [2025, 8, 25]
//            }
//            if (rawDate.isJsonObject()) {
//                JsonObject obj = rawDate.getAsJsonObject();
//                if (obj.has("$date")) {
//                    return LocalDate.parse(obj.get("$date").getAsString());
//                }
//                if (obj.has("year") && obj.has("month") && obj.has("day")) {
//                    int year = obj.get("year").getAsInt();
//                    int month = obj.get("month").getAsInt();
//                    int day = obj.get("day").getAsInt();
//                    return LocalDate.of(year, month, day);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace(); // Log lỗi để debug
//        }
//        return null;
//    }\

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<String> getTakenSeats() {
        return takenSeats;
    }

    public void setTakenSeats(List<String> takenSeats) {
        this.takenSeats = takenSeats;
    }

    // Getter tùy chỉnh để lấy thời gian bắt đầu, giữ nguyên định dạng "01:00"
    public String getFormattedStartTime() {
        return startAt != null ? startAt : ""; // Trả về "01:00" hoặc "" nếu null
    }

    // Getter tùy chỉnh để lấy thời gian kết thúc, giữ nguyên định dạng "03:45"
    public String getFormattedEndTime() {
        return endAt != null ? endAt : ""; // Trả về "03:45" hoặc "" nếu null
    }
}