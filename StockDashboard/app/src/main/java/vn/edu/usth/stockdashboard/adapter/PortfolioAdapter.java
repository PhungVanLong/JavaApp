package vn.edu.usth.stockdashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.StockItem;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private List<StockItem> stockList;

    public PortfolioAdapter(List<StockItem> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_portfolio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockItem stock = stockList.get(position);
        holder.symbolText.setText(stock.getSymbol());
        holder.priceText.setText(String.format("%.2f", stock.getPrice()));
        holder.volumeText.setText(String.format("Vol: %d", stock.getVolume()));
    }

    @Override
    public int getItemCount() {
        return stockList != null ? stockList.size() : 0;
    }

    public void updateData(List<StockItem> newList) {
        this.stockList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbolText, priceText, volumeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolText = itemView.findViewById(R.id.textSymbol);
            priceText = itemView.findViewById(R.id.textPrice);
            volumeText = itemView.findViewById(R.id.textVolume);
        }
    }
}
