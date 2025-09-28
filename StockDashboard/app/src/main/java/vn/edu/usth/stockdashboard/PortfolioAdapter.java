package vn.edu.usth.stockdashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private List<Stock> stockList;

    public PortfolioAdapter(List<Stock> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = stockList.get(position);

        holder.ticker.setText(stock.getTicker());
        holder.price.setText(stock.getPrice());
        holder.invested.setText("Invested: " + stock.getInvested());
        holder.current.setText("Current: " + stock.getCurrent());
        holder.pnl.setText("P&L: " + stock.getPnl());

        // Màu P&L: lỗ = đỏ, lãi = xanh
        if (stock.getPnl().startsWith("-")) {
            holder.pnl.setTextColor(holder.itemView.getResources()
                    .getColor(android.R.color.holo_red_dark));
        } else {
            holder.pnl.setTextColor(holder.itemView.getResources()
                    .getColor(android.R.color.holo_green_dark));
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ticker, price, invested, current, pnl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.tvTicker);
            price = itemView.findViewById(R.id.tvPrice);
            invested = itemView.findViewById(R.id.tvInvested);
            current = itemView.findViewById(R.id.tvCurrent);
            pnl = itemView.findViewById(R.id.tvPnL);
        }
    }
}
