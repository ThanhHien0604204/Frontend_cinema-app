package com.ntth.movie_ticket_booking_app.Activeties.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.ListMovieVerticalAdapter;
import com.ntth.movie_ticket_booking_app.Adapters.WatchedMovieAdapter;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.Class.Showtime;
import com.ntth.movie_ticket_booking_app.Class.Ticket;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhimDaXemFragment extends Fragment {

    private static final String TAG = "PhimDaXemFragment";
    private RecyclerView recyclerView;
    private WatchedMovieAdapter mvAdapter;
    private List<Movie> listMovie = new ArrayList<>();
    private List<Ticket> listTickets = new ArrayList<>();
    private ApiService apiService;
    private Set<String> uniqueMovieIds = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "=== onCreateView START ===");

        View view = inflater.inflate(R.layout.fragment_phim_da_xem, container, false);
        Log.d(TAG, "Layout inflated");

        recyclerView = view.findViewById(R.id.recyclerView_phim_Da_Xem);

        Log.d(TAG, "RecyclerView: " + (recyclerView != null ? "FOUND" : "NULL"));

        if (recyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);

            mvAdapter = new WatchedMovieAdapter(listMovie); // Sử dụng adapter mới
            recyclerView.setAdapter(mvAdapter);

            // Thêm listener để debug khi có data
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int firstVisible = layoutManager.findFirstVisibleItemPosition();
                    Log.d(TAG, "RecyclerView scrolled - First visible item: " + firstVisible);
                }
            });

            // Debug layout
            ViewTreeObserver vto = recyclerView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.d(TAG, "🔍 RecyclerView LAYOUT INFO:");
                    Log.d(TAG, "  - Width: " + recyclerView.getWidth() + "px");
                    Log.d(TAG, "  - Height: " + recyclerView.getHeight() + "px");
                    Log.d(TAG, "  - Visibility: " + (recyclerView.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
                    Log.d(TAG, "  - Item count: " + mvAdapter.getItemCount());
                    Log.d(TAG, "  - LayoutManager: " + (recyclerView.getLayoutManager() != null ? "OK" : "NULL"));

                    // Kiểm tra parent container
                    ViewGroup parent = (ViewGroup) recyclerView.getParent();
                    if (parent != null) {
                        Log.d(TAG, "  - Parent height: " + parent.getHeight() + "px");
                        Log.d(TAG, "  - Parent width: " + parent.getWidth() + "px");
                    }
                }
            });

            Log.d(TAG, "✅ RecyclerView setup complete");
        }

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        fetchWatchedTickets();
        Log.d(TAG, "=== onCreateView END ===");
        return view;
    }

    private void fetchWatchedTickets() {
        apiService.getMyTickets("CONFIRMED", 0, 10).enqueue(new Callback<PageResponse<Ticket>>() {
            @Override
            public void onResponse(Call<PageResponse<Ticket>> call, Response<PageResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listTickets.clear();
                    listTickets.addAll(response.body().getContent());
                    Log.d(TAG, "Fetched " + listTickets.size() + " confirmed tickets");
                    if (listTickets.isEmpty()) {
                        Toast.makeText(getContext(), "Không có phim đã xem!", Toast.LENGTH_SHORT).show();
                    } else {
                        fetchMovieIdsFromTickets();
                    }
                } else {
                    Toast.makeText(getContext(), "Không tải được vé đã xem, kiểm tra lại!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PageResponse<Ticket>> call, Throwable t) {
                Toast.makeText(getContext(), "Kết nối thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void fetchMovieIdsFromTickets() {
        AtomicInteger pendingShowtimeCalls = new AtomicInteger(listTickets.size());
        for (Ticket ticket : listTickets) {
            if (ticket != null && ticket.getShowtimeId() != null) {
                apiService.getShowtimeById(ticket.getShowtimeId()).enqueue(new Callback<Showtime>() {
                    @Override
                    public void onResponse(Call<Showtime> call, Response<Showtime> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String movieId = response.body().getMovieId();
                            if (movieId != null) {
                                uniqueMovieIds.add(movieId);
                            }
                        }
                        if (pendingShowtimeCalls.decrementAndGet() == 0) {
                            checkAndFetchMovies();
                        }
                    }

                    @Override
                    public void onFailure(Call<Showtime> call, Throwable t) {
                        Log.e(TAG, "Không thể tải được giờ chiếu: " + t.getMessage());
                        if (pendingShowtimeCalls.decrementAndGet() == 0) {
                            checkAndFetchMovies();
                        }
                    }
                });
            } else {
                Log.w(TAG, "Ticket or showtimeId is null, skipping...");
                if (pendingShowtimeCalls.decrementAndGet() == 0) {
                    checkAndFetchMovies();
                }
            }
        }
    }

    private void checkAndFetchMovies() {
        if (!uniqueMovieIds.isEmpty()) {
            fetchMoviesFromIds(new ArrayList<>(uniqueMovieIds));
        } else {
            Toast.makeText(getContext(), "Không tìm thấy phim từ vé đã xem!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Không tìm thấy ID phim duy nhất. List tickets size: " + listTickets.size());
        }
    }

    private void fetchMoviesFromIds(List<String> movieIds) {
        // Tạo list tạm để tránh modify trong lúc iterate
        Log.d(TAG, "=== fetchMoviesFromIds START ===");
        Log.d(TAG, "Number of movie IDs: " + movieIds.size());
        Log.d(TAG, "Movie IDs: " + movieIds.toString());
        if (movieIds.isEmpty()) {
            Log.w(TAG, "No movie IDs to fetch");
            return;
        }

        List<Movie> tempMovies = new ArrayList<>();

        AtomicInteger pendingMovieCalls = new AtomicInteger(movieIds.size());
        for (String movieId : movieIds) {
            apiService.getMovieById(movieId).enqueue(new Callback<Movie>() {
                @Override
                public void onResponse(Call<Movie> call, Response<Movie> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Movie movie = response.body();
                        tempMovies.add(movie);  // Thêm vào list tạm
                        Log.d(TAG, "Đã thêm phim: " + movie.getTitle());
                    }

                    if (pendingMovieCalls.decrementAndGet() == 0) {
                        // Cập nhật list chính sau khi tất cả calls hoàn thành
                        listMovie.clear();
                        listMovie.addAll(tempMovies);

                        Log.d(TAG, "Tổng số phim đã thêm: " + listMovie.size());

                        // Kiểm tra main thread trước khi notify
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            getActivity().runOnUiThread(() -> {
                                mvAdapter.notifyDataSetChanged();
                                Log.d(TAG, "Đã gọi notifyDataSetChanged()");
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    Log.e(TAG, "Failed to fetch movie: " + t.getMessage());
                    if (pendingMovieCalls.decrementAndGet() == 0) {
                        // Vẫn cập nhật với những phim đã lấy được
                        listMovie.clear();
                        listMovie.addAll(tempMovies);

                        Log.d(TAG, "Tổng số phim đã thêm: " + listMovie.size());

                        if (getActivity() != null && !getActivity().isFinishing()) {
                            getActivity().runOnUiThread(() -> {
                                mvAdapter.notifyDataSetChanged();
                                Log.d(TAG, "Đã gọi notifyDataSetChanged()");
                            });
                        }
                    }
                }
            });
        }

        // Trường hợp không có movieId nào
        if (movieIds.isEmpty()) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mvAdapter.notifyDataSetChanged();
                });
            }
        }
    }
    private void updateMovieList(List<Movie> tempMovies) {
        Log.d(TAG, "=== updateMovieList START ===");
        Log.d(TAG, "Temp movies size: " + tempMovies.size());

        if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            getActivity().runOnUiThread(() -> {
                if (!isAdded()) {
                    Log.w(TAG, "Fragment not added, skipping UI update");
                    return;
                }

                listMovie.clear();
                listMovie.addAll(tempMovies);

                Log.d(TAG, "🔍 FINAL DATA:");
                Log.d(TAG, "  - listMovie size: " + listMovie.size());
                for (int i = 0; i < listMovie.size(); i++) {
                    Movie m = listMovie.get(i);
                    Log.d(TAG, "  - Movie " + i + ": " + (m != null ? m.getTitle() : "NULL"));
                }

                if (recyclerView == null) {
                    Log.e(TAG, "❌ recyclerView is NULL!");
                    return;
                }

                if (mvAdapter == null) {
                    Log.e(TAG, "❌ mvAdapter is NULL! Re-creating...");
                    mvAdapter = new WatchedMovieAdapter(listMovie);
                    recyclerView.setAdapter(mvAdapter);
                }

                // Log RecyclerView state trước khi notify
                Log.d(TAG, "🔍 RECYCLERVIEW BEFORE NOTIFY:");
                Log.d(TAG, "  - Width: " + recyclerView.getWidth());
                Log.d(TAG, "  - Height: " + recyclerView.getHeight());
                Log.d(TAG, "  - Adapter item count: " + mvAdapter.getItemCount());
                Log.d(TAG, "  - LayoutManager children count: " +
                        (recyclerView.getLayoutManager() != null ?
                                ((LinearLayoutManager) recyclerView.getLayoutManager()).getChildCount() : "NULL"));

                // Notify và force layout
                mvAdapter.notifyDataSetChanged();

                // Scroll to top
                if (recyclerView.getLayoutManager() != null) {
                    recyclerView.getLayoutManager().scrollToPosition(0);
                }

                // Force request layout
                recyclerView.requestLayout();
                recyclerView.post(() -> {
                    Log.d(TAG, "🔍 RECYCLERVIEW AFTER LAYOUT:");
                    Log.d(TAG, "  - Width: " + recyclerView.getWidth());
                    Log.d(TAG, "  - Height: " + recyclerView.getHeight());
                    Log.d(TAG, "  - Adapter item count: " + mvAdapter.getItemCount());
                    Log.d(TAG, "  - Visible children: " +
                            (recyclerView.getLayoutManager() != null ?
                                    ((LinearLayoutManager) recyclerView.getLayoutManager()).getChildCount() : "NULL"));

                    if (mvAdapter.getItemCount() > 0 &&
                            ((LinearLayoutManager) recyclerView.getLayoutManager()).getChildCount() == 0) {
                        Log.e(TAG, "❌ DATA LOADED BUT NO VIEWS VISIBLE! Height may be too small: " + recyclerView.getHeight());
                    }
                });

                Log.d(TAG, "✅ UI update complete");
            });
        } else {
            Log.e(TAG, "❌ Cannot update UI - Activity null/finishing or fragment not added");
        }
        Log.d(TAG, "=== updateMovieList END ===");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listMovie.clear();
        listTickets.clear();
        uniqueMovieIds.clear();
        mvAdapter.notifyDataSetChanged();
    }
}