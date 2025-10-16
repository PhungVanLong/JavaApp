package vn.edu.usth.stockdashboard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.StockItem;
import vn.edu.usth.stockdashboard.adapter.StockAdapter;
import vn.edu.usth.stockdashboard.viewmodel.SharedStockViewModel;

public class PortfolioFragment extends Fragment {

    private SharedStockViewModel sharedStockViewModel;
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;

    private TextView tvTotalInvested, tvTotalCurrent, tvTotalPnL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portfolio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalInvested = view.findViewById(R.id.tvTotalInvested);
        tvTotalCurrent = view.findViewById(R.id.tvTotalCurrent);
        tvTotalPnL = view.findViewById(R.id.tvTotalPnL);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        sharedStockViewModel = new ViewModelProvider(requireActivity()).get(SharedStockViewModel.class);

        sharedStockViewModel.getStockList().observe(getViewLifecycleOwner(), stockList -> {
            if (stockList != null && !stockList.isEmpty()) {
                updatePortfolioSummary(stockList);
                updateRecycler(stockList);
            }
        });
    }

    private void updateRecycler(List<StockItem> stockList) {
        if (stockAdapter == null) {
            stockAdapter = new StockAdapter(stockList, item -> {});
            recyclerView.setAdapter(stockAdapter);
        } else {
            stockAdapter.updateData(stockList);
        }
    }

    private void updatePortfolioSummary(List<StockItem> stockList) {
        double totalInvested = 0;
        double totalCurrent = 0;

        for (StockItem item : stockList) {
            totalInvested += item.getPrice() * 100; // giả định mỗi cổ phiếu 100 đơn vị
            totalCurrent += item.getPrice() * 100;
        }

        double pnl = totalCurrent - totalInvested;

        tvTotalInvested.setText(String.format("₫%,.0f", totalInvested));
        tvTotalCurrent.setText(String.format("₫%,.0f", totalCurrent));
        tvTotalPnL.setText(String.format("%s₫%,.0f",
                pnl >= 0 ? "+" : "-", Math.abs(pnl)));
        tvTotalPnL.setTextColor(getResources().getColor(
                pnl >= 0 ? android.R.color.holo_green_light : android.R.color.holo_red_light));
    }
}
