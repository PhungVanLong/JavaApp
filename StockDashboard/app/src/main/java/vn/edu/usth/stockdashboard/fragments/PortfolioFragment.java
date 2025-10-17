package vn.edu.usth.stockdashboard.fragments;

import android.database.Cursor;
import android.os.Bundle;

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

        // Theo dõi khi Portfolio được cập nhật từ Dashboard
        SharedStockViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedStockViewModel.class);
        viewModel.getPortfolioUpdated().observe(getViewLifecycleOwner(), updated -> {
            if (updated != null && updated) {
                loadPortfolioFromDatabase();
                viewModel.resetFlag();
            }
        });

        loadPortfolioFromDatabase();
        return view;
    }

    private void loadPortfolioFromDatabase() {
        if (databaseHelper == null || currentUsername == null) return;

        stockList.clear();
        Cursor cursor = databaseHelper.getPortfolioForUser(currentUsername);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String ticker = cursor.getString(0);
                double quantity = cursor.getDouble(1);
                double avgPrice = cursor.getDouble(2);

                double investedValue = quantity * avgPrice;
                double currentPrice = avgPrice * 1.2;
                double currentValue = quantity * currentPrice;

                StockItem item = new StockItem(ticker, investedValue, currentValue);
                item.setQuantity((int) quantity);
                stockList.add(item);

            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    public void refreshPortfolio() {
        loadPortfolioFromDatabase();
    }
}
