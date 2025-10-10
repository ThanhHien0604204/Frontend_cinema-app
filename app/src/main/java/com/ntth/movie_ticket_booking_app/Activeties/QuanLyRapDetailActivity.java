package com.ntth.movie_ticket_booking_app.Activeties;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyRapDetailActivity extends AppCompatActivity {
    private EditText edTenRap, edDiaChi, edSoPhong;
    private Button btThem, btSua, btXoa;
    private ApiService apiService;
    private String locationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlyrap_detail);

        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        edTenRap = findViewById(R.id.edSoCot);  // Sửa ID nếu cần, có lẽ là edTenRap
        edDiaChi = findViewById(R.id.editDiaChi);
        edSoPhong = findViewById(R.id.editSoPhong);

        btThem = findViewById(R.id.btThemAdmin);
        btSua = findViewById(R.id.btSuaAdmin);
        btXoa = findViewById(R.id.btXoaAdmin);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        locationId = getIntent().getStringExtra("locationId");

        if (locationId != null) {
            loadLocationDetails(locationId);
            btThem.setVisibility(View.GONE);
            btSua.setVisibility(View.VISIBLE);
            btXoa.setVisibility(View.VISIBLE);
        } else {
            btSua.setVisibility(View.GONE);
            btXoa.setVisibility(View.GONE);
        }

        btThem.setOnClickListener(v -> addLocation());
        btSua.setOnClickListener(v -> updateLocation());
        btXoa.setOnClickListener(v -> deleteLocation());
    }

    private void loadLocationDetails(String locationId) {
        apiService.getCinemaId(locationId).enqueue(new Callback<Cinema>() {
            @Override
            public void onResponse(Call<Cinema> call, Response<Cinema> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Cinema cinema = response.body();
                    edTenRap.setText(cinema.getName());
                    edDiaChi.setText(cinema.getAddress());
                    // Giả định numberOfRooms là số phòng, nếu backend có list rooms thì tính length
                    edSoPhong.setText(String.valueOf(cinema.getNumberOfRooms()));  // Nếu có trường numberOfRooms
                } else {
                    Toast.makeText(QuanLyRapDetailActivity.this, "Lỗi tải chi tiết rạp!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cinema> call, Throwable t) {
                Toast.makeText(QuanLyRapDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLocation() {
        String name = edTenRap.getText().toString().trim();
        String address = edDiaChi.getText().toString().trim();
        String numberOfRoomsStr = edSoPhong.getText().toString().trim();

        if (!validateInputs(name, address, numberOfRoomsStr)) {
            return;
        }

        int numberOfRooms = Integer.parseInt(numberOfRoomsStr);

        Cinema newCinema = new Cinema();
        newCinema.setName(name);
        newCinema.setAddress(address);
        newCinema.setNumberOfRooms(numberOfRooms);  // Giả định có setter này

        apiService.addCinema(newCinema).enqueue(new Callback<Cinema>() {
            @Override
            public void onResponse(Call<Cinema> call, Response<Cinema> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyRapDetailActivity.this, "Thêm rạp thành công!", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(QuanLyRapDetailActivity.this, "Lỗi thêm rạp: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cinema> call, Throwable t) {
                Toast.makeText(QuanLyRapDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocation() {
        String name = edTenRap.getText().toString().trim();
        String address = edDiaChi.getText().toString().trim();
        String numberOfRoomsStr = edSoPhong.getText().toString().trim();

        if (!validateInputs(name, address, numberOfRoomsStr)) {
            return;
        }

        int numberOfRooms = Integer.parseInt(numberOfRoomsStr);

        Cinema updatedCinema = new Cinema();
        updatedCinema.setId(locationId);
        updatedCinema.setName(name);
        updatedCinema.setAddress(address);
        updatedCinema.setNumberOfRooms(numberOfRooms);

        apiService.updateCinema(locationId, updatedCinema).enqueue(new Callback<Cinema>() {
            @Override
            public void onResponse(Call<Cinema> call, Response<Cinema> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyRapDetailActivity.this, "Sửa rạp thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(QuanLyRapDetailActivity.this, "Lỗi sửa rạp: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cinema> call, Throwable t) {
                Toast.makeText(QuanLyRapDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteLocation() {
        if (locationId == null) {
            Toast.makeText(this, "Không có rạp để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteCinema(locationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyRapDetailActivity.this, "Xóa rạp thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(QuanLyRapDetailActivity.this, "Lỗi xóa rạp: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QuanLyRapDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String name, String address, String numberOfRoomsStr) {
        if (TextUtils.isEmpty(name)) {
            edTenRap.setError("Tên rạp không được để trống!");
            return false;
        }

        if (TextUtils.isEmpty(address)) {
            edDiaChi.setError("Địa chỉ không được để trống!");
            return false;
        }

        if (TextUtils.isEmpty(numberOfRoomsStr)) {
            edSoPhong.setError("Số phòng không được để trống!");
            return false;
        }

        try {
            int numberOfRooms = Integer.parseInt(numberOfRoomsStr);
            if (numberOfRooms <= 0) {
                edSoPhong.setError("Số phòng phải lớn hơn 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            edSoPhong.setError("Số phòng phải là số hợp lệ!");
            return false;
        }

        return true;
    }

    private void clearInputs() {
        edTenRap.setText("");
        edDiaChi.setText("");
        edSoPhong.setText("");
    }
}