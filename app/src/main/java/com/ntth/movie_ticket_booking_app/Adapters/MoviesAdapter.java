package com.ntth.movie_ticket_booking_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ntth.movie_ticket_booking_app.Activeties.MovieDetailActivity;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.VH> {
    private final List<Movie> data = new ArrayList<>();
    private OnMovieClick listener;


    private final float density;

    public MoviesAdapter(android.content.Context ctx) {
        this.density = ctx.getResources().getDisplayMetrics().density;
    }

    public interface OnMovieClick { void onClick(Movie m); }
    public void setOnMovieClick(OnMovieClick l) { this.listener = l; }

    public void setData(List<Movie> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
            // Log dữ liệu để debug
            for (Movie m : data) {
                Log.d("MoviesAdapter", "Movie: " + m.getTitle() + ", ID: " + m.getId());
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_topmovie, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        if (pos >= 0 && pos < data.size()) {
            Movie m = data.get(pos);

            com.bumptech.glide.Glide.with(h.itemView.getContext())
                    .load(m.getImageUrl())
                    .transform(new com.bumptech.glide.load.resource.bitmap.CenterCrop(),
                            new com.bumptech.glide.load.resource.bitmap.RoundedCorners((int) dp(12)))
                    .placeholder(R.drawable.load)
                    .error(R.drawable.thongbaoloi)
                    .into(h.imageTopMovie);

            h.titleMovie.setText(m.getTitle() != null ? m.getTitle() : "No Title");
            h.tvViews.setText("🔥 " + formatViews(m.getViews() != null ? m.getViews() : 0L));
            // onClick để gọi API incView và mở MovieDetailActivity
            h.itemView.setOnClickListener(v -> {
                String movieId = m.getId();
                if (movieId == null || movieId.isEmpty()) {
                    Log.e("MoviesAdapter", "ID phim là null hoặc trống: " + m.getTitle());
                    Toast.makeText(v.getContext(), "Không thể mở chi tiết phim: ID không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("MoviesAdapter", "Sending movieId: " + movieId);

                Context context = v.getContext();
                ApiService api = RetrofitClient.api();
                api.incMovieView(movieId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("MoviesAdapter", "Lượt xem phim đã tăng thành công: " + movieId);
                        } else {
                            Log.w("MoviesAdapter", "Không thể tăng lượt xem: " + response.code());
                            // Vẫn mở activity dù fail
                        }
                        openMovieDetail(context, movieId);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("MoviesAdapter", "Error increasing views: " + t.getMessage(), t);
                        // Vẫn mở activity dù fail
                        openMovieDetail(context, movieId);
                    }
                });
            });
        }
    }private void openMovieDetail(Context context, String movieId) {
        Intent intent = new Intent(context, MovieDetailActivity.class);
        intent.putExtra("movieId", movieId);
        context.startActivity(intent);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imageTopMovie;
        TextView titleMovie, tvViews;
        VH(@NonNull View itemView) {
            super(itemView);
            imageTopMovie = itemView.findViewById(R.id.imageTopMovie);
            titleMovie    = itemView.findViewById(R.id.titleMovie);
            tvViews       = itemView.findViewById(R.id.tvViews);
        }
    }
    private int dp(int d) { return Math.round(density * d); }
    private String formatViews(long v) {
        if (v >= 1_000_000) return String.format(java.util.Locale.US, "%.1fm", v/1_000_000f);
        if (v >= 1_000)     return String.format(java.util.Locale.US, "%.1fk", v/1_000f);
        return String.valueOf(v);
    }
}