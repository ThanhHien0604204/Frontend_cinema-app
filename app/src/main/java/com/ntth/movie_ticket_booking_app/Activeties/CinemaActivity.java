package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.CinemaAdapter;
import com.ntth.movie_ticket_booking_app.Adapters.ProvinceAdapter;
import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CinemaActivity extends AppCompatActivity {
    private RecyclerView provinceRecycler, addressRecycler;
    private ImageView back;
    private CinemaAdapter addressAdapter;
    private ProvinceAdapter provinceAdapter;
    private List<Cinema> allLocations = new ArrayList<>();
    private List<Cinema> filteredLocations = new ArrayList<>();
    private List<String> provinces = Arrays.asList("Hồ Chí Minh", "Đồng Nai", "Hà Nội");  // Gán cứng dữ liệu province

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema);

        provinceRecycler = findViewById(R.id.province_list);
        addressRecycler = findViewById(R.id.address_list);

        // Nút quay lại
        back=findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

        String movieId  = getIntent().getStringExtra("movieId");
        Log.d("CinemaActivity", "Nhận movieId: " + movieId);

        // Khởi tạo adapter cho province với danh sách đầy đủ
        Log.d("CinemaActivity", "Số lượng provinces: " + provinces.size()); // Kiểm tra trong onCreate
        provinceAdapter = new ProvinceAdapter(provinces, province -> {
            filterByProvince(province);
            Toast.makeText(this, "Chọn tỉnh: " + province, Toast.LENGTH_SHORT).show();
        });
        provinceRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        provinceRecycler.setAdapter(provinceAdapter);

        // Khởi tạo adapter cho address
        addressAdapter = new CinemaAdapter(filteredLocations, location -> {
            // Khi click cinema, truyền ID và chuyển sang ShowtimeActivity
            Intent intent = new Intent(CinemaActivity.this, ShowtimeActivity.class);
            intent.putExtra("cinemaId", location.getId());
            intent.putExtra("movieId", movieId);
            Log.d("CinemaActivity", "Truyền cinemaId: " + location.getId());
            Log.d("CinemaActivity", "Truyền movieId: " + movieId);
            startActivity(intent);
        });
        addressRecycler.setLayoutManager(new LinearLayoutManager(this));
        addressRecycler.setAdapter(addressAdapter);

        // Lấy dữ liệu từ API
        loadCinemasFromApi();
    }
    private void loadCinemasFromApi() {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);  // Giả sử RetrofitClient là singleton hoặc builder
        Call<List<Cinema>> call = apiService.getCinemas();
        call.enqueue(new Callback<List<Cinema>>() {
            @Override
            public void onResponse(Call<List<Cinema>> call, Response<List<Cinema>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allLocations.clear();
                    allLocations.addAll(response.body());
                    // Mặc định lọc province đầu tiên (ví dụ Hồ Chí Minh)
                    filterByProvince(provinces.get(0));
                } else {
                    Toast.makeText(CinemaActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cinema>> call, Throwable t) {
                Toast.makeText(CinemaActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void filterByProvince(String province) {
        filteredLocations.clear();
        String keyword = province;
        for (Cinema loc : allLocations) {
            if (loc.getAddress().contains(keyword)) {
                filteredLocations.add(loc);
            }
        }
        addressAdapter.notifyDataSetChanged();
    }
}