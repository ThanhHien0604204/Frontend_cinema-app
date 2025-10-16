package com.ntth.movie_ticket_booking_app.Activeties;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ntth.movie_ticket_booking_app.Activeties.fragment.ReviewDialogFragment;
import com.ntth.movie_ticket_booking_app.Adapters.ReviewAdapter;
import com.ntth.movie_ticket_booking_app.Class.Genre;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.Class.Review;
import com.ntth.movie_ticket_booking_app.Class.Ticket;
import com.ntth.movie_ticket_booking_app.Class.User;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.MovieRatingSummary;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;
import com.ntth.movie_ticket_booking_app.dto.ReviewResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Query;

public class MovieDetailActivity extends AppCompatActivity {
    private TextView movieTitleTextView;
    private TextView movieDurationTextView;
    private TextView movieDateStartTextView;
    private TextView movieGenreTextView;
    private TextView movieRatingTextView;
    private TextView movieSummaryTextView;
    private ImageView movieImageView;
    private ImageView btnBack;
    private VideoView movieTrailerView;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private Button rateReviewButton; // Nút Rate and Review
    private Button buyTicketButton;
    private boolean hasWatchedMovie = false; // Trạng thái xem phim
    //private boolean isLoggedIn = false;
    //phân trang
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private final ApiService api = RetrofitClient.api();   // Retrofit API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ views
        movieImageView = findViewById(R.id.movie_image);
        movieTitleTextView = findViewById(R.id.movie_title);
        movieDurationTextView = findViewById(R.id.movie_duration);
        movieDateStartTextView = findViewById(R.id.movie_starting);
        movieGenreTextView = findViewById(R.id.movie_genre);
        movieRatingTextView = findViewById(R.id.movie_rating);
        movieSummaryTextView = findViewById(R.id.movie_summary);
        movieTrailerView = findViewById(R.id.movie_trailer);
        recyclerViewReviews = findViewById(R.id.recyclerView_comments);
        rateReviewButton = findViewById(R.id.rateReviewButton);
        buyTicketButton = findViewById(R.id.buyTicketButton);
        btnBack = findViewById(R.id.detailBack);

        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList,false);
        recyclerViewReviews.setAdapter(reviewAdapter);

        // Lấy movieId từ Intent (từ onClick ở adapter)
        String movieId = getIntent().getStringExtra("movieId");
        Log.d("MovieDetailActivity", "Received movieId from Intent: " + movieId);
        if (movieId == null || movieId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy movieId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d("MovieDetailActivity", "Movie ID: " + movieId);
        // Xử lý click cho nút back
        btnBack.setOnClickListener(v -> onBackPressedCustom());

        // Gọi API /api/user/me
//        api.getCurrentUser().enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User> call, Response<User> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    User user = response.body();
////                    if (user.getUserName() != null) {
////                        userNameTextView.setText("Welcome, " + user.getUserName());
////                    } else {
////                        userNameTextView.setText("Welcome, User");
////                    }
//                    Log.d("MovieDetailActivity", "User: " + user.getEmail());
//                } else {
//                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "No error body";
//                    Log.d("MovieDetailActivity", "Response error: " + response.message() + ", errorBody: " + errorBody);
//                    Toast.makeText(MovieDetailActivity.this, "Lỗi tải thông tin người dùng: " + response.message(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<User> call, Throwable t) {
//                Log.e("MovieDetailActivity", "Request failed: " + t.getMessage(), t);
//                Toast.makeText(MovieDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

        if (movieId != null) {
            fetchMovieDetailsFromApi(movieId);  // Mới: Lấy từ API Retrofit
            checkIfUserHasWatchedMovie(movieId);
            fetchReviewsFromApi(movieId);
            updateMovieRating(movieId);
        } else {
            movieTitleTextView.setText("Không tìm thấy ID phim.");
        }
        // Xử lý sự kiện khi nhấn nút Rate and Review
        rateReviewButton.setOnClickListener(v -> {
            SharedPreferences prefsInner = getSharedPreferences("auth_pref", MODE_PRIVATE);
            String tokenInner = prefsInner.getString("jwt", null);
            Log.d("MovieDetail", "Token: " + (tokenInner != null ? tokenInner.substring(0, 10) + "..." : "null"));
            if (tokenInner == null) {
                Toast.makeText(MovieDetailActivity.this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MovieDetailActivity.this, LoginActivity.class));
                return;
            }

            if (!hasWatchedMovie) {
                showWatchedMovieRequiredMessage();
            } else {
                // Kiểm tra xem người dùng đã viết đánh giá chưa
                api.getMyReviewForMovie(movieId).enqueue(new Callback<ReviewResponse>() {
                    @Override
                    public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                        Log.d("MovieDetail", "getMyReview response code: " + response.code() + ", message: " + response.message());
                        if (response.code() == 401) {
                            // Token hết hạn, logout
                            RetrofitClient.clearToken();
                            Toast.makeText(MovieDetailActivity.this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MovieDetailActivity.this, LoginActivity.class));
                            finish();
                            return;
                        }
                        if (response.isSuccessful()) {
                            Log.d("MovieDetail", "Response successful, body: " + (response.body() != null ? response.body().toString() : "null"));
                            if (response.body() != null) {
                                // Đã có đánh giá
                                showAlreadyReviewedMessage();
                            } else {
                                // Chưa có đánh giá (có thể 204 No Content, nhưng nếu code 200 và body null, log để debug)
                                ReviewDialogFragment reviewDialog = new ReviewDialogFragment(movieId);
                                reviewDialog.show(getSupportFragmentManager(), "reviewDialog");
                            }
                        } else {
                            Log.e("MovieDetail", "Response not successful: " + response.code());
                            try {
                                Log.e("MovieDetail", "Error body: " + response.errorBody().string());
                            } catch (IOException e) {
                                Log.e("MovieDetail", "Error reading errorBody: " + e.getMessage());
                            }
                            Toast.makeText(MovieDetailActivity.this, "Lỗi kiểm tra đánh giá: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ReviewResponse> call, Throwable t) {
                        Log.e("MovieDetail", "getMyReview failure: " + t.getMessage(), t);
                        Toast.makeText(MovieDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        buyTicketButton.setOnClickListener(v -> {
            if (movieId != null) {
                Intent intent = new Intent(MovieDetailActivity.this, CinemaActivity.class);
                intent.putExtra("movieId", movieId);
                Log.e("PaymentActivity", "Truyền MovieID " + movieId);
                startActivity(intent);
            } else {
                Toast.makeText(MovieDetailActivity.this, "Không tìm thấy movieId", Toast.LENGTH_SHORT).show();
            }
        });
        // Xử lý nút back
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

//    private void checkLoginStatus() {
//        SharedPreferences prefs = getSharedPreferences("auth_pref", MODE_PRIVATE);
//        String token = prefs.getString("jwt", null);
//        isLoggedIn = (token != null && !token.isEmpty());
//        if (isLoggedIn) {
//            // Nếu đã login, load review và rating
//            fetchReviewsFromApi(movieId);
//            updateMovieRating(movieId);
//            checkIfUserHasWatchedMovie(movieId);  // Nếu có API check ticket
//        } else {
//            Toast.makeText(this, "Đăng nhập để xem đánh giá và review", Toast.LENGTH_SHORT).show();  // Tùy chọn
//        }
//    }

    // Xử lý tùy chỉnh khi nhấn back
    private void onBackPressedCustom() {
        finish(); // Quay về activity trước đó
    }
    private void showAlreadyReviewedMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle);
        builder.setTitle("Thông báo")
                .setMessage("Bạn đã viết đánh giá rồi!")
                .setPositiveButton("Xác nhận", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }
    private void showWatchedMovieRequiredMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle);
        builder.setTitle("Thông báo")
                .setMessage("Bạn cần xem phim này trước khi đánh giá.")
                .setPositiveButton("Xác nhận", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Tùy chỉnh nút "Xác nhận" thành màu đỏ
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }
//    private void setupRateAndReviewButton(String movieId) {
//        rateReviewButton.setOnClickListener(v -> {
//            if (!isLoggedIn()) {
//                showWatchedMovieRequiredMessage();
//            } else if (!hasWatchedMovie) {
//                Toast.makeText(this, "Bạn chưa xem phim nên chưa thể đánh giá", Toast.LENGTH_SHORT).show();
//            } else {
//                // Kiểm tra đã review chưa
//                api.getMyReviewForMovie(movieId).enqueue(new Callback<ReviewResponse>() {
//                    @Override
//                    public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
//                        if (response.isSuccessful() && response.body() != null) {
//                            Toast.makeText(MovieDetailActivity.this, "Bạn đã đánh giá phim này rồi!", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // 404 hoặc null = chưa review → mở dialog
//                            showWatchedMovieRequiredMessage();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ReviewResponse> call, Throwable t) {
//                        // Nếu lỗi, mặc định cho mở dialog
//                        showWatchedMovieRequiredMessage();
//                    }
//                });
//            }
//        });
//    }
    //kiểm tra xem người dùng đã xem phim chưa
private void checkIfUserHasWatchedMovie(String movieId) {
    rateReviewButton.setEnabled(false);
    rateReviewButton.setText("Đang kiểm tra...");

    api.getUserTickets(movieId).enqueue(new Callback<List<Ticket>>() {
        @Override
        public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Ticket> tickets = response.body();
                Log.d("WatchedCheck", "Tickets: " + tickets);
                hasWatchedMovie = !tickets.isEmpty() && tickets.stream().anyMatch(t -> "CONFIRMED".equals(t.getStatus()));
                Log.d("WatchedCheck", "hasWatchedMovie: " + hasWatchedMovie);
            } else {
                Log.e("WatchedCheck", "Response error: " + response.code());
                hasWatchedMovie = false;
            }
            updateReviewButtonState();
        }

        @Override
        public void onFailure(Call<List<Ticket>> call, Throwable t) {
            Log.e("WatchedCheck", "API failure: " + t.getMessage(), t);
            hasWatchedMovie = false;
            updateReviewButtonState();
        }
    });
}
    private void updateReviewButtonState() {
        runOnUiThread(() -> {
            rateReviewButton.setText("Đánh giá");
            rateReviewButton.setEnabled(true); // Luôn cho phép nhấn để kiểm tra logic
        });
    }

    private void handleApiResponse(Response<?> response) {
        if (response.code() == 401) {
            RetrofitClient.clearToken();
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
    // Lấy chi tiết phim từ API Retrofit
    private void fetchMovieDetailsFromApi(String movieId) {
        // Bước 1: Lấy thông tin movie
        api.getMovieById(movieId).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();

                    movieTitleTextView.setText(movie.getTitle());
                    movieDurationTextView.setText(movie.getDurationMinutes() + " phút");
                    movieDateStartTextView.setText(movie.getMovieDateStart() != null ? movie.getMovieDateStart().toString() : "");

                    // Hiển thị danh sách ID genre
                    List<String> genreIds = movie.getGenreIds();
                    // Bước 2: Lấy tên genre dựa trên genreIds
                    if (!genreIds.isEmpty()) {
                        String genreIdsParam = String.join(",", genreIds); // Tạo chuỗi ID, ví dụ: "id1,id2"
                        api.getGenresId(genreIdsParam).enqueue(new Callback<List<Genre>>() {
                            @Override
                            public void onResponse(Call<List<Genre>> call, Response<List<Genre>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Genre> genres = response.body();
                                    Map<String, String> genreMap = genres.stream()
                                            .collect(Collectors.toMap(Genre::getId, Genre::getName));
                                    List<String> genreNames = genreIds.stream()
                                            .map(genreMap::get)
                                            .filter(name -> name != null)
                                            .collect(Collectors.toList());
                                    movieGenreTextView.setText(genreNames.isEmpty() ? "N/A" : String.join(", ", genreNames));
                                } else {
                                    movieGenreTextView.setText("N/A"); // Nếu không lấy được genre, hiển thị N/A
                                    Log.e("MovieDetail", "Failed to fetch genres: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Genre>> call, Throwable t) {
                                movieGenreTextView.setText("N/A"); // Nếu lỗi, hiển thị N/A
                                Log.e("MovieDetail", "Error fetching genres: " + t.getMessage(), t);
                            }
                        });
                    } else {
                        movieGenreTextView.setText("N/A"); // Nếu không có genreIds
                    }
                    // Cập nhật rating
                    updateMovieRating(movieId);

                    movieSummaryTextView.setText(movie.getSummary());

                    // Load trailer
                    if (movie.getTrailerUrl() != null && !movie.getTrailerUrl().isEmpty()) {
                        movieTrailerView.setVideoURI(Uri.parse(movie.getTrailerUrl()));
                        movieTrailerView.setMediaController(new MediaController(MovieDetailActivity.this));
                        movieTrailerView.requestFocus();
                        movieTrailerView.start();
                    } else {
                        Toast.makeText(MovieDetailActivity.this, "Không có trailer", Toast.LENGTH_SHORT).show();
                    }

                    // Load image
                    if (movie.getImageUrl() != null) {
                        Glide.with(MovieDetailActivity.this)
                                .load(movie.getImageUrl())
                                .placeholder(R.drawable.load)
                                .error(R.drawable.thongbaoloi)
                                .into(movieImageView);
                    } else {
                        Toast.makeText(MovieDetailActivity.this, "Không có ảnh phim", Toast.LENGTH_SHORT).show();
                    }
                    // Lấy danh sách review
                    fetchReviewsFromApi(movieId);
                } else {
                    Toast.makeText(MovieDetailActivity.this, "Không tải được chi tiết phim: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e("MovieDetail", "Lỗi tải phim: " + t.getMessage(), t);
                Toast.makeText(MovieDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchReviewsFromApi(String movieId) {
        api.getReviewsByMovie(movieId, 0, 10).enqueue(new Callback<PageResponse<ReviewResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ReviewResponse>> call, Response<PageResponse<ReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviewList.clear();
                    reviewList.addAll(response.body().getContent().stream()
                            .map(reviewResponse -> new Review(
                                    reviewResponse.getId(),
                                    reviewResponse.getMovieId(),
                                    reviewResponse.getUserId(),
                                    reviewResponse.getContent(),
                                    reviewResponse.getReviewTime(),
                                    reviewResponse.getRating()
                            ))
                            .collect(Collectors.toList()));
                    reviewAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MovieDetailActivity.this, "Không tải được review: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ReviewResponse>> call, Throwable t) {
                Log.e("MovieDetail", "Lỗi tải review: " + t.getMessage(), t);
                Toast.makeText(MovieDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateMovieRating(String movieId) {
        api.getMovieRatingSummary(movieId).enqueue(new Callback<MovieRatingSummary>() {
            @Override
            public void onResponse(Call<MovieRatingSummary> call, Response<MovieRatingSummary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieRatingSummary summary = response.body();
                    @SuppressLint("DefaultLocale") String ratingText = String.format("%.2f ", summary.getAvgRating());
                    movieRatingTextView.setText(ratingText + "🔥");
                } else {
                    movieRatingTextView.setText("N/A");
                }
            }

            @Override
            public void onFailure(Call<MovieRatingSummary> call, Throwable t) {
                Log.e("MovieDetail", "Lỗi tải rating summary: " + t.getMessage(), t);
                movieRatingTextView.setText("N/A");
            }
        });
    }
}
