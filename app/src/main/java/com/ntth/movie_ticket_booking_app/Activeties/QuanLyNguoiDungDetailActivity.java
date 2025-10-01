package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.User;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.PublicUserResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyNguoiDungDetailActivity extends AppCompatActivity {

    private EditText editUserName, editEmail, editPassword, editPhone;
    private Spinner spinnerUserRole;
    private Button btThemAdmin, btSuaAdmin, btXoaAdmin;
    private ApiService apiService;
    private String userId;  // Để update/delete

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlynguoidung_detail);

        apiService = RetrofitClient.api();

        // Ánh xạ View (sửa ID cho đúng: giả sử editTenUser là userName, editTaiKhoan là email, editMatKhau là password)
        ImageView imBack = findViewById(R.id.imBack);
        editUserName = findViewById(R.id.editUserName);  // Tên user
        editEmail = findViewById(R.id.editEmail);    // Email
        editPassword = findViewById(R.id.editMatKhau);    // Password
        spinnerUserRole = findViewById(R.id.spinnerUserRole);
        btThemAdmin = findViewById(R.id.btThemAdmin);
        btSuaAdmin = findViewById(R.id.btSuaAdmin);
        btXoaAdmin = findViewById(R.id.btXoaAdmin);

        imBack.setOnClickListener(v -> finish());

        // Thiết lập Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"ADMIN", "USER"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserRole.setAdapter(spinnerAdapter);

        userId = getIntent().getStringExtra("userId");

        if (userId != null) {
            loadUserDetails(userId);
            btThemAdmin.setVisibility(View.GONE);
            btSuaAdmin.setVisibility(View.VISIBLE);
            btXoaAdmin.setVisibility(View.VISIBLE);
            editPassword.setVisibility(View.GONE);  // Ẩn password khi edit (optional)
        } else {
            btSuaAdmin.setVisibility(View.GONE);
            btXoaAdmin.setVisibility(View.GONE);
        }

        btThemAdmin.setOnClickListener(v -> addUser());
        btSuaAdmin.setOnClickListener(v -> updateUser());
        btXoaAdmin.setOnClickListener(v -> deleteUser());
    }

    private void loadUserDetails(String userId) {
        apiService.getUserByIdADMIN(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    editUserName.setText(user.getUserName());
                    editEmail.setText(user.getEmail());
                    //editPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                    // Không load password
                    spinnerUserRole.setSelection("ADMIN".equals(user.getRole()) ? 0 : 1);
                } else {
                    Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Lỗi tải chi tiết user!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addUser() {
        if (!validateInputs()) return;

        User newUser = new User();
        newUser.setUserName(editUserName.getText().toString().trim());
        newUser.setEmail(editEmail.getText().toString().trim());
        newUser.setPassword(editPassword.getText().toString().trim());
        // Xóa dòng này: newUser.setPhone(editPhone.getText().toString().trim());
        newUser.setRole(spinnerUserRole.getSelectedItem().toString());

        apiService.createUser(newUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Thêm user thành công!", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Lỗi thêm user: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser() {
        if (userId == null || !validateInputs()) return;

        User updatedUser = new User();
        updatedUser.setUserName(editUserName.getText().toString().trim());
        updatedUser.setEmail(editEmail.getText().toString().trim());
        // Password optional: chỉ set nếu user nhập mới
        String password = editPassword.getText().toString().trim();
        if (!password.isEmpty()) {
            updatedUser.setPassword(password);
        }
        // Xóa dòng này: updatedUser.setPhone(editPhone.getText().toString().trim());
        updatedUser.setRole(spinnerUserRole.getSelectedItem().toString());

        apiService.updateUser(userId, updatedUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Lỗi cập nhật: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser() {
        if (userId == null) {
            Toast.makeText(this, "Không có user để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Xóa user thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Lỗi xóa user: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QuanLyNguoiDungDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(editUserName.getText())) {
            editUserName.setError("Tên user không được để trống!");
            return false;
        }
        if (TextUtils.isEmpty(editEmail.getText())) {
            editEmail.setError("Email không được để trống!");
            return false;
        }
        if (TextUtils.isEmpty(editPassword.getText()) && userId == null) {  // Chỉ bắt buộc khi tạo mới
            editPassword.setError("Password không được để trống!");
            return false;
        }
        // Xóa đoạn kiểm tra phone
        return true;
    }

    private void clearInputs() {
        editUserName.setText("");
        editEmail.setText("");
        editPassword.setText("");
        editPhone.setText("");
        spinnerUserRole.setSelection(1);  // Default USER
    }
}