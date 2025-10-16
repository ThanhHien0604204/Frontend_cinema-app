package com.ntth.movie_ticket_booking_app.Activeties;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Genre;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.MovieRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyPhimDetailActivity extends AppCompatActivity {

    private EditText edPhim, edThoiLuong, edKhoiChieu, edAnhphim, edTrailer, edMoTa;
    private ImageView imageView;
    private AutoCompleteTextView autoCompleteGenres;
    private Button btThemAdmin, btSuaAdmin, btXoaAdmin;
    private ApiService apiService;
    private String movieId;
    private List<Genre> genreList = new ArrayList<>();
    private List<String> selectedGenreIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlyphim_detail);

        // Liên kết các View trong layout
        edPhim = findViewById(R.id.edPhim);
        edThoiLuong = findViewById(R.id.edThoiLuong);
        edKhoiChieu = findViewById(R.id.edKhoiChieu);
        autoCompleteGenres = findViewById(R.id.autoCompleteGenres);
        edAnhphim = findViewById(R.id.edAnhphim);
        edTrailer = findViewById(R.id.edTrailer);
        edMoTa = findViewById(R.id.edMoTa);
        imageView = findViewById(R.id.imageView);
        btThemAdmin = findViewById(R.id.btThemAdmin);
        btSuaAdmin = findViewById(R.id.btSuaAdmin);
        btXoaAdmin = findViewById(R.id.btXoaAdmin);

        // Khởi tạo Retrofit ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Xử lý nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        // Xử lý chọn ngày
        edKhoiChieu.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý chọn thể loại
        autoCompleteGenres.setOnClickListener(v -> showGenreSelectionDialog());

        // Load danh sách thể loại
        loadGenres();

        // Lấy movieId nếu là sửa
        movieId = getIntent().getStringExtra("movieId");
        if (movieId != null) {
            btThemAdmin.setVisibility(View.GONE);
            btSuaAdmin.setVisibility(View.VISIBLE);
            btXoaAdmin.setVisibility(View.VISIBLE);
        } else {
            btSuaAdmin.setVisibility(View.GONE);
            btXoaAdmin.setVisibility(View.GONE);
        }

        btThemAdmin.setOnClickListener(v -> addMovie());
        btSuaAdmin.setOnClickListener(v -> updateMovie());
        btXoaAdmin.setOnClickListener(v -> deleteMovie());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = String.format(Locale.US, "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
            edKhoiChieu.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showGenreSelectionDialog() {
        // Tạo ListView trong dialog
        ListView listView = new ListView(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter<Genre> adapter = new ArrayAdapter<Genre>(this, android.R.layout.simple_list_item_multiple_choice, genreList) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                android.widget.TextView textView = (android.widget.TextView) view;
                textView.setText(genreList.get(position).getName());
                return view;
            }
        };
        listView.setAdapter(adapter);

        // Đánh dấu các thể loại đã chọn
        for (int i = 0; i < genreList.size(); i++) {
            Log.d("GenreDebug", "Position " + i + ": ID=" + genreList.get(i).getId() + ", Name=" + genreList.get(i).getName());
            if (selectedGenreIds.contains(genreList.get(i).getId())) {
                Log.d("GenreDebug", "Selected: " + genreList.get(i).getName());
                listView.setItemChecked(i, true);
            }
        }

        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại");
        builder.setView(listView);
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedGenreIds.clear();
            for (int i = 0; i < listView.getCount(); i++) {
                if (listView.isItemChecked(i)) {
                    selectedGenreIds.add(genreList.get(i).getId());
                }
            }
            updateSelectedGenresText();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateSelectedGenresText() {
        String selectedGenres = genreList.stream()
                .filter(genre -> selectedGenreIds.contains(genre.getId()))
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
        Log.d("SelectedGenres", "IDs: " + selectedGenreIds + ", Names: " + selectedGenres);
        autoCompleteGenres.setText(selectedGenres.isEmpty() ? "" : selectedGenres);
    }
    private void loadGenres() {
        apiService.getGenres().enqueue(new Callback<List<Genre>>() {
            @Override
            public void onResponse(Call<List<Genre>> call, Response<List<Genre>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    genreList.clear();
                    genreList.addAll(response.body());
                    // Cập nhật lại khi sửa phim
                    if (movieId != null) {
                        loadMovieDetails(movieId);
                    }
                } else {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi tải thể loại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Genre>> call, Throwable t) {
                Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi kết nối thể loại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMovieDetails(String movieId) {
        apiService.getMovieById(movieId).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    edPhim.setText(movie.getTitle());
                    edThoiLuong.setText(String.valueOf(movie.getDurationMinutes()));
                    LocalDate date = movie.getMovieDateStart();
                    if (date != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        edKhoiChieu.setText(date.format(formatter));
                    }
                    selectedGenreIds.clear();
                    selectedGenreIds.addAll(movie.getGenreIds());
                    updateSelectedGenresText();
                    edAnhphim.setText(movie.getImageUrl());
                    edTrailer.setText(movie.getTrailerUrl());
                    edMoTa.setText(movie.getSummary());
                } else {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi tải chi tiết phim!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addMovie() {
        String title = edPhim.getText().toString().trim();
        String durationStr = edThoiLuong.getText().toString().trim();
        String releaseDateStr = edKhoiChieu.getText().toString().trim();
        String imageUrl = edAnhphim.getText().toString().trim();
        String trailerUrl = edTrailer.getText().toString().trim();
        String summary = edMoTa.getText().toString().trim();

        if (title.isEmpty() || durationStr.isEmpty() || releaseDateStr.isEmpty() ||
                imageUrl.isEmpty() || trailerUrl.isEmpty() || summary.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGenreIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một thể loại!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra và ánh xạ ID sang Genre hợp lệ
        List<String> selectedGenreNames = genreList.stream()
                .filter(genre -> selectedGenreIds.contains(genre.getId()))
                .map(Genre::getName)
                .collect(Collectors.toList());

        if (selectedGenreNames.isEmpty()) {
            Toast.makeText(this, "Một số thể loại không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate releaseDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            releaseDate = LocalDate.parse(releaseDateStr, formatter);
        } catch (DateTimeParseException e) {
            Toast.makeText(this, "Định dạng ngày không hợp lệ! Vui lòng dùng dd-MM-yyyy (e.g., 23-09-2025).", Toast.LENGTH_SHORT).show();
            return;
        }

        MovieRequest movieRequest = new MovieRequest();
        movieRequest.setTitle(title);
        movieRequest.setDurationMinutes(duration);
        movieRequest.setMovieDateStart(releaseDate);
        movieRequest.setGenre(selectedGenreNames);  // Sửa: gửi tên thay vì ID
        movieRequest.setImageUrl(imageUrl);
        movieRequest.setTrailerUrl(trailerUrl);
        movieRequest.setSummary(summary);
        movieRequest.setRating(null);
        movieRequest.setAuthor(null);
        movieRequest.setActors(null);
        movieRequest.setViews(null);

        Log.d("MovieRequest", "Genre Names sent: " + selectedGenreNames);
        apiService.addMovie(movieRequest).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    loadGenres(); // Cập nhật lại danh sách thể loại
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("QuanLyPhimDetail", "Thêm thất bại: " + response.code() + ", " + errorBody);
                        Toast.makeText(QuanLyPhimDetailActivity.this, "Thêm thất bại: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(QuanLyPhimDetailActivity.this, "Thêm thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e("QuanLyPhimDetail", "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMovie() {
        String updatedTitle = edPhim.getText().toString().trim();
        String updatedDurationStr = edThoiLuong.getText().toString().trim();
        String updatedReleaseDateStr = edKhoiChieu.getText().toString().trim();
        String updatedImageUrl = edAnhphim.getText().toString().trim();
        String updatedTrailerUrl = edTrailer.getText().toString().trim();
        String updatedSummary = edMoTa.getText().toString().trim();

        if (updatedTitle.isEmpty() || updatedDurationStr.isEmpty() || updatedReleaseDateStr.isEmpty() ||
                updatedImageUrl.isEmpty() || updatedTrailerUrl.isEmpty() || updatedSummary.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGenreIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một thể loại!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra và ánh xạ ID sang Genre hợp lệ
        List<String> selectedGenreNames = genreList.stream()
                .filter(genre -> selectedGenreIds.contains(genre.getId()))
                .map(Genre::getName)
                .collect(Collectors.toList());

        if (selectedGenreNames.isEmpty()) {
            Toast.makeText(this, "Một số thể loại không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer updatedDuration;
        try {
            updatedDuration = Integer.parseInt(updatedDurationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate updatedReleaseDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            updatedReleaseDate = LocalDate.parse(updatedReleaseDateStr, formatter);
        } catch (DateTimeParseException e) {
            Toast.makeText(this, "Định dạng ngày không hợp lệ! Vui lòng dùng dd-MM-yyyy (e.g., 23-09-2025).", Toast.LENGTH_SHORT).show();
            return;
        }

        MovieRequest updatedMovieRequest = new MovieRequest();
        updatedMovieRequest.setTitle(updatedTitle);
        updatedMovieRequest.setDurationMinutes(updatedDuration);
        updatedMovieRequest.setMovieDateStart(updatedReleaseDate);
        updatedMovieRequest.setGenre(selectedGenreNames);  // Sửa: gửi tên thay vì ID
        updatedMovieRequest.setImageUrl(updatedImageUrl);
        updatedMovieRequest.setTrailerUrl(updatedTrailerUrl);
        updatedMovieRequest.setSummary(updatedSummary);
        updatedMovieRequest.setRating(null);
        updatedMovieRequest.setAuthor(null);
        updatedMovieRequest.setActors(null);
        updatedMovieRequest.setViews(null);
        Log.d("MovieRequest", "Genre Names sent: " + selectedGenreNames);
        Log.d("MovieRequest", "Title: " + updatedMovieRequest.getTitle() + ", Genre Names: " + selectedGenreNames);
        apiService.updateMovie(movieId, updatedMovieRequest).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("QuanLyPhimDetail", "Cập nhật thất bại: " + response.code() + ", " + errorBody);
                        Toast.makeText(QuanLyPhimDetailActivity.this, "Cập nhật thất bại: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(QuanLyPhimDetailActivity.this, "Cập nhật thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e("QuanLyPhimDetail", "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMovie() {
        if (movieId == null) {
            Toast.makeText(this, "Không có phim để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteMovie(movieId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("QuanLyPhimDetail", "Xóa thất bại: " + response.code() + ", " + errorBody);
                        Toast.makeText(QuanLyPhimDetailActivity.this, "Xóa thất bại: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(QuanLyPhimDetailActivity.this, "Xóa thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("QuanLyPhimDetail", "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}