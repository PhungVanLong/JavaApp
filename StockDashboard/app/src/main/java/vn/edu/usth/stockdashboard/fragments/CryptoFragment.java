package vn.edu.usth.stockdashboard.fragments;

import android.content.*;
import android.os.Build;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.*;

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
    private boolean receiverRegistered = false;
    private String currentUsername;
    private static final String SYMBOLS = "btcusdt,ethusdt,bnbusdt,adausdt,xrpusdt,solusdt," +
            "dotusdt,avxusdt,ltcusdt,linkusdt,maticusdt,uniusdt,atomusdt,trxusdt,aptusdt," +
            "filusdt,nearusdt,icpusdt,vetusdt";

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

        // Disable change animations to prevent flickering
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        adapter = new CryptoAdapter(cryptoList, this::showAddToPortfolioDialog);
        recyclerView.setAdapter(adapter);

        startForegroundSSEService();
        registerCryptoReceiver();

        return view;
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
            }
        };

        IntentFilter filter = new IntentFilter("CRYPTO_UPDATE");
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(cryptoReceiver, filter);
        receiverRegistered = true;
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (receiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext())
                    .unregisterReceiver(cryptoReceiver);
            receiverRegistered = false;
        }
    }
}
