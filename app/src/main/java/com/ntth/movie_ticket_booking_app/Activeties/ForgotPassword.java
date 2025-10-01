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
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.ForgotPasswordRequest;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ForgotPassword extends AppCompatActivity {
    private ProgressBar progressBar;
    private Button btnSendResetLink;
    private EditText txtEmailXT;
    private TextView txtDK;
    private ApiService api;

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
        btnSendResetLink = findViewById(R.id.btnGui);
        progressBar = findViewById(R.id.progressBar);
        txtEmailXT = findViewById(R.id.txtEmailXT);
        txtDK = findViewById(R.id.txtDK);

        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        btnSendResetLink.setOnClickListener(v -> sendResetLink());

        txtDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPassword.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    private void sendResetLink() {
        String email = txtEmailXT.getText().toString().trim();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSendResetLink.setEnabled(false);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        api.forgotPassword(request, "https://movie-ticket-booking-app-fvau.onrender.com")
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        progressBar.setVisibility(View.GONE);
                        btnSendResetLink.setEnabled(true);

                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String message = json.getString("message");
                            boolean success = json.getBoolean("success");

                            if (success) {
                                Toast.makeText(ForgotPassword.this,
                                        "Link đặt lại mật khẩu đã được gửi đến " + email,
                                        Toast.LENGTH_LONG).show();
                                // Chuyển đến màn hình nhập token
                                Intent intent = new Intent(ForgotPassword.this, ResetPasswordActivity.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(ForgotPassword.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnSendResetLink.setEnabled(true);
                        Toast.makeText(ForgotPassword.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}