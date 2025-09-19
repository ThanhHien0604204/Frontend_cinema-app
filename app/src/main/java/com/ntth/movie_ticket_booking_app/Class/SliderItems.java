package com.ntth.movie_ticket_booking_app.Class;

public class SliderItems {
    private String id;
    private String imageUrl;   // link ảnh poster từ Movie.imageUrl
    private String movieId;    // optional: để mở chi tiết phim khi click
    private String title;      // optional: tiêu đề phim để hiển thị/analytics

    public SliderItems() {
    }

    public SliderItems(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public SliderItems(String id, String imageUrl, String movieId, String title) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.movieId = movieId;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
