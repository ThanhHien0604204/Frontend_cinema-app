package com.ntth.movie_ticket_booking_app.dto;

public class ZpCreateOrderResponse {
    // THAY ĐỔI: Dùng camelCase để match backend
    public String orderUrl;        // thay vì order_url
    public String appTransId;      // thay vì app_trans_id
    public String zpTransToken;    // thay vì zp_trans_token

    // Getters/Setters - THAY ĐỔI TÊN METHOD
    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public String getAppTransId() {
        return appTransId;
    }

    public void setAppTransId(String appTransId) {
        this.appTransId = appTransId;
    }

    public String getZpTransToken() {
        return zpTransToken;
    }

    public void setZpTransToken(String zpTransToken) {
        this.zpTransToken = zpTransToken;
    }

    // Debug helper
    @Override
    public String toString() {
        return "ZpCreateOrderResponse{" +
                "orderUrl='" + orderUrl + '\'' +
                ", appTransId='" + appTransId + '\'' +
                ", zpTransToken='" + zpTransToken + '\'' +
                '}';
    }
}