package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.Class.Room;
import com.ntth.movie_ticket_booking_app.Class.Showtime;
import com.ntth.movie_ticket_booking_app.Class.Ticket;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.BookingResponse;
import com.ntth.movie_ticket_booking_app.dto.ConfirmResponse;
import com.ntth.movie_ticket_booking_app.dto.HoldSeatsResponse;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;
import com.ntth.movie_ticket_booking_app.dto.ZpCreateOrderResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentActivity extends AppCompatActivity {

    private TextView priceTextView, tvCountdown, tvMovieTitle, tvCinemaName, tvsoLuong, tvRoomName;
    private TextView tvShowtimeDate, tvShowtimeTime, tvSeatsInfo;
    private ImageView ivMoviePoster, back;
    private Button btnPayZalo, btnPayCash;

    private String showtimeId;
    private ArrayList<String> seats;
    private Showtime showtime;
    private Movie movie;
    private String cinemaName;
    private String roomName;

    private String holdId;
    private long amount;
    private String expiresAtIso;
    private String bookingId;
    private CountDownTimer holdTimer;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private boolean isPolling;

    private ApiService api;

    private String savedShowtimeId;
    private ArrayList<String> savedSeats;
    private boolean triedAutoConfirm = false;
    private long orderOpenedAt = 0L;
    private boolean allowAutoConfirm = false; // chỉ true khi quay về từ deeplink PENDING
    private static final long AUTO_CONFIRM_GRACE_MS = 150000; // 150s

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

        savedShowtimeId = showtimeId;
        savedSeats = seats;

        setButtonsEnabled(false);
        // Load thông tin chi tiết trước khi hold ghế
        loadShowtimeDetails();
        // Hold ghế sau khi có thông tin showtime
        doHold(showtimeId, seats);

        btnPayCash.setOnClickListener(v -> payCash());
        btnPayZalo.setOnClickListener(v -> payZalo());

        //handleDeeplink(getIntent()); // đề phòng trường hợp app được mở trực tiếp bằng deeplink
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (holdTimer != null) holdTimer.cancel();
    }

    private void mapViews() {
        priceTextView = findViewById(R.id.priceTextView);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvMovieTitle = findViewById(R.id.movieNameTextView);
        tvCinemaName = findViewById(R.id.locationTextView);
        tvsoLuong = findViewById(R.id.soluongveTextView);
        tvShowtimeDate = findViewById(R.id.showTimeTextView);
//        tvShowtimeTime = findViewById(R.id.tvShowtimeTime);
        tvSeatsInfo = findViewById(R.id.tvSeatsInfo);
        ivMoviePoster = findViewById(R.id.movieImageView);
        btnPayZalo = findViewById(R.id.btnPayZalo);
        btnPayCash = findViewById(R.id.btnPayCash);
        back = findViewById(R.id.back1);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnPayCash.setEnabled(enabled);
        btnPayZalo.setEnabled(enabled);
    }
    // ================== LOAD SHOWTIME DETAILS ==================
    private void loadShowtimeDetails() {
        Log.d("PaymentActivity", "Fetching showtimeId: " + showtimeId);

        api.getShowtimeById(showtimeId).enqueue(new Callback<Showtime>() {
            @Override
            public void onResponse(Call<Showtime> call, Response<Showtime> response) {
                Log.d("PaymentActivity", "getShowtimeById response code: " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("PaymentActivity", "getShowtimeById failed: " + response.code());
                    Toast.makeText(PaymentActivity.this, "Không lấy được thông tin suất chiếu", Toast.LENGTH_LONG).show();
                    if (tvShowtimeDate != null) {
                        tvShowtimeDate.setText("Suất chiếu: N/A");
                    } else {
                        Log.e("PaymentActivity", "tvShowtimeDate is null");
                    }
                    if (tvCinemaName != null) {
                        tvCinemaName.setText("Rạp: N/A");
                    } else {
                        Log.e("PaymentActivity", "tvCinemaName is null");
                    }
                    return;
                }

                showtime = response.body();
                Log.d("PaymentActivity", "Showtime data: startAt=" + showtime.getStartAt() +
                        ", movieId=" + showtime.getMovieId() + ", roomId=" + showtime.getRoomId() +
                        ", price=" + showtime.getPrice());

                // Set showtime với kiểm tra null
                String formattedStart = showtime.getFormattedStartTime();
                String formattedEnd = showtime.getFormattedEndTime();
                String formattedDate = showtime.getFormattedDate();
                if (tvShowtimeDate != null) {
                    tvShowtimeDate.setText("Suất: " + formattedStart + " - " + formattedEnd + " (" + formattedDate + ")");
                    Log.d("PaymentActivity", "Showtime formatted: " + tvShowtimeDate.getText());
                } else {
                    Log.e("PaymentActivity", "tvShowtimeDate is null");
                }

                // Hiển thị số lượng vé riêng
                updateTicketCountUI();

                // Tính toán giá vé tạm thời từ showtime (trước khi hold)
                calculateTemporaryPrice();

                // Fetch movie
                String movieId = showtime.getMovieId();
                Log.d("PaymentActivity", "Fetching movieId: " + movieId);
                if (movieId == null || movieId.isEmpty()) {
                    if (tvMovieTitle != null) {
                        tvMovieTitle.setText("Phim: N/A");
                    } else {
                        Log.e("PaymentActivity", "tvMovieTitle is null");
                    }
                    return;
                }

                api.getMovieById(movieId).enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(Call<Movie> call, Response<Movie> response) {
                        Log.d("PaymentActivity", "getMovieById response code: " + response.code());
                        if (response.isSuccessful() && response.body() != null) {
                            movie = response.body();
                            Log.d("PaymentActivity", "Movie data: title=" + movie.getTitle() +
                                    ", imageUrl=" + movie.getImageUrl());

                            if (tvMovieTitle != null) {
                                tvMovieTitle.setText(movie.getTitle());
                                Log.d("PaymentActivity", "Set movie name: " + tvMovieTitle.getText());
                            } else {
                                Log.e("PaymentActivity", "tvMovieTitle is null");
                            }

                            // Load poster với kiểm tra null
                            if (movie.getImageUrl() != null && !movie.getImageUrl().isEmpty()) {
                                if (ivMoviePoster != null) {
                                    Glide.with(PaymentActivity.this)
                                            .load(movie.getImageUrl())
                                            .placeholder(R.drawable.load)
                                            .error(R.drawable.thongbaoloi)
                                            .into(ivMoviePoster);
                                    Log.d("PaymentActivity", "Loading image: " + movie.getImageUrl());
                                } else {
                                    Log.e("PaymentActivity", "ivMoviePoster is null");
                                }
                            } else {
                                Log.w("PaymentActivity", "Movie image URL is null or empty");
                                if (ivMoviePoster != null) {
                                    ivMoviePoster.setImageResource(R.drawable.thongbaoloi);
                                } else {
                                    Log.e("PaymentActivity", "ivMoviePoster is null");
                                }
                            }
                        } else {
                            Log.e("PaymentActivity", "getMovieById failed: " + response.code());
                            if (tvMovieTitle != null) {
                                tvMovieTitle.setText("Phim: N/A");
                            } else {
                                Log.e("PaymentActivity", "tvMovieTitle is null");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Movie> call, Throwable t) {
                        Log.e("PaymentActivity", "getMovieById failure: " + t.getMessage());
                        if (tvMovieTitle != null) {
                            tvMovieTitle.setText("Phim: N/A");
                        } else {
                            Log.e("PaymentActivity", "tvMovieTitle is null");
                        }
                    }
                });

                // Fetch room
                String roomId = showtime.getRoomId();
                Log.d("PaymentActivity", "Fetching roomId: " + roomId);
                if (roomId == null || roomId.isEmpty()) {
                    if (tvCinemaName != null) {
                        tvCinemaName.setText("Rạp: N/A");
                    } else {
                        Log.e("PaymentActivity", "tvCinemaName is null");
                    }
                    if (tvRoomName != null) {
                        tvRoomName.setText("Phòng: N/A");
                    } else {
                        Log.e("PaymentActivity", "tvRoomName is null");
                    }
                    return;
                }

                api.getRoom(roomId).enqueue(new Callback<Room>() {
                    @Override
                    public void onResponse(Call<Room> call, Response<Room> response) {
                        Log.d("PaymentActivity", "getRoom response code: " + response.code());
                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e("PaymentActivity", "getRoom failed: " + response.code());
                            if (tvCinemaName != null) {
                                tvCinemaName.setText("Rạp: N/A");
                            } else {
                                Log.e("PaymentActivity", "tvCinemaName is null");
                            }
                            if (tvRoomName != null) {
                                tvRoomName.setText("Phòng: N/A");
                            } else {
                                Log.e("PaymentActivity", "tvRoomName is null");
                            }
                            return;
                        }

                        Room room = response.body();
                        roomName = room.getRoomName();
                        Log.d("PaymentActivity", "Room data: name=" + roomName + ", cinemaId=" + room.getCinemaId());

                        if (tvRoomName != null) {
                            tvRoomName.setText("Phòng: " + roomName);
                        } else {
                            Log.e("PaymentActivity", "tvRoomName is null");
                        }

                        // Fetch cinema
                        String cinemaId = room.getCinemaId();
                        Log.d("PaymentActivity", "Fetching cinemaId: " + cinemaId);
                        if (cinemaId == null || cinemaId.isEmpty()) {
                            if (tvCinemaName != null) {
                                tvCinemaName.setText("Rạp: N/A");
                            } else {
                                Log.e("PaymentActivity", "tvCinemaName is null");
                            }
                            return;
                        }

                        api.getCinemaId(cinemaId).enqueue(new Callback<Cinema>() {
                            @Override
                            public void onResponse(Call<Cinema> call, Response<Cinema> response) {
                                Log.d("PaymentActivity", "getCinemaId response code: " + response.code());
                                if (response.isSuccessful() && response.body() != null) {
                                    Cinema cinema = response.body();
                                    cinemaName = cinema.getName();
                                    if (tvCinemaName != null) {
                                        tvCinemaName.setText(cinemaName + " - " + roomName);
                                        Log.d("PaymentActivity", "Cinema name: " + cinemaName);
                                    } else {
                                        Log.e("PaymentActivity", "tvCinemaName is null");
                                    }
                                } else {
                                    Log.e("PaymentActivity", "getCinemaId failed: " + response.code());
                                    if (tvCinemaName != null) {
                                        tvCinemaName.setText("Rạp: N/A");
                                    } else {
                                        Log.e("PaymentActivity", "tvCinemaName is null");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Cinema> call, Throwable t) {
                                Log.e("PaymentActivity", "getCinemaId failure: " + t.getMessage());
                                if (tvCinemaName != null) {
                                    tvCinemaName.setText("Rạp: N/A");
                                } else {
                                    Log.e("PaymentActivity", "tvCinemaName is null");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Room> call, Throwable t) {
                        Log.e("PaymentActivity", "getRoom failure: " + t.getMessage());
                        if (tvCinemaName != null) {
                            tvCinemaName.setText("Rạp: N/A");
                        } else {
                            Log.e("PaymentActivity", "tvCinemaName is null");
                        }
                        if (tvRoomName != null) {
                            tvRoomName.setText("Phòng: N/A");
                        } else {
                            Log.e("PaymentActivity", "tvRoomName is null");
                        }
                    }
                });

                // Update seats info (chỉ hiển thị danh sách ghế, không có số lượng)
                if (tvSeatsInfo != null && seats != null && !seats.isEmpty()) {
                    tvSeatsInfo.setText("Ghế: " + String.join(", ", seats));
                    Log.d("PaymentActivity", "Seats info: " + tvSeatsInfo.getText());
                } else {
                    Log.e("PaymentActivity", "tvSeatsInfo is null or seats is empty");
                }
            }

            @Override
            public void onFailure(Call<Showtime> call, Throwable t) {
                Log.e("PaymentActivity", "getShowtimeById failure: " + t.getMessage());
                Toast.makeText(PaymentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                if (tvShowtimeDate != null) {
                    tvShowtimeDate.setText("Suất chiếu: N/A");
                } else {
                    Log.e("PaymentActivity", "tvShowtimeDate is null");
                }
                if (tvCinemaName != null) {
                    tvCinemaName.setText("Rạp: N/A");
                } else {
                    Log.e("PaymentActivity", "tvCinemaName is null");
                }
                // Vẫn cập nhật số lượng vé ngay cả khi load showtime fail
                updateTicketCountUI();
            }
        });
    }
    // Thêm method này để hiển thị số lượng vé riêng
    private void updateTicketCountUI() {
        if (seats == null || seats.isEmpty()) {
            Log.w("PaymentActivity", "Cannot update ticket count: seats is null or empty");
            if (tvsoLuong != null) {
                tvsoLuong.setText("Số lượng vé: 0");
            }
            return;
        }

        int ticketCount = seats.size();

        if (tvsoLuong != null) {
            String ticketText = String.format(Locale.getDefault(), "Số lượng vé: %d", ticketCount);
            tvsoLuong.setText(ticketText);
            Log.d("PaymentActivity", "Ticket count: " + ticketText);
        } else {
            Log.e("PaymentActivity", "soluongveTextView is null");
        }
    }
    // Cập nhật method tính giá tạm thời
    private void calculateTemporaryPrice() {
        if (showtime == null || showtime.getPrice() == null || seats == null) {
            Log.w("PaymentActivity", "Cannot calculate temporary price: missing data");
            return;
        }

        int ticketCount = seats.size();
        long tempAmount = (long) showtime.getPrice() * ticketCount;

        // Update price UI (không hiển thị số lượng vé ở đây nữa)
        if (priceTextView != null) {
            String priceText = String.format(Locale.getDefault(),
                    "Tạm tính: %s VNĐ",
                    formatCurrency(tempAmount)
            );
            priceTextView.setText(priceText);
            Log.d("PaymentActivity", "Temporary price: " + priceText);
        } else {
            Log.e("PaymentActivity", "priceTextView is null");
        }
    }

    // Cập nhật method cập nhật giá chính thức
    private void updateFinalPriceUI() {
        int ticketCount = seats != null ? seats.size() : 0;

        // Tính giá mỗi vé từ total amount
        long pricePerTicket = ticketCount > 0 ? amount / ticketCount : amount;

        if (priceTextView != null) {
            String priceText = String.format(Locale.getDefault(),
                    "%d vé x %s VNĐ = %s VNĐ",
                    ticketCount,
                    formatCurrency(pricePerTicket),
                    formatCurrency(amount)
            );
            priceTextView.setText(priceText);
            Log.d("PaymentActivity", "Final price: " + priceText);
        } else {
            Log.e("PaymentActivity", "priceTextView is null");
        }

        // Update seats info với số lượng vé
        if (tvSeatsInfo != null && seats != null && !seats.isEmpty()) {
            String seatsText = "Ghế: " + String.join(", ", seats) +
                    " (" + ticketCount + " vé)";
            tvSeatsInfo.setText(seatsText);
            Log.d("PaymentActivity", "Seats info: " + seatsText);
        }
    }

    // Thêm method format tiền tệ
    private String formatCurrency(long amount) {
        return String.format(Locale.getDefault(), "%,d", amount).replace(",", ".");
    }

    // Cập nhật method startHoldTimer
    private void startHoldTimer(String expiresAtIso) {
        try {
            Instant expiresAt = Instant.parse(expiresAtIso);
            long millisLeft = Duration.between(Instant.now(), expiresAt).toMillis();
            if (millisLeft <= 0) {
                showError("Giữ ghế đã hết hạn");
                finish();
                return;
            }

            holdTimer = new CountDownTimer(millisLeft, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long seconds = millisUntilFinished / 1000;
                    long minutes = seconds / 60;
                    seconds %= 60;
                    if (tvCountdown != null) {
                        tvCountdown.setText(String.format(Locale.ROOT,
                                "Thời gian giữ ghế còn lại: %02d:%02d", minutes, seconds));
                    }
                }

                @Override
                public void onFinish() {
                    showError("Giữ ghế đã hết hạn");
                    setButtonsEnabled(false);
                    finish();
                }
            }.start();

            Log.d("PaymentActivity", "Hold timer started: " + millisLeft + "ms remaining");
        } catch (Exception e) {
            Log.e("PaymentActivity", "Error parsing expiresAtIso: " + expiresAtIso, e);
            showError("Lỗi định dạng thời gian");
        }
    }
    private void updateMovieUI() {
        if (movie != null && tvMovieTitle != null && ivMoviePoster != null) {
            tvMovieTitle.setText(movie.getTitle());

            // Load poster với Glide
            if (movie.getImageUrl() != null && !movie.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(movie.getImageUrl())
                        .placeholder(R.drawable.load)
                        .error(R.drawable.thongbaoloi)
                        .into(ivMoviePoster);
            }
        }
    }

    private void updateLocationUI() {
        if (tvCinemaName != null && tvRoomName != null) {
            String cinemaText = cinemaName != null ? cinemaName : "Đang tải...";
            String roomText = roomName != null ? "Phòng " + roomName : "Phòng chiếu";

            tvCinemaName.setText(cinemaText);
            tvRoomName.setText(roomText);
        }
    }

    private void updateShowtimeUI() {
        if (showtime != null && tvShowtimeDate != null && tvShowtimeTime != null) {
            tvShowtimeDate.setText(showtime.getFormattedDate());
            tvShowtimeTime.setText(showtime.getFormattedStartTime() + " - " + showtime.getFormattedEndTime());
        }
    }

    private void updateSeatsUI() {
        if (tvSeatsInfo != null && seats != null && !seats.isEmpty()) {
            tvSeatsInfo.setText("Ghế: " + String.join(", ", seats));
        }
    }

    // ================== HOLD ==================

    // Cập nhật method doHold để hiển thị giá chính thức
    private void doHold(String showtimeId, ArrayList<String> seats) {
        Map<String, java.util.List<String>> body = new HashMap<>();
        body.put("seats", seats);

        api.holdSeats(showtimeId, body).enqueue(new Callback<HoldSeatsResponse>() {
            @Override
            public void onResponse(Call<HoldSeatsResponse> call, Response<HoldSeatsResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(PaymentActivity.this, "Giữ ghế thất bại: " + res.code(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(PaymentActivity.this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                HoldSeatsResponse h = res.body();
                holdId = h.holdId;
                amount = (h.amount == null) ? 0L : h.amount.longValue();
                expiresAtIso = h.expiresAt;

                Log.d("PaymentActivity", "Hold successful: holdId=" + holdId + ", amount=" + amount);

                // Cập nhật UI với giá chính thức từ hold response
                updateFinalPriceUI();

                startHoldTimer(expiresAtIso);
                setButtonsEnabled(true);
            }

            @Override
            public void onFailure(Call<HoldSeatsResponse> call, Throwable t) {
                showError("Lỗi giữ ghế: " + t.getMessage());
            }
        });
    }

    // ================== PAY CASH ==================
    private void payCash() {
        if (holdId == null) {
            // Kiểm tra hold ghế còn hiệu lực
            showError("HoldId không tồn tại. Vui lòng chọn ghế lại.");
            Intent intent = new Intent(PaymentActivity.this, SeatPickerActivity.class);
            intent.putExtra("showtimeId", showtimeId);
            startActivity(intent);
            finish();
            return;
        }
        // Tạo request với paymentMethod CASH
        Map<String, String> body = new HashMap<>();
        body.put("holdId", holdId);
        body.put("paymentMethod", "CASH");

        api.createBooking(body).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    String errorBody = "";
                    try { errorBody = res.errorBody() != null ? res.errorBody().string() : "No body"; } catch (Exception e) {}
                    Log.e("PaymentActivity", "Cash booking failed: " + res.code() + " - " + errorBody);
                    if (res.code() == 409) {
                        showError("HoldId hết hạn hoặc bị xung đột. Vui lòng chọn ghế lại.");
                        Intent intent = new Intent(PaymentActivity.this, SeatPickerActivity.class);
                        intent.putExtra("showtimeId", showtimeId);
                        startActivity(intent);
                        finish();
                    } else {
                        showError("Không tạo được booking: " + res.code() + " - " + errorBody);
                    }
                    return;
                }

                BookingResponse b = res.body();
                Toast.makeText(PaymentActivity.this, "Đặt vé thành công (thanh toán tại quầy): " + b.getBookingCode(), Toast.LENGTH_LONG).show();

                Intent i = new Intent(PaymentActivity.this, BillActivity.class);
                i.putExtra(BillActivity.EXTRA_BOOKING_ID, b.getBookingId());
                i.putExtra("from_payment", true); // Để xử lý back navigation
                i.putExtra("showtimeId", savedShowtimeId);
                i.putStringArrayListExtra("seats", savedSeats);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                showError("Lỗi đặt vé CASH: " + t.getMessage());
            }
        });
    }


    // ================== PAY ZALO ==================
    private void payZalo() {
        String token = RetrofitClient.getToken();
        Log.d("PaymentActivity", "Token before payZalo: " + (token != null ? token.substring(0, 20) + "..." : "null"));

        Map<String, String> body = new HashMap<>();
        body.put("holdId", holdId);
        body.put("paymentMethod", "ZALOPAY");

        api.createBookingZaloPay(body).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    showError("Không tạo được booking: " + res.code());
                    return;
                }
                BookingResponse b = res.body();
                bookingId = b.bookingId;
                Log.d("PaymentActivity", "Booking created: " + bookingId);

                Map<String, String> body2 = new HashMap<>();
                body2.put("bookingId", bookingId);

                api.createZpOrder(body2).enqueue(new Callback<ZpCreateOrderResponse>() {
                    @Override
                    public void onResponse(Call<ZpCreateOrderResponse> call, Response<ZpCreateOrderResponse> r2) {
                        Log.d("PaymentActivity", "ZP Response Code: " + r2.code());
                        Log.d("PaymentActivity", "ZP Response Body: " + r2.body());

                        if (!r2.isSuccessful() || r2.body() == null) {
                            String errorBody = "";
//                            try { errorBody = res.errorBody() != null ? res.errorBody().string() : "No body"; } catch (Exception e) {}
                            try { errorBody = r2.errorBody() != null ? r2.errorBody().string() : "No body"; } catch (Exception e) {}
                            showError("Tạo order thất bại: " + res.code() + " - " + errorBody);
                            Log.e("PaymentActivity", "Create order failed: " + res.code() + " - " + errorBody);
                            return;
                        }

                        ZpCreateOrderResponse zp = r2.body();
                        String orderUrl = (zp != null) ? zp.getOrderUrl() : null;

                        if (orderUrl == null || orderUrl.isEmpty()) {
                            String errorBody = "";
                            try { errorBody = r2.errorBody() != null ? r2.errorBody().string() : "No body"; } catch (Exception ignore) {}
                            showError("Không nhận được URL thanh toán. Chi tiết: " + errorBody);
                            Log.e("PaymentActivity", "orderUrl null/empty, resp=" + zp);
                            return;
                        }
                        // ✅ Có URL → mở ZaloPay và bắt đầu polling
                        Log.d("PaymentActivity", "Open ZaloPay (https): " + orderUrl);
                        openZpOrderUrl(orderUrl);
                    }

                    @Override
                    public void onFailure(Call<ZpCreateOrderResponse> call, Throwable t) {
                        showError("Lỗi tạo đơn ZP: " + t.getMessage());
                        Log.e("PaymentActivity", "createZpOrder onFailure", t);
                    }
                });
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                showError("Lỗi booking ZP: " + t.getMessage());
                Log.e("PaymentActivity", "createBookingZaloPay onFailure", t);
            }
        });
    }

