package vn.edu.usth.stockdashboard.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.adapter.CryptoAdapter;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;
import vn.edu.usth.stockdashboard.data.sse.service.CryptoSSEService;

import java.text.SimpleDateFormat;
import java.util.*;

public class CryptoFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CryptoAdapter adapter;
    private final List<CryptoItem> cryptoList = new ArrayList<>();

    private final BroadcastReceiver cryptoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String symbol = intent.getStringExtra("symbol");
            double price = intent.getDoubleExtra("price", 0);
            long timestamp = intent.getLongExtra("timestamp", 0);

            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(new Date(timestamp * 1000));

            CryptoItem item = new CryptoItem(symbol, price, time);
            requireActivity().runOnUiThread(() -> adapter.updateItem(item));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crypto, container, false);

        progressBar = view.findViewById(R.id.crypto_progress_bar);
        recyclerView = view.findViewById(R.id.recyclerView_crypto);

        adapter = new CryptoAdapter(cryptoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Ẩn luôn progress bar - hiển thị recycler ngay
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        // Đăng ký nhận broadcast
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().registerReceiver(
                    cryptoReceiver,
                    new IntentFilter("CRYPTO_UPDATE"),
                    Context.RECEIVER_NOT_EXPORTED
            );
        }

        // Gọi SSE Service
        String symbols = "btcusdt,ethusdt,bnbusdt,adausdt,xrpusdt,solusdt,dotusdt,avxusdt,ltcusdt,linkusdt,maticusdt,uniusdt,atomusdt,trxusdt,aptusdt,filusdt,nearusdt,icpusdt,vetusdt";
        Intent sseIntent = new Intent(requireContext(), CryptoSSEService.class);
        sseIntent.putExtra("symbols", symbols);
        requireContext().startService(sseIntent);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().unregisterReceiver(cryptoReceiver);
    }
}
