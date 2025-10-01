package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ntth.movie_ticket_booking_app.Class.User;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyNguoiDungActivity extends AppCompatActivity {

    private ListView lvAccount;
    private ArrayList<String> userList; // Danh sách chứa tên và quyền của người dùng
    private ArrayList<String> filteredUserList; // Danh sách lọc sau tìm kiếm
    private HashMap<String, String> userMap; // Lưu trữ tên và userId
    private ArrayAdapter<String> adapter;
    private ApiService apiService;
    private EditText searchUser;
    private static final int REQUEST_CODE_DETAIL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_quanlynguoidung);

        // Khởi tạo ApiService
        apiService = RetrofitClient.api();

        // Khởi tạo các view
        ImageView imAdd = findViewById(R.id.imAdd);
        imAdd.setOnClickListener(v -> {
            Intent intent = new Intent(QuanLyNguoiDungActivity.this, QuanLyNguoiDungDetailActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DETAIL);
        });

        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        lvAccount = findViewById(R.id.lvAccount);
        searchUser = findViewById(R.id.etSearch);

        // Khởi tạo danh sách và adapter
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        userMap = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredUserList);
        lvAccount.setAdapter(adapter);

        // Lấy dữ liệu từ API
        fetchUsers();

        // Tìm kiếm
        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Click item để edit
        lvAccount.setOnItemClickListener((parent, view, position, id) -> {
            String userInfo = filteredUserList.get(position);
            String userId = userMap.get(userInfo);
            if (userId != null) {
                Intent intent = new Intent(QuanLyNguoiDungActivity.this, QuanLyNguoiDungDetailActivity.class);
                intent.putExtra("userId", userId);
                startActivityForResult(intent, REQUEST_CODE_DETAIL);
            } else {
                Toast.makeText(this, "Không tìm thấy user!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lấy dữ liệu người dùng từ API
    private void fetchUsers() {
        // Gọi API với search rỗng để lấy tất cả
        apiService.getAllUsers("").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userMap.clear();
                    for (User user : response.body()) {
                        String role = user.getRole() != null ? user.getRole() : "Unknown";
                        String userInfo = (user.getUserName() != null ? user.getUserName() : "N/A") + " - " + role;
                        userList.add(userInfo);
                        userMap.put(userInfo, user.getId());
                    }
                    filterUsers(searchUser.getText().toString());
                } else {
                    Toast.makeText(QuanLyNguoiDungActivity.this, "Lỗi tải dữ liệu users!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(QuanLyNguoiDungActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Fetch users failed: " + t.getMessage());
            }
        });
    }

    // Bộ lọc tìm kiếm người dùng (local filter)
    private void filterUsers(String query) {
        filteredUserList.clear();
        for (String userInfo : userList) {
            if (userInfo.toLowerCase().contains(query.toLowerCase())) {
                filteredUserList.add(userInfo);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            fetchUsers();
        }
    }
}