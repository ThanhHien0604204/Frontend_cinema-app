package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.SeatAdapter;
import com.ntth.movie_ticket_booking_app.Class.GridSpacingItemDecoration;
import com.ntth.movie_ticket_booking_app.Class.Seat;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.BookingRequest;
import com.ntth.movie_ticket_booking_app.dto.BookingResponse;
import com.ntth.movie_ticket_booking_app.dto.HoldSeatsRequest;
import com.ntth.movie_ticket_booking_app.dto.HoldSeatsResponse;
import com.ntth.movie_ticket_booking_app.dto.SeatResponse;
import com.ntth.movie_ticket_booking_app.dto.ZpCreateOrderResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeatPickerActivity extends AppCompatActivity {

    private RecyclerView recyclerSeats;
    private TextView tvInfo, priceTextView;
    private ImageView back;
    private Button btnPay;

    private String showtimeId;
    private SeatAdapter adapter;
    private final Set<String> selected = new HashSet<>();
    private long unitPrice = 60000; // hoặc lấy từ API suất chiếu

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_seat_picker);

        showtimeId = getIntent().getStringExtra("showtimeId");
        recyclerSeats = findViewById(R.id.recyclerSeats);
        tvInfo = findViewById(R.id.tvInfo);
        priceTextView = findViewById(R.id.priceTextView);
        btnPay = findViewById(R.id.btnPay);

        adapter = new SeatAdapter(code -> {
            if (selected.contains(code)) selected.remove(code);
            else selected.add(code);
            renderPrice();
        });

        // Nút quay lại
        back=findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

        recyclerSeats.setLayoutManager(new GridLayoutManager(this, 5)); // số cột tùy phòng
        recyclerSeats.setAdapter(adapter);

        btnPay.setOnClickListener(v -> gotoPayment());

        loadSeatLedger();
    }

    private void loadSeatLedger() {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getSeatLedger(showtimeId).enqueue(new Callback<List<SeatResponse>>() {
            @Override public void onResponse(Call<List<SeatResponse>> call, Response<List<SeatResponse>> resp) {
                if (!resp.isSuccessful() || resp.body()==null) {
                    Toast.makeText(SeatPickerActivity.this, "Không tải được sơ đồ ghế", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.setData(resp.body());
                adapter.setSelected(selected);
                tvInfo.setText("Chọn ghế");
            }
            @Override public void onFailure(Call<List<SeatResponse>> call, Throwable t) {
                Toast.makeText(SeatPickerActivity.this, "Lỗi mạng: "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderPrice() {
        long amount = unitPrice * selected.size();
        priceTextView.setText("Giá vé: " + amount + "đ");
    }

    private void gotoPayment() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "Hãy chọn ít nhất 1 ghế", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, PaymentActivity.class);
        Log.d("SeatPickerActivity", "Starting PaymentActivity with showtimeId: " + showtimeId + ", seats: " + selected.toString());
        i.putExtra("showtimeId", showtimeId);
        i.putStringArrayListExtra("seats", new ArrayList<>(selected));
        startActivity(i);
    }
    private void setupRecyclerView() {
        // Tạo GridLayoutManager với span count linh hoạt
        int spanCount = calculateSpanCount(); // Tính dựa trên kích thước màn hình
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);

        // Thêm ItemDecoration để tạo khoảng cách
        int spacing = 8; // 8dp spacing
        RecyclerView.ItemDecoration itemDecoration =
                new GridSpacingItemDecoration(spanCount, spacing, true);

        recyclerSeats.setLayoutManager(layoutManager);
        recyclerSeats.addItemDecoration(itemDecoration);
        recyclerSeats.setAdapter(adapter);
    }

    // Tính số cột dựa trên màn hình
    private int calculateSpanCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int itemWidth = 60; // 60dp cho mỗi ghế
        return width / (itemWidth * (int) getResources().getDisplayMetrics().density);
    }
}
