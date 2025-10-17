package vn.edu.usth.stockdashboard.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.usth.stockdashboard.CryptoDetailActivity;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.adapter.CryptoAdapter;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;
import vn.edu.usth.stockdashboard.data.sse.service.CryptoSSEService;

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
        recyclerView = view.findViewById(R.id.recyclerView_crypto);

        setupRecyclerView();
        setupAdapter();
        setupCryptoReceiver(); // Chuẩn bị receiver

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        // Tối ưu hiệu năng, không chạy animation khi dữ liệu thay đổi
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    private void setupAdapter() {
        adapter = new CryptoAdapter(cryptoList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(getContext(), CryptoDetailActivity.class);
            intent.putExtra("symbol", item.getSymbol());
            // Lấy tên đầy đủ thay vì chỉ symbol
            intent.putExtra("name", item.getSymbol().replace("usdt", "").toUpperCase());
            startActivity(intent);
        });
    }

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
        requireActivity().registerReceiver(cryptoReceiver, filter, Context.RECEIVER_EXPORTED);
        // 2. Khởi động service để lấy dữ liệu
        startSSEService();
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
        }
    }

    // Bỏ onActivityResult vì onResume đã xử lý việc restart service
    // Bỏ onDestroyView vì onPause đã xử lý việc unregister receiver
}