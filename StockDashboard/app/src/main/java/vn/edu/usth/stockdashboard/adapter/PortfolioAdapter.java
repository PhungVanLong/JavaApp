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

    // Constructor mới (2 tham số)
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
        StockItem item = stockList.get(position);

        holder.tvTicker.setText(item.getSymbol());
        holder.tvPrice.setText(String.format("%.2f USD", item.getPrice()));
        holder.tvInvested.setText("Invested: 1000"); // bạn có thể thay bằng dữ liệu thật
        holder.tvCurrent.setText(String.format("Current: %.0f", item.getPrice() * 10)); // ví dụ
        double pnl = item.getPrice() * 10 - 1000;
        holder.tvPnL.setText(String.format("PnL: %.0f", pnl));
        holder.tvPnL.setTextColor(pnl >= 0 ?
                holder.itemView.getResources().getColor(android.R.color.holo_green_light) :
                holder.itemView.getResources().getColor(android.R.color.holo_red_light));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicker, tvPrice, tvInvested, tvCurrent, tvPnL;

        public PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicker = itemView.findViewById(R.id.tvTicker);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvInvested = itemView.findViewById(R.id.tvInvested);
            tvCurrent = itemView.findViewById(R.id.tvCurrent);
            tvPnL = itemView.findViewById(R.id.tvPnL);
        }
    }
}
