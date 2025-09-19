package com.ntth.movie_ticket_booking_app.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.R;

import java.util.List;

public class ProvinceAdapter extends RecyclerView.Adapter<ProvinceAdapter.ProvinceViewHolder> {

    private List<String> provinceList;
    private OnProvinceClickListener listener;

    public interface OnProvinceClickListener {
        void onProvinceClick(String province);
    }

    public ProvinceAdapter(List<String> provinceList, OnProvinceClickListener listener) {
        this.provinceList = provinceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProvinceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_province, parent, false);  // Sử dụng layout item_province.xml
        return new ProvinceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProvinceViewHolder holder, int position) {
        String province = provinceList.get(position);
        holder.provinceName.setText(province);
        Log.d("ProvinceAdapter", "Hiển thị: " + province); // Kiểm tra từng item
        holder.itemView.setOnClickListener(v -> listener.onProvinceClick(province));
    }

    @Override
    public int getItemCount() {
        return provinceList.size();
    }

    public static class ProvinceViewHolder extends RecyclerView.ViewHolder {
        TextView provinceName;

        public ProvinceViewHolder(@NonNull View itemView) {
            super(itemView);
            provinceName = itemView.findViewById(R.id.province_name);  // Giả sử ID trong item_province.xml
        }
    }
}
