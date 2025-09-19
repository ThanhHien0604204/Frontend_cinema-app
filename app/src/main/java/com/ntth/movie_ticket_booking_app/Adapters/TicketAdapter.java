package com.ntth.movie_ticket_booking_app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Class.Ticket;
import com.ntth.movie_ticket_booking_app.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {

    private List<Ticket> ticketList;
    private Context context;

    public TicketAdapter(List<Ticket> ticketList, Context context) {
        this.ticketList = ticketList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        // Ảnh phim
        if (ticket.getMovieImageUrl() != null) {
            Glide.with(context).load(ticket.getMovieImageUrl()).into(holder.ivMovieImage);
        } else {
            holder.ivMovieImage.setImageResource(R.drawable.thongbaoloi);
        }

        // Mã vé
        holder.tvTicketCode.setText(ticket.getBookingCode());

        // Tên phim
        holder.tvMovieName.setText(ticket.getMovieName() != null ? ticket.getMovieName() : "Tên phim chưa có");

        // Giá tiền
        holder.tvPrice.setText(String.format("%,d đ", ticket.getAmount()));

        // Số lượng vé (dựa trên kích thước của seats)
        int ticketQuantity = ticket.getSeats() != null ? ticket.getSeats().size() : 0;
        holder.tvTicketQuantity.setText("Số lượng: " + ticketQuantity + " vé");

        // Trạng thái
        holder.tvStatus.setText(ticket.getStatus().equals("CANCELED") ? "Hủy" : "Đã xác nhận");
        holder.tvStatus.setTextColor(ticket.getStatus().equals("CANCELED")
                ? context.getResources().getColor(android.R.color.holo_red_dark)
                : context.getResources().getColor(android.R.color.holo_green_dark));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMovieImage;
        TextView tvTicketCode, tvMovieName, tvPrice, tvStatus, tvTicketQuantity;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMovieImage = itemView.findViewById(R.id.imgPoster);
            tvTicketCode = itemView.findViewById(R.id.txtMaVe);
            tvMovieName = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.txtPrice);
            tvStatus = itemView.findViewById(R.id.txtStatus);
            tvTicketQuantity = itemView.findViewById(R.id.txtSoLuongVe);
        }
    }
}