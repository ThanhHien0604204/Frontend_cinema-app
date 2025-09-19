package com.ntth.movie_ticket_booking_app.Activeties;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.Class.Room;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyPhongDetailActivity extends AppCompatActivity {

    private EditText editTenPhong, editSoGhe, editSoCot, editSoHang;
    private Spinner spChonRap; // Spinner to choose location
    private Button btThemAdmin, btSuaAdmin, btXoaAdmin;
    private ApiService apiService;
    private String roomId; // Room ID for update or delete
    private Map<String, String> locationMap = new HashMap<>(); // name -> id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlyphong_detail);

        // Khởi tạo Retrofit ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Initialize views
        editTenPhong = findViewById(R.id.editTenBac);
        editSoGhe = findViewById(R.id.editGiamGia);
        editSoCot = findViewById(R.id.editDiem);
        editSoHang = findViewById(R.id.editSoHang);
        spChonRap = findViewById(R.id.spChonRap);

        btThemAdmin = findViewById(R.id.btThemAdmin);
        btSuaAdmin = findViewById(R.id.btSuaAdmin);
        btXoaAdmin = findViewById(R.id.btXoaAdmin);

        // Get roomId from intent (for update or delete)
        roomId = getIntent().getStringExtra("roomId");

        // Load locations for spinner
        loadLocations();

        if (roomId != null) {
            loadRoomData(roomId);
            btThemAdmin.setVisibility(View.GONE);
            btSuaAdmin.setVisibility(View.VISIBLE);
            btXoaAdmin.setVisibility(View.VISIBLE);
        } else {
            btSuaAdmin.setVisibility(View.GONE);
            btXoaAdmin.setVisibility(View.GONE);
        }

        btThemAdmin.setOnClickListener(v -> addRoom());
        //btSuaAdmin.setOnClickListener(v -> updateRoom());
        btXoaAdmin.setOnClickListener(v -> deleteRoom());

        ImageView imBack = findViewById(R.id.imBack);
        if (imBack != null) {
            imBack.setOnClickListener(v -> finish());
        }
    }

    private void loadLocations() {
        apiService.getCinemas().enqueue(new Callback<List<Cinema>>() {
            @Override
            public void onResponse(Call<List<Cinema>> call, Response<List<Cinema>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> locationNames = new ArrayList<>();
                    for (Cinema cinema : response.body()) {
                        locationNames.add(cinema.getName());
                        locationMap.put(cinema.getName(), cinema.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(QuanLyPhongDetailActivity.this, android.R.layout.simple_spinner_item, locationNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spChonRap.setAdapter(adapter);
                } else {
                    Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi tải danh sách rạp", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cinema>> call, Throwable t) {
                Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRoomData(String roomId) {
        apiService.getRoom(roomId).enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Room room = response.body();
                    editTenPhong.setText(room.getRoomName());
                    editSoGhe.setText(String.valueOf(room.getTotalSeats()));
                    editSoCot.setText(String.valueOf(room.getColumns()));
                    editSoHang.setText(String.valueOf(room.getRows()));

                    // Set spinner to current cinema
                    apiService.getCinemaId(room.getCinemaId()).enqueue(new Callback<Cinema>() {
                        @Override
                        public void onResponse(Call<Cinema> call, Response<Cinema> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String cinemaName = response.body().getName();
                                ArrayAdapter adapter = (ArrayAdapter) spChonRap.getAdapter();
                                if (adapter != null) {
                                    int position = adapter.getPosition(cinemaName);
                                    spChonRap.setSelection(position);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Cinema> call, Throwable t) {
                            Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi tải rạp: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi tải dữ liệu phòng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRoom() {
        String roomName = editTenPhong.getText().toString().trim();
        String totalSeatsStr = editSoGhe.getText().toString().trim();
        String columnsStr = editSoCot.getText().toString().trim();
        String rowsStr = editSoHang.getText().toString().trim();

        if (roomName.isEmpty() || totalSeatsStr.isEmpty() || columnsStr.isEmpty() || rowsStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int totalSeats = Integer.parseInt(totalSeatsStr);
            int columns = Integer.parseInt(columnsStr);
            int rows = Integer.parseInt(rowsStr);

            if (totalSeats <= 0 || columns <= 0 || rows <= 0) {
                Toast.makeText(this, "Số ghế, cột, hàng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            String cinemaId = locationMap.get(spChonRap.getSelectedItem().toString());
            if (cinemaId == null) {
                Toast.makeText(this, "Vui lòng chọn rạp hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            Room newRoom = new Room();
            newRoom.setRoomName(roomName);
            newRoom.setTotalSeats(totalSeats);
            newRoom.setColumns(columns);
            newRoom.setRows(rows);
            newRoom.setCinemaId(cinemaId);

            apiService.addRoom(newRoom).enqueue(new Callback<Room>() {
                @Override
                public void onResponse(Call<Room> call, Response<Room> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(QuanLyPhongDetailActivity.this, "Thêm phòng thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi thêm phòng: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Room> call, Throwable t) {
                    Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ cho ghế, cột, hàng", Toast.LENGTH_SHORT).show();
        }
    }

//    private void updateRoom() {
//        String roomName = editTenPhong.getText().toString().trim();
//        String totalSeatsStr = editSoGhe.getText().toString().trim();
//        String columnsStr = editSoCot.getText().toString().trim();
//        String rowsStr = editSoHang.getText().toString().trim();
//
//        if (roomName.isEmpty() || totalSeatsStr.isEmpty() || columnsStr.isEmpty() || rowsStr.isEmpty()) {
//            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            int totalSeats = Integer.parseInt(totalSeatsStr);
//            int columns = Integer.parseInt(columnsStr);
//            int rows = Integer.parseInt(rowsStr);
//
//            if (totalSeats <= 0 || columns <= 0 || rows <= 0) {
//                Toast.makeText(this, "Số ghế, cột, hàng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            String cinemaId = locationMap.get(spChonRap.getSelectedItem().toString());
//            if (cinemaId == null) {
//                Toast.makeText(this, "Vui lòng chọn rạp hợp lệ", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Room updatedRoom = new Room();
//            updatedRoom.setId(roomId);
//            updatedRoom.setRoomName(roomName);
//            updatedRoom.setTotalSeats(totalSeats);
//            updatedRoom.setColumns(columns);
//            updatedRoom.setRows(rows);
//            updatedRoom.setCinemaId(cinemaId);
//
//            apiService.updateRoom(roomId, updatedRoom).enqueue(new Callback<Room>() {
//                @Override
//                public void onResponse(Call<Room> call, Response<Room> response) {
//                    if (response.isSuccessful()) {
//                        Toast.makeText(QuanLyPhongDetailActivity.this, "Sửa phòng thành công", Toast.LENGTH_SHORT).show();
//                        finish();
//                    } else {
//                        Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi sửa phòng: " + response.message(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Room> call, Throwable t) {
//                    Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "Vui lòng nhập số hợp lệ cho ghế, cột, hàng", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void deleteRoom() {
        if (roomId == null) {
            Toast.makeText(this, "Không có phòng để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteRoom(roomId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyPhongDetailActivity.this, "Xóa phòng thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi xóa phòng: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QuanLyPhongDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}