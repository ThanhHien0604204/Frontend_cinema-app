package com.ntth.movie_ticket_booking_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.dto.SeatResponse;

import java.util.*;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.VH> {

    public interface OnSeatClick {
        void onToggle(String seatCode);
    }

    private final List<SeatResponse> data = new ArrayList<>();
    private final Set<String> selected = new HashSet<>();
    private final OnSeatClick listener;

    public SeatAdapter(OnSeatClick listener) { this.listener = listener; }

    public void setData(List<SeatResponse> seats) {
        data.clear();
        if (seats != null) data.addAll(seats);
        notifyDataSetChanged();
    }

    public void setSelected(Set<String> selectedSeats) {
        selected.clear();
        if (selectedSeats != null) selected.addAll(selectedSeats);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seat, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        SeatResponse s = data.get(i);
        h.tv.setText(s.seat);

        boolean isSelected = selected.contains(s.seat);
        boolean isFree = "FREE".equalsIgnoreCase(s.state);
        boolean isHold = "HOLD".equalsIgnoreCase(s.state);
        boolean isSold = "CONFIRMED".equalsIgnoreCase(s.state);

        if (isSelected) {
            h.tv.setBackgroundResource(R.drawable.seat_selected);
            h.tv.setEnabled(true);
        } else if (isFree) {
            h.tv.setBackgroundResource(R.drawable.seat_background); // ghế trống
            h.tv.setEnabled(true);
        } else if (isHold) {
            h.tv.setBackgroundResource(R.drawable.seat_hold); // tạo shape xám/vàng nhạt
            h.tv.setEnabled(false);
        } else if (isSold) {
            h.tv.setBackgroundResource(R.drawable.tron_mau); // ghế đã mua (đỏ)
            h.tv.setEnabled(false);
        } else {
            h.tv.setBackgroundResource(R.drawable.seat_background);
            h.tv.setEnabled(true);
        }

        h.itemView.setOnClickListener(v -> {
            if (!"FREE".equalsIgnoreCase(s.state)) return; // chỉ cho click FREE
            if (selected.contains(s.seat)) selected.remove(s.seat);
            else selected.add(s.seat);
            notifyItemChanged(i);
            if (listener != null) listener.onToggle(s.seat);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(View v) { super(v); tv = v.findViewById(R.id.tvSeat); }
    }
}

