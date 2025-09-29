package vn.edu.usth.stockdashboard.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.List;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.StockItem;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<StockItem> stockList;

    public StockAdapter(List<StockItem> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockItem item = stockList.get(position);
        holder.symbol.setText(item.getSymbol());
        holder.time.setText(item.getTime());
        holder.price.setText("$ " + item.getPrice());
        holder.change.setText(String.valueOf(item.getChange()));
        holder.volume.setText(String.valueOf(item.getVolume()));

        // Ví dụ đổi màu nếu change âm
        if(item.getChange() < 0){
            holder.change.setTextColor(Color.RED);
        } else {
            holder.change.setTextColor(Color.GREEN);
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbol, time, price, change, volume;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            symbol = itemView.findViewById(R.id.txtSymbol);
            time   = itemView.findViewById(R.id.txtTime);
            price  = itemView.findViewById(R.id.txtPrice);
            change = itemView.findViewById(R.id.txtChange);
            volume = itemView.findViewById(R.id.txtVolume);
        }
    }
}
