package vn.edu.usth.stockdashboard.fragments;

import android.os.Bundle;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import android.database.Cursor;

import vn.edu.usth.stockdashboard.adapter.PortfolioAdapter;
import vn.edu.usth.stockdashboard.R;

import vn.edu.usth.stockdashboard.data.model.StockItem;

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

        // 1. Lấy username hiện tại
        if (getActivity() != null) {
            currentUsername = getActivity().getIntent().getStringExtra("USERNAME");
            if (currentUsername == null || currentUsername.isEmpty()) {
                currentUsername = "test";
            }
        }

        // 2. Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(getContext());
        stockList = new ArrayList<>();

        // 3. Tải dữ liệu từ database
        loadPortfolioFromDatabase();

        // 4. Thiết lập RecyclerView
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
        return view;
    }

        view.findViewById(R.id.btnRefreshPortfolio).setOnClickListener(v -> {
            // Load lại dữ liệu tổng hợp
            loadPortfolioSummary();
            loadPortfolioFromDatabase();
        });

        loadPortfolioFromDatabase();
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
                double currentPrice = avgPrice * 1.2; // hoặc lấy giá live nếu muốn
                double currentValue = quantity * currentPrice;

                StockItem item = new StockItem(ticker, investedValue, currentValue);
                item.setQuantity((int) quantity);
                stockList.add(item);

            } while (cursor.moveToNext());
            cursor.close();
        }
        // 2️⃣ Load từ Dashboard live
        List<StockItem> dashboard = sharedStockViewModel.getDashboardStocks().getValue();
        if (dashboard != null) stockList.addAll(dashboard);

        // 3️⃣ Load từ Crypto live
        List<StockItem> crypto = sharedStockViewModel.getCryptoStocks().getValue();
        if (crypto != null) stockList.addAll(crypto);

        adapter.notifyDataSetChanged();
        updateSummary();
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

    // 5. Thêm phương thức để tải dữ liệu
    private void loadPortfolioFromDatabase() {
        if (databaseHelper == null || currentUsername == null) return;

        stockList.clear();

        Cursor cursor = databaseHelper.getPortfolioForUser(currentUsername);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // P_COL_3 (ticker) là cột 0, P_COL_4 (quantity) là cột 1, P_COL_5 (avg_price) là cột 2
                String ticker = cursor.getString(0);
                double quantity = cursor.getDouble(1);
                double avgPrice = cursor.getDouble(2);

                double investedValue = quantity * avgPrice;

                // GIẢ SỬ: TẠM THỜI đặt currentPrice = avgPrice để tính toán
                // TODO: Kết nối với API để lấy giá thực tế
                double currentPrice = avgPrice * 1.2; // Ví dụ: tăng 20%
                double currentValue = quantity * currentPrice;

                StockItem item = new StockItem(ticker, investedValue, currentValue);
                item.setQuantity((int) quantity); // Set quantity
                stockList.add(item);

            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // --- Public method để refresh từ ngoài ---
    public void refreshPortfolio() {
        loadPortfolioSummary();
    }
}