//    private void launchZaloPayWebUrl(String orderUrl) {
//        Log.d("PaymentActivity", "=== LAUNCH ZALOPAY WEB ===");
//        Log.d("PaymentActivity", "URL: " + orderUrl);
//
//        try {
//            // 1. THỬ ZALOPAY APP SCHEME
//            String queryParams = orderUrl.contains("?") ? orderUrl.split("\\?")[1] : "";
//            Uri zaloPayUri = Uri.parse("zalopay://pay?" + queryParams);
//            Intent zaloPayIntent = new Intent(Intent.ACTION_VIEW, zaloPayUri);
//            zaloPayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            if (zaloPayIntent.resolveActivity(getPackageManager()) != null) {
//                Log.d("PaymentActivity", "Opening ZaloPay app scheme");
//                startActivity(zaloPayIntent);
//            } else {
//                Log.d("PaymentActivity", "ZaloPay app not found, using web");
//                openWebBrowser(orderUrl);
//            }
//
//            // START POLLING NGAY Cả app scheme và web
//            startPollingBooking();
//            startPollingBooking();
//            Toast.makeText(this, "Đang chuyển đến ZaloPay...", Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            Log.e("PaymentActivity", "ZaloPay launch failed", e);
//            // FINAL FALLBACK: WEB BROWSER
//            openWebBrowser(orderUrl);
//            startPollingBooking();
//        }
//    }

    private void openWebBrowser(String orderUrl) {
        try {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(webIntent);
            Log.d("PaymentActivity", "Web browser opened successfully");
        } catch (Exception e) {
            Log.e("PaymentActivity", "Web browser failed", e);
            showError("Không thể mở ZaloPay");

            // PLAY STORE FALLBACK
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=vn.com.vng.zalopay"));
            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(playStoreIntent);
        }
    }

    // ================== DEEPLINK HANDLING (FALLBACK) ==================
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("pending", false)) {
            allowAutoConfirm = true;
            String id = getIntent().getStringExtra("bookingId");
            if (id != null) this.bookingId = id;
            startPollingBooking();
        }
    }

