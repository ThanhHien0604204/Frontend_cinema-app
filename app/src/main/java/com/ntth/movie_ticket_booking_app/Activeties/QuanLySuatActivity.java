//package com.ntth.movie_ticket_booking_app.Activeties;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.ntth.movie_ticket_booking_app.Class.Cinema;
//import com.ntth.movie_ticket_booking_app.R;
//import com.ntth.movie_ticket_booking_app.Class.Room;
//import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
//import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
//import com.ntth.movie_ticket_booking_app.dto.ShowtimeResponse;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class QuanLySuatActivity extends AppCompatActivity {
//
//    private ListView lvShow;
//    private EditText etSearch;
//    private ImageView imAdd;
//
//    private ArrayAdapter<String> adapter;
//    private ArrayList<String> sessionDisplayList = new ArrayList<>();
//    private ArrayList<ShowtimeResponse> sessionList = new ArrayList<>();
//    private ApiService apiService;
//
//    private Map<String, String> roomToCinemaMap = new HashMap<>(); // roomId -> cinemaName
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_quanlysuat);
//
//        // Tham chiếu view
//        lvShow = findViewById(R.id.lvShow);
//        etSearch = findViewById(R.id.etSearch);
//        imAdd = findViewById(R.id.imAdd);
//        findViewById(R.id.imBack).setOnClickListener(v -> finish());
//
//        // Khởi tạo Retrofit ApiService
//        apiService = RetrofitClient.getInstance().create(ApiService.class);
//
//        // Load rooms để map roomId -> cinemaName
//        loadRooms();
//
//        // Nút thêm mới
//        imAdd.setOnClickListener(v -> {
//            Intent intent = new Intent(this, QuanLySuatDetailActivity.class);
//            startActivity(intent);
//        });
//
//        // Tìm kiếm
//        etSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                filter(s.toString());
//            }
//        });
//
//        // Xử lý khi nhấn vào item trong ListView
//        lvShow.setOnItemClickListener((parent, view, position, id) -> {
//            ShowtimeResponse selectedSession = sessionList.get(position);
//            Intent intent = new Intent(this, QuanLySuatDetailActivity.class);
//            intent.putExtra("SessionId", selectedSession.getId());
//            intent.putExtra("MovieName", selectedSession.getMovieTitle());
//            intent.putExtra("MoviePrice", selectedSession.getPrice());
//            intent.putExtra("StartDay", selectedSession.getStartAt()); // Assume string
//            // Thêm các trường khác nếu cần, ví dụ roomId, cinema from map
//            startActivity(intent);
//        });
//    }
//
//    private void loadRooms() {
//        apiService.getAllRooms().enqueue(new Callback<List<Room>>() {
//            @Override
//            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    for (Room room : response.body()) {
//                        apiService.getCinemaId(room.getCinemaId()).enqueue(new Callback<Cinema>() {
//                            @Override
//                            public void onResponse(Call<Cinema> call, Response<Cinema> response) {
//                                if (response.isSuccessful() && response.body() != null) {
//                                    roomToCinemaMap.put(room.getId(), response.body().getName());
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Call<Cinema> call, Throwable t) {
//                                Log.e("QuanLySuatActivity", "Lỗi tải cinema: " + t.getMessage());
//                            }
//                        });
//                    }
//                    // Sau khi load rooms, load sessions
//                    loadSessions();
//                } else {
//                    Toast.makeText(QuanLySuatActivity.this, "Lỗi tải rooms", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Room>> call, Throwable t) {
//                Toast.makeText(QuanLySuatActivity.this, "Lỗi kết nối rooms: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void loadSessions() {
//        apiService.getAllShowtimes().enqueue(new Callback<List<ShowtimeResponse>>() {
//            @Override
//            public void onResponse(Call<List<ShowtimeResponse>> call, Response<List<ShowtimeResponse>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    sessionList.clear();
//                    sessionDisplayList.clear();
//                    sessionList.addAll(response.body());
//                    for (ShowtimeResponse session : sessionList) {
//                        String cinemaName = roomToCinemaMap.get(session.getRoomId());
//                        String displayText = session.getMovieTitle() + " | "
//                                + session.getSessionName() + " | "
//                                + (cinemaName != null ? cinemaName : "Unknown Cinema") + " | "
//                                + session.getRoomName() + " | "
//                                + session.getStartAt() + " | "
//                                + session.getEndAt() + " | "
//                                + session.getPrice();
//                        sessionDisplayList.add(displayText);
//                    }
//                    adapter = new ArrayAdapter<>(QuanLySuatActivity.this, android.R.layout.simple_list_item_1, sessionDisplayList);
//                    lvShow.setAdapter(adapter);
//                } else {
//                    Log.e("QuanLySuatActivity", "Lỗi tải dữ liệu suất chiếu");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<ShowtimeResponse>> call, Throwable t) {
//                Log.e("QuanLySuatActivity", "Lỗi kết nối: " + t.getMessage());
//            }
//        });
//    }
//
//    private void filter(String query) {
//        if (adapter != null) {
//            adapter.getFilter().filter(query);
//        }
//    }
//}