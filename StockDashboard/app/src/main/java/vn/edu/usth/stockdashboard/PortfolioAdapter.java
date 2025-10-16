package vn.edu.usth.stockdashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {
    private List<StockItem> stockList;
    private String currentUsername; // username để thao tác DB

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat pnlFormat = new DecimalFormat("+#,##0.00;-#,##0.00");

    // Constructor nhận List<StockItem> và username
    public PortfolioAdapter(List<StockItem> stockList, String username) {
        this.stockList = stockList;
        this.currentUsername = username;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_portfolio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockItem stockItem = stockList.get(position);
        Context context = holder.itemView.getContext();

        holder.ticker.setText(stockItem.getSymbol());
        holder.price.setText(currencyFormat.format(stockItem.getPrice()) + " USD");

        double investedValue = stockItem.getInvestedValue();
        double currentValue = stockItem.getCurrentValue();

        holder.invested.setText("Invested: " + currencyFormat.format(investedValue) + " USD");
        holder.current.setText("Current: " + currencyFormat.format(currentValue) + " USD");

        double pnlValue = currentValue - investedValue;
        holder.pnl.setText("P&L: " + pnlFormat.format(pnlValue) + " USD");

        if (pnlValue < 0) {
            holder.pnl.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else if (pnlValue > 0) {
            holder.pnl.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.pnl.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }

        // Delete button listener
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Stock")
                    .setMessage("Are you sure you want to delete " + stockItem.getSymbol() + " from portfolio?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DatabaseHelper db = new DatabaseHelper(context);
                        boolean success = db.deletePortfolioItem(currentUsername, stockItem.getSymbol());
                        if (success) {
                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                stockList.remove(pos);
                                notifyItemRemoved(pos);
                                notifyItemRangeChanged(pos, stockList.size());
                                Toast.makeText(context, "Deleted " + stockItem.getSymbol(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return stockList == null ? 0 : stockList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ticker, price, invested, current, pnl;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.tvTicker);
            price = itemView.findViewById(R.id.tvPrice);
            invested = itemView.findViewById(R.id.tvInvested);
            current = itemView.findViewById(R.id.tvCurrent);
            pnl = itemView.findViewById(R.id.tvPnL);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
