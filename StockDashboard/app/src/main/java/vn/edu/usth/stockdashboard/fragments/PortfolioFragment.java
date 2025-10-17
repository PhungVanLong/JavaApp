package vn.edu.usth.stockdashboard.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.adapter.PortfolioAdapter;
import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import vn.edu.usth.stockdashboard.data.model.StockItem;
import vn.edu.usth.stockdashboard.SharedStockViewModel;

public class PortfolioFragment extends Fragment {

    private RecyclerView recyclerView;
    private PortfolioAdapter adapter;
    private List<StockItem> stockList;
    private DatabaseHelper databaseHelper;
    private String currentUsername;
    private TextView tvTotalInvested;
    private TextView tvTotalCurrent;
    private TextView tvTotalPnL;
    private SharedStockViewModel sharedStockViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        if (getActivity() != null) {
            currentUsername = getActivity().getIntent().getStringExtra("USERNAME");
            if (currentUsername == null || currentUsername.isEmpty()) {
                currentUsername = "test";
            }
        }

        databaseHelper = new DatabaseHelper(getContext());
        stockList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PortfolioAdapter(stockList, currentUsername);
        recyclerView.setAdapter(adapter);

        tvTotalInvested = view.findViewById(R.id.tvTotalInvested);
        tvTotalCurrent = view.findViewById(R.id.tvTotalCurrent);
        tvTotalPnL = view.findViewById(R.id.tvTotalPnL);


        // Khởi tạo ViewModel chia sẻ
        sharedStockViewModel = new ViewModelProvider(requireActivity()).get(SharedStockViewModel.class);

        // Sau đó mới đăng ký observer
        sharedStockViewModel.getDashboardStocks().observe(getViewLifecycleOwner(), list -> loadPortfolioSummary());
        sharedStockViewModel.getCryptoStocks().observe(getViewLifecycleOwner(), list -> loadPortfolioSummary());
        sharedStockViewModel.getPortfolioUpdated().observe(getViewLifecycleOwner(), flag -> {
            if (flag != null && flag) {
                loadPortfolioSummary();
                sharedStockViewModel.resetFlag();
            }
        });

        view.findViewById(R.id.btnRefreshPortfolio).setOnClickListener(v -> {
            // Load lại dữ liệu tổng hợp
            loadPortfolioSummary();
        });

        loadPortfolioSummary();
        return view;
    }

    // --- Load dữ liệu gộp từ database + dashboard + crypto ---
    private void loadPortfolioSummary() {
        if (databaseHelper == null || currentUsername == null) return;

        stockList.clear();
        // 1️⃣ Load từ database
        Cursor cursor = databaseHelper.getPortfolioForUser(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String ticker = cursor.getString(0);
                double quantity = cursor.getDouble(1);
                double avgPrice = cursor.getDouble(2);

                double investedValue = quantity * avgPrice;
                double currentPrice = findRealTimePrice(ticker, avgPrice);
                double currentValue = quantity * currentPrice;

                StockItem item = new StockItem(ticker, investedValue, currentValue);
                item.setQuantity((int) quantity);
                item.setPrice(currentPrice); // ✅ SET PRICE để hiển thị đúng
                stockList.add(item);

            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        updateSummary();
    }

    private double findRealTimePrice(String ticker, double defaultPrice) {
        // 2️⃣ Load từ Dashboard live
        List<StockItem> dashboard = sharedStockViewModel.getDashboardStocks().getValue();
        if (dashboard != null) {
            for (StockItem item : dashboard) {
                if (item.getSymbol().equalsIgnoreCase(ticker)) {
                    return item.getPrice(); // Giá real-time từ SSE
                }
            }
        }

        // 3️⃣ Load từ Crypto live
        List<StockItem> crypto = sharedStockViewModel.getCryptoStocks().getValue();
        if (crypto != null) {
            for (StockItem item : crypto) {
                if (item.getSymbol().equalsIgnoreCase(ticker)) {
                    // ✅ SỬA: Ưu tiên lấy price, nếu = 0 mới lấy currentValue
                    return item.getPrice() > 0 ? item.getPrice() : item.getCurrentValue();
                }
            }
        }
        return defaultPrice * 1.2;
    }

    // --- Cập nhật summary ---
    private void updateSummary() {
        double totalInvested = 0;
        double totalCurrent = 0;

        for (StockItem item : stockList) {
            totalInvested += item.getInvestedValue();
            totalCurrent += item.getCurrentValue();
        }

        double pnl = totalCurrent - totalInvested;

        if (tvTotalInvested != null)
            tvTotalInvested.setText(String.format("₫%,.0f", totalInvested));

        if (tvTotalCurrent != null)
            tvTotalCurrent.setText(String.format("₫%,.0f", totalCurrent));

        if (tvTotalPnL != null) {
            tvTotalPnL.setText(String.format("%s₫%,.0f", pnl >= 0 ? "+" : "-", Math.abs(pnl)));
            tvTotalPnL.setTextColor(pnl >= 0 ?
                    getResources().getColor(android.R.color.holo_green_light) :
                    getResources().getColor(android.R.color.holo_red_light));
        }
    }

    // --- Public method để refresh từ ngoài ---
    public void refreshPortfolio() {
        loadPortfolioSummary();
    }
}