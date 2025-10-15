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
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private List<StockItem> stockList = new ArrayList<>();
    private StockSseService sseService;
    private ProgressBar progressBar;

    // *** THÊM FLAG ĐỂ KIỂM SOÁT VIỆC ẨN PROGRESS BAR ***
    private boolean isFirstLoad = true;

    private List<String> symbols = Arrays.asList(
            "ACB", "BID", "SSI", "VPB", "HPG", "VCB", "FPT", "VIC", "MSN", "MWG", "TCB"
    );

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

        sseService = new StockSseService(symbols, this);
    }

    private void initializeStockList() {
        stockList.clear();
        for (String symbol : symbols) {
            stockList.add(new StockItem(symbol));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        stockAdapter = new StockAdapter(stockList, item ->
                Toast.makeText(requireContext(), "Clicked: " + item.getSymbol(), Toast.LENGTH_SHORT).show()
        );
        recyclerView.setAdapter(stockAdapter);
    }

    // --- SSE Listener Methods ---

    @Override
    public void onOpen() {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            Log.d(TAG, "✅ SSE Connected!");
            // *** CHỈ ẨN PROGRESS BAR KHI KHÔNG PHẢI LẦN TẢI ĐẦU TIÊN ***
            // Vì onStockUpdate sẽ xử lý việc ẩn sau khi nhận dữ liệu thực
            if (!isFirstLoad) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onStockUpdate(Map<String, StockData> newDataMap) {
        if (!isAdded() || newDataMap == null) return;

        requireActivity().runOnUiThread(() -> {
            // *** ẨN PROGRESS BAR KHI NHẬN ĐƯỢC DỮ LIỆU THỰC ***
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                isFirstLoad = false; // Đánh dấu đã tải xong lần đầu
                Log.d(TAG, "📊 First data received, hiding progress bar");
            }

            for (int i = 0; i < stockList.size(); i++) {
                StockItem currentUiItem = stockList.get(i);
                StockData newDataFromServer = newDataMap.get(currentUiItem.getSymbol());

                if (newDataFromServer != null) {
                    if (hasDataChanged(currentUiItem, newDataFromServer)) {
                        currentUiItem.updateFromData(newDataFromServer);
                        stockAdapter.notifyItemChanged(i);
                        Log.d(TAG, "📊 Updated " + currentUiItem.getSymbol());
                    }
                }
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
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
        });
    }

    // --- Lifecycle Management ---

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed, connecting SSE...");

        // *** LOGIC HIỂN THỊ PROGRESS BAR CHỈ KHI LẦN TẢI ĐẦU TIÊN ***
        if (isFirstLoad) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Log.d(TAG, "First load - showing progress bar");
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "Reconnecting - keeping data visible");
        }

        if (sseService != null) {
            sseService.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Fragment paused, disconnecting SSE...");
        if (sseService != null) {
            sseService.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (sseService != null) {
            sseService.disconnect();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sseService != null) {
            sseService.disconnect();
            sseService = null;
        }
        // Reset flag khi destroy view
        isFirstLoad = true;
    }
}