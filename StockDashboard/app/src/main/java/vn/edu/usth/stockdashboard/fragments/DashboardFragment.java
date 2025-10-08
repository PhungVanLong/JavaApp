package vn.edu.usth.stockdashboard.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.StockItem;
import vn.edu.usth.stockdashboard.adapter.StockAdapter;
import vn.edu.usth.stockdashboard.data.model.StockApiResponse;
import vn.edu.usth.stockdashboard.data.service.RetrofitClient;
import vn.edu.usth.stockdashboard.data.service.StockApiService;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    // ===== THỜI GIAN TỰ ĐỘNG CẬP NHẬT =====
    private static final long AUTO_REFRESH_INTERVAL = 5000; // 5 giây

    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private List<StockItem> stockList;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    // ===== AUTO REFRESH =====
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;
    private boolean isAutoRefreshRunning = false;

    // Danh sách mã cổ phiếu
    private List<String> symbols = Arrays.asList(
            "ACB", "SSI", "VPB", "HPG", "VCB",
            "FPT", "VIC", "MSN", "MWG", "TCB"
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo views
        recyclerView = view.findViewById(R.id.recyclerView_db);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        stockList = new ArrayList<>();

        stockAdapter = new StockAdapter(stockList, item -> {
            Toast.makeText(requireContext(),
                    "Clicked: " + item.getSymbol(),
                    Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(stockAdapter);

        // Setup SwipeRefresh
        swipeRefreshLayout.setOnRefreshListener(this::manualRefresh);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // ===== SETUP AUTO REFRESH =====
        setupAutoRefresh();

        // Load dữ liệu lần đầu
        loadInitialData();
    }

    // ===== SETUP AUTO REFRESH MECHANISM =====
    private void setupAutoRefresh() {
        // Tạo Handler trên Main Thread
        autoRefreshHandler = new Handler(Looper.getMainLooper());

        // Tạo Runnable - code sẽ chạy lặp lại
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Kiểm tra Fragment vẫn còn attached
                if (isAdded() && isAutoRefreshRunning) {

                    Log.d(TAG, "🔄 Auto refreshing... (" +
                            new java.text.SimpleDateFormat("HH:mm:ss")
                                    .format(new java.util.Date()) + ")");

                    // GỌI API ĐỂ CẬP NHẬT DỮ LIỆU
                    updateDataSilently();

                    // LÊN LỊCH CHẠY LẠI SAU 5 GIÂY
                    autoRefreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL);
                }
            }
        };
    }

    // ===== BẮT ĐẦU AUTO REFRESH =====
    private void startAutoRefresh() {
        if (!isAutoRefreshRunning) {
            isAutoRefreshRunning = true;

            // Bắt đầu sau 5 giây (sau lần load đầu tiên)
            autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL);

            Log.d(TAG, "✅ Auto refresh STARTED");
        }
    }

    // ===== DỪNG AUTO REFRESH =====
    private void stopAutoRefresh() {
        if (isAutoRefreshRunning) {
            isAutoRefreshRunning = false;

            if (autoRefreshHandler != null && autoRefreshRunnable != null) {
                autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
            }

            Log.d(TAG, "⏸️ Auto refresh STOPPED");
        }
    }

    // ===== LOAD DỮ LIỆU LẦN ĐẦU =====
    private void loadInitialData() {
        if (!isAdded()) return;

        showLoading(true);

        fetchStockData(new FetchCallback() {
            @Override
            public void onComplete(List<StockItem> items) {
                if (!isAdded()) return;

                // Cập nhật UI lần đầu
                stockList.clear();
                stockList.addAll(items);
                stockAdapter.notifyDataSetChanged();

                showLoading(false);

                // BẮT ĐẦU AUTO REFRESH SAU KHI LOAD XONG
                startAutoRefresh();

                Log.d(TAG, "✅ Initial data loaded. Auto refresh enabled.");
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                showLoading(false);
                showError("Không thể tải dữ liệu: " + error);
            }
        });
    }

    // ===== CẬP NHẬT DỮ LIỆU ÂM THẦM (BACKGROUND) =====
    private void updateDataSilently() {
        if (!isAdded()) return;

        // KHÔNG hiển thị loading
        // KHÔNG clear danh sách
        // Chỉ fetch và update âm thầm

        fetchStockData(new FetchCallback() {
            @Override
            public void onComplete(List<StockItem> newItems) {
                if (!isAdded()) return;

                // CẬP NHẬT UI MƯỢT MÀ
                updateUISmooth(newItems);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Auto refresh error: " + error);
                // KHÔNG hiển thị toast cho user (để không làm phiền)
            }
        });
    }

    // ===== CẬP NHẬT UI MƯỢT MÀ =====
    private void updateUISmooth(List<StockItem> newItems) {
        if (!isAdded() || newItems == null || newItems.isEmpty()) return;

        // Tạo map để tra cứu nhanh
        java.util.HashMap<String, StockItem> newItemsMap = new java.util.HashMap<>();
        for (StockItem item : newItems) {
            newItemsMap.put(item.getSymbol(), item);
        }

        // Cập nhật từng item
        for (int i = 0; i < stockList.size(); i++) {
            StockItem oldItem = stockList.get(i);
            StockItem newItem = newItemsMap.get(oldItem.getSymbol());

            if (newItem != null) {
                // Kiểm tra xem có thay đổi không
                if (hasDataChanged(oldItem, newItem)) {
                    // Thay thế item cũ bằng item mới
                    stockList.set(i, newItem);

                    // Chỉ notify item này (không reload toàn bộ)
                    stockAdapter.notifyItemChanged(i, newItem);

                    // Thêm hiệu ứng flash
                    flashItem(i);

                    Log.d(TAG, "📊 Updated: " + newItem.getSymbol() +
                            " → " + newItem.getPrice() +
                            " (" + String.format("%+.2f", newItem.getChange()) + ")");
                }
            }
        }
    }

    // ===== KIỂM TRA DỮ LIỆU CÓ THAY ĐỔI =====
    private boolean hasDataChanged(StockItem oldItem, StockItem newItem) {
        return oldItem.getPrice() != newItem.getPrice() ||
                oldItem.getChange() != newItem.getChange() ||
                oldItem.getVolume() != newItem.getVolume();
    }

    // ===== HIỆU ỨNG FLASH KHI CẬP NHẬT =====
    private void flashItem(int position) {
        recyclerView.post(() -> {
            RecyclerView.ViewHolder holder =
                    recyclerView.findViewHolderForAdapterPosition(position);

            if (holder != null) {
                View itemView = holder.itemView;

                // Flash animation
                itemView.setAlpha(0.3f);
                itemView.animate()
                        .alpha(1.0f)
                        .setDuration(300)
                        .start();
            }
        });
    }

    // ===== FETCH DỮ LIỆU TỪ API =====
    private void fetchStockData(FetchCallback callback) {
        if (!isAdded()) return;

        StockApiService api = RetrofitClient.getInstance().getApi();

        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalRequests = symbols.size();

        List<StockItem> fetchedItems = new ArrayList<>();

        for (String symbol : symbols) {
            api.getStockPrice(symbol).enqueue(new Callback<StockApiResponse>() {
                @Override
                public void onResponse(Call<StockApiResponse> call,
                                       Response<StockApiResponse> response) {
                    if (!isAdded()) return;

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            StockApiResponse apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                StockItem item = StockItem.fromApiResponse(apiResponse);

                                synchronized (fetchedItems) {
                                    fetchedItems.add(item);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing " + symbol + ": " + e.getMessage());
                    }

                    // Khi tất cả requests hoàn thành
                    if (completedRequests.incrementAndGet() == totalRequests) {
                        if (isAdded()) {
                            if (!fetchedItems.isEmpty()) {
                                callback.onComplete(fetchedItems);
                            } else {
                                callback.onError("No data received");
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<StockApiResponse> call, Throwable t) {
                    if (!isAdded()) return;

                    if (completedRequests.incrementAndGet() == totalRequests) {
                        if (isAdded()) {
                            if (!fetchedItems.isEmpty()) {
                                callback.onComplete(fetchedItems);
                            } else {
                                callback.onError(t.getMessage());
                            }
                        }
                    }
                }
            });
        }
    }

    // ===== CALLBACK INTERFACE =====
    private interface FetchCallback {
        void onComplete(List<StockItem> items);
        void onError(String error);
    }

    // ===== MANUAL REFRESH (SwipeRefresh) =====
    private void manualRefresh() {
        fetchStockData(new FetchCallback() {
            @Override
            public void onComplete(List<StockItem> items) {
                if (!isAdded()) return;

                stockList.clear();
                stockList.addAll(items);
                stockAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);

                Toast.makeText(requireContext(),
                        "Đã cập nhật",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                swipeRefreshLayout.setRefreshing(false);
                showError("Không thể làm mới: " + error);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (!isAdded()) return;

        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading && stockList.isEmpty() ?
                View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // ===== LIFECYCLE - QUAN TRỌNG! =====

    @Override
    public void onResume() {
        super.onResume();
        // Bắt đầu auto refresh khi Fragment hiển thị
        if (!stockList.isEmpty()) {
            startAutoRefresh();
        }
        Log.d(TAG, "📱 Fragment resumed - Auto refresh enabled");
    }

    @Override
    public void onPause() {
        super.onPause();
        // Dừng auto refresh khi Fragment bị ẩn
        stopAutoRefresh();
        Log.d(TAG, "📱 Fragment paused - Auto refresh disabled");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup hoàn toàn
        stopAutoRefresh();
        autoRefreshHandler = null;
        autoRefreshRunnable = null;

        Log.d(TAG, "🗑️ Fragment destroyed - Cleanup complete");
    }
}