//    private void handleDeeplink(Intent intent) {
//        Uri data = (intent != null) ? intent.getData() : null;
//        Log.d("PaymentActivity", "Deep link received: " + data);
//
//        if (data == null) return;
//
//        if ("myapp".equalsIgnoreCase(data.getScheme()) && "zp-callback".equalsIgnoreCase(data.getHost())) {
//            String id = data.getQueryParameter("bookingId");
//            String status = data.getQueryParameter("status");
//
//            Log.d("PaymentActivity", "Deep link: id=" + id + ", status=" + status);
//
//            if (id != null && !id.isEmpty()) {
//                this.bookingId = id;
//
//                // SUCCESS → BILL ACTIVITY NGAY (KHÔNG CẦN POLLING)
//                if ("SUCCESS".equalsIgnoreCase(status)) {
//                    stopPolling();
//                    Log.d("PaymentActivity", "SUCCESS deep link - direct to BillActivity");
//                    showError("Thanh toán thành công!");
//                    navigateToBillActivity();
//                    return;
//                }
//                if ("PENDING".equalsIgnoreCase(status)) { // cho phép auto-confirm
//                    allowAutoConfirm = true;
//                    startPollingBooking(); // poll tiếp, sẽ confirm ở bước 3
//                }
//                // CANCELED → ERROR
//                String canceled = data.getQueryParameter("canceled");
//                if (canceled != null && ("1".equals(canceled) || "true".equals(canceled))) {
//                    stopPolling();
//                    showError("Thanh toán đã bị hủy");
//                    finish();
//                    return;
//                }
//            }
//        }
//    }


    // Dùng khi this.bookingId đã được set sẵn
    private void startPollingBooking() {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            Log.w("PaymentActivity", "startPollingBooking(): bookingId null/empty → bỏ qua");
            return;
        }
        if (isPolling) return;
        isPolling = true;
        pollHandler.post(pollTask);
    }

    // Dùng khi bạn đang cầm id mà chưa set vào field
    private void startPollingBooking(String id) {
        if (id == null || id.trim().isEmpty()) {
            Log.w("PaymentActivity", "startPollingBooking(id): id null/empty");
            return;
        }
        this.bookingId = id;
        startPollingBooking(); // gọi về bản không tham số
    }


    private void stopPolling() {
        isPolling = false;
        pollHandler.removeCallbacks(pollTask);
    }

    private final Runnable pollTask = new Runnable() {
        private long started = System.currentTimeMillis();
        private static final long TIMEOUT_MS = 5 * 60 * 1000; // 5 phút

        @Override
        public void run() {
            if (!isPolling || bookingId == null) {
                Log.w("PaymentActivity", "Polling stopped: isPolling=" + isPolling + ", bookingId=" + bookingId);
                return;
            }

            Log.d("PaymentActivity", "Polling booking: " + bookingId);

            api.getBooking(bookingId).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(Call<BookingResponse> call, Response<BookingResponse> res) {
                    if (!isPolling) {
                        Log.w("PaymentActivity", "Cuộc thăm dò đã bị hủy bỏ: isPolling=false");
                        return;
                    }

                    Log.d("PaymentActivity", "Poll response: " + res.code() +
                            ", body: " + (res.body() != null ? res.body().getStatus() : "null"));

                    if (res.isSuccessful() && res.body() != null) {
                        BookingResponse b = res.body();
                        String currentStatus = b.getStatus();

                        // CHỈ CONFIRMED MỚI SUCCESS, PENDING_PAYMENT CONTINUE POLLING
                        if ("CONFIRMED".equalsIgnoreCase(currentStatus)) {
                            stopPolling();
                            Log.d("PaymentActivity", "Payment CONFIRMED! Navigating to BillActivity");
                            showMessage("Thanh toán thành công: " + b.getBookingCode());
                            navigateToBillActivity();
                            return;
                        }

                        // PENDING_PAYMENT → CONTINUE POLLING
                        if ("PENDING_PAYMENT".equalsIgnoreCase(currentStatus)) {
                            long elapsed = System.currentTimeMillis() - orderOpenedAt;

                            // Chỉ auto-confirm nếu:
                            // 1) đã qua grace time (>= 15s) và chưa thử lần nào, hoặc
                            // 2) allowAutoConfirm == true (tức đã quay về deeplink status=PENDING)
                            if (!triedAutoConfirm && (elapsed >= AUTO_CONFIRM_GRACE_MS || allowAutoConfirm)) {
                                triedAutoConfirm = true;
                                api.confirmBooking(bookingId, Collections.emptyMap()).enqueue(new Callback<BookingResponse>() {
                                    @Override public void onResponse(Call<BookingResponse> c, Response<BookingResponse> r) {
                                        if (r.isSuccessful() && r.body()!=null && "CONFIRMED".equalsIgnoreCase(r.body().getStatus())) {
                                            stopPolling();
                                            navigateToBillActivity();
                                        } else {
                                            scheduleNextRun();
                                        }
                                    }
                                    @Override public void onFailure(Call<BookingResponse> c, Throwable t) { scheduleNextRun(); }
                                });
                                return;
                            }

                            scheduleNextRun();
                            return;
                        }

                        // FAILED/CANCELED → STOP
                        if ("FAILED".equalsIgnoreCase(currentStatus) || "CANCELED".equalsIgnoreCase(currentStatus)) {
                            stopPolling();
                            showError("Thanh toán thất bại: " + currentStatus);
                            finish();
                            return;
                        }

                        // UNKNOWN → CONTINUE
                        Log.w("PaymentActivity", "Unknown status: " + currentStatus + " - continue polling");
                        scheduleNextRun();
                    } else {
                        Log.w("PaymentActivity", "Poll failed: " + res.code() + " - retry");
                        scheduleNextRun();
                    }
                }

                @Override
                public void onFailure(Call<BookingResponse> call, Throwable t) {
                    Log.e("PaymentActivity", "Polling network error: " + t.getMessage());
                    scheduleNextRun(); // Retry network error
                }

                private void scheduleNextRun() {
                    if (!isPolling) {
                        Log.w("PaymentActivity", "ScheduleNext đã bị hủy: isPolling=false");
                        return;
                    }

                    long elapsed = System.currentTimeMillis() - started;
                    if (elapsed > TIMEOUT_MS) {
                        stopPolling();
                        showError("Hết thời gian chờ thanh toán. Vui lòng thử lại.");
                        finish();
                        return;
                    }

                    // Backoff: 2s → 3s → 4s → 5s max
                    long delay = Math.min(2000 + (elapsed / 2000), 5000);
                    Log.d("PaymentActivity", "Next poll in " + delay + "ms (total elapsed: " + elapsed + "ms)");
                    pollHandler.postDelayed(pollTask, delay); // SỬA: Dùng pollTask tham chiếu rõ ràng
                }
            });
        }
    };
    private void openZpOrderUrl(@NonNull String orderUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl));
            startActivity(intent);
            Log.d("PaymentActivity", "Opened ZaloPay with orderUrl: " + orderUrl);

            // đánh dấu thời điểm mở + bắt đầu polling
            orderOpenedAt = System.currentTimeMillis();
            triedAutoConfirm = false;
            allowAutoConfirm = false;
            startPollingBooking(); // bản không tham số, dùng field bookingId
        } catch (Exception e) {
            showError("Không mở được ZaloPay: " + e.getMessage());
            Log.e("PaymentActivity", "Failed to open orderUrl: " + orderUrl, e);
        }
    }
    private void navigateToBillActivity() {
        if (bookingId == null) {
            Log.e("PaymentActivity", "Không thể điều hướng: bookingId là null");
            return;
        }
        Log.d("PaymentActivity", "Điều hướng đến BillActivity với bookingId: " + bookingId);

        Intent i = new Intent(PaymentActivity.this, BillActivity.class);
        i.putExtra(BillActivity.EXTRA_BOOKING_ID, bookingId);
        if (savedShowtimeId != null) i.putExtra("showtimeId", savedShowtimeId);
        if (savedSeats != null) i.putStringArrayListExtra("seats", savedSeats);

        startActivity(i);
        stopPolling();
        finish();
    }

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

    private void checkBookingStatusDirect(String bookingId) {
        api.getBooking(bookingId).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> res) {
                if (res.isSuccessful() && res.body() != null) {
                    BookingResponse b = res.body();

                    // THÊM PENDING_PAYMENT vào success condition
                    if ("CONFIRMED".equalsIgnoreCase(b.getStatus()) ||
                            "PENDING_PAYMENT".equalsIgnoreCase(b.getStatus())) {

                        Toast.makeText(PaymentActivity.this, "Thanh toán thành công: " + b.getBookingCode(), Toast.LENGTH_LONG).show();

                        // DÙNG SEATS TỪ savedSeats (từ SeatPicker), hoặc fetch sau
                        Intent i = new Intent(PaymentActivity.this, BillActivity.class);
                        i.putExtra(BillActivity.EXTRA_BOOKING_ID, b.getBookingId());
                        if (savedShowtimeId != null) i.putExtra("showtimeId", savedShowtimeId);
                        if (savedSeats != null) i.putStringArrayListExtra("seats", savedSeats);
                        // BillActivity sẽ fetch lại nếu thiếu
                        startActivity(i);
                        finish();

                    } else if ("FAILED".equalsIgnoreCase(b.getStatus()) || "CANCELED".equalsIgnoreCase(b.getStatus())) {
                        showError("Thanh toán thất bại: " + b.getStatus());
                        finish();
                    } else {
                        showError("Trạng thái thanh toán chưa xác nhận: " + b.getStatus());
                    }
                } else {
                    showError("Lỗi kiểm tra trạng thái: " + res.code());
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    private void checkBookingStatus() {
        if (bookingId == null) return;
        api.getBooking(bookingId).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> res) {
                if (res.isSuccessful() && res.body() != null) {
                    BookingResponse b = res.body();
                    if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                        Toast.makeText(PaymentActivity.this, "Thanh toán thành công: " + b.getBookingCode(), Toast.LENGTH_LONG).show();
                        Intent i = new Intent(PaymentActivity.this, BillActivity.class);
                        i.putExtra("bookingId", b.getBookingId());
                        i.putExtra("showtimeId", savedShowtimeId);
                        i.putStringArrayListExtra("seats", savedSeats);
                        startActivity(i);
                        finish();
                    } else if ("FAILED".equalsIgnoreCase(b.getStatus()) || "CANCELED".equalsIgnoreCase(b.getStatus())) {
                        showError("Thanh toán thất bại: " + b.getStatus());
                        finish();
                    } else {
                        showError("Trạng thái thanh toán chưa xác nhận. Vui lòng kiểm tra sau.");
                    }
                } else {
                    showError("Lỗi kiểm tra trạng thái: " + res.code());
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                showError("Lỗi kiểm tra trạng thái: " + t.getMessage());
            }
        });
    }

    private void showError(String msg) {
        Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_LONG).show();
        Log.e("PaymentActivity", msg);
    }
    private void showMessage(String msg) {
        Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_LONG).show();
        Log.e("PaymentActivity", msg);
    }
}