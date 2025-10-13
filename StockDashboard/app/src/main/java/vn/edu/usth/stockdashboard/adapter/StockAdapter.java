// StockAdapter.java - REFINED VERSION
package vn.edu.usth.stockdashboard.adapter;

import android.graphics.Color;
import android.os.Bundle;
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
    private final OnItemClickListener listener;

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

    // This is for a FULL bind (when the view is first created)
    // Dùng cho việc bind TOÀN BỘ (khi view được tạo lần đầu)
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        holder.bind(stockList.get(position), listener);
    }

    // This is for PARTIAL updates (when only some data changes)
    // Dùng cho việc cập nhật TỪNG PHẦN (khi chỉ một vài dữ liệu thay đổi)
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            // If no payload, do a full bind
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // If there is a payload, do a partial update
            // Payload is a Bundle of changes
            Bundle diffBundle = (Bundle) payloads.get(0);
            holder.updateFromPayload(diffBundle);
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    // --- ViewHolder remains largely the same, but logic is separated ---
    static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol, tvTime, tvPrice, tvChange, tvVolume;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymbol = itemView.findViewById(R.id.txtSymbol);
            tvTime = itemView.findViewById(R.id.txtTime);
            tvPrice = itemView.findViewById(R.id.txtPrice);
            tvChange = itemView.findViewById(R.id.txtChange);
            tvVolume = itemView.findViewById(R.id.txtVolume); // Assuming R.id.txtVolume exists
        }

        // Binds all data, including static data like the symbol
        public void bind(StockItem item, OnItemClickListener listener) {
            tvSymbol.setText(item.getSymbol());
            bindDynamicData(item); // Bind the data that changes

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }

        // Binds only the data that is expected to change
        // Inside StockAdapter.java -> StockViewHolder class

        private void bindDynamicData(StockItem item) {
            // 1. Set Price
            tvPrice.setText(String.format(java.util.Locale.US, "%.2f", item.getPrice()));

            // 2. *** THE FIX: Format a string with BOTH change and percent change ***
            double change = item.getChange();
            double percentChange = item.getPercentChange();
            // This format creates a string like "+0.10 (+1.18%)"
            String changeText = String.format(java.util.Locale.US, "%+.2f (%+.2f%%)", change, percentChange);
            tvChange.setText(changeText);

            // 3. Set the color based on the change value
            int color = change >= 0 ?
                    Color.parseColor("#4CAF50") :  // Green
                    Color.parseColor("#F44336");   // Red
            tvPrice.setTextColor(color);
            tvChange.setTextColor(color);

            // 4. Set Volume (and hide it for VNI, which doesn't have volume)
            if (item.getSymbol().equals("VNI")) {
                tvVolume.setText("");
            } else {
                tvVolume.setText(formatVolume(item.getVolume()));
            }

            // 5. Set the Time
            tvTime.setText(item.getTime());
        }

        // Updates the view based on a payload Bundle
        public void updateFromPayload(Bundle payload) {
            if (payload.containsKey("PRICE")) {
                double price = payload.getDouble("PRICE");
                tvPrice.setText(String.format(java.util.Locale.US, "%.2f", price));
            }
            if (payload.containsKey("CHANGE")) {
                double change = payload.getDouble("CHANGE");
                tvChange.setText(String.format(java.util.Locale.US, "%+.2f", change));

                int color = change >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336");
                tvPrice.setTextColor(color);
                tvChange.setTextColor(color);
            }
            if (payload.containsKey("VOLUME")) {
                double volume = payload.getDouble("VOLUME");
                tvVolume.setText(formatVolume(volume));
            }
            if (payload.containsKey("TIME")) {
                tvTime.setText(payload.getString("TIME"));
            }
        }

        private String formatVolume(double volume) {
            if (volume >= 1_000_000) {
                return String.format(java.util.Locale.US, "%.1fM", volume / 1_000_000.0);
            } else if (volume >= 1_000) {
                return String.format(java.util.Locale.US, "%.1fK", volume / 1_000.0);
            }
            return String.valueOf((long)volume);
        }
    }
}