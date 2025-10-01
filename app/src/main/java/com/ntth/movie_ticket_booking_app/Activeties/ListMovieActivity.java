package com.ntth.movie_ticket_booking_app.Activeties;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
    private ViewPager2 viewPagerBanner;
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

//        //Khởi tạo ViewPager2 và CircleIndicator cho biểu ngữ
//        viewPagerBanner = findViewById(R.id.viewPager_banner);
//        bannerAdapter = new MovieHorizontalAdapter(bannerList, this);
//        viewPagerBanner.setAdapter(bannerAdapter);

        // Setup auto-slide for banners
//        setupAutoSlideImages();
//        setupViewPagerBanner();

        // Gọi API để lấy danh sách phim
        fetchMoviesFromApi();
        //fetchBannersFromApi();
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
//    private void fetchBannersFromApi() {
//        api.getMoiveBanner().enqueue(new Callback<List<SliderItems>>() {
//            @Override
//            public void onDataChange(Call<List<SliderItems>> call, Response<List<SliderItems>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    bannerList.clear();
//                    bannerList.addAll(response.body());
//                    bannerAdapter.notifyDataSetChanged();
//                    circleIndicator.setViewPager(viewPagerBanner);
//                    handler.postDelayed(runnable, 3000); // Start auto slide
//                    Log.d("ListMovieActivity", "Loaded " + bannerList.size() + " banners");
//                } else {
//                    Toast.makeText(ListMovieActivity.this, "Không tải được danh sách banner: " + response.code(), Toast.LENGTH_SHORT).show();
//                    Log.e("ListMovieActivity", "Failed to load banners: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<SliderItems>> call, Throwable t) {
//                Toast.makeText(ListMovieActivity.this, "Lỗi tải banner: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                Log.e("ListMovieActivity", "Error loading banners: " + t.getMessage(), t);
//            }
//        });
//    }
//
//    private void setupAutoSlideImages() {
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (bannerList.isEmpty()) return;
//                if (viewPagerBanner.getCurrentItem() == bannerList.size() - 1) {
//                    viewPagerBanner.setCurrentItem(0);
//                } else {
//                    viewPagerBanner.setCurrentItem(viewPagerBanner.getCurrentItem() + 1);
//                }
//                handler.postDelayed(this, 3000); // 3 seconds delay
//            }
//        };
//    }
//
//    private void setupViewPagerBanner() {
//        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                handler.removeCallbacks(runnable);
//                handler.postDelayed(runnable, 3000);
//            }
//        });
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        handler.removeCallbacks(runnable); // Prevent memory leaks
//    }
}
