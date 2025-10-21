package vn.edu.usth.stockdashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    private final List<StockItem> stockList;
    private OnPortfolioActionListener listener;

    public PortfolioAdapter(List<StockItem> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_portfolio, parent, false);
        return new PortfolioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {
        StockItem item = stockList.get(position);

        holder.tvTicker.setText(item.getSymbol());
        holder.tvPrice.setText(String.format("%.2f USD", item.getPrice()));
        holder.tvQuantity.setText(String.format("Quantity: %.2f @ %.2f", item.getQuantity(), item.getInvestedValue() / item.getQuantity()));
        holder.tvInvested.setText(String.format("Invested: %.0f", item.getInvestedValue()));
        holder.tvCurrent.setText(String.format("Current: %.0f", item.getCurrentValue()));

        double pnl = item.getCurrentValue() - item.getInvestedValue();
        holder.tvPnL.setText(String.format("PnL: %.0f", pnl));
        holder.tvPnL.setTextColor(pnl >= 0 ?
                holder.itemView.getResources().getColor(android.R.color.holo_green_light) :
                holder.itemView.getResources().getColor(android.R.color.holo_red_light));

        // Nút xóa
        holder.btnDeleteStock.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteStock(item.getSymbol());
        });
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public void setOnPortfolioActionListener(OnPortfolioActionListener listener) {
        this.listener = listener;
    }

    static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicker, tvPrice, tvQuantity, tvInvested, tvCurrent, tvPnL;
        Button btnDeleteStock;

        public PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicker = itemView.findViewById(R.id.tvTicker);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvInvested = itemView.findViewById(R.id.tvInvested);
            tvCurrent = itemView.findViewById(R.id.tvCurrent);
            tvPnL = itemView.findViewById(R.id.tvPnL);
            btnDeleteStock = itemView.findViewById(R.id.btnDeleteStock);
        }
    }

    public interface OnPortfolioActionListener {
        void onDeleteStock(String ticker);
    }
}
