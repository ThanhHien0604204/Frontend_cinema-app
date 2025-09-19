package com.ntth.movie_ticket_booking_app.Activeties;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.ListMovieVerticalAdapter;
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

public class SearchActivity extends AppCompatActivity {
    private RecyclerView rvSearchResults;
    private ListMovieVerticalAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private ApiService api = RetrofitClient.api();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvSearchResults = findViewById(R.id.rvSearchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListMovieVerticalAdapter(movieList);
        rvSearchResults.setAdapter(adapter);

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        String query = getIntent().getStringExtra("query");
        if (query != null && !query.isEmpty()) {
            searchMovies(query);
        } else {
            Toast.makeText(this, "Không có từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void searchMovies(String query) {
        api.searchMovies(query, 0, 12).enqueue(new Callback<PageResponse<Movie>>() {
            @Override
            public void onResponse(Call<PageResponse<Movie>> call, Response<PageResponse<Movie>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieList.clear();
                    movieList.addAll(response.body().getContent());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SearchActivity.this, "Lỗi tìm kiếm: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<Movie>> call, Throwable t) {
                Log.e("SearchActivity", "Lỗi kết nối: " + t.getMessage(), t);
                Toast.makeText(SearchActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}