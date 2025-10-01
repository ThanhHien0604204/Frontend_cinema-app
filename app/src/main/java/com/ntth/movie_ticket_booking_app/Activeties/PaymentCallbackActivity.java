package com.ntth.movie_ticket_booking_app.Activeties;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ntth.movie_ticket_booking_app.R;

public class PaymentCallbackActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent() != null ? getIntent().getData() : null;
        String id = uri != null ? uri.getQueryParameter("bookingId") : null;
        String status = uri != null ? uri.getQueryParameter("status") : null;
        if (id == null) { finish(); return; }

        Intent next;
        if ("FAILED".equalsIgnoreCase(status)) {
            Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
            finish(); return;
        } else if ("SUCCESS".equalsIgnoreCase(status)) {
            // Có thể vào Bill ngay; Bill sẽ tự fetch bằng bookingId
            next = new Intent(this, BillActivity.class);
        } else { // PENDING hoặc null -> quay Payment để polling
            next = new Intent(this, PaymentActivity.class)
                    .putExtra("pending", true);
        }
        next.putExtra("bookingId", id);
        // Đưa activity đích hiện có lên (nếu đang mở), không tạo thêm instance
        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(next);
        finish();
    }
}
