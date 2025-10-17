package vn.edu.usth.stockdashboard.fragments;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.CryptoDetailActivity;
import vn.edu.usth.stockdashboard.adapter.CryptoAdapter;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;
import vn.edu.usth.stockdashboard.data.sse.service.CryptoSSEService;

import java.text.SimpleDateFormat;
import java.util.*;

public class CryptoFragment extends Fragment {
    private RecyclerView recyclerView;
    private CryptoAdapter adapter;
    private final List<CryptoItem> cryptoList = new ArrayList<>();
    private BroadcastReceiver cryptoReceiver;
    private boolean receiverRegistered = false;

    private static final String SYMBOLS = "btcusdt,ethusdt,bnbusdt,adausdt,xrpusdt,solusdt,"
            + "dotusdt,avxusdt,ltcusdt,linkusdt,maticusdt,uniusdt,atomusdt,trxusdt,aptusdt,"
            + "filusdt,nearusdt,icpusdt,vetusdt";

    private Intent sseIntent;
    private static final int DETAIL_REQUEST_CODE = 1234;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crypto, container, false);
        recyclerView = view.findViewById(R.id.recyclerView_crypto);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        adapter = new CryptoAdapter(cryptoList);
        recyclerView.setAdapter(adapter);

        // ✅ Bắt sự kiện click để mở detail
        adapter.setOnItemClickListener(item -> {
            stopSSEService(); // Dừng SSE khi vào detail
            Intent intent = new Intent(getContext(), CryptoDetailActivity.class);
            intent.putExtra("symbol", item.getSymbol());
            intent.putExtra("name", item.getSymbol().toUpperCase());
            startActivityForResult(intent, DETAIL_REQUEST_CODE);
        });

        registerCryptoReceiver();
        startSSEService();
        return view;
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.updateItem(item));
                }
            }
        };

        IntentFilter filter = new IntentFilter("CRYPTO_UPDATE");
        requireActivity().registerReceiver(cryptoReceiver, filter, Context.RECEIVER_EXPORTED);
        receiverRegistered = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSSEService();
    }

    @Override
    public void onResume() {
        super.onResume();
        startSSEService();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // ✅ Khi quay lại fragment thì restart SSE
            startSSEService();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (receiverRegistered) {
            requireActivity().unregisterReceiver(cryptoReceiver);
            receiverRegistered = false;
        }
        stopSSEService();
    }
}
