package com.ntth.movie_ticket_booking_app.Class;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateItem {
    private String dayOfWeek;  // "Mon", "Tue",...
    private String dateNumber; // "31", "01",...
    private String fullDate;   // "2025-09-04" cho API
    private boolean isSelected;

    public DateItem(LocalDate date, boolean isSelected) {
        this.dayOfWeek = date.getDayOfWeek().toString().substring(0, 3); // Mon, Tue,...
        this.dateNumber = date.format(DateTimeFormatter.ofPattern("dd"));
        this.fullDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
        this.isSelected = isSelected;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getDateNumber() {
        return dateNumber;
    }

    public String getFullDate() {
        return fullDate;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}