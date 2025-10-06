package vn.edu.usth.stockdashboard.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.stockdashboard.ChartActivity;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.StockItem;
import vn.edu.usth.stockdashboard.adapter.StockAdapter;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thêm logic cho Dashboard fragment ở đây
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_db);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<StockItem> stockList = new ArrayList<>();
        stockList.add(new StockItem("AAPL", "12:12:00 AM", 173.31, -1.69, 212694));
        stockList.add(new StockItem("GOOG", "12:15:00 AM", 2820.50, 3.14, 185000));
        stockList.add(new StockItem("MSFT", "12:20:00 AM", 299.99, -0.52, 99000));
        stockList.add(new StockItem("VFS", "12:12:00 AM", 173.31, -1.69, 212694));
        stockList.add(new StockItem("VNI", "12:15:00 AM", 2820.50, 3.14, 185000));

        stockList.add(new StockItem("AAPL", "12:12:00 AM", 173.1, -1.69, 212694));
        stockList.add(new StockItem("GOOG", "12:15:00 AM", 2820.0, 3.14, 185000));
        stockList.add(new StockItem("MSFT", "12:20:00 AM", 299.9, -0.52, 99000));
        stockList.add(new StockItem("VFS", "12:12:00 AM", 173.1, -1.69, 212694));
        stockList.add(new StockItem("VNI", "12:15:00 AM", 2820.0, 3.14, 185000));
        stockList.add(new StockItem("USTH", "12:20:00 AM", 299.9, -0.52, 99000));
        stockList.add(new StockItem("AAPL", "12:12:00 AM", 173.1, -1.69, 212694));
        stockList.add(new StockItem("GOOG", "12:15:00 AM", 2820.0, 3.14, 185000));
        stockList.add(new StockItem("MSFT", "12:20:00 AM", 299.9, -0.52, 99000));
        stockList.add(new StockItem("VFS", "12:12:00 AM", 173.1, -1.69, 212694));
        stockList.add(new StockItem("VNI", "12:15:00 AM", 2820.0, 3.14, 185000));
        stockList.add(new StockItem("USTH", "12:20:00 AM", 299.9, -0.52, 99000));

        StockAdapter stockAdapter = new StockAdapter(stockList,item -> {
            // Khi click vào 1 item
            Intent intent= new Intent(requireContext(), ChartActivity.class);
            startActivity(intent);
            // Tạo fragment chi tiết
    });
        recyclerView.setAdapter(stockAdapter);
    }
}