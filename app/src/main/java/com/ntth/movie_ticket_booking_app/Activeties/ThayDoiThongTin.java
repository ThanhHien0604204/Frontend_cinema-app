package com.ntth.movie_ticket_booking_app.Activeties;

import android.os.Bundle;
import android.util.Log;
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

import com.ntth.movie_ticket_booking_app.Class.User;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.PublicUserResponse;
import com.ntth.movie_ticket_booking_app.dto.UpdateUserRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThayDoiThongTin extends AppCompatActivity {
    private Button btnCapNhat;
    private EditText editEmail, editText_Ten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thay_doi_thong_tin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        btnCapNhat = findViewById(R.id.btnCapNhat);
        editText_Ten = findViewById(R.id.editText_Ten);
        editEmail = findViewById(R.id.editEmail);

        // Load thông tin user
        loadUserInformation();

        btnCapNhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickUpdateUser();
            }
        });
    }

    private void loadUserInformation() {
        RetrofitClient.api().getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    editText_Ten.setText(user.getUserName() != null ? user.getUserName() : "");  // Kiểm tra null
                    editEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                    // Nếu có phone: editTextPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                    // Hiển thị role nếu cần
                    // Toast.makeText(ThayDoiThongTin.this, "Role: " + user.getRole(), Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();  // Chi tiết lỗi từ backend
                        } catch (Exception e) {
                            Log.e("API Error", e.getMessage());
                        }
                    }
                    Toast.makeText(ThayDoiThongTin.this, "Lỗi tải thông tin: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ThayDoiThongTin.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Failure", t.getMessage());
            }
        });
    }

    private void onClickUpdateUser() {
        String name = editText_Ten.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        // String phone = editTextPhone.getText().toString().trim();  // Nếu có

        if (name.isEmpty() || email.isEmpty()) {  // Thêm validation phone nếu có
            Toast.makeText(ThayDoiThongTin.this, "Hãy nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }


        // Sửa constructor: userName, email (nếu đổi, иначе null), phone (nếu có, иначе null)
        // Giả sử không đổi phone, set phone = null
        UpdateUserRequest request = new UpdateUserRequest(name, email);  // Thứ tự đúng: userName, email, phone=null (không đổi phone)

        // Sửa method name: updateUser thay vì updateMe
        RetrofitClient.api().updateMe(request).enqueue(new Callback<PublicUserResponse>() {
            @Override
            public void onResponse(Call<PublicUserResponse> call, Response<PublicUserResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ThayDoiThongTin.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = response.message();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();  // Lấy chi tiết lỗi từ backend (ví dụ: "Email đã tồn tại")
                        } catch (Exception e) {
                            Log.e("API Error", e.getMessage());
                        }
                    }
                    Toast.makeText(ThayDoiThongTin.this, "Cập nhật thất bại: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PublicUserResponse> call, Throwable t) {
                //if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ThayDoiThongTin.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Failure", t.getMessage());
            }
        });
    }
}