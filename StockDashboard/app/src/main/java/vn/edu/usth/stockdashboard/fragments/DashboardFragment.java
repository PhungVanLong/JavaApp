package vn.edu.usth.stockdashboard.fragments;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.StockItem;
import vn.edu.usth.stockdashboard.adapter.StockAdapter;
import vn.edu.usth.stockdashboard.data.sse.StockData;
import vn.edu.usth.stockdashboard.data.sse.service.StockSseService;
import androidx.lifecycle.ViewModelProvider;
import vn.edu.usth.stockdashboard.viewmodel.SharedStockViewModel;

public class DashboardFragment extends Fragment implements StockSseService.SseUpdateListener {

    private static final String TAG = "DashboardFragment";
    private static final String KEY_STOCK_LIST = "stock_list_state";
    private static final String KEY_HAS_DATA = "has_initial_data";

    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private List<StockItem> stockList = new ArrayList<>();
    private StockSseService sseService;
    private ProgressBar progressBar;

    // Cờ để kiểm tra đã có dữ liệu ban đầu chưa
    private boolean hasInitialData = false;

    private List<String> symbols = Arrays.asList(
            "ACB", "BID", "SSI", "VPB", "HPG", "VCB", "FPT", "VIC", "MSN", "MWG", "TCB"
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore state nếu có
        if (savedInstanceState != null) {
            hasInitialData = savedInstanceState.getBoolean(KEY_HAS_DATA, false);
            Log.d(TAG, "📦 Restored state - hasInitialData: " + hasInitialData);
        }

        // Khởi tạo SSE service
        sseService = new StockSseService(symbols, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView_db);
        progressBar = view.findViewById(R.id.dashboard_progress_bar);

        initializeStockList();
        setupRecyclerView();

        // Chỉ show progress bar nếu chưa có dữ liệu
        if (hasInitialData) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "🔄 Fragment recreated - keeping data visible");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Log.d(TAG, "🆕 First load - showing progress bar");
        }
    }

    private void initializeStockList() {
        // Chỉ khởi tạo lại nếu list rỗng
        if (stockList.isEmpty()) {
            for (String symbol : symbols) {
                stockList.add(new StockItem(symbol));
            }
            Log.d(TAG, "📋 Initialized stock list with " + stockList.size() + " items");
        } else {
            Log.d(TAG, "♻️ Reusing existing stock list with " + stockList.size() + " items");
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        stockAdapter = new StockAdapter(stockList, item -> {
            // Mở ChartFragment với mã chứng khoán được click
            openChartFragment(item.getSymbol());
        });
        recyclerView.setAdapter(stockAdapter);
    }

    /**
     * Mở ChartFragment với mã chứng khoán được chọn
     * @param stockSymbol Mã chứng khoán (VD: "ACB", "VCB")
     */
    private void openChartFragment(String stockSymbol) {
        Log.d(TAG, "📊 Opening chart for: " + stockSymbol);

        ChartFragment chartFragment = ChartFragment.newInstance(stockSymbol);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, chartFragment)
                .addToBackStack(null)
                .commit();
    }

    // --- SSE Listener Methods ---

    @Override
    public void onOpen() {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            Log.d(TAG, "✅ SSE Connected!");
        });
    }

    @Override
    public void onStockUpdate(Map<String, StockData> newDataMap) {
        if (!isAdded() || newDataMap == null) return;

        requireActivity().runOnUiThread(() -> {
            // Ẩn progress bar sau khi nhận dữ liệu đầu tiên
            if (!hasInitialData) {
                hasInitialData = true;
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Log.d(TAG, "📊 First data received, hiding progress bar");
            }

            // Update UI ngầm - không show/hide gì cả
            int updatedCount = 0;
            for (int i = 0; i < stockList.size(); i++) {
                StockItem currentUiItem = stockList.get(i);
                StockData newDataFromServer = newDataMap.get(currentUiItem.getSymbol());

                if (newDataFromServer != null) {
                    if (hasDataChanged(currentUiItem, newDataFromServer)) {
                        currentUiItem.updateFromData(newDataFromServer);
                        stockAdapter.notifyItemChanged(i);
                        updatedCount++;
                    }
                }
            }

            if (updatedCount > 0) {
                Log.d(TAG, "🔄 Silent update: " + updatedCount + " items changed");
            }
        });
    }

    private boolean hasDataChanged(StockItem oldItem, StockData newItemData) {
        return oldItem.getPrice() != newItemData.getClose() ||
                oldItem.getVolume() != newItemData.getVolume();
    }

    @Override
    public void onClose() {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> Log.d(TAG, "✗ SSE Connection Closed"));
    }

    @Override
    public void onFailure(String error) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            // Không ẩn progress bar nếu chưa có dữ liệu
            if (hasInitialData) {
                // Có data rồi, chỉ log error thôi
                Log.e(TAG, "⚠️ SSE Error (silent): " + error);
            } else {
                // Chưa có data, show error
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Lifecycle Management ---

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ Fragment resumed, connecting SSE silently...");

        // Không show progress bar khi resume
        // SSE service sẽ tự động update UI ngầm
        if (sseService != null) {
            sseService.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ Fragment paused, disconnecting SSE...");
        if (sseService != null) {
            sseService.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Lưu trạng thái có dữ liệu hay chưa
        outState.putBoolean(KEY_HAS_DATA, hasInitialData);
        Log.d(TAG, "💾 Saved state - hasInitialData: " + hasInitialData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🗑️ View destroyed");
        // KHÔNG reset hasInitialData và stockList ở đây
        // Để giữ data khi back
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "💥 Fragment destroyed completely");

        // Chỉ cleanup khi fragment thực sự bị destroy (không phải config change)
        if (requireActivity().isFinishing() || !requireActivity().isChangingConfigurations()) {
            if (sseService != null) {
                sseService.disconnect();
                sseService = null;
            }
            stockList.clear();
            hasInitialData = false;
        }
    }
}