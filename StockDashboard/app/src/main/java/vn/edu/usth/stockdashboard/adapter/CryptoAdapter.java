package vn.edu.usth.stockdashboard.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;
import vn.edu.usth.stockdashboard.CryptoDetailActivity;

public class CryptoAdapter extends RecyclerView.Adapter<CryptoAdapter.ViewHolder> {
    private final List<CryptoItem> cryptoList;
    private final Map<String, Double> lastPrices = new HashMap<>();
    private OnItemClickListener listener;

    // ===== Interface click item =====
    public interface OnItemClickListener {
        void onItemClick(CryptoItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    private final OnCryptoLongClickListener longClickListener; // thêm callback
    public interface OnCryptoLongClickListener {
        void onCryptoLongClick(CryptoItem item);
    }


    public CryptoAdapter(List<CryptoItem> cryptoList, OnCryptoLongClickListener longClickListener) {
        this.cryptoList = cryptoList;
        this.longClickListener = longClickListener;
        setHasStableIds(true); // Enable stable IDs
    }

    @Override
    public long getItemId(int position) {
        // Return unique ID based on symbol
        return cryptoList.get(position).getSymbol().hashCode();
    }

    // ===== Cập nhật item từ SSE =====
    public void updateItem(CryptoItem item) {
        for (int i = 0; i < cryptoList.size(); i++) {
            if (cryptoList.get(i).getSymbol().equalsIgnoreCase(item.getSymbol())) {
                cryptoList.set(i, item);
                notifyItemChanged(i, item); // Use payload to avoid full rebind
                return;
            }
        }
        cryptoList.add(item);
        notifyItemInserted(cryptoList.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crypto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        onBindViewHolder(holder, position, Collections.emptyList());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        CryptoItem item = cryptoList.get(position);

        if (payloads != null && !payloads.isEmpty()) {
            updateItemData(holder, item); // chỉ update dữ liệu thay đổi
            return;
        }

        // Full bind
        holder.ctSymbol.setText(item.getSymbol().toUpperCase().replace("USDT", "/USDT"));
        holder.ctPrice.setText(String.format("$%.2f", item.getPrice()));
        holder.ctTime.setText(item.getTime());

        if (holder.ctVolume != null) {
            holder.ctVolume.setText("Vol: N/A");
        }

        updateItemData(holder, item);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CryptoDetailActivity.class);
            intent.putExtra("symbol", item.getSymbol());
            intent.putExtra("name", getCryptoName(item.getSymbol()));
            intent.putExtra("price", item.getPrice());
            intent.putExtra("priceChange", item.getPrice() - item.getOpen());
            intent.putExtra("changePercent", item.getChangePercent());
            v.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onCryptoLongClick(item);
            }
            return true;
        });
    }



    private void updateItemData(ViewHolder holder, CryptoItem item) {
        // Set price with background highlight
        holder.ctPrice.setText(String.format("$%.2f", item.getPrice()));

        // Set change percent text
        String changeText;
        if (item.getChangePercent() > 0) {
            changeText = String.format("▲ +%.2f%%", item.getChangePercent());
            holder.ctChange.setTextColor(Color.parseColor("#4CAF50")); // xanh
        } else if (item.getChangePercent() < 0) {
            changeText = String.format("▼ %.2f%%", item.getChangePercent());
            holder.ctChange.setTextColor(Color.parseColor("#F44336")); // đỏ
        } else {
            changeText = String.format("%.2f%%", item.getChangePercent());
            holder.ctChange.setTextColor(Color.GRAY);
        }
        holder.ctChange.setText(changeText);

        // ✅ Flash màu khi giá thay đổi
        Double last = lastPrices.get(item.getSymbol());
        if (last != null) {
            if (item.getPrice() > last)
                flashColor(holder.ctPrice, Color.WHITE, Color.parseColor("#4CAF50"));
            else if (item.getPrice() < last)
                flashColor(holder.ctPrice, Color.WHITE, Color.parseColor("#F44336"));
        }
        lastPrices.put(item.getSymbol(), item.getPrice());

        // ✅ Sự kiện click item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    // Helper method to get crypto full name
    private String getCryptoName(String symbol) {
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("btcusdt", "Bitcoin");
        nameMap.put("ethusdt", "Ethereum");
        nameMap.put("bnbusdt", "BNB");
        nameMap.put("adausdt", "Cardano");
        nameMap.put("xrpusdt", "XRP");
        nameMap.put("solusdt", "Solana");
        nameMap.put("dotusdt", "Polkadot");
        nameMap.put("avxusdt", "Avalanche");
        nameMap.put("ltcusdt", "Litecoin");
        nameMap.put("linkusdt", "Chainlink");
        nameMap.put("maticusdt", "Polygon");
        nameMap.put("uniusdt", "Uniswap");
        nameMap.put("atomusdt", "Cosmos");
        nameMap.put("trxusdt", "Tron");
        nameMap.put("aptusdt", "Aptos");
        nameMap.put("filusdt", "Filecoin");
        nameMap.put("nearusdt", "Near");
        nameMap.put("icpusdt", "Internet Computer");
        nameMap.put("vetusdt", "VeChain");

        return nameMap.getOrDefault(symbol.toLowerCase(), symbol.toUpperCase());
    }
    private void flashColor(TextView tv, int from, int to) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), to, from);
        animator.setDuration(500);
        animator.addUpdateListener(a -> tv.setTextColor((int) a.getAnimatedValue()));
        animator.start();
    }

    @Override
    public int getItemCount() {
        return cryptoList.size();
    }

    // ===== ViewHolder =====
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ctSymbol, ctPrice, ctChange, ctTime, ctVolume;

        ViewHolder(View itemView) {
            super(itemView);
            ctSymbol = itemView.findViewById(R.id.ctSymbol);
            ctPrice = itemView.findViewById(R.id.ctPrice);
            ctChange = itemView.findViewById(R.id.ctChange);
            ctTime = itemView.findViewById(R.id.ctTime);
//            ctVolume = itemView.findViewById(R.id.ctVolume);

            // Disable change animations on TextViews
            ctPrice.setHasTransientState(false);
            ctChange.setHasTransientState(false);
        }
    }
}
