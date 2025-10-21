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

import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import vn.edu.usth.stockdashboard.adapter.PortfolioAdapter;
import vn.edu.usth.stockdashboard.data.model.StockItem;
import vn.edu.usth.stockdashboard.SharedStockViewModel;
import vn.edu.usth.stockdashboard.R;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class PortfolioFragment extends Fragment {

    private RecyclerView recyclerView;
    private PortfolioAdapter adapter;
    private List<StockItem> stockList;
    private DatabaseHelper databaseHelper;
    private String currentUsername;
    private TextView tvTotalInvested, tvTotalCurrent, tvTotalPnL;
    private SharedStockViewModel sharedStockViewModel;

    // Chart
    private LineChart summaryChart;
    private LineDataSet lineDataSet;
    private LineData lineData;
    private int xIndex = 0; // trục X tăng dần

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        // Username
        currentUsername = getActivity() != null ?
                getActivity().getIntent().getStringExtra("USERNAME") : "test";
        if (currentUsername == null || currentUsername.isEmpty()) currentUsername = "test";

        databaseHelper = new DatabaseHelper(getContext());
        stockList = new ArrayList<>();

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PortfolioAdapter(stockList);
        recyclerView.setAdapter(adapter);

        // TextViews
        tvTotalInvested = view.findViewById(R.id.tvTotalInvested);
        tvTotalCurrent = view.findViewById(R.id.tvTotalCurrent);
        tvTotalPnL = view.findViewById(R.id.tvTotalPnL);

        // LineChart
        summaryChart = view.findViewById(R.id.summaryChart);
        initChart();

        // Shared ViewModel
        sharedStockViewModel = new ViewModelProvider(requireActivity()).get(SharedStockViewModel.class);

        // Callback xóa stock
        adapter.setOnPortfolioActionListener(ticker -> {
            databaseHelper.deletePortfolioItem(currentUsername, ticker);
            loadPortfolioSummary();
        });

        // Observe dữ liệu dashboard + crypto + portfolio
        sharedStockViewModel.getDashboardStocks().observe(getViewLifecycleOwner(), list -> loadPortfolioSummary());
        sharedStockViewModel.getCryptoStocks().observe(getViewLifecycleOwner(), list -> loadPortfolioSummary());
        sharedStockViewModel.getPortfolioUpdated().observe(getViewLifecycleOwner(), flag -> {
            if (flag != null && flag) {
                loadPortfolioSummary();
                sharedStockViewModel.resetFlag();
            }
        });

        loadPortfolioSummary();
        return view;
    }

    // Khởi tạo chart 1 lần
    private void initChart() {
        lineDataSet = new LineDataSet(new ArrayList<>(), "Portfolio Value");
        lineDataSet.setColor(getResources().getColor(android.R.color.holo_green_light));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(getResources().getColor(android.R.color.holo_green_light));

        lineData = new LineData(lineDataSet);
        summaryChart.setData(lineData);

        summaryChart.getDescription().setEnabled(false);
        summaryChart.getLegend().setEnabled(false);
        summaryChart.getAxisRight().setEnabled(false);
        summaryChart.getAxisLeft().setTextColor(getResources().getColor(android.R.color.white));
        summaryChart.getXAxis().setTextColor(getResources().getColor(android.R.color.white));
        summaryChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        summaryChart.getXAxis().setDrawGridLines(false);
        summaryChart.getAxisLeft().setDrawGridLines(false);
    }

    private void loadPortfolioSummary() {
        if (databaseHelper == null || currentUsername == null) return;

        stockList.clear();
        Cursor cursor = databaseHelper.getPortfolioForUser(currentUsername);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String ticker = cursor.getString(0);
                double quantity = cursor.getDouble(1);
                double avgPrice = cursor.getDouble(2);

                double investedValue = quantity * avgPrice;
                double currentPrice = findRealTimePrice(ticker, avgPrice);
                double currentValue = quantity * currentPrice;

                StockItem item = new StockItem(ticker);
                item.setQuantity(quantity);
                item.setPrice(currentPrice);
                item.setInvestedValue(investedValue);
                item.setCurrentValue(currentValue);

                stockList.add(item);
            } while (cursor.moveToNext());
        }

        if (cursor != null) cursor.close();
        adapter.notifyDataSetChanged();
        updateSummary();
    }

    private double findRealTimePrice(String ticker, double defaultPrice) {
        List<StockItem> dashboard = sharedStockViewModel.getDashboardStocks().getValue();
        if (dashboard != null) {
            for (StockItem item : dashboard) {
                if (item.getSymbol().equalsIgnoreCase(ticker)) return item.getPrice();
            }
        }

        List<StockItem> crypto = sharedStockViewModel.getCryptoStocks().getValue();
        if (crypto != null) {
            for (StockItem item : crypto) {
                if (item.getSymbol().equalsIgnoreCase(ticker))
                    return item.getPrice() > 0 ? item.getPrice() : item.getCurrentValue();
            }
        }
        return defaultPrice;
    }

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

        // ===== Cập nhật chart real-time =====
        if (summaryChart != null) {
            lineData.addEntry(new Entry(xIndex++, (float) totalCurrent), 0);
            lineData.notifyDataChanged();
            summaryChart.notifyDataSetChanged();
            summaryChart.setVisibleXRangeMaximum(20); // chỉ hiển thị 20 điểm gần nhất
            summaryChart.moveViewToX(lineData.getEntryCount());
        }
    }
}
