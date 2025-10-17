package vn.edu.usth.stockdashboard.fragments;

import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.adapter.CryptoAdapter;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;
import vn.edu.usth.stockdashboard.data.model.StockItem;
import vn.edu.usth.stockdashboard.data.sse.service.CryptoSSEService;
import vn.edu.usth.stockdashboard.data.manager.PortfolioManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class CryptoFragment extends Fragment {

    private RecyclerView recyclerView;
    private CryptoAdapter adapter;

    // ✅ Danh sách hiển thị
    private final List<CryptoItem> cryptoList = new ArrayList<>();

    // ✅ Map tạm để batch update
    private final Map<String, CryptoItem> pendingUpdateMap = new HashMap<>();

    private BroadcastReceiver cryptoReceiver;
    private boolean receiverRegistered = false;
    private Timer updateTimer;

    private static final String SYMBOLS = "btcusdt,ethusdt,bnbusdt,adausdt,xrpusdt,solusdt," +
            "dotusdt,avxusdt,ltcusdt,linkusdt,maticusdt,uniusdt,atomusdt,trxusdt,aptusdt," +
            "filusdt,nearusdt,icpusdt,vetusdt";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crypto, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_crypto);

        // ✅ RecyclerView setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        adapter = new CryptoAdapter(cryptoList, this::showAddToPortfolioDialog);
        recyclerView.setAdapter(adapter);

        startSSEService();
        registerCryptoReceiver();
        startBatchUpdateTimer();

        return view;
    }

    // ✅ Start SSE service
    private void startSSEService() {
        try {
            Context ctx = requireContext().getApplicationContext();
            Intent intent = new Intent(ctx, CryptoSSEService.class);
            intent.putExtra("symbols", SYMBOLS);
            ctx.startService(intent);
        } catch (Exception e) {
            Log.e("CryptoFragment", "Error starting SSE service", e);
        }
    }

    // ✅ Register local broadcast receiver
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

                // ✅ Lưu vào map để batch update
                synchronized (pendingUpdateMap) {
                    pendingUpdateMap.put(symbol, item);
                }
            }
        };

        IntentFilter filter = new IntentFilter("CRYPTO_UPDATE");
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(cryptoReceiver, filter);
        receiverRegistered = true;
    }

    // ✅ Timer batch update UI
    private void startBatchUpdateTimer() {
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<CryptoItem> batchList;
                synchronized (pendingUpdateMap) {
                    if (pendingUpdateMap.isEmpty()) return;
                    batchList = new ArrayList<>(pendingUpdateMap.values());
                    pendingUpdateMap.clear();
                }
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> adapter.updateList(batchList));
                }
            }
        }, 0, 200); // update mỗi 200ms
    }

    // ✅ Dialog thêm vào portfolio
    private void showAddToPortfolioDialog(CryptoItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add " + item.getSymbol().toUpperCase() + " to Portfolio");

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_portfolio, null);
        EditText edtQuantity = dialogView.findViewById(R.id.edtQuantity);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (d, w) -> {});
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, w) -> dialog.dismiss());
        dialog.show();

        Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        addBtn.setOnClickListener(v -> {
            String quantityStr = edtQuantity.getText().toString().trim();
            if (quantityStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    Toast.makeText(getContext(), "Quantity must be > 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                double invested = quantity * item.getPrice();
                StockItem stock = new StockItem(item.getSymbol(), invested, invested);
                stock.setQuantity(quantity);

                PortfolioManager.addStock(requireContext(), stock);

                Toast.makeText(getContext(),
                        item.getSymbol().toUpperCase() + " added to portfolio!",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
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
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
}
