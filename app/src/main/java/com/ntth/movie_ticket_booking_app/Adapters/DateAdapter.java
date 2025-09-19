package com.ntth.movie_ticket_booking_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Class.DateItem;
import com.ntth.movie_ticket_booking_app.R;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.VH> {
    private final List<DateItem> dates;
    private int selectedPosition = 0; // Default chọn ngày đầu
    private final OnDateClickListener onDateClickListener;

    public interface OnDateClickListener {
        void onDateClick(DateItem dateItem);
    }

    public DateAdapter(List<DateItem> dates, OnDateClickListener listener) {
        this.dates = dates;
        this.onDateClickListener = listener;
        if (!dates.isEmpty()) dates.get(0).setSelected(true); // Default select first
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DateItem item = dates.get(position);
        holder.dayOfWeek.setText(item.getDayOfWeek());
        holder.dateNumber.setText(item.getDateNumber());

        // Highlight nếu selected
        if (item.isSelected()) {
            holder.itemView.setBackgroundColor(0xFFFCC5C0); // Màu đỏ như hình (hoặc custom)
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF); // Trắng
        }

        holder.itemView.setOnClickListener(v -> {
            // Update selected
            dates.get(selectedPosition).setSelected(false);
            selectedPosition = position;
            item.setSelected(true);
            notifyDataSetChanged();
            onDateClickListener.onDateClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView dayOfWeek, dateNumber;
        VH(@NonNull View v) {
            super(v);
            dayOfWeek = v.findViewById(R.id.day_of_week);
            dateNumber = v.findViewById(R.id.date_number);
        }
    }
}
