//package com.ntth.movie_ticket_booking_app.Activeties;
//
//import static android.os.Build.VERSION_CODES.R;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.ArrayList;
//import java.util.Objects;
//
//public class QuanLyBacThanhVienActivity extends AppCompatActivity {
//
//    private ListView lvRank;
//    private ArrayList<String> rankNames;
//    private ArrayList<Rank> rankList;
//    private ArrayAdapter<String> adapter;
//    private DatabaseReference databaseReference;
//    private EditText etSearch;
//    private Handler handler;
//    private Runnable searchRunnable;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_quanlybacthanhvien);
//
//        // Nút thêm bậc thành viên
//        ImageView imAdd = findViewById(R.id.imAdd);
//        imAdd.setOnClickListener(v -> {
//            Intent intent = new Intent(QuanLyBacThanhVienActivity.this, QuanLyBacThanhVienDetailActivity.class);
//            startActivity(intent);
//        });
//
//        // Nút quay lại
//        ImageView imBack = findViewById(R.id.imBack);
//        imBack.setOnClickListener(v -> finish());
//
//        // Ánh xạ các thành phần giao diện
//        lvRank = findViewById(R.id.lvRank);
//        etSearch = findViewById(R.id.etSearch);
//
//        // Khởi tạo danh sách và adapter
//        rankNames = new ArrayList<>();
//        rankList = new ArrayList<>();
//        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rankNames);
//        lvRank.setAdapter(adapter);
//
//        // Kết nối Firebase
//        databaseReference = FirebaseDatabase.getInstance().getReference("Rank");
//
//        // Lấy dữ liệu ban đầu
//        fetchRankData();
//
//        // Xử lý tìm kiếm với độ trễ
//        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
//        etSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // Không xử lý gì trước khi text thay đổi
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Hủy các tìm kiếm trước đó nếu người dùng đang gõ
//                if (searchRunnable != null) {
//                    handler.removeCallbacks(searchRunnable);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // Đặt độ trễ tìm kiếm 1 giây sau khi người dùng dừng gõ
//                searchRunnable = () -> searchRanks(s.toString().trim());
//                handler.postDelayed(searchRunnable, 1000); // 1 giây
//            }
//        });
//
//        // Xử lý sự kiện click vào danh sách bậc thành viên
//        lvRank.setOnItemClickListener((parent, view, position, id) -> {
//            Rank selectedRank = rankList.get(position);
//            Intent intent = new Intent(QuanLyBacThanhVienActivity.this, QuanLyBacThanhVienDetailActivity.class);
//            intent.putExtra("rankId", selectedRank.getRankId()); // Truyền rankId
//            intent.putExtra("discount", selectedRank.getDiscount());
//            intent.putExtra("name", selectedRank.getName());
//            intent.putExtra("totalpoint", selectedRank.getTotalPoint());
//            startActivity(intent);
//        });
//    }
//
//    private void fetchRankData() {
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                rankNames.clear();
//                rankList.clear();
//                for (DataSnapshot data : snapshot.getChildren()) {
//                    Rank rank = data.getValue(Rank.class);
//                    if (rank != null) {
//                        rank.setRankId(data.getKey()); // Gán rankId từ key Firebase
//                        rankNames.add(rank.getName());
//                        rankList.add(rank);
//                    }
//                }
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(QuanLyBacThanhVienActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void searchRanks(String query) {
//        if (query.isEmpty()) {
//            fetchRankData(); // Hiển thị lại toàn bộ danh sách nếu từ khoá rỗng
//            return;
//        }
//
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                rankNames.clear();
//                rankList.clear();
//                for (DataSnapshot data : snapshot.getChildren()) {
//                    Rank rank = data.getValue(Rank.class);
//                    if (rank != null && rank.getName().toLowerCase().contains(query.toLowerCase())) {
//                        rank.setRankId(data.getKey());
//                        rankNames.add(rank.getName());
//                        rankList.add(rank);
//                    }
//                }
//
//                if (rankNames.isEmpty()) {
//                    Toast.makeText(QuanLyBacThanhVienActivity.this, "Không tìm thấy bậc thành viên nào!", Toast.LENGTH_SHORT).show();
//                }
//
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(QuanLyBacThanhVienActivity.this, "Failed to search data", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//    }
//
//    public static class Rank {
//        private String rankId;
//        private String name;
//        private double discount;
//        private int totalpoint;
//
//        public Rank() {
//        }
//
//        public String getRankId() {
//            return rankId;
//        }
//
//        public void setRankId(String rankId) {
//            this.rankId = rankId;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public double getDiscount() {
//            return discount;
//        }
//
//        public int getTotalPoint() {
//            return totalpoint;
//        }
//    }
//}
