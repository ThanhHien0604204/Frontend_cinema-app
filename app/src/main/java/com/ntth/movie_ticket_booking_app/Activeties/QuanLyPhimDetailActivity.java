// QuanLyPhimDetailActivity.java (updated to use MovieRequest)
package com.ntth.movie_ticket_booking_app.Activeties;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.MovieRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyPhimDetailActivity extends AppCompatActivity {

    private EditText edPhim, edThoiLuong, edKhoiChieu, edTheLoai, edAnhphim, edTrailer, edMoTa;
    private ImageView imageView;
    private Button btThemAdmin, btSuaAdmin, btXoaAdmin;
    private ApiService apiService;
    private String movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlyphim_detail);

        // Xử lý nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        // Liên kết các View trong layout
        edPhim = findViewById(R.id.edPhim);
        edThoiLuong = findViewById(R.id.edThoiLuong);
        edKhoiChieu = findViewById(R.id.edKhoiChieu);
        edTheLoai = findViewById(R.id.edGiaVe);  // Giả định đây là genre names, tách bằng dấu phẩy
        edAnhphim = findViewById(R.id.edAnhphim);
        edTrailer = findViewById(R.id.edTrailer);
        edMoTa = findViewById(R.id.edMoTa);
        imageView = findViewById(R.id.imageView);
        btThemAdmin = findViewById(R.id.btThemAdmin);
        btSuaAdmin = findViewById(R.id.btSuaAdmin);
        btXoaAdmin = findViewById(R.id.btXoaAdmin);

        // Khởi tạo Retrofit ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Lấy movieId từ Intent
        movieId = getIntent().getStringExtra("movieId");

        // Kiểm tra nếu movieId không null, truy xuất dữ liệu từ API
        if (movieId != null) {
            btThemAdmin.setVisibility(View.GONE);
            btSuaAdmin.setVisibility(View.VISIBLE);
            btXoaAdmin.setVisibility(View.VISIBLE);
            loadMovieDetails(movieId);
        } else {
            btSuaAdmin.setVisibility(View.GONE);
            btXoaAdmin.setVisibility(View.GONE);
        }

        btThemAdmin.setOnClickListener(v -> addMovie());
        btXoaAdmin.setOnClickListener(v -> deleteMovie());
        btSuaAdmin.setOnClickListener(v -> updateMovie());
    }

    private void loadMovieDetails(String movieId) {
        apiService.getMovieById(movieId).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    edPhim.setText(movie.getTitle());
                    edThoiLuong.setText(String.valueOf(movie.getDurationMinutes()));
                    edKhoiChieu.setText(movie.getMovieDateStart().toString());  // Chuyển LocalDate sang String
                    edTheLoai.setText(String.join(",", movie.getGenreIds()));  // Join genre IDs thành string
                    edAnhphim.setText(movie.getImageUrl());
                    edTrailer.setText(movie.getTrailerUrl());
                    edMoTa.setText(movie.getSummary());
                    // Nếu cần load image vào imageView, dùng Glide hoặc Picasso
                } else {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi tải dữ liệu phim", Toast.LENGTH_SHORT).show();
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
        String movieDateStartStr = edKhoiChieu.getText().toString().trim();
        String genreStr = edTheLoai.getText().toString().trim();
        String imageUrl = edAnhphim.getText().toString().trim();
        String trailerUrl = edTrailer.getText().toString().trim();
        String summary = edMoTa.getText().toString().trim();

        if (title.isEmpty() || durationStr.isEmpty() || movieDateStartStr.isEmpty() || genreStr.isEmpty() ||
                imageUrl.isEmpty() || trailerUrl.isEmpty() || summary.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer durationMinutes;
        try {
            durationMinutes = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> genreNames = Arrays.asList(genreStr.split("\\s*,\\s*"));  // Tách genre names bằng dấu phẩy

        MovieRequest newMovieRequest = new MovieRequest();
        newMovieRequest.setTitle(title);
        newMovieRequest.setDurationMinutes(durationMinutes);
        newMovieRequest.setMovieDateStart(LocalDate.parse(movieDateStartStr));  // Chuyển String sang LocalDate
        newMovieRequest.setGenre(genreNames);
        newMovieRequest.setImageUrl(imageUrl);
        newMovieRequest.setTrailerUrl(trailerUrl);
        newMovieRequest.setSummary(summary);

        apiService.addMovie(newMovieRequest).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Thêm phim thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMovie() {
        if (movieId == null) {
            Toast.makeText(this, "Không tìm thấy phim để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteMovie(movieId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Xóa phim thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Xóa phim thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMovie() {
        if (movieId == null) {
            Toast.makeText(this, "Không tìm thấy phim để sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        String updatedTitle = edPhim.getText().toString().trim();
        String updatedDurationStr = edThoiLuong.getText().toString().trim();
        String updatedMovieDateStartStr = edKhoiChieu.getText().toString().trim();
        String updatedGenreStr = edTheLoai.getText().toString().trim();
        String updatedImageUrl = edAnhphim.getText().toString().trim();
        String updatedTrailerUrl = edTrailer.getText().toString().trim();
        String updatedSummary = edMoTa.getText().toString().trim();

        if (updatedTitle.isEmpty() || updatedDurationStr.isEmpty() || updatedMovieDateStartStr.isEmpty() || updatedGenreStr.isEmpty() ||
                updatedImageUrl.isEmpty() || updatedTrailerUrl.isEmpty() || updatedSummary.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer updatedDuration;
        try {
            updatedDuration = Integer.parseInt(updatedDurationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời lượng phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> updatedGenreNames = Arrays.asList(updatedGenreStr.split("\\s*,\\s*"));

        MovieRequest updatedMovieRequest = new MovieRequest();
        updatedMovieRequest.setTitle(updatedTitle);
        updatedMovieRequest.setDurationMinutes(updatedDuration);
        updatedMovieRequest.setMovieDateStart(LocalDate.parse(updatedMovieDateStartStr));
        updatedMovieRequest.setGenre(updatedGenreNames);
        updatedMovieRequest.setImageUrl(updatedImageUrl);
        updatedMovieRequest.setTrailerUrl(updatedTrailerUrl);
        updatedMovieRequest.setSummary(updatedSummary);

        apiService.updateMovie(movieId, updatedMovieRequest).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(QuanLyPhimDetailActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(QuanLyPhimDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}