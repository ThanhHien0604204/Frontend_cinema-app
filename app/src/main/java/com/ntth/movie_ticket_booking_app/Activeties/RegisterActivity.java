package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.ntth.movie_ticket_booking_app.Class.User;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText txtNhapLaiMatKhau, txtEmail, txtMatKhauDK, txtTen;
    private CheckBox checkBoxDieuKhoan;
    private Button btDangKy2;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtTen = findViewById(R.id.txtTen);
        txtEmail = findViewById(R.id.txtEmail);
        txtMatKhauDK = findViewById(R.id.txtMatKhauDK);
        txtNhapLaiMatKhau = findViewById(R.id.txtNhapLaiMatKhau);
        checkBoxDieuKhoan = findViewById(R.id.checkBoxDieuKhoan);
        btDangKy2 = findViewById(R.id.btDangKy2);
        progressBar = findViewById(R.id.progressBar);

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack_register);
        imBack.setOnClickListener(v -> onBackPressed());
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }; getOnBackPressedDispatcher().addCallback(this, callback);
        initView();
    }

    private void initView() {
        btDangKy2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String ten = txtTen.getText().toString().trim();
                String email = txtEmail.getText().toString().trim();
                String password = txtMatKhauDK.getText().toString().trim();
                String NhapLai = txtNhapLaiMatKhau.getText().toString().trim();

                // Validation
                if (ten.isEmpty() || NhapLai.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "Hãy nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!checkBoxDieuKhoan.isChecked()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "Vui lòng đồng ý điều khoản!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (password.length() < 6) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "Mật khẩu phải ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tạo request
                RegisterRequest request = new RegisterRequest(ten, email, password);

                // Gọi API đăng ký
                ApiService api = RetrofitClient.getInstance().create(ApiService.class);
                api.register(request).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            // Giả định backend trả token trong header hoặc cần gọi login
                            // TODO: Nếu backend trả token, lưu token
                            // RetrofitClient.saveToken(response.headers().get("Authorization").replace("Bearer ", ""));
                            Toast.makeText(RegisterActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                            // Chuyển đến LoginActivity để đăng nhập
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finishAffinity();
                        } else {
                            String errorMsg = response.errorBody() != null ? response.errorBody().toString() : response.message();
                            Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Validation mật khẩu real-time
        txtMatKhauDK.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 6) {
                    txtMatKhauDK.setError("Mật khẩu phải có ít nhất 6 ký tự");
                } else {
                    txtMatKhauDK.setError(null);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
