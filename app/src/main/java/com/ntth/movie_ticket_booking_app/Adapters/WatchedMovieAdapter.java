package com.ntth.movie_ticket_booking_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.ntth.movie_ticket_booking_app.Activeties.MovieDetailActivity;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.R;

import java.util.List;

public class WatchedMovieAdapter extends RecyclerView.Adapter<WatchedMovieAdapter.WatchedMovieViewHolder> {

    private List<Movie> movieList;
    private Context context;

    public WatchedMovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public WatchedMovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.items_movie_doc_listmovie, viewGroup, false);
        return new WatchedMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchedMovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        Log.d("Adapter", "Phim liên kết ở vị trí " + position + ": " + (movie != null ? movie.getTitle() : "null"));

        holder.setImage(movie);
        holder.tvTitle.setText(movie.getTitle() != null ? movie.getTitle() : "N/A");
        holder.tvDuration.setText("Thời lượng: " + (movie.getDurationMinutes() != null ? movie.getDurationMinutes() + " phút" : "N/A"));
        holder.tvShowTime.setText("Khởi chiếu: " + (movie.getMovieDateStart() != null ? movie.getMovieDateStart().toString() : "N/A"));

        // Thiết lập nút "Đã lên lịch" - disabled
        holder.btnBooking.setText("Đã lên lịch");
        holder.btnBooking.setBackgroundResource(R.color.prink_nhat);
        holder.btnBooking.setEnabled(false);
        holder.btnBooking.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class WatchedMovieViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvDuration, tvShowTime;
        private Button btnBooking;
        private ImageView imageViewmv;

        public WatchedMovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvShowTime = itemView.findViewById(R.id.tvShowTime);
            imageViewmv = itemView.findViewById(R.id.imgPoster);
            btnBooking = itemView.findViewById(R.id.btnBooking);
        }

        void setImage(final Movie movie) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(60));
            Glide.with(itemView.getContext())
                    .load(movie.getImageUrl())
                    .apply(requestOptions)
                    .into(imageViewmv);

            imageViewmv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                    intent.putExtra("imageMovieUrl", movie.getImageUrl());
                    intent.putExtra("movieId", movie.getId());
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}