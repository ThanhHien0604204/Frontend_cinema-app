package com.ntth.movie_ticket_booking_app.Activeties;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.ListMovieVerticalAdapter;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMovieActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private ListMovieVerticalAdapter mvAdapter;
    private List<Movie> movieList = new ArrayList<>();
    private final ApiService api = RetrofitClient.api();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listmovie);
        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        // Ánh xạ và khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerView_ListMovie);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mvAdapter = new ListMovieVerticalAdapter(movieList);
        recyclerView.setAdapter(mvAdapter);

        // Gọi API để lấy danh sách phim
        fetchMoviesFromApi();
    }

    private void fetchMoviesFromApi() {
        api.getMoives().enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieList.clear();
                    movieList.addAll(response.body());
                    mvAdapter.notifyDataSetChanged();
                    Log.d("ListMovieActivity", "Loaded " + movieList.size() + " movies");
                } else {
                    Toast.makeText(ListMovieActivity.this, "Không tải được danh sách phim: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("ListMovieActivity", "Failed to load movies: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                Toast.makeText(ListMovieActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ListMovieActivity", "Error loading movies: " + t.getMessage(), t);
            }
        });
    }
}
