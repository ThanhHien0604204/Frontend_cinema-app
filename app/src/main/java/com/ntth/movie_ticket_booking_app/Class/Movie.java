package com.ntth.movie_ticket_booking_app.Class;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Movie {
    @SerializedName("id")
    private String id;

    private String title;
    private String imageUrl;
    private Integer durationMinutes;// phút
    @SerializedName("genreIds")// Ánh xạ với "genreIds" từ backend
    private List<String> genreIds;       // Danh sách ID genre
   // private LocalDate movieDateStart;   // Sử dụng để lưu kết quả chuyển đổi
    private Double rating;
    private String summary;
    private String trailerUrl;

    // Lưu dữ liệu thô từ JSON
    @SerializedName("movieDateStart")  // Ánh xạ trường JSON "movieDateStart"
    private JsonElement rawMovieDateStart;

    @SerializedName(value="views", alternate={"viewCount","watchCount"})
    private Long views; // <== thêm

    public Long getViews() {
        return views == null ? 0L : views; }

    public void setViews(Long views) {
        this.views = views;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public List<String> getGenreIds() {
        return genreIds != null ? genreIds : new ArrayList<>(); // Trả về danh sách rỗng nếu null
    }

    public void setGenreIds(List<String> genreIds) { this.genreIds = genreIds; }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public LocalDate getMovieDateStart() {
        Log.d("Movie", "Raw movieDateStart: " + rawMovieDateStart);
        if (rawMovieDateStart == null || rawMovieDateStart.isJsonNull()) return null;
        try {
            if (rawMovieDateStart.isJsonPrimitive()) {// String
                return LocalDate.parse(rawMovieDateStart.getAsString()); // "2025-08-21"
            }
            if (rawMovieDateStart.isJsonArray()) {
                JsonArray array = rawMovieDateStart.getAsJsonArray();
                int year = array.get(0).getAsInt();
                int month = array.get(1).getAsInt();
                int day = array.get(2).getAsInt();
                return LocalDate.of(year, month, day); // [2025, 8, 21]
            }
            if (rawMovieDateStart.isJsonObject()) {
                JsonObject obj = rawMovieDateStart.getAsJsonObject();
                if (obj.has("$date")) {
                    return LocalDate.parse(obj.get("$date").getAsString());
                }
                if (obj.has("year") && obj.has("month") && obj.has("day")) {
                    int year = obj.get("year").getAsInt();
                    int month = obj.get("month").getAsInt();
                    int day = obj.get("day").getAsInt();
                    return LocalDate.of(year, month, day);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi để debug
        }
        return null;
    }

    // Setter cho rawMovieDateStart (Gson sẽ ánh xạ trường "movieDateStart" từ JSON vào đây)
    public void setRawMovieDateStart(JsonElement rawMovieDateStart) {
        this.rawMovieDateStart = rawMovieDateStart;
    }
}
