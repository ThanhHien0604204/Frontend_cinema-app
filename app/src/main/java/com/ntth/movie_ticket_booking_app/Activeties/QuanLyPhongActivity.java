package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Room;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyPhongActivity extends AppCompatActivity {

    private ListView lvRoom;
    private ArrayAdapter<String> roomAdapter;
    private List<String> roomTitles;
    private List<String> roomIds;
    private EditText etSearch;
    private Handler handler; // Để tạo độ trễ khi tìm kiếm
    private Runnable searchRunnable;
    private ApiService apiService;
    private static final int REQUEST_CODE_DETAIL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlyphong);

        // Nút thêm phòng
        ImageView imAdd = findViewById(R.id.imAdd);
        imAdd.setOnClickListener(v -> {
            Intent intent = new Intent(QuanLyPhongActivity.this, QuanLyPhongDetailActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DETAIL);
        });

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        // Khởi tạo ListView và EditText
        lvRoom = findViewById(R.id.lvRoom);
        etSearch = findViewById(R.id.etSearch);

        // Khởi tạo danh sách phòng và adapter
        roomTitles = new ArrayList<>();
        roomIds = new ArrayList<>();
        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roomTitles);
        lvRoom.setAdapter(roomAdapter);

        // Khởi tạo Retrofit ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Lấy dữ liệu từ API
        fetchRoomData();

        // Khởi tạo Handler để tạo độ trễ
        handler = new Handler(Looper.getMainLooper());

        // Thêm sự kiện TextWatcher cho EditText
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> searchRooms(s.toString());
                handler.postDelayed(searchRunnable, 500); // Độ trễ 500ms
            }
        });

        // Xử lý click item
        lvRoom.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRoomId = roomIds.get(position);
            Intent intent = new Intent(QuanLyPhongActivity.this, QuanLyPhongDetailActivity.class);
            intent.putExtra("roomId", selectedRoomId);
            // Lấy locationId nếu cần, nhưng có thể load trong detail
            startActivityForResult(intent, REQUEST_CODE_DETAIL);
        });
    }

    private void fetchRoomData() {
        apiService.getAllRooms().enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomTitles.clear();
                    roomIds.clear();
                    for (Room room : response.body()) {
                        roomTitles.add(room.getRoomName());
                        roomIds.add(room.getId());
                    }
                    roomAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(QuanLyPhongActivity.this, "Lỗi khi tải dữ liệu phòng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                Toast.makeText(QuanLyPhongActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchRooms(String query) {
        if (query.isEmpty()) {
            fetchRoomData();
            return;
        }

        // Giả định API có endpoint search rooms, nếu không thì filter local
        // Ở đây filter local cho đơn giản
        ArrayList<String> filteredTitles = new ArrayList<>();
        ArrayList<String> filteredIds = new ArrayList<>();
        for (int i = 0; i < roomTitles.size(); i++) {
            if (roomTitles.get(i).toLowerCase().contains(query.toLowerCase())) {
                filteredTitles.add(roomTitles.get(i));
                filteredIds.add(roomIds.get(i));
            }
        }
        roomTitles.clear();
        roomIds.clear();
        roomTitles.addAll(filteredTitles);
        roomIds.addAll(filteredIds);
        roomAdapter.notifyDataSetChanged();
        if (roomTitles.isEmpty()) {
            Toast.makeText(QuanLyPhongActivity.this, "Không tìm thấy phòng nào!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            fetchRoomData();
        }
    }
}