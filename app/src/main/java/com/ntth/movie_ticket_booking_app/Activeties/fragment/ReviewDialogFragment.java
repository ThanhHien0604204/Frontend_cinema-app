package com.ntth.movie_ticket_booking_app.Activeties.fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.ntth.movie_ticket_booking_app.Activeties.MovieDetailActivity;
import com.ntth.movie_ticket_booking_app.Class.Review;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.ReviewRequest;
import com.ntth.movie_ticket_booking_app.dto.ReviewResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Query;

public class ReviewDialogFragment extends DialogFragment {
    private RatingBar ratingBar;
    private EditText reviewEditText;
    private Button cancelButton;
    private Button confirmButton;

    private String movieId;
    private ReviewResponse existingReview;

    public ReviewDialogFragment(String movieId) {
        this.movieId = movieId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_review, null);

        ratingBar = view.findViewById(R.id.ratingBar);
        reviewEditText = view.findViewById(R.id.reviewEditText);
        cancelButton = view.findViewById(R.id.cancelButton);
        confirmButton = view.findViewById(R.id.confirmButton);

        cancelButton.setOnClickListener(v -> dismiss());
        confirmButton.setOnClickListener(v -> submitReview());

        // Kiểm tra xem người dùng đã có bình luận trước đó chưa
        checkIfUserHasReviewed();

        builder.setView(view);
        return builder.create();
    }

    private void checkIfUserHasReviewed() {
        ApiService api = RetrofitClient.api();
        api.getMyReviewForMovie(movieId).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    existingReview = response.body();
                    ratingBar.setRating((float) existingReview.getRating());
                    reviewEditText.setText(existingReview.getContent());
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Log.e("ReviewDialog", "Lỗi kiểm tra review: " + t.getMessage(), t);
            }
        });
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String reviewText = reviewEditText.getText().toString();

        if (rating == 0 || reviewText.isEmpty()) {
            Toast.makeText(getActivity(), "Please provide a rating and review", Toast.LENGTH_SHORT).show();
            return;
        }

        ReviewRequest reviewRequest = new ReviewRequest(movieId,(double) rating, reviewText);
        ApiService api = RetrofitClient.api();
        api.upsertReview(reviewRequest).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Đánh giá đã được gửi thành công", Toast.LENGTH_SHORT).show();
                    ((MovieDetailActivity) getActivity()).updateMovieRating(movieId);
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "Không thể gửi đánh giá: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}