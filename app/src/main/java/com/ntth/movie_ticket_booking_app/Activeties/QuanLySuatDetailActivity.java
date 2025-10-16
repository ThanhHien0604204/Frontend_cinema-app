package com.ntth.movie_ticket_booking_app.Activeties;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.Class.Room;
import com.ntth.movie_ticket_booking_app.Class.Showtime;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.CreateShowtimeRequest;
import com.ntth.movie_ticket_booking_app.dto.ShowtimeResponse;
import com.ntth.movie_ticket_booking_app.dto.UpdateShowtimeRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLySuatDetailActivity extends AppCompatActivity {

    private Button btChonNgay, btChonGio, btChonGioHet, btThemAdmin, btSuaAdmin, btXoaAdmin;
    private Spinner spPhim, spPhong, spRap;
    private EditText edGiaVe, edSessionName;
    private ImageView back;

    private ApiService apiService;

    private String sessionId;

    private Map<String, String> movieMap = new HashMap<>(); // title -> id
    private Map<String, String> cinemaMap = new HashMap<>(); // name -> id
    private Map<String, String> roomMap = new HashMap<>(); // name -> id

    private String selectedStartDay = "";
    private String selectedStartHour = "";
    private String selectedEndHour = "";
    private boolean isDataLoaded = false;
    private Showtime currentSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlysuat_detail);

        // Ánh xạ các view
        btChonNgay = findViewById(R.id.btChonNgay);
        btChonGio = findViewById(R.id.btChonGio);
        btChonGioHet = findViewById(R.id.btChonGioHet);
        btThemAdmin = findViewById(R.id.btThemAdmin);
        btSuaAdmin = findViewById(R.id.btSuaAdmin);
        btXoaAdmin = findViewById(R.id.btXoaAdmin);

        spPhim = findViewById(R.id.spPhim);
        spPhong = findViewById(R.id.spPhong);
        spRap = findViewById(R.id.spRap);

        edGiaVe = findViewById(R.id.edGiaVe);
        edSessionName = findViewById(R.id.edSessionName);

        // Nút quay lại
        back = findViewById(R.id.imBack);
        back.setOnClickListener(v -> finish());

        // Khởi tạo Retrofit ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Xử lý chọn rạp để load phòng
        spRap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCinemaName = (String) parent.getItemAtPosition(position);
                String selectedCinemaId = cinemaMap.get(selectedCinemaName);
                if (selectedCinemaId != null) {
                    loadCinemaAndRoomDetails(selectedCinemaId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Xử lý chọn ngày
        btChonNgay.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý chọn giờ bắt đầu
        btChonGio.setOnClickListener(v -> showTimePickerDialog(btChonGio));

        // Xử lý chọn giờ kết thúc
        btChonGioHet.setOnClickListener(v -> showTimePickerDialog(btChonGioHet));

        // Nút thêm
        btThemAdmin.setOnClickListener(v -> addSessionDetails());

        // Nút sửa
        btSuaAdmin.setOnClickListener(v -> updateSessionDetails());

        // Nút xóa
        btXoaAdmin.setOnClickListener(v -> deleteSessionDetails());

        // Kiểm tra nếu đang chỉnh sửa (có SessionId)
        sessionId = getIntent().getStringExtra("SessionId");
        if (sessionId != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isDataLoaded) {
                    loadSessionDetails(sessionId);
                    btThemAdmin.setVisibility(View.GONE);
                    btSuaAdmin.setVisibility(View.VISIBLE);
                    btXoaAdmin.setVisibility(View.VISIBLE);
                }
            }, 500); // Delay tạm thời, thay bằng logic kiểm tra chính xác
        } else {
            btThemAdmin.setVisibility(View.VISIBLE);
            btSuaAdmin.setVisibility(View.GONE);
            btXoaAdmin.setVisibility(View.GONE);
        }
        // Nạp dữ liệu spinner trước
        loadMovies();
        loadCinemas();
        loadRooms();
    }
    private void loadRoomsByCinema(String cinemaId) {
        apiService.getRoomsByCinema(cinemaId).enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> roomNames = new ArrayList<>();
                    roomMap.clear();
                    for (Room room : response.body()) {
                        roomNames.add(room.getRoomName());
                        roomMap.put(room.getRoomName(), room.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(QuanLySuatDetailActivity.this, android.R.layout.simple_spinner_item, roomNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPhong.setAdapter(adapter);
                }
                checkDataLoaded();
            }
            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải phòng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadSessionDetails(String sessionId) {
        apiService.getShowtimeById(sessionId).enqueue(new Callback<Showtime>() {
            @Override
            public void onResponse(Call<Showtime> call, Response<Showtime> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentSession = response.body();
                    edSessionName.setText(currentSession.getSessionName());
                    edGiaVe.setText(String.valueOf(currentSession.getPrice()));
                    btChonNgay.setText(currentSession.getFormattedDate());
                    btChonGio.setText(currentSession.getFormattedStartTime());
                    btChonGioHet.setText(currentSession.getFormattedEndTime());

                    // Load thông tin phim dựa trên movieId
                    loadMovieDetails(currentSession.getMovieId());
                    // Load thông tin rạp và phòng dựa trên roomId
                    loadCinemaAndRoomDetails(currentSession.getRoomId());
                } else {
                    Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải chi tiết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Showtime> call, Throwable t) {
                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMovieDetails(String movieId) {
        apiService.getMovieById(movieId).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    String movieTitle = movie.getTitle();
                    // Cập nhật map nếu chưa có
                    if (!movieMap.containsKey(movieTitle)) {
                        movieMap.put(movieTitle, movieId);
                    }
                    // Set selection trong spinner đã nạp trước đó
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spPhim.getAdapter();
                    if (adapter != null) {
                        int position = adapter.getPosition(movieTitle);
                        if (position >= 0) {
                            spPhim.setSelection(position);
                        } else {
                            Log.w("SpinnerDebug", "Movie title not found: " + movieTitle);
                        }
                    }
                }
                checkDataLoaded();
            }
            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải phim: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadCinemaAndRoomDetails(String roomId) {
        apiService.getRoom(roomId).enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Room room = response.body();
                    String roomName = room.getRoomName();
                    String cinemaId = room.getCinemaId();
                    // Cập nhật map nếu chưa có
                    if (!roomMap.containsKey(roomName)) {
                        roomMap.put(roomName, roomId);
                    }
                    // Set selection trong spinner phòng
                    ArrayAdapter<String> roomAdapter = (ArrayAdapter<String>) spPhong.getAdapter();
                    if (roomAdapter != null) {
                        int position = roomAdapter.getPosition(roomName);
                        if (position >= 0) {
                            spPhong.setSelection(position);
                        } else {
                            Log.w("SpinnerDebug", "Room name not found: " + roomName);
                        }
                    }

                    // Load thông tin rạp dựa trên cinemaId
                    apiService.getCinemaId(cinemaId).enqueue(new Callback<Cinema>() {
                        @Override
                        public void onResponse(Call<Cinema> call, Response<Cinema> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Cinema cinema = response.body();
                                String cinemaName = cinema.getName();
                                // Cập nhật map nếu chưa có
                                if (!cinemaMap.containsKey(cinemaName)) {
                                    cinemaMap.put(cinemaName, cinemaId);
                                }
                                // Set selection trong spinner rạp
                                ArrayAdapter<String> cinemaAdapter = (ArrayAdapter<String>) spRap.getAdapter();
                                if (cinemaAdapter != null) {
                                    int position = cinemaAdapter.getPosition(cinemaName);
                                    if (position >= 0) {
                                        spRap.setSelection(position);
                                    } else {
                                        Log.w("SpinnerDebug", "Cinema name not found: " + cinemaName);
                                    }
                                }
                            }
                            checkDataLoaded();
                        }
                        @Override
                        public void onFailure(Call<Cinema> call, Throwable t) {
                            Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải rạp: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                checkDataLoaded();
            }
            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                Log.e("QuanLySuatDetail", "Lỗi load rooms: " + t.getMessage());
            }
        });
    }
    private void loadMovies() {
        apiService.getMoives().enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> movieTitles = new ArrayList<>();
                    for (Movie movie : response.body()) {
                        movieTitles.add(movie.getTitle());
                        movieMap.put(movie.getTitle(), movie.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(QuanLySuatDetailActivity.this, android.R.layout.simple_spinner_item, movieTitles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPhim.setAdapter(adapter);
                }
                checkDataLoaded();
            }
            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải phim: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCinemas() {
        apiService.getCinemas().enqueue(new Callback<List<Cinema>>() {
            @Override
            public void onResponse(Call<List<Cinema>> call, Response<List<Cinema>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> cinemaNames = new ArrayList<>();
                    for (Cinema cinema : response.body()) {
                        cinemaNames.add(cinema.getName());
                        cinemaMap.put(cinema.getName(), cinema.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(QuanLySuatDetailActivity.this, android.R.layout.simple_spinner_item, cinemaNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spRap.setAdapter(adapter);
                }
                checkDataLoaded();
            }
            @Override
            public void onFailure(Call<List<Cinema>> call, Throwable t) {
                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải rạp: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRooms() {
        apiService.getAllRooms().enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> roomNames = new ArrayList<>();
                    for (Room room : response.body()) {
                        roomNames.add(room.getRoomName());
                        roomMap.put(room.getRoomName(), room.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(QuanLySuatDetailActivity.this, android.R.layout.simple_spinner_item, roomNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPhong.setAdapter(adapter);
                }
                checkDataLoaded();
            }
            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                Log.e("QuanLySuatDetail", "Lỗi load rooms: " + t.getMessage());
            }
        });
    }

    private void checkDataLoaded() {
        if (spPhim.getAdapter() != null && spRap.getAdapter() != null && spPhong.getAdapter() != null) {
            isDataLoaded = true;
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedStartDay = String.format("%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    btChonNgay.setText(selectedStartDay);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(Button button) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute1);
                    if (button == btChonGio) {
                        selectedStartHour = time;
                        btChonGio.setText(selectedStartHour);
                    } else if (button == btChonGioHet) {
                        selectedEndHour = time;
                        btChonGioHet.setText(selectedEndHour);
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }
    private void setSpinnerSelection(Spinner spinner, String displayName, Map<String, String> map) {
        if (displayName != null && map.containsKey(displayName)) {
            int position = ((ArrayAdapter<String>) spinner.getAdapter()).getPosition(displayName);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }
    private void addSessionDetails() {
        String sessionName = edSessionName.getText().toString().trim();
        String movieId = movieMap.get(spPhim.getSelectedItem().toString());
        String roomId  = roomMap.get(spPhong.getSelectedItem().toString());
        String priceStr = edGiaVe.getText().toString().trim();

        // Lấy đúng định dạng từ UI
        String dateStr  = btChonNgay.getText().toString().trim();   // "yyyy-MM-dd"
        String startStr = btChonGio.getText().toString().trim();    // "HH:mm"
        String endStr   = btChonGioHet.getText().toString().trim(); // "HH:mm"

        // Validate cơ bản
        if (sessionName.isEmpty() || movieId == null || roomId == null || priceStr.isEmpty()
                || dateStr.isEmpty() || startStr.isEmpty() || endStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Ngày phải là yyyy-MM-dd!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!startStr.matches("\\d{2}:\\d{2}") || !endStr.matches("\\d{2}:\\d{2}")) {
            Toast.makeText(this, "Thời gian phải là HH:mm (ví dụ: 10:00)!", Toast.LENGTH_SHORT).show();
            return;
        }

        Long price = Long.parseLong(priceStr);

        CreateShowtimeRequest request= new CreateShowtimeRequest();
        request.setMovieId(movieId);
        request.setRoomId(roomId);
        request.setSessionName(sessionName);
        request.setDate(dateStr);         // "2025-09-20"
        request.setStartTime(startStr);    // "15:00"
        request.setEndTime(endStr);        // "17:05"
        request.setPrice(price);

        // Nếu có ô nhập tổng ghế & ghế trống thì set luôn (nếu DTO có field)
        // request.setTotalSeats(Integer.parseInt(edTongGhe.getText().toString().trim()));
        // request.setAvailableSeats(Integer.parseInt(edGheTrong.getText().toString().trim()));

        Log.d("QuanLySuatDetail", "Request JSON: " + new com.google.gson.Gson().toJson(request));

        apiService.createShowtime(request).enqueue(new Callback<ShowtimeResponse>() {
            @Override
            public void onResponse(Call<ShowtimeResponse> call, Response<ShowtimeResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLySuatDetailActivity.this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : response.message();
                        Log.e("QuanLySuatDetail", "Error: " + response.code() + " - " + errorBody);
                        Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ShowtimeResponse> call, Throwable t) {
                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSessionDetails() {
        if (sessionId == null) {
            Toast.makeText(this, "Không tìm thấy suất chiếu!", Toast.LENGTH_SHORT).show();
            return;
        }

        String sessionName = edSessionName.getText().toString().trim();
        String movieId     = movieMap.get(spPhim.getSelectedItem().toString());
        String roomId      = roomMap.get(spPhong.getSelectedItem().toString());
        String priceStr    = edGiaVe.getText().toString().trim();
        String dateStr     = btChonNgay.getText().toString().trim();     // "yyyy-MM-dd"
        String startStr    = btChonGio.getText().toString().trim();      // "HH:mm"
        String endStr      = btChonGioHet.getText().toString().trim();   // "HH:mm"

        if (sessionName.isEmpty() || movieId == null || roomId == null ||
                priceStr.isEmpty() || dateStr.isEmpty() || startStr.isEmpty() || endStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Ngày phải là yyyy-MM-dd!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!startStr.matches("\\d{2}:\\d{2}") || !endStr.matches("\\d{2}:\\d{2}")) {
            Toast.makeText(this, "Thời gian phải là HH:mm (vd: 10:00)!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Long price;
        try { price = Long.parseLong(priceStr); }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Giá vé phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // (Khuyên dùng) Validate logic thời gian để tránh 400 "startAt must be in the future"
        ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
        DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
        try {
            LocalDate d = LocalDate.parse(dateStr);
            LocalTime st = LocalTime.parse(startStr, HHMM);
            LocalTime en = LocalTime.parse(endStr, HHMM);
            if (!en.isAfter(st)) {
                Toast.makeText(this, "Giờ kết thúc phải > giờ bắt đầu!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ZonedDateTime.of(d, st, VN).isAfter(ZonedDateTime.now(VN))) {
                Toast.makeText(this, "Giờ bắt đầu phải lớn hơn thời điểm hiện tại!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Định dạng ngày/giờ không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer totalSeats     = 40; // TODO: lấy từ UI nếu có
        Integer availableSeats = 40; // TODO: lấy từ UI nếu có

        // ⬇️ Quan trọng: truyền startAt/endAt
        UpdateShowtimeRequest request = new UpdateShowtimeRequest(
                movieId,
                roomId,
                sessionName,
                dateStr,
                startStr,    // startAt
                endStr,      // endAt
                price,
                totalSeats,
                availableSeats
        );

        apiService.updateShowtime(sessionId, request).enqueue(new Callback<Showtime>() {
            @Override
            public void onResponse(Call<Showtime> call, Response<Showtime> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLySuatDetailActivity.this, "Sửa thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String err = response.message();
                    try { if (response.errorBody() != null) err = response.errorBody().string(); } catch (Exception ignore) {}
                    Log.e("QuanLySuatDetail", "Error: " + response.code() + " - " + err);
                    Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi sửa: " + err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Showtime> call, Throwable t) {
                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSessionDetails() {
        if (sessionId == null) {
            Toast.makeText(this, "Không tìm thấy suất chiếu để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteShowtime(sessionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLySuatDetailActivity.this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi xóa: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}