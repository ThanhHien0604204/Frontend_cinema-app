package com.ntth.movie_ticket_booking_app.Activeties;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.Class.Room;
import com.ntth.movie_ticket_booking_app.Class.Showtime;
import com.ntth.movie_ticket_booking_app.Class.Ticket;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.BookingResponse;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "booking_id";

    private TextView tvBookingCode, tvMovieName, tvSeats, tvAmount, tvStatus, tvShowtime, tvCinemaName;
    private ImageView imgPoster, btnBack;
    private Button btnCancel;
    private ApiService apiService;
    private boolean fromPayment = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        // Khởi tạo views
        mapViews();

        // Khởi tạo Retrofit API
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        //String bookingId = getIntent().getStringExtra("bookingId");
        // Lấy booking ID từ intent
        String bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Thiếu mã đặt vé", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Kiểm tra extra để xác định luồng từ thanh toán
        fromPayment = getIntent().getBooleanExtra("from_payment", false);

        // Gán sự kiện cho nút quay lại
        btnBack.setOnClickListener(v -> handleBackNavigation());

        // Lấy và hiển thị chi tiết vé
        fetchTicketDetails(bookingId);
    }
//    @Override
//    public void onBackPressed() {
//        handleBackNavigation(); // Xử lý nút Back của thiết bị tương tự
//    }

    private void handleBackNavigation() {
        if (fromPayment) {
            // Nếu từ luồng thanh toán, quay về ShowtimeActivity và xóa các trang trung gian
            Intent intent = new Intent(this, ShowtimeActivity.class); // Thay ShowtimeActivity bằng tên Activity thực tế của màn hình showtime
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            // Nếu từ các luồng khác (ví dụ: lịch sử vé), chỉ đóng BillActivity
            finish();
        }
    }

    private void mapViews() {
        tvBookingCode = findViewById(R.id.tvBookingCode);
        tvMovieName = findViewById(R.id.tvMovieName);
        tvSeats = findViewById(R.id.tvSeats);
        tvAmount = findViewById(R.id.tvAmount);
        tvStatus = findViewById(R.id.tvStatus);
        tvShowtime = findViewById(R.id.tvShowtime);
        tvCinemaName = findViewById(R.id.tvCinemaName);
        imgPoster = findViewById(R.id.imgPoster);
        btnBack = findViewById(R.id.btnBack);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void fetchTicketDetails(String bookingId) {
        Log.d("BillActivity", "Fetching bookingId: " + bookingId);

        apiService.getBooking(bookingId).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                Log.d("BillActivity", "getBooking response code: " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("BillActivity", "getBooking failed: " + response.code());
                    Toast.makeText(BillActivity.this, "Không lấy được thông tin vé", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                BookingResponse booking = response.body();
                Log.d("BillActivity", "Booking data: " + booking.getBookingCode() + ", status: " + booking.getStatus());

                // Kiểm tra null trước khi setText cho các TextView
                if (tvBookingCode != null) {
                    tvBookingCode.setText("Mã vé: " + booking.getBookingCode());
                } else {
                    Log.e("BillActivity", "tvBookingCode is null");
                }
                if (tvSeats != null) {
                    List<String> seats = booking.getSeats();
                    tvSeats.setText("Ghế: " + (seats != null ? String.join(", ", seats) : "N/A"));
                } else {
                    Log.e("BillActivity", "tvSeats is null");
                }
                if (tvAmount != null) {
                    NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
                    tvAmount.setText(numberFormat.format(booking.getAmount()) + " VNĐ");
                } else {
                    Log.e("BillActivity", "tvAmount is null");
                }
                if (tvStatus != null) {
                    tvStatus.setText(booking.getStatus().equals("CANCELED") ? "Đã hủy" : "Hoàn tất");
                } else {
                    Log.e("BillActivity", "tvStatus is null");
                }

                // Xử lý nút hủy với kiểm tra null
                if (booking.getStatus().equals("CANCELED")) {
                    if (btnCancel != null) {
                        btnCancel.setEnabled(false);
                        btnCancel.setAlpha(0.5f);
                        Log.d("BillActivity", "Ticket is CANCELED, disabling cancel button");
                    } else {
                        Log.e("BillActivity", "btnCancel is null");
                    }
                } else {
                    if (btnCancel != null) {
                        btnCancel.setEnabled(true);
                        btnCancel.setAlpha(1.0f);
                        btnCancel.setOnClickListener(v -> cancelBooking(bookingId));
                        Log.d("BillActivity", "Ticket is CONFIRMED, enabling cancel button");
                    } else {
                        Log.e("BillActivity", "btnCancel is null");
                    }
                }

                // Fetch showtime
                String showtimeId = booking.getShowtimeId();
                Log.d("BillActivity", "Fetching showtimeId: " + showtimeId);
                if (showtimeId == null || showtimeId.isEmpty()) {
                    if (tvShowtime != null) {
                        tvShowtime.setText("Suất chiếu: N/A");
                    } else {
                        Log.e("BillActivity", "tvShowtime is null");
                    }
                    if (tvCinemaName != null) {
                        tvCinemaName.setText("Rạp: N/A");
                    } else {
                        Log.e("BillActivity", "tvCinemaName is null");
                    }
                    return;
                }

                apiService.getShowtimeById(showtimeId).enqueue(new Callback<Showtime>() {
                    @Override
                    public void onResponse(Call<Showtime> call, Response<Showtime> response) {
                        Log.d("BillActivity", "getShowtimeById response code: " + response.code());
                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e("BillActivity", "getShowtimeById failed: " + response.code());
                            if (tvShowtime != null) {
                                tvShowtime.setText("Suất chiếu: N/A");
                            } else {
                                Log.e("BillActivity", "tvShowtime is null");
                            }
                            if (tvCinemaName != null) {
                                tvCinemaName.setText("Rạp: N/A");
                            } else {
                                Log.e("BillActivity", "tvCinemaName is null");
                            }
                            return;
                        }

                        Showtime showtime = response.body();
                        Log.d("BillActivity", "Showtime data: startAt=" + showtime.getStartAt() +
                                ", movieId=" + showtime.getMovieId() + ", roomId=" + showtime.getRoomId());

                        // Set showtime với kiểm tra null
                        String formattedStart = showtime.getFormattedStartTime();
                        String formattedEnd = showtime.getFormattedEndTime();
                        String formattedDate = showtime.getFormattedDate();
                        if (tvShowtime != null) {
                            tvShowtime.setText("Suất: " + formattedStart + " - " + formattedEnd + " (" + formattedDate + ")");
                            Log.d("BillActivity", "Showtime formatted: " + tvShowtime.getText());
                        } else {
                            Log.e("BillActivity", "tvShowtime is null");
                        }

                        // Fetch movie
                        String movieId = showtime.getMovieId();
                        Log.d("BillActivity", "Fetching movieId: " + movieId);
                        if (movieId == null || movieId.isEmpty()) {
                            if (tvMovieName != null) {
                                tvMovieName.setText("Phim: N/A");
                            } else {
                                Log.e("BillActivity", "tvMovieName is null");
                            }
                            return;
                        }

                        apiService.getMovieById(movieId).enqueue(new Callback<Movie>() {
                            @Override
                            public void onResponse(Call<Movie> call, Response<Movie> response) {
                                Log.d("BillActivity", "getMovieById response code: " + response.code());
                                if (response.isSuccessful() && response.body() != null) {
                                    Movie movie = response.body();
                                    Log.d("BillActivity", "Movie data: title=" + movie.getTitle() +
                                            ", imageUrl=" + movie.getImageUrl());

                                    if (tvMovieName != null) {
                                        tvMovieName.setText("Phim: " + movie.getTitle());
                                        Log.d("BillActivity", "Set movie name: " + tvMovieName.getText());
                                    } else {
                                        Log.e("BillActivity", "tvMovieName is null");
                                    }

                                    // Load poster với kiểm tra null
                                    if (movie.getImageUrl() != null && !movie.getImageUrl().isEmpty()) {
                                        RequestOptions options = new RequestOptions()
                                                .transform(new CenterCrop(), new RoundedCorners(30));
                                        if (imgPoster != null) {
                                            Glide.with(BillActivity.this)
                                                    .load(movie.getImageUrl())
                                                    .apply(options)
                                                    .placeholder(R.drawable.thongbaoloi)
                                                    .error(R.drawable.thongbaoloi)
                                                    .into(imgPoster);
                                            Log.d("BillActivity", "Loading image: " + movie.getImageUrl());
                                        } else {
                                            Log.e("BillActivity", "imgPoster is null");
                                        }
                                    } else {
                                        Log.w("BillActivity", "Movie image URL is null or empty");
                                        if (imgPoster != null) {
                                            imgPoster.setImageResource(R.drawable.thongbaoloi);
                                        } else {
                                            Log.e("BillActivity", "imgPoster is null");
                                        }
                                    }
                                } else {
                                    Log.e("BillActivity", "getMovieById failed: " + response.code());
                                    if (tvMovieName != null) {
                                        tvMovieName.setText("Phim: N/A");
                                    } else {
                                        Log.e("BillActivity", "tvMovieName is null");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Movie> call, Throwable t) {
                                Log.e("BillActivity", "getMovieById failure: " + t.getMessage());
                                if (tvMovieName != null) {
                                    tvMovieName.setText("Phim: N/A");
                                } else {
                                    Log.e("BillActivity", "tvMovieName is null");
                                }
                            }
                        });

                        // Fetch room
                        String roomId = showtime.getRoomId();
                        Log.d("BillActivity", "Fetching roomId: " + roomId);
                        if (roomId == null || roomId.isEmpty()) {
                            if (tvCinemaName != null) {
                                tvCinemaName.setText("Rạp: N/A");
                            } else {
                                Log.e("BillActivity", "tvCinemaName is null");
                            }
                            return;
                        }

                        apiService.getRoom(roomId).enqueue(new Callback<Room>() {
                            @Override
                            public void onResponse(Call<Room> call, Response<Room> response) {
                                Log.d("BillActivity", "getRoom response code: " + response.code());
                                if (!response.isSuccessful() || response.body() == null) {
                                    Log.e("BillActivity", "getRoom failed: " + response.code());
                                    if (tvCinemaName != null) {
                                        tvCinemaName.setText("Rạp: N/A");
                                    } else {
                                        Log.e("BillActivity", "tvCinemaName is null");
                                    }
                                    return;
                                }

                                Room room = response.body();
                                Log.d("BillActivity", "Room data: cinemaId=" + room.getCinemaId());

                                // Fetch cinema
                                String cinemaId = room.getCinemaId();
                                Log.d("BillActivity", "Fetching cinemaId: " + cinemaId);
                                if (cinemaId == null || cinemaId.isEmpty()) {
                                    if (tvCinemaName != null) {
                                        tvCinemaName.setText("Rạp: N/A");
                                    } else {
                                        Log.e("BillActivity", "tvCinemaName is null");
                                    }
                                    return;
                                }

                                apiService.getCinemaId(cinemaId).enqueue(new Callback<Cinema>() {
                                    @Override
                                    public void onResponse(Call<Cinema> call, Response<Cinema> response) {
                                        Log.d("BillActivity", "getCinemaId response code: " + response.code());
                                        if (response.isSuccessful() && response.body() != null) {
                                            Cinema cinema = response.body();
                                            if (tvCinemaName != null) {
                                                tvCinemaName.setText("Rạp: " + cinema.getName());
                                                Log.d("BillActivity", "Cinema name: " + cinema.getName());
                                            } else {
                                                Log.e("BillActivity", "tvCinemaName is null");
                                            }
                                        } else {
                                            Log.e("BillActivity", "getCinemaId failed: " + response.code());
                                            if (tvCinemaName != null) {
                                                tvCinemaName.setText("Rạp: N/A");
                                            } else {
                                                Log.e("BillActivity", "tvCinemaName is null");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Cinema> call, Throwable t) {
                                        Log.e("BillActivity", "getCinemaId failure: " + t.getMessage());
                                        if (tvCinemaName != null) {
                                            tvCinemaName.setText("Rạp: N/A");
                                        } else {
                                            Log.e("BillActivity", "tvCinemaName is null");
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<Room> call, Throwable t) {
                                Log.e("BillActivity", "getRoom failure: " + t.getMessage());
                                if (tvCinemaName != null) {
                                    tvCinemaName.setText("Rạp: N/A");
                                } else {
                                    Log.e("BillActivity", "tvCinemaName is null");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Showtime> call, Throwable t) {
                        Log.e("BillActivity", "getShowtimeById failure: " + t.getMessage());
                        if (tvShowtime != null) {
                            tvShowtime.setText("Suất chiếu: N/A");
                        } else {
                            Log.e("BillActivity", "tvShowtime is null");
                        }
                        if (tvCinemaName != null) {
                            tvCinemaName.setText("Rạp: N/A");
                        } else {
                            Log.e("BillActivity", "tvCinemaName is null");
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e("BillActivity", "getBooking failure: " + t.getMessage());
                Toast.makeText(BillActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayTicketDetails(Ticket ticket, BookingResponse booking) {
        // Hiển thị thông tin vé
        tvBookingCode.setText("Mã đặt vé: " + (ticket.getBookingCode() != null ? ticket.getBookingCode() : "N/A"));
        tvMovieName.setText("Phim: " + (ticket.getMovieName() != null ? ticket.getMovieName() : "N/A")); // Giả định có movie name trong Ticket hoặc BookingResponse
        tvSeats.setText("Ghế: " + (ticket.getSeats() != null && !ticket.getSeats().isEmpty() ? String.join(", ", ticket.getSeats()) : "N/A"));
        tvStatus.setText("Trạng thái: " + (ticket.getStatus() != null ? ticket.getStatus() : "N/A"));

        // Định dạng và hiển thị tổng tiền
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(0);
        String formattedAmount = numberFormat.format(ticket.getAmount());
        tvAmount.setText("Tổng tiền: " + formattedAmount + " đ");

        // Tải poster phim
        if (ticket.getMovieImageUrl() != null && !ticket.getMovieImageUrl().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(60))
                    .error(R.drawable.thongbaoloi);
            Glide.with(this)
                    .load(ticket.getMovieImageUrl())
                    .apply(requestOptions)
                    .into(imgPoster);
        } else {
            imgPoster.setImageResource(R.drawable.thongbaoloi);
        }

        // Lấy thông tin suất chiếu
        String showtimeId = ticket.getShowtimeId();
        if (showtimeId == null || showtimeId.isEmpty()) {
            tvShowtime.setText("Suất chiếu: N/A");
            tvCinemaName.setText("Rạp: N/A");
            return;
        }

        apiService.getShowtimeById(showtimeId).enqueue(new Callback<Showtime>() {
            @Override
            public void onResponse(Call<Showtime> call, Response<Showtime> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvShowtime.setText("Suất chiếu: N/A");
                    tvCinemaName.setText("Rạp: N/A");
                    return;
                }

                Showtime showtime = response.body();
                // Định dạng thời gian suất chiếu
                double startAt = showtime.getStartAt();
                if (startAt != 0) {
                    try {
                        long timestampMillis = (long) (startAt * 1000); // Chuyển từ giây sang millis
                        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
                        Date date = new Date(timestampMillis);
                        tvShowtime.setText("Suất chiếu: " + displayFormat.format(date));
                    } catch (Exception e) {
                        tvShowtime.setText("Suất chiếu: N/A");
                    }
                } else {
                    tvShowtime.setText("Suất chiếu: N/A");
                }

                // Lấy thông tin phòng chiếu
                String roomId = showtime.getRoomId();
                if (roomId == null || roomId.isEmpty()) {
                    tvCinemaName.setText("Rạp: N/A");
                    return;
                }

                apiService.getRoom(roomId).enqueue(new Callback<Room>() {
                    @Override
                    public void onResponse(Call<Room> call, Response<Room> roomResponse) {
                        if (!roomResponse.isSuccessful() || roomResponse.body() == null) {
                            tvCinemaName.setText("Rạp: N/A");
                            return;
                        }

                        Room room = roomResponse.body();
                        String cinemaId = room.getCinemaId();
                        if (cinemaId == null || cinemaId.isEmpty()) {
                            tvCinemaName.setText("Rạp: N/A");
                            return;
                        }

                        // Lấy thông tin rạp chiếu
                        apiService.getCinemaId(cinemaId).enqueue(new Callback<Cinema>() {
                            @Override
                            public void onResponse(Call<Cinema> call, Response<Cinema> cinemaResponse) {
                                if (cinemaResponse.isSuccessful() && cinemaResponse.body() != null) {
                                    tvCinemaName.setText("Rạp: " + cinemaResponse.body().getName());
                                } else {
                                    tvCinemaName.setText("Rạp: N/A");
                                }
                            }

                            @Override
                            public void onFailure(Call<Cinema> call, Throwable t) {
                                tvCinemaName.setText("Rạp: N/A");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Room> call, Throwable t) {
                        tvCinemaName.setText("Rạp: N/A");
                    }
                });
            }

            @Override
            public void onFailure(Call<Showtime> call, Throwable t) {
                tvShowtime.setText("Suất chiếu: N/A");
                tvCinemaName.setText("Rạp: N/A");
            }
        });
    }

    // Method hủy vé
    private void cancelBooking(String bookingId) {
        Map<String, String> body = new HashMap<>();  // Body rỗng nếu không cần
        apiService.cancelBooking(bookingId, body).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BillActivity.this, "Hủy vé thành công", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("Đã hủy");
                    btnCancel.setEnabled(false);
                    btnCancel.setAlpha(0.5f);  // Disable sau khi hủy
                } else {
                    Toast.makeText(BillActivity.this, "Hủy thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Toast.makeText(BillActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}