// StockAdapter.java
package vn.edu.usth.stockdashboard.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.StockItem;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final List<StockItem> stockList;
    private final  OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(StockItem item);
    }

    public StockAdapter(List<StockItem> stockList, OnItemClickListener listener) {
        this.stockList = stockList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_dashboard, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockItem item = stockList.get(position);
        holder.bind(item, listener);
    }

    // ===== HỖ TRỢ PARTIAL UPDATE =====
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Partial update - chỉ update data thay đổi
            StockItem newItem = (StockItem) payloads.get(0);
            holder.updateData(newItem);
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol, tvTime, tvPrice, tvChange, tvVolume;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymbol = itemView.findViewById(R.id.txtSymbol);
            tvTime = itemView.findViewById(R.id.txtTime);
            tvPrice = itemView.findViewById(R.id.txtPrice);
            tvChange = itemView.findViewById(R.id.txtChange);
            tvVolume = itemView.findViewById(R.id.txtVolume);
        }

        public void bind(StockItem item, OnItemClickListener listener) {
            tvSymbol.setText(item.getSymbol());
            updateData(item);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }

        // ===== UPDATE CHỈ DATA (KHÔNG RELOAD TOÀN BỘ VIEW) =====
        public void updateData(StockItem item) {
            // Update price
            tvPrice.setText(String.format("%.2f", item.getPrice()));

            // Update change
            double change = item.getChange();
            double percentChange = item.getPercentChange();
            String changeText = String.format("%+.2f (%+.2f%%)", change, percentChange);
            tvChange.setText(changeText);

            // Update color
            int color = change >= 0 ?
                    Color.parseColor("#4CAF50") :  // Xanh lá
                    Color.parseColor("#F44336");   // Đỏ

            tvChange.setTextColor(color);
            tvPrice.setTextColor(color);

            // Update volume
            tvVolume.setText(formatVolume(item.getVolume()));

            // Update time
            tvTime.setText(item.getTime());
        }

        private String formatVolume(long volume) {
            if (volume >= 1_000_000) {
                return String.format("%.1fM", volume / 1_000_000.0);
            } else if (volume >= 1_000) {
                return String.format("%.1fK", volume / 1_000.0);
            }
            return String.valueOf(volume);
        }
    }
}