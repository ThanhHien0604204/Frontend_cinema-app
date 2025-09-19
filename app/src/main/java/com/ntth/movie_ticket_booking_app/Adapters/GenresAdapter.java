package com.ntth.movie_ticket_booking_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Class.Genre;
import com.ntth.movie_ticket_booking_app.R;

import java.util.ArrayList;
import java.util.List;

public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.VH> {
    private final List<Genre> data = new ArrayList<>();
    private OnGenreClickListener onGenreClickListener;

    public interface OnGenreClickListener {
        void onGenreClick(Genre genre);
    }
    public void setOnGenreClickListener(OnGenreClickListener listener) {
        this.onGenreClickListener = listener;
    }

    public void setData(List<Genre> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chip_genre, parent, false); // tạo layout chip
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Genre genre = data.get(i);
        h.tv.setText(data.get(i).getName());
        h.itemView.setOnClickListener(v -> {
            if (onGenreClickListener != null) {
                onGenreClickListener.onGenreClick(genre);
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvGenreName);
        }
    }
}
