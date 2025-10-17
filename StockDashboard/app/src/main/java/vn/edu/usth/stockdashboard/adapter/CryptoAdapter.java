package vn.edu.usth.stockdashboard.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;

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

    public CryptoAdapter(List<CryptoItem> cryptoList) {
        this.cryptoList = cryptoList;
    }

    // ===== Cập nhật item từ SSE =====
    public void updateItem(CryptoItem item) {
        for (int i = 0; i < cryptoList.size(); i++) {
            if (cryptoList.get(i).getSymbol().equalsIgnoreCase(item.getSymbol())) {
                cryptoList.set(i, item);
                notifyItemChanged(i);
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
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CryptoItem item = cryptoList.get(position);

        h.ctSymbol.setText(item.getSymbol().toUpperCase().replace("USDT", "/USDT"));
        h.ctPrice.setText(String.format("$%.2f", item.getPrice()));
        h.ctTime.setText(item.getTime());

        // ✅ Hiển thị màu phần trăm thay đổi
        String changeText;
        if (item.getChangePercent() > 0) {
            changeText = String.format("▲ +%.2f%%", item.getChangePercent());
            h.ctChange.setTextColor(Color.parseColor("#4CAF50")); // xanh
        } else if (item.getChangePercent() < 0) {
            changeText = String.format("▼ %.2f%%", item.getChangePercent());
            h.ctChange.setTextColor(Color.parseColor("#F44336")); // đỏ
        } else {
            changeText = String.format("%.2f%%", item.getChangePercent());
            h.ctChange.setTextColor(Color.GRAY);
        }
        h.ctChange.setText(changeText);

        // ✅ Flash màu khi giá thay đổi
        Double last = lastPrices.get(item.getSymbol());
        if (last != null) {
            if (item.getPrice() > last)
                flashColor(h.ctPrice, Color.WHITE, Color.parseColor("#4CAF50"));
            else if (item.getPrice() < last)
                flashColor(h.ctPrice, Color.WHITE, Color.parseColor("#F44336"));
        }
        lastPrices.put(item.getSymbol(), item.getPrice());

        // ✅ Sự kiện click item
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
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
        TextView ctSymbol, ctPrice, ctChange, ctTime;
        ViewHolder(View v) {
            super(v);
            ctSymbol = v.findViewById(R.id.ctSymbol);
            ctPrice = v.findViewById(R.id.ctPrice);
            ctChange = v.findViewById(R.id.ctChange);
            ctTime = v.findViewById(R.id.ctTime);
        }
    }
}
