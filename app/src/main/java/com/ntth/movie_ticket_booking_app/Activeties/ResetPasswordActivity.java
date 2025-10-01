package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.ResetPasswordRequest;
import com.ntth.movie_ticket_booking_app.dto.ResetPasswordResponse;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextInputLayout tilToken, tilNewPassword, tilConfirmPassword;
    private EditText etToken, etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private TextView tvBackToLogin;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initViews();
        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        setupClickListeners();
        setupTextWatchers();
        setupApi();
    }

    private void initViews() {
        tilToken = findViewById(R.id.tilToken);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etToken = findViewById(R.id.etToken);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> resetPassword());
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupTextWatchers() {
        // Token validation
        etToken.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateToken();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password validation
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateNewPassword();
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm password validation
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupApi() {
        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);
    }

    private boolean validateToken() {
        String token = etToken.getText().toString().trim();
        if (token.isEmpty()) {
            tilToken.setError("Mã token không được để trống");
            return false;
        } else if (token.length() < 10) {
            tilToken.setError("Mã token phải có ít nhất 10 ký tự");
            return false;
        } else {
            tilToken.setError(null);
            return true;
        }
    }

    private boolean validateNewPassword() {
        String password = etNewPassword.getText().toString().trim();
        if (password.isEmpty()) {
            tilNewPassword.setError("Mật khẩu mới không được để trống");
            return false;
        } else if (password.length() < 6) {
            tilNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        } else {
            tilNewPassword.setError(null);
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String password = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            return false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        } else {
            tilConfirmPassword.setError(null);
            return true;
        }
    }

    private boolean isFormValid() {
        return validateToken() && validateNewPassword() && validateConfirmPassword();
    }

    private void resetPassword() {
        if (!isFormValid()) {
            Toast.makeText(this, "Vui lòng kiểm tra lại thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = etToken.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        showLoading(true);

        ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword, confirmPassword);

        api.resetPassword(request).enqueue(new Callback<ResetPasswordResponse>() {
            @Override
            public void onResponse(Call<ResetPasswordResponse> call, Response<ResetPasswordResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().toString());
                        String message = json.optString("message", "Thành công!");
                        boolean success = json.optBoolean("success", false);

                        if (success) {
                            Toast.makeText(ResetPasswordActivity.this,
                                    message, Toast.LENGTH_LONG).show();

                            // Chuyển về Login với animation
                            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finishAffinity();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this,
                                    message, Toast.LENGTH_LONG).show();

                            // Focus vào field lỗi
                            if (message.contains("token")) {
                                etToken.requestFocus();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(ResetPasswordActivity.this,
                                "Có lỗi xảy ra khi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Lỗi server: " + response.code();
                    if (response.code() == 400) {
                        errorMessage = "Token không hợp lệ hoặc đã hết hạn";
                    } else if (response.code() == 401) {
                        errorMessage = "Không được phép thực hiện";
                    }

                    Toast.makeText(ResetPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResetPasswordResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ResetPasswordActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ResetPassword", "API call failed", t);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!show);
        btnResetPassword.setText(show ? "Đang xử lý..." : "Đặt lại mật khẩu");

        // Disable inputs khi loading
        etToken.setEnabled(!show);
        etNewPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
    }
}