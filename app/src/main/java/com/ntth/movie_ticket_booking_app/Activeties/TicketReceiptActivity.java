package com.ntth.movie_ticket_booking_app.Activeties;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.BookingResponse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketReceiptActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "booking_id";

    private TextView tvCode, tvStatus, tvAmount, tvSeats, tvShowtime, tvGateway;
    private ProgressBar progress;

    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_receipt);

        tvCode = findViewById(R.id.tvCode);
        tvStatus = findViewById(R.id.tvStatus);
        tvAmount = findViewById(R.id.tvAmount);
        tvSeats = findViewById(R.id.tvSeats);
        tvShowtime = findViewById(R.id.tvShowtime);
        tvGateway = findViewById(R.id.tvGateway);
        progress = findViewById(R.id.progress);

        bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        if (bookingId == null) {
            Toast.makeText(this, "Thiếu bookingId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBooking();
    }

    private void loadBooking() {
        progress.setVisibility(View.VISIBLE);
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getBooking(bookingId).enqueue(new Callback<BookingResponse>() {
            @Override public void onResponse(Call<BookingResponse> call, Response<BookingResponse> resp) {
                progress.setVisibility(View.GONE);
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(TicketReceiptActivity.this, "Không tải được vé", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                bind(resp.body());
            }

            @Override public void onFailure(Call<BookingResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(TicketReceiptActivity.this, "Lỗi kết nối: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bind(BookingResponse b) {
        String code = b.bookingCode != null ? b.bookingCode : "(n/a)";
        String status = b.status != null ? b.status : "(n/a)";
        long amount = b.amount;
        List<String> seats = b.seats;
        String showtime = b.showtimeId != null ? b.showtimeId : "(n/a)";
        String gateway = (b.payment != null && b.payment.gateway != null) ? b.payment.gateway : "(n/a)";

        tvCode.setText(code);
        tvStatus.setText(status);
        tvAmount.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount));
        tvSeats.setText(seats != null ? String.join(", ", seats) : "(n/a)");
        tvShowtime.setText(showtime);
        tvGateway.setText(gateway);
    }
}
