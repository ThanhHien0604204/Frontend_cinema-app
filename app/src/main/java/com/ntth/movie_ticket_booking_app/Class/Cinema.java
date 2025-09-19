package com.ntth.movie_ticket_booking_app.Class;

import com.google.gson.annotations.SerializedName;

public class Cinema {
    @SerializedName("id")
    private String id;

    private String name;
    private String address;
    private Integer numberOfRooms;

    public Cinema() {
    }

    public Cinema(String id, String name, String address, Integer numberOfRooms) {
        this.id=id;
        this.name = name;
        this.address = address;
        this.numberOfRooms = numberOfRooms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }


    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }
}
