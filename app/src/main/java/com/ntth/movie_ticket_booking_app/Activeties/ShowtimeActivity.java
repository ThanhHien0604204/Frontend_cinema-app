package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.DateAdapter;
import com.ntth.movie_ticket_booking_app.Adapters.MovieSessionAdapter; // Đổi import sang MovieSessionAdapter
import com.ntth.movie_ticket_booking_app.Adapters.ShowtimeAdapter;
import com.ntth.movie_ticket_booking_app.Class.DateItem;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.ShowtimeResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowtimeActivity extends AppCompatActivity {
    private RecyclerView showtimeRecycler, dateRecycler;
    private ImageView back;
    private ShowtimeAdapter showtimeAdapter;
    private DateAdapter dateAdapter;
    private ApiService api;
    private List<ShowtimeResponse> showtimes = new ArrayList<>();
    private List<DateItem> dateItems = new ArrayList<>();
    private String selectedDate;
    private Map<String, String> movieNameCache = new HashMap<>();
    private String cinemaId;
    private String movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime);

        api = RetrofitClient.api();

        showtimeRecycler = findViewById(R.id.showtime_list);
        showtimeRecycler.setLayoutManager(new LinearLayoutManager(this));
        dateRecycler = findViewById(R.id.date_recycler);
        dateRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            dateItems.add(new DateItem(date, i == 0));
        }
        selectedDate = dateItems.get(0).getFullDate();

        dateAdapter = new DateAdapter(dateItems, dateItem -> {
            selectedDate = dateItem.getFullDate();
            loadShowtimesBasedOnParams();
        });
        dateRecycler.setAdapter(dateAdapter);

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

        cinemaId = getIntent().getStringExtra("cinemaId");
        movieId = getIntent().getStringExtra("movieId");
        String dateFromIntent = getIntent().getStringExtra("date");
        Log.d("ShowtimeActivity", "Nhận cinemaId: " + cinemaId + ", movieId: " + movieId);

        if (cinemaId == null || cinemaId.isEmpty()) {
            Toast.makeText(this, "Thiếu cinemaId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (dateFromIntent != null && !dateFromIntent.isEmpty()) {
            selectedDate = dateFromIntent;
            for (int i = 0; i < dateItems.size(); i++) {
                if (dateItems.get(i).getFullDate().equals(selectedDate)) {
                    dateItems.get(i).setSelected(true);
                    dateAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }

        loadShowtimesBasedOnParams();
    }

    private void loadShowtimesBasedOnParams() {
        if (cinemaId == null || cinemaId.isEmpty()) {
            Toast.makeText(this, "Thiếu cinemaId", Toast.LENGTH_SHORT).show();
            return;
        }
        if (movieId != null && !movieId.isEmpty()) {
            loadShowtimesCinemaandMovie(cinemaId, movieId, selectedDate);
        } else {
            loadShowtimes(cinemaId, selectedDate);
        }
    }

    private void loadShowtimes(String cinemaId, String date) {
        String d = (date == null) ? null : date.trim();

        Call<List<ShowtimeResponse>> call = api.getShowtimesByCinema(cinemaId, d);
        call.enqueue(new Callback<List<ShowtimeResponse>>() {
            @Override
            public void onResponse(Call<List<ShowtimeResponse>> call, Response<List<ShowtimeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ShowtimeResponse> list = response.body();
                    for (ShowtimeResponse s : list) {
                        Log.d("ShowtimeActivity", "Showtime ID: " + s.getId());
                    }
                    showtimes = list;
                    groupAndDisplayShowtimes(list);
                    if (list.isEmpty()) {
                        Toast.makeText(ShowtimeActivity.this, "Không có suất chiếu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShowtimeActivity.this, "Lỗi tải showtime: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShowtimeResponse>> call, Throwable t) {
                Toast.makeText(ShowtimeActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadShowtimesCinemaandMovie(String cinemaId, String movieId, String date) {
        Call<List<ShowtimeResponse>> call = api.getShowtimesByCinemaAndMovie(cinemaId, movieId, date == null ? null : date.trim());
        findViewById(R.id.constraintLayout2).setEnabled(false);
        call.enqueue(new Callback<List<ShowtimeResponse>>() {
            @Override public void onResponse(Call<List<ShowtimeResponse>> c, Response<List<ShowtimeResponse>> r) {
                findViewById(R.id.constraintLayout2).setEnabled(true);
                if (r.isSuccessful() && r.body() != null) {
                    List<ShowtimeResponse> list = r.body();
                    showtimes = list;
                    groupAndDisplayShowtimes(list);
                    if (r.body().isEmpty()) {
                        Toast.makeText(ShowtimeActivity.this, "Không có suất chiếu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShowtimeActivity.this, "Lỗi: " + r.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<List<ShowtimeResponse>> c, Throwable t) {
                findViewById(R.id.constraintLayout2).setEnabled(true);
                Toast.makeText(ShowtimeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void groupAndDisplayShowtimes(List<ShowtimeResponse> list) {
        Map<String, List<ShowtimeResponse>> showtimeMap = new HashMap<>();
        Set<String> uniqueMovieIds = new HashSet<>();

        for (ShowtimeResponse s : list) {
            String mId = s.getMovieId();
            if (mId != null) {
                showtimeMap.computeIfAbsent(mId, k -> new ArrayList<>()).add(s);
                uniqueMovieIds.add(mId);
            }
        }

        for (String mId : uniqueMovieIds) {
            if (!movieNameCache.containsKey(mId)) {
                Call<Movie> call = api.getMovieById(mId);
                call.enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(Call<Movie> call, Response<Movie> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            movieNameCache.put(mId, response.body().getTitle());
                        } else {
                            movieNameCache.put(mId, "Không xác định");
                        }
                        setMovieAdapter(showtimeMap);
                    }

                    @Override
                    public void onFailure(Call<Movie> call, Throwable t) {
                        movieNameCache.put(mId, "Không xác định");
                        setMovieAdapter(showtimeMap);
                    }
                });
            }
        }

        if (movieNameCache.size() >= uniqueMovieIds.size()) {
            setMovieAdapter(showtimeMap);
        }
    }

    private void setMovieAdapter(Map<String, List<ShowtimeResponse>> showtimeMap) {
        List<String> movieIds = new ArrayList<>(showtimeMap.keySet());
        MovieSessionAdapter movieAdapter = new MovieSessionAdapter(movieIds, showtimeMap, movieNameCache, this); // Cập nhật constructor
        showtimeRecycler.setAdapter(movieAdapter);
    }
}