package com.ntth.movie_ticket_booking_app.Activeties.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.ListMovieVerticalAdapter;
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
    private ListMovieVerticalAdapter mvAdapter;
    private List<Movie> listMovie = new ArrayList<>();
    private List<Ticket> listTickets = new ArrayList<>();
    private ProgressBar progressBar;
    private ApiService apiService;
    private Set<String> uniqueMovieIds = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phim_da_xem, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_phim_Da_Xem);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mvAdapter = new ListMovieVerticalAdapter(listMovie);
        recyclerView.setAdapter(mvAdapter);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Hiển thị ProgressBar và bắt đầu fetch
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "ProgressBar not found in layout!");
        }
        fetchWatchedTickets();

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
                        hideProgressBar();
                        Toast.makeText(getContext(), "Không có phim đã xem!", Toast.LENGTH_SHORT).show();
                    } else {
                        fetchMovieIdsFromTickets();
                    }
                } else {
                    hideProgressBar();
                    Toast.makeText(getContext(), "Không tải được vé đã xem, kiểm tra lại!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PageResponse<Ticket>> call, Throwable t) {
                hideProgressBar();
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
            hideProgressBar();
            Toast.makeText(getContext(), "Không tìm thấy phim từ vé đã xem!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Không tìm thấy ID phim duy nhất. List tickets size: " + listTickets.size());
        }
    }

    private void fetchMoviesFromIds(List<String> movieIds) {
        AtomicInteger pendingMovieCalls = new AtomicInteger(movieIds.size());
        for (String movieId : new ArrayList<>(movieIds)) {
            apiService.getMovieById(movieId).enqueue(new Callback<Movie>() {
                @Override
                public void onResponse(Call<Movie> call, Response<Movie> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        listMovie.add(response.body());
                        Log.d(TAG, "Đã thêm phim: " + response.body().getTitle());
                    } else {
                        Log.w(TAG, "Failed to fetch movie with id: " + movieId + ", code: " + response.code());
                    }
                    if (pendingMovieCalls.decrementAndGet() == 0) {
                        hideProgressBar();
                        // Debug: Kiểm tra số lượng phim và notify
                        Log.d(TAG, "Tổng số phim đã thêm: " + listMovie.size());
                        mvAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    Log.e(TAG, "Failed to fetch movie: " + t.getMessage());
                    if (pendingMovieCalls.decrementAndGet() == 0) {
                        hideProgressBar();
                        mvAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        } else {
            Log.w(TAG, "ProgressBar is null, cannot hide!");
        }
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