package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyRapActivity extends AppCompatActivity {
    private ListView lvRap;
    private EditText searchBar;
    private ArrayList<String> rapList;
    private HashMap<String, String> rapMap; // Map để lưu tên và ID của rạp
    private ArrayAdapter<String> adapter;
    private ApiService apiService;
    private static final int REQUEST_CODE_DETAIL = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlyrap);

        lvRap = findViewById(R.id.lvRap);
        searchBar = findViewById(R.id.etSearch);
        rapList = new ArrayList<>();
        rapMap = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rapList);
        lvRap.setAdapter(adapter);

        // Khởi tạo Retrofit ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Lấy dữ liệu từ API
        fetchRaps();

        ImageView imAdd = findViewById(R.id.imAdd);
        imAdd.setOnClickListener(v -> {
            Intent intent = new Intent(QuanLyRapActivity.this, QuanLyRapDetailActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DETAIL);
        });

        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        lvRap.setOnItemClickListener((parent, view, position, id) -> {
            String name = rapList.get(position);
            String rapId = rapMap.get(name);

            if (rapId != null) {
                Intent intent = new Intent(QuanLyRapActivity.this, QuanLyRapDetailActivity.class);
                intent.putExtra("locationId", rapId);
                startActivityForResult(intent, REQUEST_CODE_DETAIL);
            } else {
                Toast.makeText(QuanLyRapActivity.this, "Không tìm thấy rạp này!", Toast.LENGTH_SHORT).show();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchRaps() {
        apiService.getCinemas().enqueue(new Callback<List<Cinema>>() {
            @Override
            public void onResponse(Call<List<Cinema>> call, Response<List<Cinema>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rapList.clear();
                    rapMap.clear();
                    for (Cinema cinema : response.body()) {
                        rapList.add(cinema.getName());
                        rapMap.put(cinema.getName(), cinema.getId());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(QuanLyRapActivity.this, "Lỗi tải dữ liệu rạp!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cinema>> call, Throwable t) {
                Toast.makeText(QuanLyRapActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            fetchRaps();
        }
    }
}