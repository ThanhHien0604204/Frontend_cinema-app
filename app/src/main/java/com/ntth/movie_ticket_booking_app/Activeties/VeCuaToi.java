package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ntth.movie_ticket_booking_app.Activeties.fragment.LichSuHuyVeFragment;
import com.ntth.movie_ticket_booking_app.Activeties.fragment.LichSuMuaVeFragment;
import com.ntth.movie_ticket_booking_app.R;

public class VeCuaToi extends AppCompatActivity {
    private Button btnLichSuMuaVe;
    private Button btnLichSuHuyVe;  // Hoặc btnLichSuHuyVe
    private Button btnDSQua;

    private static final int Fragment_LICHSUMUAVE = 1;
    private static final int Fragment_LICHSUHUYVE = 2;
    private int currentFragment = Fragment_LICHSUMUAVE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ve_cua_toi);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> {
            Intent intent = new Intent(VeCuaToi.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnLichSuMuaVe = findViewById(R.id.btnLichSuMuaVe);
        btnLichSuHuyVe= findViewById(R.id.btnLichSuHuyVe);

        btnLichSuMuaVe.setOnClickListener(this::onButtonClick);
        btnLichSuHuyVe.setOnClickListener(this::onButtonClick);

        // Load fragment mặc định
        replaceFragment(new LichSuMuaVeFragment());
    }

    public void onButtonClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnLichSuMuaVe) {
            btnLichSuMuaVe.setBackgroundResource(R.drawable.chon_gach_chan);
            btnLichSuHuyVe.setBackgroundResource(R.drawable.khong_chon_k_gach_chan);
            if (currentFragment != Fragment_LICHSUMUAVE) {
                replaceFragment(new LichSuMuaVeFragment());
                currentFragment = Fragment_LICHSUMUAVE;
            }
        } else if (viewId == R.id.btnLichSuHuyVe) {
            btnLichSuHuyVe.setBackgroundResource(R.drawable.chon_gach_chan);
            btnLichSuMuaVe.setBackgroundResource(R.drawable.khong_chon_k_gach_chan);
            if (currentFragment != Fragment_LICHSUHUYVE) {
                replaceFragment(new LichSuHuyVeFragment());  // Sửa tên nếu cần
                currentFragment = Fragment_LICHSUHUYVE;
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_VeCuaToi, fragment);  // ID container trong XML
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}