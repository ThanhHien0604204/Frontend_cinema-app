//package com.ntth.movie_ticket_booking_app.Activeties;
//
//import android.app.DatePickerDialog;
//import android.app.TimePickerDialog;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Spinner;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.ntth.movie_ticket_booking_app.Class.Cinema;
//import com.ntth.movie_ticket_booking_app.Class.Movie;
//import com.ntth.movie_ticket_booking_app.Class.Room;
//import com.ntth.movie_ticket_booking_app.Class.Showtime;
//import com.ntth.movie_ticket_booking_app.R;
//import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
//import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
//import com.ntth.movie_ticket_booking_app.dto.CreateShowtimeRequest;
//import com.ntth.movie_ticket_booking_app.dto.ShowtimeResponse;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Locale;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class QuanLySuatDetailActivity extends AppCompatActivity {
//
//    private Button btChonNgay, btChonGio, btChonGioHet, btThemAdmin, btSuaAdmin, btXoaAdmin;
//    private Spinner spPhim, spPhong, spRap;
//    private EditText edGiaVe, edSessionName;
//    private ImageView back;
//
//    private ApiService apiService;
//
//    private String sessionId;
//
//    private Map<String, String> movieMap = new HashMap<>(); // title -> id
//    private Map<String, String> cinemaMap = new HashMap<>(); // name -> id
//    private Map<String, String> roomMap = new HashMap<>(); // name -> id
//
//    private String selectedStartDay = "";
//    private String selectedStartHour = "";
//    private String selectedEndHour = "";
//
//    private Showtime currentSession;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_quanlysuat_detail);
//
//        // Ánh xạ các view
//        btChonNgay = findViewById(R.id.btChonNgay);
//        btChonGio = findViewById(R.id.btChonGio);
//        btChonGioHet = findViewById(R.id.btChonGioHet);
//        btThemAdmin = findViewById(R.id.btThemAdmin);
//        btSuaAdmin = findViewById(R.id.btSuaAdmin);
//        btXoaAdmin = findViewById(R.id.btXoaAdmin);
//
//        spPhim = findViewById(R.id.spPhim);
//        spPhong = findViewById(R.id.spPhong);
//        spRap = findViewById(R.id.spRap);
//
//        edGiaVe = findViewById(R.id.edGiaVe);
//        edSessionName = findViewById(R.id.edSessionName);
//
//        // Nút quay lại
//        back=findViewById(R.id.imBack);
//        back.setOnClickListener(v -> finish());
//
//        // Khởi tạo Retrofit ApiService
//        apiService = RetrofitClient.getInstance().create(ApiService.class);
//
//        // Load data for spinners
//        loadMovies();
//        loadCinemas();
//
//        // Xử lý chọn rạp để load phòng
//        spRap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedCinemaName = (String) parent.getItemAtPosition(position);
//                String selectedCinemaId = cinemaMap.get(selectedCinemaName);
//                if (selectedCinemaId != null) {
//                    loadRoomsByCinema(selectedCinemaId);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });
//
//        // Xử lý chọn ngày
//        btChonNgay.setOnClickListener(v -> showDatePickerDialog());
//
//        // Xử lý chọn giờ bắt đầu
//        btChonGio.setOnClickListener(v -> showTimePickerDialog(true));
//
//        // Xử lý chọn giờ kết thúc
//        btChonGioHet.setOnClickListener(v -> showTimePickerDialog(false));
//
//        // Nút thêm
//        btThemAdmin.setOnClickListener(v -> addSessionDetails());
//
//        // Nút sửa
//        btSuaAdmin.setOnClickListener(v -> updateSessionDetails());
//
//        // Nút xóa
//        btXoaAdmin.setOnClickListener(v -> deleteSessionDetails());
//
//        // Kiểm tra nếu đang chỉnh sửa (có SessionId)
//        sessionId = getIntent().getStringExtra("SessionId");
//        if (sessionId != null) {
//            loadSessionDetails(sessionId);
//            btThemAdmin.setVisibility(View.GONE);
//            btSuaAdmin.setVisibility(View.VISIBLE);
//            btXoaAdmin.setVisibility(View.VISIBLE);
//        } else {
//            btThemAdmin.setVisibility(View.VISIBLE);
//            btSuaAdmin.setVisibility(View.GONE);
//            btXoaAdmin.setVisibility(View.GONE);
//        }
//    }
//
//    private void loadMovies() {
//        apiService.getMoives().enqueue(new Callback<List<Movie>>() {
//            @Override
//            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<String> movieTitles = new ArrayList<>();
//                    for (Movie movie : response.body()) {
//                        movieTitles.add(movie.getTitle());
//                        movieMap.put(movie.getTitle(), movie.getId());
//                    }
//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(QuanLySuatDetailActivity.this, android.R.layout.simple_spinner_item, movieTitles);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    spPhim.setAdapter(adapter);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Movie>> call, Throwable t) {
//                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải phim: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void loadCinemas() {
//        apiService.getCinemas().enqueue(new Callback<List<Cinema>>() {
//            @Override
//            public void onResponse(Call<List<Cinema>> call, Response<List<Cinema>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<String> cinemaNames = new ArrayList<>();
//                    for (Cinema cinema : response.body()) {
//                        cinemaNames.add(cinema.getName());
//                        cinemaMap.put(cinema.getName(), cinema.getId());
//                    }
//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(QuanLySuatDetailActivity.this, android.R.layout.simple_spinner_item, cinemaNames);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    spRap.setAdapter(adapter);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Cinema>> call, Throwable t) {
//                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải rạp: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void loadRoomsByCinema(String cinemaId) {
//        apiService.getRoomsByCinema(cinemaId).enqueue(new Callback<List<Room>>() {
//            @Override
//            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<String> roomNames = new ArrayList<>();
//                    roomMap.clear();
//                    for (Room room : response.body()) {
//                        roomNames.add(room.getRoomName());
//                        roomMap.put(room.getRoomName(), room.getId());
//                    }
//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(QuanLySuatDetailActivity.this, android.R.layout.simple_spinner_item, roomNames);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    spPhong.setAdapter(adapter);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Room>> call, Throwable t) {
//                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải phòng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void showDatePickerDialog() {
//        Calendar calendar = Calendar.getInstance();
//        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//            selectedStartDay = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
//            btChonNgay.setText(selectedStartDay);
//        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//        datePickerDialog.show();
//    }
//
//    private void showTimePickerDialog(boolean isStart) {
//        Calendar calendar = Calendar.getInstance();
//        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
//            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
//            if (isStart) {
//                selectedStartHour = time;
//                btChonGio.setText(time);
//            } else {
//                selectedEndHour = time;
//                btChonGioHet.setText(time);
//            }
//        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
//        timePickerDialog.show();
//    }
//
//    private void addSessionDetails() {
//        String sessionName = edSessionName.getText().toString().trim();
//        String selectedMovieName = (String) spPhim.getSelectedItem();
//        String selectedRoomName = (String) spPhong.getSelectedItem();
//        String price = edGiaVe.getText().toString().trim();
//
//        String movieId = movieMap.get(selectedMovieName);
//        String roomId = roomMap.get(selectedRoomName);
//
//        if (sessionName.isEmpty() || movieId == null || roomId == null || price.isEmpty() || selectedStartDay.isEmpty() || selectedStartHour.isEmpty() || selectedEndHour.isEmpty()) {
//            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        CreateShowtimeRequest request = new CreateShowtimeRequest();
//        request.movieId = movieId;
//        request.roomId = roomId;
//        request.date = LocalDate.parse(selectedStartDay, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        request.startAt = selectedStartHour;
//        request.endAt = selectedEndHour;
//        request.price = Long.parseLong(price);
//
//        apiService.createShowtime(request).enqueue(new Callback<ShowtimeResponse>() {
//            @Override
//            public void onResponse(Call<ShowtimeResponse> call, Response<ShowtimeResponse> response) {
//                if (response.isSuccessful()) {
//                    Toast.makeText(QuanLySuatDetailActivity.this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi thêm: " + response.message(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ShowtimeResponse> call, Throwable t) {
//                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void loadSessionDetails(String sessionId) {
//        apiService.getShowtimeById(sessionId).enqueue(new Callback<Showtime>() {
//            @Override
//            public void onResponse(Call<Showtime> call, Response<Showtime> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    currentSession = response.body();
//                    edSessionName.setText(currentSession.getSessionName());
//                    edGiaVe.setText(String.valueOf(currentSession.getPrice()));
//                    btChonNgay.setText(currentSession.getStartAt().split("T")[0]); // Assume ISO format
//                    btChonGio.setText(currentSession.getStartAt().split("T")[1]);
//                    btChonGioHet.setText(currentSession.getEndAt().split("T")[1]);
//                } else {
//                    Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi tải chi tiết", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Showtime> call, Throwable t) {
//                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void updateSessionDetails() {
//        // Lấy dữ liệu từ các trường
//        String sessionName = edSessionName.getText().toString().trim();
//        String selectedMovieName = (String) spPhim.getSelectedItem();
//        String selectedRoomName = (String) spPhong.getSelectedItem();
//        int price = Integer.parseInt(edGiaVe.getText().toString().trim());// Đổi tên cho rõ ràng
//
//        String movieId = movieMap.get(selectedMovieName);
//        String roomId = roomMap.get(selectedRoomName);
//
//        // Kiểm tra các trường bắt buộc
//        if (sessionName.isEmpty() || movieId == null || roomId == null || price != 0 ||
//                selectedStartDay.isEmpty() || selectedStartHour.isEmpty() || selectedEndHour.isEmpty()) {
//            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Tạo đối tượng Showtime
//        Showtime showtime = new Showtime();
//        showtime.setId(sessionId);
//        showtime.setSessionName(sessionName);
//        showtime.setMovieId(movieId);
//        showtime.setRoomId(roomId);
//        showtime.setPrice(price);
//        // Đặt startAt, endAt (cần định dạng đầy đủ nếu backend yêu cầu)
//
//        apiService.updateShowtime(sessionId, showtime).enqueue(new Callback<Showtime>() {
//            @Override
//            public void onResponse(Call<Showtime> call, Response<Showtime> response) {
//                if (response.isSuccessful()) {
//                    Toast.makeText(QuanLySuatDetailActivity.this, "Sửa thành công!", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi sửa: " + response.message(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Showtime> call, Throwable t) {
//                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void deleteSessionDetails() {
//        if (sessionId == null) {
//            Toast.makeText(this, "Không tìm thấy suất chiếu để xóa!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        apiService.deleteShowtime(sessionId).enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    Toast.makeText(QuanLySuatDetailActivity.this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
//                    Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi xóa: " + response.message(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Toast.makeText(QuanLySuatDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}