package com.ntth.movie_ticket_booking_app.Class;

import com.google.gson.annotations.SerializedName;

public class Seat {
    @SerializedName("row")
    private char row;

    @SerializedName("column")
    private int column;

    @SerializedName("seatName")
    private String seatName;

    @SerializedName("status")
    private String status; // Thêm trường status (ví dụ: AVAILABLE, PURCHASED)

    public Seat() {
    }

    public Seat(char row, int column) {
        this.row = row;
        this.column = column;
        this.seatName = row + String.valueOf(column);
    }

    public char getRow() {
        return row;
    }

    public void setRow(char row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
