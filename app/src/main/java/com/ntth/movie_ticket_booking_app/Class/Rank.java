package com.ntth.movie_ticket_booking_app.Class;

import com.google.gson.annotations.SerializedName;

public class Rank {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    // Các trường khác nếu cần

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}