package com.ntth.movie_ticket_booking_app.Class;


import com.google.gson.annotations.SerializedName;

public class Genre {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name != null ? name : "Unknown"; }
    public void setName(String name) { this.name = name; }
}

