package com.ntth.movie_ticket_booking_app.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Class.Review;
import com.ntth.movie_ticket_booking_app.Class.User;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.AuthToken;
import com.ntth.movie_ticket_booking_app.dto.PublicUserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context mContext;
    private List<Review> mReviews;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.mContext = context;
        this.mReviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = mReviews.get(position);

        holder.commentTime.setText(review.getReviewTime());
        holder.reviewContent.setText(review.getContent());

        // ✅ Set rating cho MaterialRatingBar
        holder.userRating.setStepSize(0.5f);
        holder.userRating.setRating((float) review.getRating());
        holder.userRating.setIsIndicator(true); // Đảm bảo không bấm được

        Log.d("ReviewAdapter", "Review: " + review.getContent() + ", Rating: " + review.getRating());

        // Code lấy tên user giữ nguyên...
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getUserById(review.getUserId()).enqueue(new Callback<PublicUserResponse>() {
            @Override
            public void onResponse(Call<PublicUserResponse> call, Response<PublicUserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.userName.setText(response.body().getUserName());
                } else {
                    holder.userName.setText("Người dùng không xác định");
                }
            }

            @Override
            public void onFailure(Call<PublicUserResponse> call, Throwable t) {
                Log.e("ReviewAdapter", "Lỗi tải tên người dùng: " + t.getMessage(), t);
                holder.userName.setText("Unknown User");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentTime, reviewContent;
        RatingBar userRating; // ✅ THAY ĐỔI: RatingBar thay vì MaterialRatingBar

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            commentTime = itemView.findViewById(R.id.comment_time);
            reviewContent = itemView.findViewById(R.id.review);
            userRating = itemView.findViewById(R.id.user_rating); // ✅ RatingBar
        }
    }
}
