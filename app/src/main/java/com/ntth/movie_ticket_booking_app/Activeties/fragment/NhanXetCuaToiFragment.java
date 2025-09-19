package com.ntth.movie_ticket_booking_app.Activeties.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ntth.movie_ticket_booking_app.Adapters.ReviewAdapter;
import com.ntth.movie_ticket_booking_app.Class.Review;
import com.ntth.movie_ticket_booking_app.R;
import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
import com.ntth.movie_ticket_booking_app.dto.PageResponse;
import com.ntth.movie_ticket_booking_app.dto.ReviewResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the factory method to create an instance of this fragment.
 */
public class NhanXetCuaToiFragment extends Fragment {

    // Parameters for fragment initialization
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> listReview = new ArrayList<>();
    private ApiService api;

    public NhanXetCuaToiFragment() {
        // Required empty public constructor
    }

    /**
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NhanXetCuaToiFragment.
     */
    public static NhanXetCuaToiFragment newInstance(String param1, String param2) {
        NhanXetCuaToiFragment fragment = new NhanXetCuaToiFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Khởi tạo ApiService từ Retrofit
        api = RetrofitClient.getInstance().create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Nạp layout cho fragment
        View view = inflater.inflate(R.layout.fragment_nhan_xet_cua_toi, container, false);

        // Thiết lập RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView_NhanXet);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter(getContext(), listReview);
        recyclerView.setAdapter(reviewAdapter);

        fetchReviewsFromApi();
        return view;
    }

    private void fetchReviewsFromApi() {
        // Gọi API getMyReviews với trang 0 và kích thước trang 10
        Call<PageResponse<ReviewResponse>> call = api.getMyReviews(0, 10);
        call.enqueue(new Callback<PageResponse<ReviewResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ReviewResponse>> call, Response<PageResponse<ReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listReview.clear();
                    List<ReviewResponse> reviewResponses = response.body().getContent();
                    // Ánh xạ ReviewResponse sang Review (giả sử ReviewResponse có các trường tương ứng)
                    for (ReviewResponse reviewResponse : reviewResponses) {
                        Review review = new Review();
                        // Ánh xạ các trường từ ReviewResponse sang Review
                        review.setId(reviewResponse.getId());
                        review.setMovieId(reviewResponse.getMovieId());
                        review.setUserId(reviewResponse.getUserId());
                        review.setRating(reviewResponse.getRating());
                        review.setContent(reviewResponse.getContent());
                        review.setReviewTime(reviewResponse.getReviewTime());
                        // Thêm các trường khác nếu cần
                        listReview.add(review);
                    }
                    // Cập nhật adapter để hiển thị dữ liệu mới
                    reviewAdapter.notifyDataSetChanged();
                } else {
                    Log.e("ApiError", "Lỗi khi lấy dữ liệu đánh giá: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ReviewResponse>> call, Throwable t) {
                Log.e("ApiError", "Lỗi kết nối API: " + t.getMessage());
            }
        });
    }
}
