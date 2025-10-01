package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;


public class AdminMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Kết nối các ImageView
        ImageView imQuanLyRap = findViewById(R.id.imQuanLyRap);
        ImageView imQuanLyPhim = findViewById(R.id.imQuanLyPhim);
        ImageView imQuanLyPhong = findViewById(R.id.imQuanLyPhong);
        //ImageView imQuanLyBacThanhVien = findViewById(R.id.imQuanLyBacThanhVien);
        ImageView imQuanLyNguoiDung = findViewById(R.id.imQuanLyNguoiDung);
        ImageView imQuanLySuat = findViewById(R.id.imQuanLySuat);
        ImageView imDangXuat = findViewById(R.id.imDangXuat); // Kết nối ImageView




        // Thêm sự kiện click
        imQuanLyRap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, QuanLyRapActivity.class);
                startActivity(intent);
            }
        });

        imQuanLyPhim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, QuanLyPhimActivity.class);
                startActivity(intent);
            }
        });

        imQuanLyPhong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, QuanLyPhongActivity.class);
                startActivity(intent);
            }
        });

//        imQuanLyBacThanhVien.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AdminMainActivity.this, QuanLyBacThanhVienActivity.class);
//                startActivity(intent);
//            }
//        });

        imQuanLyNguoiDung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, QuanLyNguoiDungActivity.class);
                startActivity(intent);
            }
        });

        imQuanLySuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, QuanLySuatActivity.class);
                startActivity(intent);
            }
        });

//        imThongKe.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AdminMainActivity.this, ThongKeActivity.class);
//                startActivity(intent);
//            }
//        });

        imDangXuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đăng xuất người dùng
                // Xóa token
                RetrofitClient.clearToken();

                // Xóa thông tin người dùng trong SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("auth_pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // Quay về màn hình chính (MainActivity)
                Intent intent = new Intent(AdminMainActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Đảm bảo thoát hoàn toàn khỏi AdminMainActivity
                startActivity(intent);
                finish(); // Kết thúc AdminMainActivity để tránh quay lại bằng nút Back
            }
        });
    }
}
