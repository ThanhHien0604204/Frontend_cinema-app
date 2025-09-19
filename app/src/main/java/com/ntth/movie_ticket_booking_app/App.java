package com.ntth.movie_ticket_booking_app;

import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

public class App extends android.app.Application {
    @Override public void onCreate() {
        super.onCreate();
        RetrofitClient.init(this); // <-- cấp applicationContext 1 lần
    }
}
