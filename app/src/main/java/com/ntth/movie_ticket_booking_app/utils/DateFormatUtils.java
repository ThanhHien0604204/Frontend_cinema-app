package com.ntth.movie_ticket_booking_app.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateFormatUtils {
    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public static String isoToVN_HHmm(String iso) {
        if (iso == null || iso.isBlank()) return "";
        Instant t = Instant.parse(iso.trim());       // "2025-08-25T11:00:00Z"
        return HM.format(LocalDateTime.ofInstant(t, VN));
    }

    private DateFormatUtils(){}
}
