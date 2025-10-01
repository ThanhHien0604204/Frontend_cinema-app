//package com.ntth.movie_ticket_booking_app.dto;
//
//public class MomoCreateOrderResponse {
//    // tuỳ BE trả về field nào, dưới đây là các tên phổ biến từ MoMo:
//    public String payUrl;        // URL mở trình duyệt/app MoMo
//    public String deeplink;      // một số BE trả "deeplink" riêng
//    public String deeplinkMiniApp;
//    public String qrCodeUrl;     // optional
//    public String partnerClientId; // optional
//
//    // dùng cái nào có giá trị: ưu tiên deeplink -> payUrl
//    public String bestUrl() {
//        if (deeplink != null && !deeplink.isEmpty()) return deeplink;
//        if (payUrl != null && !payUrl.isEmpty()) return payUrl;
//        if (deeplinkMiniApp != null && !deeplinkMiniApp.isEmpty()) return deeplinkMiniApp;
//        return null;
//    }
//}