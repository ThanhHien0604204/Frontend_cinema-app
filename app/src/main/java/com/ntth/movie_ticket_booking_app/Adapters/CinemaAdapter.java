package com.ntth.movie_ticket_booking_app.Adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.ntth.movie_ticket_booking_app.Class.Cinema;
import com.ntth.movie_ticket_booking_app.R;
import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {

    private List<Cinema> locationList;
    private OnLocationClickListener listener;

    public interface OnLocationClickListener {
        void onLocationClick(Cinema location);
    }

    public CinemaAdapter(List<Cinema> locationList, OnLocationClickListener listener) {
        this.locationList = locationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema location = locationList.get(position);
        holder.name.setText(location.getName());
        holder.address.setText(location.getAddress());

        holder.itemView.setOnClickListener(v -> listener.onLocationClick(location));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class CinemaViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.address_name);
            address = itemView.findViewById(R.id.address_detail);
        }
    }
}
