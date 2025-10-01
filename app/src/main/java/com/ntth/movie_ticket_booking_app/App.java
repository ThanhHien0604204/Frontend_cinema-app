package com.ntth.movie_ticket_booking_app;

import android.util.Log;

import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

public class App extends android.app.Application {
    @Override public void onCreate() {
        super.onCreate();
        RetrofitClient.init(this); // <-- cấp applicationContext 1 lần
    }
}
