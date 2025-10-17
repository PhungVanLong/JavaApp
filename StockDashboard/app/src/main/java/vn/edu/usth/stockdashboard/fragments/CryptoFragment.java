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

import java.text.SimpleDateFormat;
import java.util.*;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.adapter.CryptoAdapter;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;
import vn.edu.usth.stockdashboard.data.model.StockItem;
import vn.edu.usth.stockdashboard.data.sse.service.CryptoSSEService;
import vn.edu.usth.stockdashboard.data.manager.PortfolioManager;

public class CryptoFragment extends Fragment {

    private RecyclerView recyclerView;
    private CryptoAdapter adapter;
    private BroadcastReceiver cryptoReceiver;
    private boolean receiverRegistered = false;
    private String currentUsername;
    private static final String SYMBOLS = "btcusdt,ethusdt,bnbusdt,adausdt,xrpusdt,solusdt," +
            "dotusdt,avxusdt,ltcusdt,linkusdt,maticusdt,uniusdt,atomusdt,trxusdt,aptusdt," +
            "filusdt,nearusdt,icpusdt,vetusdt";

    private final List<CryptoItem> cryptoList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crypto, container, false);

        // Lấy username hiện tại từ Intent hoặc mặc định "test"
        if (getActivity() != null) {
            currentUsername = getActivity().getIntent().getStringExtra("USERNAME");
            if (currentUsername == null || currentUsername.isEmpty()) {
                currentUsername = "test";
            }
        }

        recyclerView = view.findViewById(R.id.recyclerView_crypto);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        recyclerView.setAdapter(adapter);

        startSSEService();
        registerCryptoReceiver();

        return view;
    }

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

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> adapter.updateItem(item));
                }
            }
        };

        IntentFilter filter = new IntentFilter("CRYPTO_UPDATE");
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(cryptoReceiver, filter);
        receiverRegistered = true;
    }

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

                // Lưu vào database thông qua PortfolioManager
                PortfolioManager.addStock(requireContext(), stock, currentUsername);

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
    }
}
