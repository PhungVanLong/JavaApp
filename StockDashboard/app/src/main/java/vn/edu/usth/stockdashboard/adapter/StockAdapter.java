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
import vn.edu.usth.stockdashboard.Stock;
import vn.edu.usth.stockdashboard.StockItem;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.MyViewHolder> {

    private final List<StockItem> stockList;
    private OnItemClickListener listener;

    public StockAdapter(List<StockItem> stockList,OnItemClickListener listener) {
        this.stockList = stockList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_dashboard, parent, false);
        return new MyViewHolder(view);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView symbol, time, price, change, volume;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            symbol = itemView.findViewById(R.id.txtSymbol);
            time   = itemView.findViewById(R.id.txtTime);
            price  = itemView.findViewById(R.id.txtPrice);
            change = itemView.findViewById(R.id.txtChange);
            volume = itemView.findViewById(R.id.txtVolume);
        }


        public void bind(StockItem item, OnItemClickListener listener){
            symbol.setText(item.getSymbol());
            time.setText(item.getTime());
            price.setText("$"+item.getPrice());
            change.setText(String.valueOf(item.getChange()));
            volume.setText(String.valueOf(item.getVolume()));
            if (item.getChange() < 0) {
                change.setTextColor(Color.RED);
            } else {
                change.setTextColor(Color.GREEN);
            }
            itemView.setOnClickListener(view -> listener.onItemClick(item));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
       holder.bind(stockList.get(position),listener);

    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
    public interface OnItemClickListener {
        void onItemClick(StockItem item);
    }
}
