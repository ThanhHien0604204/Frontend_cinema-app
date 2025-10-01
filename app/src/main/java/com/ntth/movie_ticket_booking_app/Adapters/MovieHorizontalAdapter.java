//package com.ntth.movie_ticket_booking_app.Adapters;
//
//import android.content.Context;
//import android.content.Intent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.resource.bitmap.CenterCrop;
//import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
//import com.bumptech.glide.request.RequestOptions;
//import com.ntth.movie_ticket_booking_app.Activeties.MovieDetailActivity;
//import com.ntth.movie_ticket_booking_app.Class.Movie;
//import com.ntth.movie_ticket_booking_app.R;
//
//import java.util.List;
//
//public class MovieHorizontalAdapter extends RecyclerView.Adapter<MovieHorizontalAdapter.MovieHorizontalViewHolder> {
//    private List<Movie> adList;
//    private Context context;
//
//    public MovieHorizontalAdapter(List<Movie> adList, Context context) {
//        this.adList = adList;
//        this.context = context;
//    }
//
//    @NonNull
//    @Override
//    public MovieHorizontalAdapter.MovieHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        context = viewGroup.getContext();
//        return new AdViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
//                R.layout.item_movie_horizontal, viewGroup, false
//        ));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MovieHorizontalAdapter.MovieHorizontalViewHolder movieViewHolder, int i) {
//        movieViewHolder.setImage(adList.get(i));
//    }
//
//    @Override
//    public int getItemCount() {
//        return adList.size();
//    }
//
//    public class AdViewHolder extends RecyclerView.ViewHolder {
//        private ImageView imageViewad;
//
//        public AdViewHolder(@NonNull View itemView) {
//            super(itemView);
//            this.imageViewad = itemView.findViewById(R.id.ad_item_image);
//        }
//
//        @SuppressWarnings("unchecked")
//            void setImage(final Movie ad) {
//            RequestOptions requestOptions = new RequestOptions();
//            requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(60));
//            Glide.with(context)
//                    .load(ad.getImageUrl())
//                    .apply(requestOptions)
//                    .into(imageViewad);
//
//            imageViewad.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(context, MovieDetailActivity.class);
//                    intent.putExtra("imageAdUrl", ad.getImageUrl());
//                    intent.putExtra("adId", ad.getId()); // Truyền ID quảng cáo dưới dạng chuỗi
//                    context.startActivity(intent);
//                }
//            });
//        }
//    }
//
//}
