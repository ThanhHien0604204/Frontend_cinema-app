//package com.ntth.movie_ticket_booking_app.Activeties;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class QuanLyBacThanhVienDetailActivity extends AppCompatActivity {
//
//    private EditText editTenBac, editGiamGia, editDiem;
//    private Button btThemAdmin, btSuaAdmin, btXoaAdmin;
//    private DatabaseReference databaseReference;
//    private String rankId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_quanlybacthanhvien_detail);
//
//        // Ánh xạ view
//        editTenBac = findViewById(R.id.editTenBac);
//        editGiamGia = findViewById(R.id.editGiamGia);
//        editDiem = findViewById(R.id.editDiem);
//        btThemAdmin = findViewById(R.id.btThemAdmin);
//        btSuaAdmin = findViewById(R.id.btSuaAdmin);
//        btXoaAdmin = findViewById(R.id.btXoaAdmin);
//        ImageView imBack = findViewById(R.id.imBack);
//
//        databaseReference = FirebaseDatabase.getInstance().getReference("Rank");
//
//        // Lấy dữ liệu từ Intent
//        rankId = getIntent().getStringExtra("rankId");
//        String name = getIntent().getStringExtra("name");
//        double discount = getIntent().getDoubleExtra("discount", -1);
//        int totalPoint = getIntent().getIntExtra("totalpoint", -1);
//
//        // Kiểm tra và set dữ liệu, nếu không có thì để trống
//        editTenBac.setText(name != null ? name : "");
//        editGiamGia.setText(discount != -1 ? String.valueOf(discount) : "");
//        editDiem.setText(totalPoint != -1 ? String.valueOf(totalPoint) : "");
//
//        // Ẩn nút "Thêm" nếu đang sửa hoặc xóa
//        if (rankId != null) {
//            btThemAdmin.setVisibility(View.GONE);
//        } else {
//            btSuaAdmin.setVisibility(View.GONE);
//            btXoaAdmin.setVisibility(View.GONE);
//        }
//
//        // Thêm bậc mới
//        btThemAdmin.setOnClickListener(v -> {
//            String newName = editTenBac.getText().toString().trim();
//            String newDiscount = editGiamGia.getText().toString().trim();
//            String newTotalPoint = editDiem.getText().toString().trim();
//
//            // Kiểm tra dữ liệu đầu vào
//            if (newName.isEmpty() || newDiscount.isEmpty() || newTotalPoint.isEmpty()) {
//                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Tạo mới dữ liệu Rank
//            String newId = databaseReference.push().getKey();
//            if (newId == null) {
//                Toast.makeText(this, "Lỗi tạo ID!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Rank rank = new Rank(newId, newName, Double.parseDouble(newDiscount), Integer.parseInt(newTotalPoint));
//            databaseReference.child(newId).setValue(rank)
//                    .addOnSuccessListener(aVoid -> {
//                        Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
//                        finish();
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Thêm thất bại!", Toast.LENGTH_SHORT).show();
//                        Log.e("ERROR", "Thêm thất bại", e);
//                    });
//        });
//
//        // Sửa bậc hiện tại
//        btSuaAdmin.setOnClickListener(v -> {
//            if (rankId == null) {
//                Toast.makeText(this, "Không có dữ liệu để sửa!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            String updatedName = editTenBac.getText().toString().trim();
//            String updatedDiscount = editGiamGia.getText().toString().trim();
//            String updatedTotalPoint = editDiem.getText().toString().trim();
//
//            if (updatedName.isEmpty() || updatedDiscount.isEmpty() || updatedTotalPoint.isEmpty()) {
//                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Map<String, Object> updates = new HashMap<>();
//            updates.put("name", updatedName);
//            updates.put("discount", Double.parseDouble(updatedDiscount));
//            updates.put("totalpoint", Integer.parseInt(updatedTotalPoint));
//
//            databaseReference.child(rankId).updateChildren(updates)
//                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show())
//                    .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show());
//                    finish(); // Thoát khỏi Activity
//        });
//
//        // Xóa bậc hiện tại
//        btXoaAdmin.setOnClickListener(v -> {
//            if (rankId == null) {
//                Toast.makeText(this, "Không có dữ liệu để xóa!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            databaseReference.child(rankId).removeValue()
//                    .addOnSuccessListener(aVoid -> {
//                        Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
//                        finish();
//                    })
//                    .addOnFailureListener(e -> Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show());
//        });
//
//        // Quay lại
//        imBack.setOnClickListener(v -> finish());
//    }
//
//    // Lớp Rank để lưu trữ dữ liệu
//    public static class Rank {
//        public String rankId;
//        public String name;
//        public double discount;
//        public int totalpoint;
//
//        public Rank() {
//            // Constructor mặc định cho Firebase
//        }
//
//        public Rank(String rankId, String name, double discount, int totalpoint) {
//            this.rankId = rankId;
//            this.name = name;
//            this.discount = discount;
//            this.totalpoint = totalpoint;
//        }
//    }
//}
