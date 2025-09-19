package com.ntth.movie_ticket_booking_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.ntth.movie_ticket_booking_app.Activeties.MovieDetailActivity;
import com.ntth.movie_ticket_booking_app.Class.SliderItems;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SliderAdapters extends RecyclerView.Adapter<SliderAdapters.SliderViewHolder> {
    private List<SliderItems> sliderItems;

//    private ViewPager2 viewPager2;
//    private Context context;

//    public SlderAdapters(ViewPager2 viewPager2, List<SliderItems> sliderItems) {
//        this.viewPager2 = viewPager2;
//        this.sliderItems = sliderItems;
//    }

    public SliderAdapters(List<SliderItems> sliderItems) {
        this.sliderItems = sliderItems;
    }
    public void setData(List<SliderItems> items) {
        this.sliderItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.slider_item_container,viewGroup,false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder sliderViewHolder, int i) {
        SliderItems item = sliderItems.get(i);
        int radiusPx = dp(sliderViewHolder.itemView.getContext(), 26);   // dùng context từ holder

        Glide.with(sliderViewHolder.itemView.getContext())
                .load(item.getImageUrl())
                .transform(new CenterCrop(), new RoundedCorners(radiusPx)) // <-- bo góc ở bitmap
                .placeholder(R.drawable.load) // thêm ảnh vào drawable
                .error(R.drawable.thongbaoloi)      // thêm ảnh vào drawable
                .into(sliderViewHolder.imageView);
        // onClick để gọi API incView và mở MovieDetailActivity
        sliderViewHolder.itemView.setOnClickListener(v -> {
            String movieId = item.getId();  // Giả sử SliderItems có getId()
            if (movieId == null || movieId.isEmpty()) {
                Log.e("SliderAdapter", "Movie ID is null or empty for slider item");
                Toast.makeText(v.getContext(), "Không thể mở chi tiết phim", Toast.LENGTH_SHORT).show();
                return;
            }

            Context context = v.getContext();
            ApiService api = RetrofitClient.api();
            api.incMovieView(movieId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("SliderAdapter", "Lượt xem phim đã tăng thành công: " + movieId);
                    } else {
                        Log.w("SliderAdapter", "Không thể tăng lượt xem: " + response.code());
                        // Vẫn mở activity dù fail
                    }
                    openMovieDetail(context, movieId);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("SliderAdapter", "Error increasing views: " + t.getMessage(), t);
                    // Vẫn mở activity dù fail
                    openMovieDetail(context, movieId);
                }
            });
        });
    }
    private void openMovieDetail(Context context, String movieId) {
        Intent intent = new Intent(context, MovieDetailActivity.class);
        intent.putExtra("movieId", movieId);
        context.startActivity(intent);
    }
    private static int dp (Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return sliderItems == null ? 0 : sliderItems.size();
    }

    public class SliderViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {// itemsView toàn bộ view của item trong RecyclerVie
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }
    }

//    private Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            sliderItems.addAll(sliderItems);
//            notifyDataSetChanged();
//        }
//    };
}
