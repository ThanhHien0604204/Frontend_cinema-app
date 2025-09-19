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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyPhimActivity extends AppCompatActivity {

    private ListView lvMovieList;
    private ArrayAdapter<String> movieAdapter;
    private List<String> movieTitles;
    private List<String> movieIds;
    private EditText etSearch;
    private Handler handler; // Để tạo độ trễ khi tìm kiếm
    private Runnable searchRunnable;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlyphim);

        // Nút thêm phim
        ImageView imAdd = findViewById(R.id.imAdd);
        imAdd.setOnClickListener(v -> {
            Intent intent = new Intent(QuanLyPhimActivity.this, QuanLyPhimDetailActivity.class);
            startActivity(intent);
        });

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        // Khởi tạo ListView và EditText
        lvMovieList = findViewById(R.id.lvMovieList);
        etSearch = findViewById(R.id.etSearch);

        // Khởi tạo danh sách phim và adapter
        movieTitles = new ArrayList<>();
        movieIds = new ArrayList<>();
        movieAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, movieTitles);
        lvMovieList.setAdapter(movieAdapter);

        // Khởi tạo Retrofit ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Lấy dữ liệu từ API
        fetchMovieData();

        // Khởi tạo Handler để tạo độ trễ
        handler = new Handler(Looper.getMainLooper());

        // Tìm kiếm với độ trễ 500ms
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
                searchRunnable = () -> searchMovies(s.toString());
                handler.postDelayed(searchRunnable, 500); // Độ trễ 500ms
            }
        });

        // Xử lý click vào item
        lvMovieList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMovieId = movieIds.get(position);
            Intent intent = new Intent(QuanLyPhimActivity.this, QuanLyPhimDetailActivity.class);
            intent.putExtra("movieId", selectedMovieId);
            startActivity(intent);
        });
    }

    private void fetchMovieData() {
        apiService.getMoives().enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieTitles.clear();
                    movieIds.clear();
                    for (Movie movie : response.body()) {
                        movieTitles.add(movie.getTitle());
                        movieIds.add(movie.getId());
                    }
                    movieAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(QuanLyPhimActivity.this, "Lỗi khi tải dữ liệu phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                Toast.makeText(QuanLyPhimActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMovies(String query) {
        if (query.isEmpty()) {
            fetchMovieData(); // Hiển thị lại toàn bộ danh sách nếu từ khoá rỗng
            return;
        }

        // Gọi API tìm kiếm phim
        apiService.searchMovies(query, 0, 12).enqueue(new Callback<PageResponse<Movie>>() {
            @Override
            public void onResponse(Call<PageResponse<Movie>> call, Response<PageResponse<Movie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieTitles.clear();
                    movieIds.clear();
                    List<Movie> movies = response.body().getContent();
                    for (Movie movie : movies) {
                        movieTitles.add(movie.getTitle());
                        movieIds.add(movie.getId());
                    }
                    movieAdapter.notifyDataSetChanged();
                    if (movieTitles.isEmpty()) {
                        Toast.makeText(QuanLyPhimActivity.this, "Không tìm thấy phim nào!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QuanLyPhimActivity.this, "Lỗi khi tìm kiếm phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<Movie>> call, Throwable t) {
                Toast.makeText(QuanLyPhimActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}