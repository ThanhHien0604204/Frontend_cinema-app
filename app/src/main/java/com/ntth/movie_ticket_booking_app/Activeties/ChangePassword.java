package com.ntth.movie_ticket_booking_app.Activeties;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.ChangePasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword extends AppCompatActivity {
    private EditText txtMatKhau1, txtMatKhauMoi, txtNhapLaiMK;
    private Button btnDongY;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack_change);
        imBack.setOnClickListener(v -> finish());

        initUI();
        btnDongY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickChangePassword();
            }
        });
    }

    private void initUI() {
        txtMatKhau1 = findViewById(R.id.txtMatKhau1);  // Mật khẩu hiện tại
        txtMatKhauMoi = findViewById(R.id.txtMatKhauMoi);
        txtNhapLaiMK = findViewById(R.id.txtNhapLaiMK);
        btnDongY = findViewById(R.id.btnDongY);
        progressBar = findViewById(R.id.progressBar);
    }

    private void onClickChangePassword() {
        String currentPassword = txtMatKhau1.getText().toString().trim();
        String newPassword = txtMatKhauMoi.getText().toString().trim();
        String confirmPassword = txtNhapLaiMK.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(ChangePassword.this, "Hãy nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(ChangePassword.this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(ChangePassword.this, "Mật khẩu mới phải ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword, confirmPassword);
        RetrofitClient.api().changePassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePassword.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = response.errorBody() != null ? response.errorBody().toString() : response.message();
                    Toast.makeText(ChangePassword.this, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChangePassword.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}