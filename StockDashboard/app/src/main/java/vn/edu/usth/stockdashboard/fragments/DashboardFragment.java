package vn.edu.usth.stockdashboard.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.ProgressBar; // REMOVED
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // REMOVED

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.StockItem;
import vn.edu.usth.stockdashboard.adapter.StockAdapter;
import vn.edu.usth.stockdashboard.data.sse.StockSymbolData;
import vn.edu.usth.stockdashboard.data.sse.service.StockSseService;

public class DashboardFragment extends Fragment implements StockSseService.SseUpdateListener {

    private static final String TAG = "DashboardFragment";
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private List<StockItem> stockList = new ArrayList<>();
    // private ProgressBar progressBar; // REMOVED
    // private SwipeRefreshLayout swipeRefreshLayout; // REMOVED
    private StockSseService sseService;
    private ProgressBar progressBar;

    private List<String> symbols = Arrays.asList(
            "VNI", "ACB", "BID", "SSI", "VPB", "HPG", "VCB", "FPT", "VIC", "MSN", "MWG", "TCB"
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views (only RecyclerView is left)
        recyclerView = view.findViewById(R.id.recyclerView_db);
        progressBar = view.findViewById(R.id.progressBar);

        initializeStockList();
        setupRecyclerView();

        sseService = new StockSseService(symbols, this);
        // Connection will be started in onResume
    }

    private void initializeStockList() {
        stockList.clear();
        for (String symbol : symbols) {
            stockList.add(new StockItem(symbol)); // Add placeholders
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        stockAdapter = new StockAdapter(stockList, item ->
                Toast.makeText(requireContext(), "Clicked: " + item.getSymbol(), Toast.LENGTH_SHORT).show()
        );
        recyclerView.setAdapter(stockAdapter);
    }

    // METHOD REMOVED: private void setupSwipeRefresh() { ... }

    // --- SSE Listener Methods ---

    @Override
    public void onOpen() {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            Log.d(TAG, "✅ SSE Connected! Hiding loader and showing list.");
            // THE MAIN LOGIC: Hide the loader and show the content
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onStockUpdate(List<StockSymbolData> newStockDataList) {
        // This method remains the same
        if (!isAdded() || newStockDataList == null) return;
        Map<String, StockSymbolData> newDataMap = newStockDataList.stream()
                .collect(Collectors.toMap(StockSymbolData::getSymbol, Function.identity()));
        requireActivity().runOnUiThread(() -> {
            for (int i = 0; i < stockList.size(); i++) {
                StockItem currentUiItem = stockList.get(i);
                StockSymbolData newDataFromServer = newDataMap.get(currentUiItem.getSymbol());
                if (newDataFromServer != null) {
                    StockItem newItemFromSse = StockItem.fromStockSymbolData(newDataFromServer);
                    if (hasDataChanged(currentUiItem, newItemFromSse)) {
                        stockList.set(i, newItemFromSse);
                        stockAdapter.notifyItemChanged(i);
                    }
                }
            }
        });
    }

    private boolean hasDataChanged(StockItem oldItem, StockItem newItem) {
        return oldItem.getPrice() != newItem.getPrice() ||
                oldItem.getVolume() != newItem.getVolume() ||
                !oldItem.getTime().equals(newItem.getTime());
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
            // Also hide the loader on failure
            progressBar.setVisibility(View.GONE);
            // Optionally show an error message or an empty state view here
            Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
        });
    }

    // --- Lifecycle Management ---

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed, connecting SSE...");
        // When the screen appears, ensure the loader is visible
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
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
    public void onDestroyView() {
        super.onDestroyView();
        if (sseService != null) {
            sseService.disconnect();
            sseService = null;
        }
    }
}