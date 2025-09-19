package com.ntth.movie_ticket_booking_app.Activeties;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import jp.wasabeef.glide.transformations.BlurTransformation;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.ntth.movie_ticket_booking_app.Adapters.GenresAdapter;
import com.ntth.movie_ticket_booking_app.Adapters.MoviesAdapter;
import com.ntth.movie_ticket_booking_app.Adapters.SliderAdapters;
import com.ntth.movie_ticket_booking_app.Class.Genre;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.Class.Rank;
import com.ntth.movie_ticket_booking_app.Class.SliderItems;
import com.ntth.movie_ticket_booking_app.Class.User;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ProgressBar progressBarGenres, progressBarTop;
    private ViewPager2 viewPager2;
    private androidx.recyclerview.widget.RecyclerView rvTopMovies, rvGenres;

    private SliderAdapters sliderAdapter;
    private MoviesAdapter moviesAdapter;
    private GenresAdapter genresAdapter;

    private List<SliderItems> listMovie = new ArrayList<>();

    private EditText edtSearch;
    private Runnable searchTask;
    private BottomNavigationView bottomAppBar;
    private final Handler handler = new Handler();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView backgroundImageView;
    private boolean isLoading = false;
    private final ApiService api = RetrofitClient.api();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RetrofitClient.init(getApplicationContext());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        checkUserRole();
        showUserInfomation();

        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);
        backgroundImageView = findViewById(R.id.backgroundImageView);
        ImageView imageAccount = findViewById(R.id.imageAccount);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cập nhật menu điều hướng dựa trên trạng thái đăng nhập
        updateNavigationMenu();

        // Xử lý sự kiện nhấp vào biểu tượng điều hướng
        imageAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mAuth = FirebaseAuth.getInstance();
                User currentUser = null;
                if (currentUser == null) {
                    // Mở ngăn kéo điều hướng
                    drawerLayout.openDrawer(GravityCompat.START);

                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);

        // 1) Ánh xạ view (ID trùng layout của bạn)
        bottomAppBar = findViewById(R.id.bottomAppBar);
        viewPager2 = findViewById(R.id.viewMain);
        rvTopMovies = findViewById(R.id.View1);
        rvGenres = findViewById(R.id.View2);
        progressBarTop = findViewById(R.id.progressBar1);
        progressBarGenres = findViewById(R.id.progressBar2);
        edtSearch = findViewById(R.id.editText);

        // 2) Setup adapters rỗng
        sliderAdapter = new SliderAdapters(new ArrayList<>());
        viewPager2.setAdapter(sliderAdapter);
        // === PEEK + SCALE cho slider ===
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);//thiết lập cái này để ảnh của 2 bộ phim phía sau vẫn được hiển thị 2 bên
        viewPager2.setOffscreenPageLimit(3);

        // tắt overscroll glow
        RecyclerView internalRV = (RecyclerView) viewPager2.getChildAt(0);
        internalRV.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // margin giữa các page + scale mượt
        CompositePageTransformer cpt = new CompositePageTransformer();
        cpt.addTransformer(new MarginPageTransformer(dp(12)));           // khoảng cách giữa các banner
        cpt.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.92f + r * 0.08f);                          // thu nhỏ nhẹ các page bên cạnh
        });
        viewPager2.setPageTransformer(cpt);

        // Thiết lập adapter cho rvTopMovies
        rvTopMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        moviesAdapter = new MoviesAdapter(this);
        rvTopMovies.setAdapter(moviesAdapter);

        // Thiết lập adapter cho rvGenres
        rvGenres.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        genresAdapter = new GenresAdapter();
        rvGenres.setAdapter(genresAdapter);

        // Thêm listener cho click genre
        genresAdapter.setOnGenreClickListener(genre -> {
            String genreId = genre.getId();
            if (genreId != null && !genreId.isEmpty()) {
                searchMoviesByGenre(genreId);
            } else {
                Toast.makeText(MainActivity.this, "Genre ID không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup BottomNavigationView
        bottomAppBar.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });
        // 3) Gọi API để nạp dữ liệu thật
        loadBanners();
        loadHotMovies();
        loadGenres();

        // Cập nhật ảnh nền khi khởi tạo ứng dụng
        if (!listMovie.isEmpty()) {
            String initialImageUrl = listMovie.get(0).getImageUrl();
            updateBackgroundImage(initialImageUrl);
        }
        // Đăng ký lắng nghe sự kiện thay đổi trang trong ViewPager2
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Lấy URL ảnh của bộ phim hiện tại
                String imageUrl = listMovie.get(position).getImageUrl();
                // Cập nhật nền ứng dụng
                updateBackgroundImage(imageUrl);
            }
        });

        // 4) Xử lý tìm kiếm keyword
        // Thêm listener cho nhấn Search/Enter
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String query = edtSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("query", query);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Ngăn nhiều instance
                    startActivity(intent);
                    edtSearch.setText(""); // Xóa query sau khi tìm kiếm
                } else {
                    //Toast.makeText(MainActivity.this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void handleNavigation(int id) {
        if (id == R.id.nav_movie) {
            startActivity(new Intent(this, ListMovieActivity.class));
        } else if (id == R.id.nav_cinema) {
            startActivity(new Intent(this, CinemaActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, VeCuaToi.class));
        } else if (id == R.id.nav_yourTicket) {
            startActivity(new Intent(this, PhimDaXem.class));
        }
    }

    // helper dp -> px
    private int dp(int d) {
        return Math.round(getResources().getDisplayMetrics().density * d);
    }

    private void loadBanners() {
        //show(progressBarBanners, true);
        api.getMoiveBanner().enqueue(new Callback<List<SliderItems>>() {
            @Override
            public void onResponse(Call<List<SliderItems>> c, Response<List<SliderItems>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    listMovie = r.body();
                    sliderAdapter.setData(r.body());
                } else {
                    toast("Không tải được banner");
                }
            }

            @Override
            public void onFailure(Call<List<SliderItems>> c, Throwable t) {
                //show(progressBarBanners, false);
                toast("Lỗi banner: " + t.getMessage());
            }
        });
    }

    private void loadHotMovies() {
        progressBarTop.setVisibility(View.VISIBLE);
        api.getHotMovies(10).enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                progressBarTop.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> hotMovies = response.body();
                    Log.d(TAG, "Phim nóng được tải: " + hotMovies.size());
                    for (Movie movie : hotMovies) {
                        Log.d(TAG, "Movie ID: " + movie.getId() + ",Movie: " + movie.getTitle() + ", Date: " + movie.getMovieDateStart());
                        if (movie.getId() == null || movie.getId().isEmpty()) {
                            Log.e("MainActivity", "Null or empty ID for movie: " + movie.getTitle());
                        }
                    }
                    //MoviesAdapter adapter = new MoviesAdapter(MainActivity.this);
                    moviesAdapter.setData(hotMovies);
                    rvTopMovies.setAdapter(moviesAdapter);
                } else {
                    Log.e(TAG, "Không tải được phim hấp dẫn: " + response.code() + " - " + response.message());
                    Toast.makeText(MainActivity.this, "Không tải được phim hấp dẫn: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                progressBarTop.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi khi tải phim nóng: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGenres() {
        show(progressBarGenres, true);      // (1) Hiện ProgressBar trong lúc tải
        // (2) Gọi mạng bất đồng bộ bằng Retrofit
        api.getGenres().enqueue(new Callback<List<Genre>>() {
            @Override
            public void onResponse(Call<List<Genre>> c, Response<List<Genre>> r) {
                show(progressBarGenres, false);                // (3) Tắt ProgressBar khi có phản hồi

                if (r.isSuccessful() && r.body() != null) { // (4) HTTP 200..299 và có body
                    List<Genre> genres = r.body();
                    Log.d(TAG, "Danh sách thể loại được tải: " + genres.size());
                    genresAdapter.setData(r.body());     // (5) Đẩy list<Genre> vào RecyclerView adapter
                } else {
                    toast("Không tải được thể loại"); // (6) Lỗi(404/500...)
                }
            }

            @Override
            public void onFailure(Call<List<Genre>> c, Throwable t) {
                show(progressBarGenres, false);      // (7) Tắt ProgressBar nếu request thất bại (mạng, timeout...)
                toast("Lỗi genres: " + t.getMessage());
            }
        });
    }

    private void updateBackgroundImage(String imageUrl) {
        RequestOptions requestOptions = new RequestOptions()
                .transform(new BlurTransformation(25, 3));
        // Điều chỉnh độ mờ
        Glide.with(this).load(imageUrl).apply(requestOptions).into(backgroundImageView);
    }

    // Phương thức tìm phim theo genre và cập nhật rvTopMovies
    private void searchMoviesByGenre(String genreId) {
        progressBarTop.setVisibility(View.VISIBLE);
        api.searchByGenre(genreId, 0, 10).enqueue(new Callback<PageResponse<Movie>>() {
            @Override
            public void onResponse(Call<PageResponse<Movie>> call, Response<PageResponse<Movie>> response) {
                progressBarTop.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    moviesAdapter.setData(response.body().getContent());
                    Toast.makeText(MainActivity.this, "Đã tải phim theo thể loại", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi tìm phim theo genre: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<Movie>> call, Throwable t) {
                progressBarTop.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void show(View v, boolean on) {
        if (v != null) v.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        Uri u = intent.getData();
        if (u == null) return;
        if ("yourapp".equals(u.getScheme()) && "payresult".equals(u.getHost())) {
            String bookingId = u.getQueryParameter("bookingId");
            // gọi API kiểm tra trạng thái booking
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Xử lý các mục trong NavigationView tại đây
        int id = item.getItemId();
        if (id == R.id.nav_history) {
            startActivity(new Intent(MainActivity.this, PhimDaXem.class));
        } else if (id == R.id.nav_changeTT) {
            startActivity(new Intent(MainActivity.this, ThayDoiThongTin.class));
        } else if (id == R.id.nav_point) {
            startActivity(new Intent(MainActivity.this, PhimDaXem.class));
        } else if (id == R.id.nav_login) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else if (id == R.id.nav_change) {
            startActivity(new Intent(MainActivity.this, ChangePassword.class));
        } else if (id == R.id.nav_logout) {
            RetrofitClient.clearToken();
            updateNavigationMenu(); // Cập nhật menu sau khi đăng xuất
            startActivity(new Intent(this, LoginActivity.class));
            finish(); //không quay lại bằng nút back
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateNavigationMenu() {
        String token = RetrofitClient.getToken();
        MenuItem loginItem = navigationView.getMenu().findItem(R.id.nav_login);
        MenuItem logoutItem = navigationView.getMenu().findItem(R.id.nav_logout);
        MenuItem historyItem = navigationView.getMenu().findItem(R.id.nav_history);
        MenuItem changeTTItem = navigationView.getMenu().findItem(R.id.nav_changeTT);
        MenuItem pointItem = navigationView.getMenu().findItem(R.id.nav_point);
        MenuItem changePasswordItem = navigationView.getMenu().findItem(R.id.nav_change);

        if (token == null || token.isEmpty()) {
            // Chưa đăng nhập: hiển thị nút đăng nhập, ẩn các nút khác
            loginItem.setVisible(true);
            logoutItem.setVisible(false);
            historyItem.setVisible(false);
            changeTTItem.setVisible(false);
            pointItem.setVisible(false);
            changePasswordItem.setVisible(false);
        } else {
            // Đã đăng nhập: ẩn nút đăng nhập, hiển thị các nút khác
            loginItem.setVisible(false);
            logoutItem.setVisible(true);
            historyItem.setVisible(true);
            changeTTItem.setVisible(true);
            pointItem.setVisible(true);
            changePasswordItem.setVisible(true);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void showUserInfomation() {
        if (isLoading) return; // Ngăn gọi lặp
        isLoading = true;

        // Lấy token từ RetrofitClient
        String token = RetrofitClient.getToken();
        Log.d(TAG, "Token retrieved ở main: " + (token != null ? token : "null"));
        if (token == null) {
            Toast.makeText(this, "Token không tồn tại, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            isLoading = false;
            return;
        }

        // Gọi API để lấy thông tin người dùng
        ApiService api = RetrofitClient.api();
        Call<User> call = api.getCurrentUser(); // <-- không truyền token
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                isLoading = false;
                Log.d(TAG, "Response code ở MainActivity: " + response.code());
                Log.d(TAG, "Request headers ở MainActivity: " + call.request().headers().toString()); // Log header gửi đi
                Log.d(TAG, "Response body ở MainActivity: " + (response.body() != null ? response.body().toString() : "null"));
                String errorBodyStr = "null";
                if (response.errorBody() != null) {
                    try {
                        errorBodyStr = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "Lỗi đọc error body: " + e.getMessage(), e);
                        errorBodyStr = "Lỗi đọc error body";
                    }
                }
                Log.d(TAG, "Error body: " + errorBodyStr);
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "Thông tin người dùng từ API: " + user.getUserName());
                    // Lấy các TextView từ nav_header
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View headerView = navigationView.getHeaderView(0);
                    TextView txt_name = headerView.findViewById(R.id.txt_name);
                    TextView txt_email = headerView.findViewById(R.id.txt_email);
                    TextView txtDiemRank = headerView.findViewById(R.id.txtDiemRank);
                    TextView rankhientai = headerView.findViewById(R.id.rankhientai);

                    // Hiển thị tên người dùng
                    String name = user.getUserName();
                    if (name == null || name.isEmpty()) {
                        txt_name.setVisibility(View.GONE);
                    } else {
                        txt_name.setVisibility(View.VISIBLE);
                        txt_name.setText(name);
                    }

                    // Hiển thị email người dùng
                    String email = user.getEmail();
                    if (email != null) {
                        txt_email.setText(email);
                    }

                    // Hiển thị điểm số người dùng
//                    int diemTV = user.getDiemTV();
//                    txtDiemRank.setText(String.valueOf(diemTV));
//
//                    // Hiển thị rank (giả sử User có getRankName() hoặc getRankId())
//                    String rankName = user.getRankName();
//                    if (rankName != null) {
//                        rankhientai.setText(rankName);
//                    } else {
//                        String rankId = user.getRankId();
//                        if (rankId != null) {
//                            loadRankFromApi(rankId, rankhientai);
//                        } else {
//                            rankhientai.setText("Unknown");
//                        }
//                    }
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi ở MainActivity: " + response.code() + " - " + errorBodyStr, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                isLoading = false;
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi gọi API user/me: " + t.getMessage(), t);
            }
        });
    }

    // Phương thức phụ để lấy Rank nếu chỉ có rankId (thêm endpoint vào ApiService nếu cần)
    private void loadRankFromApi(String rankId, TextView rankTextView) {
        ApiService api = RetrofitClient.api();
        Call<Rank> call = api.getRankById(rankId); // Giả sử có endpoint @GET("/api/ranks/{id}") Call<Rank> getRankById(@Path("id") String id);
        call.enqueue(new Callback<Rank>() {
            @Override
            public void onResponse(Call<Rank> call, Response<Rank> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Rank rank = response.body();
                    rankTextView.setText(rank.getName()); // Giả sử Rank có getName()
                } else {
                    rankTextView.setText("Unknown");
                    Toast.makeText(MainActivity.this, "Lỗi tải rank", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Rank> call, Throwable t) {
                rankTextView.setText("Unknown");
                Toast.makeText(MainActivity.this, "Lỗi kết nối khi tải rank: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole() {
        api.getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    if ("ADMIN".equals(user.getRole())) {
                        // Chuyển đến AdminMainActivity nếu là ADMIN
                        Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
                        startActivity(intent);
                    } else {
                        Log.e(TAG, "Bạn không có quyền truy cập admin! ");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi tải thông tin user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}