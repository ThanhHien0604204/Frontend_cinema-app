package com.ntth.movie_ticket_booking_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.ntth.movie_ticket_booking_app.Activeties.MovieDetailActivity;
import com.ntth.movie_ticket_booking_app.Class.Movie;
import com.ntth.movie_ticket_booking_app.Class.SliderItems;
import com.ntth.movie_ticket_booking_app.R;

import java.util.List;

public class MovieHorizontalAdapter extends RecyclerView.Adapter<MovieHorizontalAdapter.MovieHorizontalViewHolder> {
    private List<SliderItems> bannerList;
    private Context context;

    public MovieHorizontalAdapter(List<SliderItems> bannerList, Context context) {
        this.bannerList = bannerList;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_horizontal, viewGroup, false);
        return new MovieHorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieHorizontalViewHolder movieViewHolder, int i) {
        movieViewHolder.setImage(bannerList.get(i)); // Gọi setImage với SliderItems
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    public class MovieHorizontalViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewBanner;

        public MovieHorizontalViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageViewBanner = itemView.findViewById(R.id.ad_item_image);
        }

        @SuppressWarnings("unchecked")
        void setImage(SliderItems banner) {
            if (banner != null && banner.getImageUrl() != null) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(60));
                Glide.with(context)
                        .load(banner.getImageUrl())
                        .apply(requestOptions)
                        .into(imageViewBanner);
            } else {
                // Xử lý trường hợp null (ví dụ: đặt ảnh mặc định)
                imageViewBanner.setImageResource(R.drawable.thongbaoloi); // Thay bằng ID ảnh mặc định
            }

            imageViewBanner.setOnClickListener(v -> {
                Intent intent = new Intent(context, MovieDetailActivity.class);
                intent.putExtra("imageAdUrl", banner.getImageUrl());
                intent.putExtra("adId", banner.getId());
                context.startActivity(intent);
            });
        }
    }

}
