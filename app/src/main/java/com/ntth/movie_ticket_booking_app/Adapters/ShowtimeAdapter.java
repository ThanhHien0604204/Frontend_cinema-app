package com.ntth.movie_ticket_booking_app.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.dto.ShowtimeResponse;

import java.util.ArrayList;
import java.util.List;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.VH> {

    public interface OnClick {
        void onClick(ShowtimeResponse s);
    }

    private final List<ShowtimeResponse> data = new ArrayList<>();
    private final OnClick onClick;

    public ShowtimeAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<ShowtimeResponse> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_session, parent, false); // Thay R.layout.item_showtime bằng item_movie_session.xml để hộp tím
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ShowtimeResponse s = data.get(pos);
        Log.d("ShowtimeAdapter", "Binding item: " + s.sessionName);

        String sessionName = (s.sessionName != null && !s.sessionName.isBlank()) ? s.sessionName: (s.id);
        h.sessionNameTextView.setText(sessionName);

        String startHM = s.getFormattedStartTime();
        String endHM = s.getFormattedEndTime();
        h.timeTextView.setText(startHM + " - " + endHM);

        int avail = (s.availableSeats != null) ? s.availableSeats : 0;
        h.availableSeatsTextView.setText(String.valueOf(avail)); // Thay "Còn " + avail + " ghế" thành chỉ số ghế như ảnh

        h.itemView.setOnClickListener(v -> {
            if (onClick != null && s.getId() != null) {
                onClick.onClick(s);
            } else {
                Log.e("ShowtimeAdapter", "Showtime ID is null for position: " + pos);
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView sessionNameTextView, timeTextView, availableSeatsTextView;
        VH(@NonNull View v) {
            super(v);
            sessionNameTextView = v.findViewById(R.id.sessionNameTextView);
            timeTextView = v.findViewById(R.id.timeTextView);
            availableSeatsTextView = v.findViewById(R.id.availableSeatsTextView);
        }
    }
}