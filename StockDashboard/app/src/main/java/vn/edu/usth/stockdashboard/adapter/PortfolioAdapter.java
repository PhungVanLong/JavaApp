package vn.edu.usth.stockdashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    private List<StockItem> stockList;
    private String username; // thêm dòng này

    // ✅ Constructor mới (2 tham số)
    public PortfolioAdapter(List<StockItem> stockList, String username) {
        this.stockList = stockList;
        this.username = username;
    }

    public PortfolioAdapter(List<StockItem> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_portfolio, parent, false); // dùng file XML bạn vừa gửi
        return new PortfolioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {
        StockItem stock = stockList.get(position);

        holder.tvTicker.setText(stock.getSymbol());
        holder.tvPrice.setText("Price: " + String.format("%.2f", stock.getPrice()));
        holder.tvQuantity.setText("Quantity: " + stock.getQuantity());

        double invested = stock.getInvestedValue();
        double current = stock.getCurrentValue();
        double pnl = current - invested;
        double pnlPercent = (invested != 0) ? (pnl / invested * 100) : 0;

        holder.tvInvested.setText("Invested: " + String.format("%.2f", invested));
        holder.tvCurrent.setText("Current: " + String.format("%.2f", current));

        holder.tvPnL.setText(String.format("%.2f (%.2f%%)", pnl, pnlPercent));
        holder.tvPnL.setTextColor(pnl >= 0
                ? holder.itemView.getContext().getColor(android.R.color.holo_green_light)
                : holder.itemView.getContext().getColor(android.R.color.holo_red_light));
    }


    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicker, tvPrice, tvQuantity, tvInvested, tvCurrent, tvPnL;

        public PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicker = itemView.findViewById(R.id.tvTicker);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvInvested = itemView.findViewById(R.id.tvInvested);
            tvCurrent = itemView.findViewById(R.id.tvCurrent);
            tvPnL = itemView.findViewById(R.id.tvPnL);
        }
    }
}
