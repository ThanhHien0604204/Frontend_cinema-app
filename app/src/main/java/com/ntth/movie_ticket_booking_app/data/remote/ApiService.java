package com.ntth.movie_ticket_booking_app.data.remote;

import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.Class.Genre;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.Class.Rank;
import com.ntth.movie_ticket_booking_app.Class.Room;
import com.ntth.movie_ticket_booking_app.Class.Showtime;
import com.ntth.movie_ticket_booking_app.Class.SliderItems;
import com.ntth.movie_ticket_booking_app.Class.Ticket;
import com.ntth.movie_ticket_booking_app.Class.User;
import com.ntth.movie_ticket_booking_app.dto.AuthRequest;
import com.ntth.movie_ticket_booking_app.dto.BookingRequest;
import com.ntth.movie_ticket_booking_app.dto.BookingResponse;
import com.ntth.movie_ticket_booking_app.dto.AuthToken;
import com.ntth.movie_ticket_booking_app.dto.ChangePasswordRequest;
import com.ntth.movie_ticket_booking_app.dto.CreateShowtimeRequest;
import com.ntth.movie_ticket_booking_app.dto.ForgotPasswordRequest;
import com.ntth.movie_ticket_booking_app.dto.HoldSeatsRequest;
import com.ntth.movie_ticket_booking_app.dto.HoldSeatsResponse;
import com.ntth.movie_ticket_booking_app.dto.MovieRatingSummary;
import com.ntth.movie_ticket_booking_app.dto.MovieRequest;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;
import com.ntth.movie_ticket_booking_app.dto.PublicUserResponse;
import com.ntth.movie_ticket_booking_app.dto.RegisterRequest;
import com.ntth.movie_ticket_booking_app.dto.ReviewRequest;
import com.ntth.movie_ticket_booking_app.dto.ReviewResponse;
import com.ntth.movie_ticket_booking_app.dto.SeatResponse;
import com.ntth.movie_ticket_booking_app.dto.ShowtimeResponse;
import com.ntth.movie_ticket_booking_app.dto.UpdateUserRequest;
import com.ntth.movie_ticket_booking_app.dto.ZpCreateOrderResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Khai báo các endpoint Spring Boot.
 */
public interface ApiService {
    //Movie
    @GET("/api/movies")        // trả về list banner (imageUrl)
    Call<List<SliderItems>> getMoiveBanner();

    @GET("/api/movies")
    Call<List<Movie>> getMoives();

    @GET("/api/movies/{id}")
    Call<Movie> getMovieById(@Path("id") String id);

    @GET("/api/movies/upcoming")
    Call<List<Movie>> getUpcomingMovies(@Query("from") String from, @Query("to") String to);

    @GET("/api/movies/hot")
    Call<List<Movie>> getHotMovies(@Query("limit") Integer limit);

    //Thêm endpoint tìm kiếm
    @GET("/api/movies/search")
    Call<PageResponse<Movie>> searchMovies(@Query("q") String keyword, @Query("page") int page, @Query("size") int size);

    @GET("/api/movies/search-by-genre")
    Call<PageResponse<Movie>> searchByGenre(@Query("genreId") String genreId, @Query("page") int page, @Query("size") int size);

    @GET("/api/movies/searchs")
    Call<PageResponse<Movie>> searchMoviesWithFilters(
            @Query("q") String query,
            @Query("genre") String genre,
            @Query("minRating") Double minRating,
            @Query("from") String from,
            @Query("to") String to,
            @Query("page") int page,
            @Query("size") int size
    );
    //ADMIN
    @POST("/api/movies")
    Call<Movie> addMovie(@Body MovieRequest movieRequest);

    @PUT("/api/movies/{id}")
    Call<Movie> updateMovie(@Path("id") String id, @Body MovieRequest movieRequest);

    @DELETE("/api/movies/{id}")
    Call<Void> deleteMovie(@Path("id") String id);

    // API tăng views khi xem chi tiết phim
    @POST("/api/movies/{id}/view")
    Call<Void> incMovieView(@Path("id") String movieId);

    //Thể Loại
    @GET("/api/genres")
    Call<List<Genre>> getGenres();
    @GET("/api/genres")         // danh sách thể loại
    Call<List<Genre>> getGenresId(@Query("ids") String genreIds); // Truyền danh sách ID dưới dạng chuỗi

    //User
    @POST("/api/login")
    Call<AuthToken> login(@Body AuthRequest request);
        // Đăng ký tài khoản
    @POST("/register")
    Call<String> register(@Body RegisterRequest request);
    // Quên mật khẩu
    @POST("/forgot-password")
    Call<Void> forgotPassword(@Body ForgotPasswordRequest request);
    // Lấy thông tin người dùng hiện tại
    @GET("/api/user/me")
    Call<User> getCurrentUser();  // <-- không cần @Header nữa
    // Lấy 1 user theo id (dùng khi hiển thị người viết review)
    @GET("/api/users/{userId}")
    Call<PublicUserResponse> getUserById(@Path("userId") String userId);
    // Cập nhật thông tin cá nhân (yêu cầu JWT, Interceptor đã gắn Bearer tự động)
    @PUT("/api/users/me")
    Call<PublicUserResponse> updateMe(@Body UpdateUserRequest body);
    // Đổi mật khẩu (yêu cầu JWT)
    @PATCH("/api/users/me/password")
    Call<Void> changePassword(@Body ChangePasswordRequest body);

    //Rank
    @GET("/api/ranks/{id}")
    Call<Rank> getRankById(@Path("id") String id);

    //Rạp
    @GET("/api/cinemas")
    Call<List<Cinema>> getCinemas();//Lấy tất cả

