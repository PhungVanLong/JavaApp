package vn.edu.usth.stockdashboard.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;

public class CryptoAdapter extends RecyclerView.Adapter<CryptoAdapter.ViewHolder> {
    private final List<CryptoItem> cryptoList;

    public CryptoAdapter(List<CryptoItem> cryptoList) {
        this.cryptoList = cryptoList;
    }

    public void updateItem(CryptoItem item) {
        for (int i = 0; i < cryptoList.size(); i++) {
            if (cryptoList.get(i).getSymbol().equalsIgnoreCase(item.getSymbol())) {
                cryptoList.set(i, item);
                notifyItemChanged(i);
                return;
            }
        }
        cryptoList.add(item);
        notifyItemInserted(cryptoList.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crypto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CryptoItem item = cryptoList.get(position);

        // Set symbol
        holder.ctSymbol.setText(item.getSymbol().toUpperCase().replace("USDT", "/USDT"));

        // Set price with background highlight
        holder.ctPrice.setText(String.format("$%.2f", item.getPrice()));

        // Set change percent text
        String changeText;
        if (item.getChangePercent() > 0) {
            changeText = String.format("▲ +%.2f%%", item.getChangePercent());
        } else if (item.getChangePercent() < 0) {
            changeText = String.format("▼ %.2f%%", item.getChangePercent());
        } else {
            changeText = String.format("%.2f%%", item.getChangePercent());
        }
        holder.ctChange.setText(changeText);

        // Apply colors based on price change
        int backgroundColor;
        int textColor;

        if (item.isPriceUp()) {
            backgroundColor = Color.parseColor("#4CAF50"); // Green background
            textColor = Color.parseColor("#4CAF50");       // Green text
        } else if (item.isPriceDown()) {
            backgroundColor = Color.parseColor("#F44336"); // Red background
            textColor = Color.parseColor("#F44336");       // Red text
        } else {
            backgroundColor = Color.parseColor("#9E9E9E"); // Gray background
            textColor = Color.parseColor("#9E9E9E");       // Gray text
        }

        // Set background for price with rounded corners
        GradientDrawable priceBackground = new GradientDrawable();
        priceBackground.setColor(backgroundColor);
        priceBackground.setCornerRadius(8f);
        holder.ctPrice.setBackground(priceBackground);

        // Set text color for change percent
        holder.ctChange.setTextColor(textColor);

        // Set time
        holder.ctTime.setText(item.getTime());

        // Set volume if available
        if (holder.ctVolume != null) {
            holder.ctVolume.setText("Vol: N/A");
        }
    }

    @Override
    public int getItemCount() {
        return cryptoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ctSymbol, ctPrice, ctChange, ctTime, ctVolume;

        ViewHolder(View itemView) {
            super(itemView);
            ctSymbol = itemView.findViewById(R.id.ctSymbol);
            ctPrice = itemView.findViewById(R.id.ctPrice);
            ctChange = itemView.findViewById(R.id.ctChange);
            ctTime = itemView.findViewById(R.id.ctTime);
//            ctVolume = itemView.findViewById(R.id.ctVolume);
        }
    }
}