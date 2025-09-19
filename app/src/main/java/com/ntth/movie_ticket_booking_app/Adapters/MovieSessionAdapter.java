package com.ntth.movie_ticket_booking_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Activeties.SeatPickerActivity;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.dto.ShowtimeResponse;

import java.util.List;
import java.util.Map;

public class MovieSessionAdapter extends RecyclerView.Adapter<MovieSessionAdapter.MovieViewHolder> {

    private List<String> movieIds;
    private Map<String, List<ShowtimeResponse>> showtimeMap;
    private Map<String, String> movieNames;
    private Context context;

    public MovieSessionAdapter(List<String> movieIds, Map<String, List<ShowtimeResponse>> showtimeMap, Map<String, String> movieNames, Context context) {
        this.movieIds = movieIds;
        this.showtimeMap = showtimeMap;
        this.movieNames = movieNames;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        String mId = movieIds.get(position);
        String movieName = movieNames.get(mId);
        if (holder.movieNameTextView != null) { // Kiểm tra null để debug
            holder.movieNameTextView.setText(movieName != null ? movieName : "Không xác định");
        } else {
            Log.e("MovieSessionAdapter", "movieNameTextView is null at position: " + position);
        }

        List<ShowtimeResponse> showtimes = showtimeMap.get(mId);
        if (showtimes != null && holder.showtimeRecyclerView != null) {
            ShowtimeAdapter showtimeAdapter = new ShowtimeAdapter(s -> {
                if (s.getId() == null) {
                    Toast.makeText(context, "Lỗi: Showtime ID không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(context, SeatPickerActivity.class);
                intent.putExtra("showtimeId", s.getId());
                context.startActivity(intent);
            });
            holder.showtimeRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.showtimeRecyclerView.setAdapter(showtimeAdapter);
            showtimeAdapter.submit(showtimes);
        } else {
            Log.e("MovieSessionAdapter", "showtimeRecyclerView is null or showtimes is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return movieIds.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView movieNameTextView;
        RecyclerView showtimeRecyclerView;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            movieNameTextView = itemView.findViewById(R.id.movieNameTextView); // Kiểm tra ID này
            showtimeRecyclerView = itemView.findViewById(R.id.showtimeRecyclerView); // Kiểm tra ID này
            if (movieNameTextView == null || showtimeRecyclerView == null) {
                Log.e("MovieViewHolder", "One or more views are null in item_session.xml");
            }
        }
    }
}