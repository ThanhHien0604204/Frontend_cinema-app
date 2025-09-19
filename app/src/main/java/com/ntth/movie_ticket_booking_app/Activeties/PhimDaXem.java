package com.ntth.movie_ticket_booking_app.Activeties;

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

//import com.ntth.movie_ticket_booking_app.Activeties.fragment.NhanXetCuaToiFragment;
//import com.ntth.movie_ticket_booking_app.Activeties.fragment.PhimDaXemFragment;
import com.ntth.movie_ticket_booking_app.Activeties.fragment.NhanXetCuaToiFragment;
import com.ntth.movie_ticket_booking_app.Activeties.fragment.PhimDaXemFragment;
import com.ntth.movie_ticket_booking_app.R;

public class PhimDaXem extends AppCompatActivity {
    private Button btnphimDaXem;
    private Button btnnhanXet;

    private static final int Fragment_PHIMDAXEM = 1;
    private static final int Fragment_NHANXETCUATOI = 2;
    private int currentFragment = Fragment_PHIMDAXEM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phim_da_xem);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Nút quay lại
        ImageView imBack = findViewById(R.id.imBack);
        imBack.setOnClickListener(v -> finish());

        btnphimDaXem = findViewById(R.id.btnPhimdaxem);
        btnnhanXet = findViewById(R.id.btnNhanxet);

        replaceFragment(new PhimDaXemFragment());
    }


    //đổi màu button
    public void onButtonClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnPhimdaxem) {
            btnphimDaXem.setBackgroundResource(R.drawable.icon_genre);
            btnphimDaXem.setTextColor(getResources().getColor(android.R.color.white));
            btnnhanXet.setBackgroundResource(R.drawable.button_khong_chon);
            btnnhanXet.setTextColor(getResources().getColor(android.R.color.black));
            if (currentFragment != Fragment_PHIMDAXEM) {
                replaceFragment(new PhimDaXemFragment());
                currentFragment = Fragment_PHIMDAXEM;
            }
        } else if (viewId == R.id.btnNhanxet) {
            btnnhanXet.setBackgroundResource(R.drawable.icon_genre);
            btnnhanXet.setTextColor(getResources().getColor(android.R.color.white));
            btnphimDaXem.setBackgroundResource(R.drawable.button_khong_chon);
            btnphimDaXem.setTextColor(getResources().getColor(android.R.color.black));
            if (currentFragment != Fragment_NHANXETCUATOI) {
                replaceFragment(new NhanXetCuaToiFragment());
                currentFragment = Fragment_NHANXETCUATOI;
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}