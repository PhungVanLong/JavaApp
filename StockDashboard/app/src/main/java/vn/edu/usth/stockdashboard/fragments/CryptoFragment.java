package vn.edu.usth.stockdashboard.fragments;

import android.content.*;
import android.os.Build;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.text.SimpleDateFormat;
import java.util.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.usth.stockdashboard.CryptoDetailActivity;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.SharedStockViewModel;
import vn.edu.usth.stockdashboard.adapter.CryptoAdapter;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;
import vn.edu.usth.stockdashboard.data.model.StockItem;
import vn.edu.usth.stockdashboard.data.sse.service.CryptoSSEService;
import vn.edu.usth.stockdashboard.data.manager.PortfolioManager;


public class CryptoFragment extends Fragment {
    private RecyclerView recyclerView;
    private CryptoAdapter adapter;
    private final List<CryptoItem> cryptoList = new ArrayList<>();
    private BroadcastReceiver cryptoReceiver;
    private Intent sseIntent;

    private static final String SYMBOLS = "btcusdt,ethusdt,bnbusdt,adausdt,xrpusdt,solusdt,"
            + "dotusdt,avxusdt,ltcusdt,linkusdt,maticusdt,uniusdt,atomusdt,trxusdt,aptusdt,"
            + "filusdt,nearusdt,icpusdt,vetusdt";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crypto, container, false);

        // Lấy username từ Intent hoặc mặc định
        if (getActivity() != null) {
            currentUsername = getActivity().getIntent().getStringExtra("USERNAME");
            if (currentUsername == null || currentUsername.isEmpty()) currentUsername = "test";
        }

        recyclerView = view.findViewById(R.id.recyclerView_crypto);

        // Optimize RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        setupRecyclerView();
        setupAdapter();
        setupCryptoReceiver(); // Chuẩn bị receiver

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        // Tối ưu hiệu năng, không chạy animation khi dữ liệu thay đổi
        // Disable change animations to prevent flickering
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    private void setupAdapter() {
        adapter = new CryptoAdapter(cryptoList);
        adapter = new CryptoAdapter(cryptoList, this::showAddToPortfolioDialog);
        recyclerView.setAdapter(adapter);

        startForegroundSSEService();
        registerCryptoReceiver();

        return view;
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(getContext(), CryptoDetailActivity.class);
            intent.putExtra("symbol", item.getSymbol());
            // Lấy tên đầy đủ thay vì chỉ symbol
            intent.putExtra("name", item.getSymbol().replace("usdt", "").toUpperCase());
            startActivity(intent);
        });
    }

    // Khởi chạy service đúng cách với Android 12+
    private void startForegroundSSEService() {
        try {
            Intent intent = new Intent(requireContext(), CryptoSSEService.class);
            intent.putExtra("symbols", SYMBOLS);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(intent);
                } else {
                    requireContext().startService(intent);
                }
            }

        } catch (Exception e) {
            Log.e("CryptoFragment", "Cannot start SSE service", e);
            Toast.makeText(getContext(), "Cannot start crypto service", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerCryptoReceiver() {
        if (receiverRegistered) return;

    // Tách logic khởi tạo receiver ra riêng
    private void setupCryptoReceiver() {
        cryptoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String symbol = intent.getStringExtra("symbol");
                double price = intent.getDoubleExtra("price", 0);
                double open = intent.getDoubleExtra("open", 0);
                double changePercent = intent.getDoubleExtra("change_percent", 0);
                long timestamp = intent.getLongExtra("timestamp", 0);

                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(new Date(timestamp * 1000));

                CryptoItem item = new CryptoItem(symbol, price, open, changePercent, time);

                if (getActivity() != null && !getActivity().isFinishing()) {
                    getActivity().runOnUiThread(() -> {
                        adapter.updateItem(item);

                        // ✅ THÊM ĐOẠN NÀY: Chuyển đổi CryptoItem -> StockItem và push lên ViewModel
                        List<StockItem> cryptoStockList = new ArrayList<>();
                        for (CryptoItem crypto : cryptoList) {
                            // Giả sử quantity = 0 cho crypto chưa mua
                            StockItem stock = new StockItem(
                                    crypto.getSymbol(),
                                    0, // investedValue
                                    crypto.getPrice() // currentValue = giá hiện tại
                            );
                            stock.setQuantity(0);
                            cryptoStockList.add(stock);
                        }

                        SharedStockViewModel viewModel = new ViewModelProvider(requireActivity())
                                .get(SharedStockViewModel.class);
                        viewModel.setCryptoStocks(cryptoStockList);
                    });
                }
                // Không cần check getActivity() vì receiver chỉ chạy khi fragment is resumed
                requireActivity().runOnUiThread(() -> adapter.updateItem(item));
            }
        };
    }

    // Bắt đầu các tác vụ khi Fragment được hiển thị
    @Override
    public void onResume() {
        super.onResume();
        // 1. Đăng ký receiver để bắt đầu lắng nghe
        IntentFilter filter = new IntentFilter("CRYPTO_UPDATE");
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(cryptoReceiver, filter);
        receiverRegistered = true;
        requireActivity().registerReceiver(cryptoReceiver, filter, Context.RECEIVER_EXPORTED);
        // 2. Khởi động service để lấy dữ liệu
        startSSEService();
    }
    private void showAddToPortfolioDialog(CryptoItem item) {
        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add " + item.getSymbol().toUpperCase() + " to Portfolio");

        // Inflate layout dialog_add_portfolio.xml
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_stock, null);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        builder.setView(dialogView);

        // Tạo dialog
        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (d, w) -> {});
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, w) -> dialog.dismiss());
        dialog.show();

        // Gắn xử lý khi nhấn nút Add
        Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        addBtn.setOnClickListener(v -> {
            String quantityStr = etQuantity.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (quantityStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both quantity and price", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantity = Double.parseDouble(quantityStr);
                double buyPrice = Double.parseDouble(priceStr);

                if (quantity <= 0 || buyPrice <= 0) {
                    Toast.makeText(requireContext(), "Values must be > 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                double investedValue = quantity * buyPrice;
                double currentValue = quantity * item.getPrice();

                // Tạo StockItem
                StockItem stock = new StockItem(item.getSymbol(), investedValue, currentValue);
                stock.setQuantity(quantity);

                // Lưu vào SQLite
                PortfolioManager.addStock(requireContext(), stock, currentUsername);

                Toast.makeText(requireContext(),
                        item.getSymbol().toUpperCase() + " added to portfolio!",
                        Toast.LENGTH_SHORT).show();

                dialog.dismiss();

                // Gửi tín hiệu cập nhật Portfolio cho ViewModel
                SharedStockViewModel viewModel = new ViewModelProvider(requireActivity())
                        .get(SharedStockViewModel.class);
                viewModel.notifyPortfolioUpdated();

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Dừng các tác vụ khi Fragment bị che khuất
    @Override
    public void onPause() {
        super.onPause();
        // 1. Hủy đăng ký receiver để ngừng lắng nghe
        requireActivity().unregisterReceiver(cryptoReceiver);
        // 2. Dừng service để tiết kiệm tài nguyên
        stopSSEService();
    }

    private void startSSEService() {
        Context ctx = requireContext().getApplicationContext();
        sseIntent = new Intent(ctx, CryptoSSEService.class);
        sseIntent.putExtra("symbols", SYMBOLS);
        ctx.startService(sseIntent);
    }

    private void stopSSEService() {
        if (sseIntent != null) {
            requireContext().stopService(sseIntent);
    public void onDestroyView() {
        super.onDestroyView();
        if (receiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext())
                    .unregisterReceiver(cryptoReceiver);
            receiverRegistered = false;
        }
    }

    // Bỏ onActivityResult vì onResume đã xử lý việc restart service
    // Bỏ onDestroyView vì onPause đã xử lý việc unregister receiver
}
