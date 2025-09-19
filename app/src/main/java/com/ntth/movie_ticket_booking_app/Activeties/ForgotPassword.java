package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.ForgotPasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassword extends AppCompatActivity {
    private ProgressBar progressBar;
    private Button btnGui;
    private EditText txtEmailXT;
    private TextView txtDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnGui = findViewById(R.id.btnGui);
        progressBar = findViewById(R.id.progressBar);
        txtEmailXT = findViewById(R.id.txtEmailXT);
        txtDK = findViewById(R.id.txtDK);

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        btnGui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = txtEmailXT.getText().toString().trim();
                if (userEmail.isEmpty()) {
                    Toast.makeText(ForgotPassword.this, "Hãy nhập Email!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                ForgotPasswordRequest request = new ForgotPasswordRequest(userEmail);
                RetrofitClient.api().forgotPassword(request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(ForgotPassword.this, "Đã gửi email reset mật khẩu!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ForgotPassword.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ForgotPassword.this, "Email không tồn tại hoặc lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ForgotPassword.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        txtDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPassword.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}