//package com.ntth.movie_ticket_booking_app.Activeties;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.net.http.SslError;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.view.View;
//import android.webkit.SslErrorHandler;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.widget.Button;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.ntth.movie_ticket_booking_app.R;
//import com.ntth.movie_ticket_booking_app.data.remote.ApiService;
//import com.ntth.movie_ticket_booking_app.data.remote.RetrofitClient;
//import com.ntth.movie_ticket_booking_app.dto.BookingResponse;
//
//import java.util.Locale;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class WebPayActivity extends AppCompatActivity {
//
//    public static final String EXTRA_ORDER_URL = "order_url";
//    public static final String EXTRA_BOOKING_ID = "booking_id";
//
//    private static final boolean DEV_ALLOW_INSECURE_SSL = true;
//
//    private WebView webView;
//    private ProgressBar progress;
//    private TextView tvStatus;
//    private Button btnOpenExternal;
//
//    private String orderUrl;
//    private String bookingId;
//
//    private ScheduledExecutorService pollExec;
//    private ScheduledFuture<?> pollTask;
//    private CountDownTimer failSafeTimer;
//
//    @SuppressLint("SetJavaScriptEnabled")
//    @Override protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_web_pay);
//
//        webView = findViewById(R.id.webView);
//        progress = findViewById(R.id.progress);
//        tvStatus = findViewById(R.id.tvStatus);
//        btnOpenExternal = findViewById(R.id.btnOpenExternal);
//
//        orderUrl = getIntent().getStringExtra(EXTRA_ORDER_URL);
//        bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
//        if (orderUrl == null || bookingId == null) {
//            Toast.makeText(this, "Thiếu dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        WebSettings ws = webView.getSettings();
//        ws.setJavaScriptEnabled(true);
//        ws.setDomStorageEnabled(true);
//        ws.setLoadWithOverviewMode(true);
//        ws.setUseWideViewPort(true);
//
//        webView.setWebViewClient(new WebViewClient() {
//            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                progress.setVisibility(View.VISIBLE);
//                super.onPageStarted(view, url, favicon);
//            }
//            @Override public void onPageFinished(WebView view, String url) {
//                progress.setVisibility(View.GONE);
//                super.onPageFinished(view, url);
//            }
//            @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                if (DEV_ALLOW_INSECURE_SSL) {
//                    handler.proceed();
//                } else {
//                    handler.cancel();
//                    Toast.makeText(WebPayActivity.this, "Lỗi SSL", Toast.LENGTH_SHORT).show();
//                }
//            }
//            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (!url.startsWith("http")) {
//                    try {
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//                    } catch (Exception ignored) {}
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        webView.loadUrl(orderUrl);
//
//        btnOpenExternal.setOnClickListener(v -> {
//            try {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl)));
//            } catch (Exception e) {
//                Toast.makeText(this, "Không mở được trình duyệt", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        startPollingBooking(bookingId);
//
//        failSafeTimer = new CountDownTimer(10 * 60_000L, 1_000L) {
//            public void onTick(long millis) {
//                long s = millis / 1000;
//                tvStatus.setText(String.format(Locale.getDefault(),
//                        "Đang chờ xác nhận (%02d:%02d)...", s/60, s%60));
//            }
//            public void onFinish() {
//                stopPolling();
//                Toast.makeText(WebPayActivity.this, "Quá thời gian chờ. Vui lòng kiểm tra lịch sử vé.", Toast.LENGTH_LONG).show();
//            }
//        }.start();
//    }
//
//    private void startPollingBooking(String id) {
//        stopPolling();
//        pollExec = Executors.newSingleThreadScheduledExecutor();
//        pollTask = pollExec.scheduleAtFixedRate(() -> {
//            ApiService api = RetrofitClient.getInstance().create(ApiService.class);
//            api.getBooking(id).enqueue(new Callback<BookingResponse>() {
//                @Override public void onResponse(Call<BookingResponse> call, Response<BookingResponse> resp) {
//                    if (!resp.isSuccessful() || resp.body() == null) return;
//                    String st = resp.body().status;
//                    if ("CONFIRMED".equalsIgnoreCase(st)) {
//                        runOnUiThread(() -> {
//                            stopPolling();
//                            if (failSafeTimer != null) failSafeTimer.cancel();
//                            Toast.makeText(WebPayActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
////                            Intent i = new Intent(WebPayActivity.this, BillActivity.class);
////                            i.putExtra(BillActivity.EXTRA_BOOKING_ID,
////                                    resp.body().bookingId != null ? resp.body().bookingId : resp.body().id);
////                            startActivity(i);
////                            finish();
//                        });
//                    } else if ("CANCELED".equalsIgnoreCase(st) || "FAILED".equalsIgnoreCase(st)) {
//                        runOnUiThread(() -> {
//                            stopPolling();
//                            if (failSafeTimer != null) failSafeTimer.cancel();
//                            Toast.makeText(WebPayActivity.this, "Thanh toán thất bại/hủy", Toast.LENGTH_LONG).show();
//                            finish();
//                        });
//                    }
//                }
//                @Override public void onFailure(Call<BookingResponse> call, Throwable t) { /* ignore vài lần */ }
//            });
//        }, 2, 2, TimeUnit.SECONDS);
//    }
//
//    private void stopPolling() {
//        if (pollTask != null) { pollTask.cancel(true); pollTask = null; }
//        if (pollExec != null) { pollExec.shutdownNow(); pollExec = null; }
//    }
//
//    @Override protected void onDestroy() {
//        super.onDestroy();
//        stopPolling();
//        if (failSafeTimer != null) failSafeTimer.cancel();
//    }
//
//    @Override public void onBackPressed() {
//        if (webView.canGoBack()) webView.goBack();
//        else super.onBackPressed();
//    }
//}