    @GET("/api/cinemas/{id}")
    Call<Cinema> getCinemaId(@Path("id") String id);

    @POST("/api/cinemas")
    Call<Cinema> addCinema(@Body Cinema cinema);

    // Room
    @GET("/api/rooms")
    Call<List<Room>> getAllRooms();

    @GET("/api/rooms/{id}")
    Call<Room> getRoom(@Path("id") String id);

    @GET("/api/cinemas/{cinemaId}/rooms")
    Call<List<Room>> getRoomsByCinema(@Path("cinemaId") String cinemaId);

    @POST("/api/rooms")
    Call<Room> addRoom(@Body Room room);

    @PUT("/api/rooms/{id}")
    Call<Room> updateRoom(@Path("id") String id, @Body Room room);

    @DELETE("/api/rooms/{id}")
    Call<Void> deleteRoom(@Path("id") String id);

    //Showtime
    @GET("/api/showtimes")
    Call<List<ShowtimeResponse>> getAllShowtimes();

    @GET("/api/showtimes/{id}")
    Call<Showtime> getShowtimeById(@Path("id") String id);

    @POST("/api/showtimes")
    Call<ShowtimeResponse> createShowtime(@Body CreateShowtimeRequest request);

    @PUT("/api/showtimes/{id}")
    Call<Showtime> updateShowtime(@Path("id") String id, @Body Showtime showtime);

    @DELETE("/api/showtimes/{id}")
    Call<Void> deleteShowtime(@Path("id") String id);

    // GET /api/cinemas/{cinemaId}/movies/{movieId}/showtimes?date=YYYY-MM-DD
    @GET("api/showtimes/{cinemaId}/movies/{movieId}/showtimes")
    Call<List<ShowtimeResponse>> getShowtimesByCinemaAndMovie(
            @Path("cinemaId") String cinemaId,
            @Path("movieId") String movieId,
            @Query("date") String date // ví dụ "2025-08-25"
    );

    // (tuỳ chọn) GET /api/cinemas/{cinemaId}/showtimes?date=YYYY-MM-DD
    @GET("api/showtimes/{cinemaId}/showtimes")
    Call<List<ShowtimeResponse>> getShowtimesByCinema(
            @Path("cinemaId") String cinemaId,
            @Query("date") String date
    );

//    // Thêm endpoint để lấy danh sách ghế
//    @GET("/api/showtimes/{showtimeId}/seats")
//    Call<SeatResponse> getSeatsByShowtimeId(@Path("showtimeId") String showtimeId);

    // Review APIs
    @POST("/api/reviews")
    Call<ReviewResponse> upsertReview(@Body ReviewRequest reviewRequest);

    @GET("/api/reviews/me")
    Call<PageResponse<ReviewResponse>> getMyReviews(@Query("page") int page, @Query("size") int size);

    @GET("/api/reviews/movie/{movieId}/me")
    Call<ReviewResponse> getMyReviewForMovie(@Path("movieId") String movieId);

    @DELETE("/api/reviews/{id}")
    Call<Void> deleteReview(@Path("id") String reviewId);

    @GET("/api/reviews/movie/{movieId}")
    Call<PageResponse<ReviewResponse>> getReviewsByMovie(@Path("movieId") String movieId, @Query("page") int page, @Query("size") int size);

    @GET("/api/reviews/movie/{movieId}/summary")
    Call<MovieRatingSummary> getMovieRatingSummary(@Path("movieId") String movieId);

    // Lấy seat-ledger của 1 showtime
    @GET("/api/showtimes/{showtimeId}/seats")
    Call<List<SeatResponse>> getSeatLedger(@Path("showtimeId") String showtimeId);

//    // BOOKING
//    @POST("/api/bookings")
//    Call<BookingResponse> createBooking(@Body BookingRequest req);

    // Ticket API (mới)
    @GET("/api/tickets/user/me")
    Call<List<Ticket>> getUserTickets(@Query("movieId") String movieId);

//    @GET("/api/bookings/me")
//    Call<List<Ticket>> getUserBookings(@Query("status") String status);

    @GET("/api/bookings/me")
    Call<PageResponse<Ticket>> getMyTickets(
            @Query("status") String status,  // "CONFIRMED" hoặc "CANCELED"
            @Query("page") int page,
            @Query("size") int size
    );
    // === HOLD ghế ===
    @POST("/api/showtimes/{showtimeId}/hold")
    Call<HoldSeatsResponse> holdSeats(@Path("showtimeId") String showtimeId,
                                      @Body Map<String, List<String>> body);

    // BOOKING (CASH): /api/bookings {holdId, paymentMethod:"CASH"}
    @POST("/api/bookings")
    Call<BookingResponse> createBooking(@Body Map<String, String> body);

    // === Tạo booking ZaloPay: PENDING_PAYMENT ===
    @POST("/api/bookings/zalopay")
    Call<BookingResponse> createBookingZaloPay(@Body Map<String, String> body); // {holdId}

    // === Tạo đơn ZP: nhận order_url để mở app/Chrome ===
    @POST("/api/payments/zalopay/create")
    Call<ZpCreateOrderResponse> createZpOrder(@Body Map<String, String> body); // {bookingId}

    // === Lấy trạng thái booking ===
    @GET("/api/bookings/{id}")
    Call<BookingResponse> getBooking(@Path("id") String bookingId);

    // === Hủy vé (tùy chọn) ===
    @POST("/api/bookings/{id}/cancel")
    Call<BookingResponse> cancelBooking(@Path("id") String bookingId, @Body Map<String, String> body);

}
