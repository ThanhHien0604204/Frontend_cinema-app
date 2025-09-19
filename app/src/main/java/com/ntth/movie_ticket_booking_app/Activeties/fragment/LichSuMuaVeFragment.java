package com.ntth.movie_ticket_booking_app.Activeties.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.TicketAdapter;
import com.ntth.movie_ticket_booking_app.Class.Movie;  // Import Movie class nếu cần
import com.ntth.movie_ticket_booking_app.Class.Showtime;  // Import Showtime class nếu cần
import com.ntth.movie_ticket_booking_app.Class.Ticket;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LichSuMuaVeFragment extends Fragment {

    private RecyclerView recyclerView_LichSuMuaVe;
    private TicketAdapter ticketAdapter;
    private List<Ticket> listTickets = new ArrayList<>();
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lich_su_mua_ve, container, false);

        recyclerView_LichSuMuaVe = view.findViewById(R.id.recyclerView_Lich_Su_Ve);
        recyclerView_LichSuMuaVe.setLayoutManager(new LinearLayoutManager(getContext()));
        ticketAdapter = new TicketAdapter(listTickets, getContext());
        recyclerView_LichSuMuaVe.setAdapter(ticketAdapter);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        fetchTicketsFromApi("CONFIRMED");  // Lịch sử mua vé (đã xác nhận)

        return view;
    }

    private void fetchTicketsFromApi(String status) {
        apiService.getMyTickets(status, 0, 10).enqueue(new Callback<PageResponse<Ticket>>() {
            @Override
            public void onResponse(Call<PageResponse<Ticket>> call, Response<PageResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listTickets.clear();
                    listTickets.addAll(response.body().getContent());
                    // Fetch movie info cho từng ticket
                    fetchMovieInfoForTickets(0);
                } else {
                    Toast.makeText(getContext(), "Không tải được vé, kiểm tra lại nha!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<Ticket>> call, Throwable t) {
                Toast.makeText(getContext(), "Kết nối hỏng rồi, thử lại sau nha: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm recursive để fetch movie info sequentially (tránh overload API)
    private void fetchMovieInfoForTickets(int index) {
        if (index >= listTickets.size()) {
            ticketAdapter.notifyDataSetChanged();  // Cập nhật UI sau khi fetch hết
            return;
        }

        Ticket ticket = listTickets.get(index);
        apiService.getShowtimeById(ticket.getShowtimeId()).enqueue(new Callback<Showtime>() {  // Giả định bạn có endpoint getShowtimeById trong ApiService
            @Override
            public void onResponse(Call<Showtime> call, Response<Showtime> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String movieId = response.body().getMovieId();  // Giả định Showtime có getMovieId()
                    apiService.getMovieById(movieId).enqueue(new Callback<Movie>() {
                        @Override
                        public void onResponse(Call<Movie> call, Response<Movie> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Movie movie = response.body();
                                ticket.setMovieName(movie.getTitle());
                                ticket.setMovieImageUrl(movie.getImageUrl());
                            }
                            fetchMovieInfoForTickets(index + 1);  // Tiếp tục ticket tiếp theo
                        }

                        @Override
                        public void onFailure(Call<Movie> call, Throwable t) {
                            fetchMovieInfoForTickets(index + 1);  // Bỏ qua nếu fail
                        }
                    });
                } else {
                    fetchMovieInfoForTickets(index + 1);  // Bỏ qua nếu fail
                }
            }

            @Override
            public void onFailure(Call<Showtime> call, Throwable t) {
                fetchMovieInfoForTickets(index + 1);  // Bỏ qua nếu fail
            }
        });
    }

    public void clearData() {
        listTickets.clear();
        ticketAdapter.notifyDataSetChanged();
    }
}