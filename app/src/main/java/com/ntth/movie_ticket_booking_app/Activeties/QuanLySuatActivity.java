package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.Class.Room;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.ShowtimeResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLySuatActivity extends AppCompatActivity {

    private ListView lvShow;
    private EditText etSearch;
    private ImageView imAdd;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> sessionDisplayList = new ArrayList<>();
    private ArrayList<ShowtimeResponse> sessionList = new ArrayList<>();
    private Map<String, String> movieIdToTitleMap = new HashMap<>(); // movieId -> movieTitle
    private Map<String, String> roomIdToNameMap = new HashMap<>(); // roomId -> roomName
    private ApiService apiService;

    private Map<String, String> roomToCinemaMap = new HashMap<>(); // roomId -> cinemaName

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlysuat);

        // Tham chiếu view
        lvShow = findViewById(R.id.lvShow);
        etSearch = findViewById(R.id.etSearch);
        imAdd = findViewById(R.id.imAdd);
        findViewById(R.id.imBack).setOnClickListener(v -> finish());

        // Khởi tạo Retrofit ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Load rooms để map roomId -> cinemaName
        loadRooms();

        // Nút thêm mới
        imAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuanLySuatDetailActivity.class);
            startActivity(intent);
        });

        // Tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        // Xử lý khi nhấn vào item trong ListView
        lvShow.setOnItemClickListener((parent, view, position, id) -> {
            ShowtimeResponse selectedSession = sessionList.get(position);
            Intent intent = new Intent(this, QuanLySuatDetailActivity.class);
            intent.putExtra("SessionId", selectedSession.getId());
            intent.putExtra("MovieName", selectedSession.getMovieTitle());
            intent.putExtra("MoviePrice", selectedSession.getPrice());
            intent.putExtra("StartDay", selectedSession.getStartAt()); // Assume string
            // Thêm các trường khác nếu cần, ví dụ roomId, cinema from map
            startActivity(intent);
        });
    }

    private void loadRooms() {
        apiService.getAllRooms().enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Room room : response.body()) {
                        apiService.getCinemaId(room.getCinemaId()).enqueue(new Callback<Cinema>() {
                            @Override
                            public void onResponse(Call<Cinema> call, Response<Cinema> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    roomToCinemaMap.put(room.getId(), response.body().getName());
                                }
                            }

                            @Override
                            public void onFailure(Call<Cinema> call, Throwable t) {
                                Log.e("QuanLySuatActivity", "Lỗi tải cinema: " + t.getMessage());
                            }
                        });
                    }
                    // Sau khi load rooms, load sessions
                    loadSessions();
                } else {
                    Toast.makeText(QuanLySuatActivity.this, "Lỗi tải rooms", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                Toast.makeText(QuanLySuatActivity.this, "Lỗi kết nối rooms: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSessions() {
        apiService.getAllShowtimes().enqueue(new Callback<List<ShowtimeResponse>>() {
            @Override
            public void onResponse(Call<List<ShowtimeResponse>> call, Response<List<ShowtimeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionList.clear();
                    sessionDisplayList.clear();
                    sessionList.addAll(response.body());

                    // Load movie titles và room names nếu chưa có
                    loadMissingMovieTitles();
                    loadMissingRoomNames();
                } else {
                    Log.e("QuanLySuatActivity", "Lỗi tải dữ liệu suất chiếu");
                }
            }

            @Override
            public void onFailure(Call<List<ShowtimeResponse>> call, Throwable t) {
                Log.e("QuanLySuatActivity", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    // Hàm load movie titles cho các movieId chưa có
    private void loadMissingMovieTitles() {
        List<String> missingMovieIds = new ArrayList<>();
        for (ShowtimeResponse session : sessionList) {
            String movieId = session.getMovieId();
            if (movieId != null && !movieIdToTitleMap.containsKey(movieId)) {
                missingMovieIds.add(movieId);
            }
        }

        if (missingMovieIds.isEmpty()) {
            updateDisplayList(); // Nếu không cần load, cập nhật danh sách ngay
            return;
        }

        // Gọi API cho từng movieId (hoặc batch nếu backend hỗ trợ)
        for (String movieId : missingMovieIds) {
            apiService.getMovieById(movieId).enqueue(new Callback<Movie>() {
                @Override
                public void onResponse(Call<Movie> call, Response<Movie> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        movieIdToTitleMap.put(movieId, response.body().getTitle());
                    }
                    checkIfAllLoaded(); // Kiểm tra nếu tất cả đã load xong
                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    Log.e("QuanLySuatActivity", "Lỗi load movie: " + t.getMessage());
                }
            });
        }
    }

    // Hàm load room names cho các roomId chưa có
    private void loadMissingRoomNames() {
        List<String> missingRoomIds = new ArrayList<>();
        for (ShowtimeResponse session : sessionList) {
            String roomId = session.getRoomId();
            if (roomId != null && !roomIdToNameMap.containsKey(roomId)) {
                missingRoomIds.add(roomId);
            }
        }

        if (missingRoomIds.isEmpty()) {
            updateDisplayList();
            return;
        }

        for (String roomId : missingRoomIds) {
            apiService.getRoom(roomId).enqueue(new Callback<Room>() {
                @Override
                public void onResponse(Call<Room> call, Response<Room> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        roomIdToNameMap.put(roomId, response.body().getRoomName());
                    }
                    checkIfAllLoaded();
                }

                @Override
                public void onFailure(Call<Room> call, Throwable t) {
                    Log.e("QuanLySuatActivity", "Lỗi load room: " + t.getMessage());
                }
            });
        }
    }
    // Kiểm tra nếu tất cả data đã load, rồi cập nhật UI
    private void checkIfAllLoaded() {
        for (ShowtimeResponse session : sessionList) {
            if (!movieIdToTitleMap.containsKey(session.getMovieId()) || !roomIdToNameMap.containsKey(session.getRoomId())) {
                return; // Chưa load hết, chờ
            }
        }
        updateDisplayList(); // Đã load hết, cập nhật danh sách
    }

    // Hàm cập nhật danh sách hiển thị
    private void updateDisplayList() {
        sessionDisplayList.clear();
        for (ShowtimeResponse session : sessionList) {
            String cinemaName = roomToCinemaMap.get(session.getRoomId());
            String movieTitle = movieIdToTitleMap.get(session.getMovieId());
            String roomName = roomIdToNameMap.get(session.getRoomId());
            String displayText = (movieTitle != null ? movieTitle : "Unknown Movie") + " | "
                    + session.getSessionName() + " | "
                    + (cinemaName != null ? cinemaName : "Unknown Cinema") + " | "
                    + (roomName != null ? roomName : "Unknown Room") + " | "
                    + session.getStartAt() + " | "
                    + session.getEndAt() + " | "
                    + session.getPrice();
            sessionDisplayList.add(displayText);
        }
        adapter = new ArrayAdapter<>(QuanLySuatActivity.this, android.R.layout.simple_list_item_1, sessionDisplayList);
        lvShow.setAdapter(adapter);
        adapter.notifyDataSetChanged(); // Thông báo thay đổi
    }
    private void filter(String query) {
        if (adapter != null) {
            adapter.getFilter().filter(query);
        }
    }
}