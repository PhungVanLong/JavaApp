package vn.edu.usth.stockdashboard.fragments;

import android.os.Bundle;
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

import vn.edu.usth.stockdashboard.PortfolioAdapter;
import vn.edu.usth.stockdashboard.R;

import vn.edu.usth.stockdashboard.data.model.StockItem;

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
        view.findViewById(R.id.btnRefreshPortfolio).setOnClickListener(v -> {
            refreshPortfolio();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data mỗi khi fragment được hiển thị lại
        loadPortfolioFromDatabase();
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

    /**
     * Public method để refresh portfolio từ bên ngoài
     */
    public void refreshPortfolio() {
        loadPortfolioFromDatabase();
    }
}