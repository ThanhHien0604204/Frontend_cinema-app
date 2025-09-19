package com.ntth.movie_ticket_booking_app.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit singleton – thay BASE_URL thành URL backend Spring Boot của bạn.
 * Dev local (giả sử chạy trên PC): dùng IP máy tính + cổng, VD: http://192.168.1.10:8080
 */
public class RetrofitClient {
    private static final String BASE_URL = "https://movie-ticket-booking-app-fvau.onrender.com";

    private static Retrofit retrofit = null;

    private static volatile Context appCtx;   // applicationContext an toàn

    private static final String PREF = "auth_pref";
    private static final String KEY_JWT = "jwt";

    /**
     * Gọi 1 lần trong Application.onCreate()
     */
    public static void init(Context context) {
        appCtx = context.getApplicationContext();
    }

    /**
     * Hàm gốc getInstance, giữ nguyên nhưng bổ sung JWT
     */
    public static Retrofit getInstance() {
        ensureInited();
        if (retrofit == null) {
            String jwt = getToken();

            // Log HTTP (chỉ nên bật khi dev)
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder okb = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        var req = chain.request();
                        var urlB = req.url().newBuilder();
                        for (int i = 0; i < req.url().querySize(); i++) {
                            String name = req.url().queryParameterName(i);
                            String value = req.url().queryParameterValue(i);
                            if ("date".equals(name) && value != null) {
                                urlB.setQueryParameter(name, value.trim());
                            }
                        }
                        return chain.proceed(req.newBuilder().url(urlB.build()).build());
                    })
                    .addInterceptor(chain -> {
                        Request req = chain.request();
                        Log.d("RetrofitClient", "Request URL: " + req.url());
                        Log.d("RetrofitClient", "Request Headers: " + req.headers());
                        Log.d("RetrofitClient", "Authorization Header: " + req.header("Authorization"));
                        return chain.proceed(req);
                    })
                    .retryOnConnectionFailure(true)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)
                    .writeTimeout(90, TimeUnit.SECONDS)
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)); // Đảm bảo log đầy đủ

            // Nếu có token thì gắn Authorization header
            if (jwt != null && !jwt.isEmpty()) {
                okb.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request req = chain.request().newBuilder()
                                .addHeader("Accept", "application/json")
                                .addHeader("Authorization", "Bearer " + jwt)
                                .build();
                        return chain.proceed(req);

                    }
                });
            }
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .registerTypeAdapter(double.class, new JsonDeserializer<Double>() {
                        @Override
                        public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            try {
                                return json.getAsDouble();
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }
                    })
                    .create();

            OkHttpClient client = okb.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static ApiService api() {
        return getInstance().create(ApiService.class);
    }

    // ==== Token helpers ====

    /**
     * Lưu JWT sau khi login
     * Token sẽ tồn tại cho đến khi logout hoặc hết hạn
     */
    public static void saveToken(String token) {
        ensureInited();
        prefs().edit().putString(KEY_JWT, token).apply();
        retrofit = null; // rebuild để buộc gắn header mới
    }

    public static void saveToken(Context ignoredCtx, String token) {
        saveToken(token);
    }
    /**
     * logout xoá token ở client khỏi SharedPreferences
     * và rebuild Retrofit (xóa header Authorization).
     */
    public static void clearToken() {
        ensureInited();
        prefs().edit().remove(KEY_JWT).apply();
        retrofit = null; // rebuild để xoá interceptor Authorization
    }

    /**
     * Lấy JWT trong SharedPreferences
     */
    public static String getToken() {
        ensureInited();
        return prefs().getString(KEY_JWT, null);
    }

    private static SharedPreferences prefs() {
        ensureInited();
        return appCtx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static void ensureInited() {
        if (appCtx == null) {
            throw new IllegalStateException(
                    "RetrofitClient not initialized. Call RetrofitClient.init(applicationContext) in Application.onCreate()."
            );
        }
    }
//    // ==== Base URL helper (nếu muốn đổi lúc runtime) ====
//    public static void setBaseUrl(String baseUrl) {
//        BASE_URL = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
//        retrofit = null;
//    }

    // Adapter tùy chỉnh (xử lý parse đơn giản, bạn có thể mở rộng cho nhiều format)
    static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            try {
                return LocalDate.parse(json.getAsString());
            } catch (DateTimeParseException e) {
                return null;  // Hoặc throw exception tùy nhu cầu
            }
        }
    }
}
