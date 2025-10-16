package com.ntth.movie_ticket_booking_app.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private ApiService api = RetrofitClient.api();
    private String currentUserId;
    private boolean isMyReviews; // Flag để biết chế độ (true: NhanXetCuaToiFragment, false: MovieDetailActivity)

    public ReviewAdapter(Context context, List<Review> reviews,boolean isMyReviews) {
        this.mContext = context;
        this.mReviews = reviews;
        this.isMyReviews = isMyReviews;
        SharedPreferences prefs = context.getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        this.currentUserId = prefs.getString("userId", null); // Giả sử userId được lưu sau đăng nhập

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
        // Luôn hiển thị nút xóa ở cả hai nơi
        holder.btnDelete.setVisibility(View.VISIBLE);
        holder.btnDelete.setOnClickListener(v -> handleDeleteClick(position));
    }
    private void handleDeleteClick(int position) {
        Review review = mReviews.get(position);

        if (isMyReviews) {
            // Chế độ NhanXetCuaToiFragment: Luôn hiển thị dialog xác nhận và cho phép xóa
            showDeleteConfirmationDialog(position);
        } else {
            // Chế độ MovieDetailActivity: Kiểm tra userId
            if (currentUserId != null && currentUserId.equals(review.getUserId())) {
                showDeleteConfirmationDialog(position);
            } else {
                //showCannotDeleteDialog("Không thể xóa đánh giá này.");
                showDeleteConfirmationDialog(position);
            }
        }
    }
    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CustomAlertDialogStyle);
        builder.setTitle("Xác nhận xóa")
                .setMessage("Bạn có muốn xóa đánh giá này không?")
                .setPositiveButton("OK", (dialog, which) -> deleteReview(position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(android.R.color.holo_red_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    private void deleteReview(int position) {
        Review review =mReviews.get(position);
        api.deleteReview(review.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    mReviews.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mReviews.size());
                    Toast.makeText(mContext, "Đánh giá đã được xóa", Toast.LENGTH_SHORT).show();
                } else {
                    int code = response.code();
                    if (code == 403) {
                        showCannotDeleteDialog("Bạn không có quyền xóa đánh giá này.");
                    } else if (code == 404) {
                        showCannotDeleteDialog("Đánh giá không tồn tại.");
                    } else if (code == 500) {
                        showCannotDeleteDialog("Lỗi máy chủ, không thể xóa đánh giá.");
                    } else {
                        Toast.makeText(mContext, "Lỗi xóa đánh giá: " + code, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(mContext, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCannotDeleteDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CustomAlertDialogStyle);
        builder.setTitle("Thông báo")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());


        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(android.R.color.holo_red_dark));
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentTime, reviewContent;
        RatingBar userRating; // ✅ THAY ĐỔI: RatingBar thay vì MaterialRatingBar
        Button btnDelete;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            commentTime = itemView.findViewById(R.id.comment_time);
            reviewContent = itemView.findViewById(R.id.review);
            userRating = itemView.findViewById(R.id.user_rating); // ✅ RatingBar
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
