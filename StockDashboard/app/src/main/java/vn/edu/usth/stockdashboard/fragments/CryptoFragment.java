package vn.edu.usth.stockdashboard.fragments;

import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import vn.edu.usth.stockdashboard.R;
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

    private static final String SYMBOLS = "btcusdt,ethusdt,bnbusdt,adausdt,xrpusdt,solusdt," +
            "dotusdt,avxusdt,ltcusdt,linkusdt,maticusdt,uniusdt,atomusdt,trxusdt,aptusdt," +
            "filusdt,nearusdt,icpusdt,vetusdt";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crypto, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_crypto);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CryptoAdapter(cryptoList);
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

                if (getActivity() != null && !getActivity().isFinishing()) {
                    getActivity().runOnUiThread(() -> adapter.updateItem(item));
                }
            }
        };

        IntentFilter filter = new IntentFilter("CRYPTO_UPDATE");
        requireActivity().registerReceiver(cryptoReceiver, filter, Context.RECEIVER_EXPORTED);
        receiverRegistered = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (receiverRegistered) {
            requireActivity().unregisterReceiver(cryptoReceiver);
            receiverRegistered = false;
        }
    }
}
