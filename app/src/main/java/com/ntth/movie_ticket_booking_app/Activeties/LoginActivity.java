package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.AuthRequest;
import com.ntth.movie_ticket_booking_app.dto.AuthToken;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private TextView txtQuenMK;
    private Button loginButton,registerBtn;
    private final ApiService api = RetrofitClient.api();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.txtEmail);
        passwordEditText = findViewById(R.id.txtMatKhau);
        txtQuenMK =findViewById(R.id.txtQuenMK);
        loginButton = findViewById(R.id.btLogin);
        registerBtn =findViewById(R.id.btDangKy1);

        txtQuenMK.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this,ForgotPassword.class)));
        registerBtn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this,RegisterActivity.class)));
        loginButton.setOnClickListener(v -> doLogin());
    }
    private void doLogin () {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        AuthRequest request = new AuthRequest(
                emailEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim()
        );
        Log.d("LoginActivity", "Gửi login request: " + request.getEmail() + ", password: " + request.getPassword());

        api.login(request).enqueue(new Callback<AuthToken>() {
            @Override
            public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                Log.d("LoginActivity", "Response code ở Login: " + response.code() + ", body: " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    RetrofitClient.saveToken(LoginActivity.this, response.body().getAccessToken());

                    // Chuyển sang MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "No error body";
                    Log.d("LoginActivity", "Response error: " + response.message() + ", errorBody: " + errorBody);
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthToken> call, Throwable t) {
                Log.e("LoginActivity", "Request failed: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}