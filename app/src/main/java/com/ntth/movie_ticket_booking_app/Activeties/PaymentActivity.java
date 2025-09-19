package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.BookingResponse;
import com.ntth.movie_ticket_booking_app.dto.HoldSeatsResponse;
import com.ntth.movie_ticket_booking_app.dto.ZpCreateOrderResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PaymentActivity extends AppCompatActivity {

    private TextView priceTextView, tvCountdown;
    private AppCompatButton btnPayZalo, btnPayCash;

    private String showtimeId;
    private ArrayList<String> seats;

    private String holdId;
    private long amount;
    private String expiresAtIso;
    private ImageView back;
    private String bookingId;
    private CountDownTimer holdTimer;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private boolean isPolling;

    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mapViews();
        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);

        // Nút quay lại
        back = findViewById(R.id.back1);
        back.setOnClickListener(v -> finish());

        // Nhận 2 extra từ SeatPicker
        Intent it = getIntent();
        showtimeId = it.getStringExtra("showtimeId");
        seats = it.getStringArrayListExtra("seats");

        if (showtimeId == null || seats == null || seats.isEmpty()) {
            Toast.makeText(this, "Thiếu showtimeId hoặc seats", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Chuẩn hoá ghế
        seats = new ArrayList<>(new LinkedHashSet<>(seats));
        for (int i = 0; i < seats.size(); i++) {
            String s = seats.get(i);
            if (s != null) seats.set(i, s.trim().toUpperCase(Locale.ROOT));
        }

        setButtonsEnabled(false);
        doHold(showtimeId, seats);

        btnPayCash.setOnClickListener(v -> payCash());
        btnPayZalo.setOnClickListener(v -> payZalo());

        handleDeeplink(getIntent()); // đề phòng trường hợp app được mở trực tiếp bằng deeplink
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (holdTimer != null) holdTimer.cancel();
        stopPolling();
    }

    private void mapViews() {
        priceTextView = findViewById(R.id.priceTextView);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnPayZalo = findViewById(R.id.btnPayZalo);
        btnPayCash = findViewById(R.id.btnPayCash);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnPayCash.setEnabled(enabled);
        btnPayZalo.setEnabled(enabled);
    }

    // ================== HOLD ==================
    private void doHold(String showtimeId, ArrayList<String> seats) {
        Map<String, java.util.List<String>> body = new HashMap<>();
        body.put("seats", seats);

        api.holdSeats(showtimeId, body).enqueue(new Callback<HoldSeatsResponse>() {
            @Override
            public void onResponse(Call<HoldSeatsResponse> call, Response<HoldSeatsResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(PaymentActivity.this, "Giữ ghế thất bại: " + res.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                HoldSeatsResponse h = res.body();
                holdId = h.holdId;
                amount = (h.amount == null) ? 0L : h.amount.longValue(); // <<— CHỖ QUAN TRỌNG
                expiresAtIso = h.expiresAt;

                priceTextView.setText(
                        "Ghế: " + String.join(", ", seats) + "\nTạm tính: " + amount + " đ"
                );
                setButtonsEnabled(true);
                startHoldCountdown(expiresAtIso);
            }

            @Override
            public void onFailure(Call<HoldSeatsResponse> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Lỗi giữ ghế: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private static long asLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue(); // xử lý Double -> long
        try {
            String s = String.valueOf(o).trim();
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return Long.parseLong(s);
        } catch (Exception ex) {
            try {
                double d = Double.parseDouble(String.valueOf(o));
                return (long) d; // hoặc Math.round(d) tuỳ bạn
            } catch (Exception ignore) {
                return 0L;
            }
        }
    }

    private void startHoldCountdown(String expiresAtIso) {
        if (expiresAtIso == null) return;
        try {
            long ms = Duration.between(Instant.now(), Instant.parse(expiresAtIso)).toMillis();
            if (ms < 0) ms = 0;
            if (holdTimer != null) holdTimer.cancel();
            holdTimer = new CountDownTimer(ms, 1000) {
                @Override
                public void onTick(long l) {
                    long s = l / 1000;
                    tvCountdown.setText("Giữ ghế còn: " + (s / 60) + "m " + (s % 60) + "s");
                }

                @Override
                public void onFinish() {
                    tvCountdown.setText("Hết hạn giữ ghế");
                    setButtonsEnabled(false);
                }
            }.start();
        } catch (Throwable ignore) {
        }
    }

    // ================== CASH ==================
    private void payCash() {
        if (holdId == null || holdId.isBlank()) {
            Toast.makeText(this, "Thiếu holdId", Toast.LENGTH_SHORT).show();
            return;
        }
        // /api/bookings — body Map<String,String>
        Map<String, String> body = new HashMap<>();
        body.put("holdId", holdId);
        body.put("paymentMethod", "CASH");

        api.createBooking(body).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    showError("Tạo vé (CASH) thất bại: " + res.code());
                    return;
                }
                BookingResponse b = res.body();
                if (!"CONFIRMED".equalsIgnoreCase(b.status)) {
                    Toast.makeText(PaymentActivity.this, "Vé chưa xác nhận: " + b.status, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(PaymentActivity.this, "Đặt vé thành công (CASH): " + b.bookingCode, Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                showError("Lỗi CASH: " + t.getMessage());
            }
        });
    }

    // ================== ZALOPAY ==================
    private void payZalo() {
        if (holdId == null || holdId.isBlank()) {
            Toast.makeText(this, "Thiếu holdId", Toast.LENGTH_SHORT).show();
            return;
        }
        // B1. /api/bookings/zalopay — body Map<String,String> {holdId}
        Map<String, String> body = new HashMap<>();
        body.put("holdId", holdId);

        api.createBookingZaloPay(body).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    showError("Không tạo được booking ZP: " + res.code());
                    return;
                }
                BookingResponse b = res.body();
                bookingId = b.bookingId; // server trả về id để poll

                // B2. /api/payments/zalopay/create — body Map<String,String> {bookingId}
                Map<String, String> body2 = new HashMap<>();
                body2.put("bookingId", bookingId);

                api.createZpOrder(body2).enqueue(new Callback<ZpCreateOrderResponse>() {
                    @Override
                    public void onResponse(Call<ZpCreateOrderResponse> call, Response<ZpCreateOrderResponse> r2) {
                        if (!r2.isSuccessful() || r2.body() == null
                                || r2.body().order_url == null || r2.body().order_url.trim().isEmpty()) {
                            showError("Không tạo được đơn thanh toán: " + r2.code());
                            return;
                        }

                        ZpCreateOrderResponse zp = r2.body();
                        String orderUrl = zp.order_url;              // ✅ Đọc từ field của DTO
                        // (tuỳ chọn) bạn cũng có thể lấy thêm:
                        // String appTransId = zp.app_trans_id;
                        // String zpTransToken = zp.zp_trans_token;

                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl));
                            i.addCategory(Intent.CATEGORY_BROWSABLE);
                            startActivity(i);

                        } catch (Exception e) {
                            showError("Không mở được trình duyệt: " + e.getMessage());
                            return;
                        }

                        // B3. poll trạng thái booking
                        startPollingBooking();
                        Toast.makeText(PaymentActivity.this, "Đang chờ thanh toán...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<ZpCreateOrderResponse> call, Throwable t) {
                        showError("Lỗi tạo đơn ZP: " + t.getMessage());
                    }
                });

            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                showError("Lỗi booking ZP: " + t.getMessage());
            }
        });
    }

    // ================== POLLING ==================
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeeplink(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Phòng trường hợp hệ thống deliver intent lúc resume
        handleDeeplink(getIntent());
    }

    private void handleDeeplink(Intent intent) {
        Uri data = (intent != null) ? intent.getData() : null;
        if (data == null) return;
        if ("myapp".equalsIgnoreCase(data.getScheme())
                && "zp-callback".equalsIgnoreCase(data.getHost())) {
            String id = data.getQueryParameter("bookingId");
            String canceled = data.getQueryParameter("canceled");
            if (canceled != null) {
                stopPolling();
                showError("Bạn đã hủy thanh toán.");
                return;
            }
            if (id != null && !id.isEmpty()) {
                // GÁN vào field để startPollingBooking() dùng
                this.bookingId = id;
                startPollingBooking();
            }
        }
    }

    private void startPollingBooking() {
        if (bookingId == null) return;
        if (isPolling) return;
        isPolling = true;
        pollHandler.postDelayed(pollTask, 2000);
    }

    private void stopPolling() {
        isPolling = false;
        pollHandler.removeCallbacks(pollTask);
    }

    private final Runnable pollTask = new Runnable() {
        final long started = System.currentTimeMillis();
        final long TIMEOUT_MS = 3 * 60 * 1000; // 3 phút

        @Override
        public void run() {
            if (!isPolling || bookingId == null) return;
            api.getBooking(bookingId).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(Call<BookingResponse> call, Response<BookingResponse> res) {
                    if (!isPolling) return;
                    if (res.isSuccessful() && res.body() != null) {
                        BookingResponse b = res.body();
                        if ("CONFIRMED".equalsIgnoreCase(b.status)) {
                            stopPolling();
                            Toast.makeText(PaymentActivity.this, "Thanh toán thành công: " + b.bookingCode, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        if ("FAILED".equalsIgnoreCase(b.status) || "CANCELED".equalsIgnoreCase(b.status)) {
                            stopPolling();
                            showError("Thanh toán thất bại: " + b.status);
                            return;
                        }
                    }
                    scheduleNext();
                }

                @Override
                public void onFailure(Call<BookingResponse> call, Throwable t) {
                    scheduleNext();
                }

                private void scheduleNext() {
                    if (!isPolling) return;
                    if (System.currentTimeMillis() - started > TIMEOUT_MS) {
                        stopPolling();
                        showError("Hết thời gian chờ thanh toán.");
                        return;
                    }
                    pollHandler.postDelayed(pollTask, 2000);
                }
            });
        }
    };

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static long longOrZero(Object o) {
        try {
            return o == null ? 0L : Long.parseLong(String.valueOf(o));
        } catch (Exception e) {
            return 0L;
        }
    }

    private void showError(String msg) {
        Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_LONG).show();
        Log.e("PaymentActivity", msg);
    }
}
