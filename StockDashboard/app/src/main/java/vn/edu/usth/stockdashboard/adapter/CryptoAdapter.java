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
        setHasStableIds(true); // ✅ Enable stable IDs
    }

    @Override
    public long getItemId(int position) {
        // ✅ Return unique ID based on symbol
        return cryptoList.get(position).getSymbol().hashCode();
    }

    public void updateItem(CryptoItem item) {
        for (int i = 0; i < cryptoList.size(); i++) {
            if (cryptoList.get(i).getSymbol().equalsIgnoreCase(item.getSymbol())) {
                cryptoList.set(i, item);
                notifyItemChanged(i, item); // ✅ Use payload to avoid full rebind
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
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        CryptoItem item = cryptoList.get(position);

        // ✅ If payload exists, only update changed data
        if (payloads != null && !payloads.isEmpty()) {
            updateItemData(holder, item);
            return;
        }

        // Full bind
        holder.ctSymbol.setText(item.getSymbol().toUpperCase().replace("USDT", "/USDT"));
        holder.ctTime.setText(item.getTime());

        if (holder.ctVolume != null) {
            holder.ctVolume.setText("Vol: N/A");
        }

        updateItemData(holder, item);
    }

    private void updateItemData(ViewHolder holder, CryptoItem item) {
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
            backgroundColor = Color.parseColor("#4CAF50");
            textColor = Color.parseColor("#4CAF50");
        } else if (item.isPriceDown()) {
            backgroundColor = Color.parseColor("#F44336");
            textColor = Color.parseColor("#F44336");
        } else {
            backgroundColor = Color.parseColor("#9E9E9E");
            textColor = Color.parseColor("#9E9E9E");
        }

        // Set background for price with rounded corners
        GradientDrawable priceBackground = new GradientDrawable();
        priceBackground.setColor(backgroundColor);
        priceBackground.setCornerRadius(8f);
        holder.ctPrice.setBackground(priceBackground);

        // Set text color for change percent
        holder.ctChange.setTextColor(textColor);
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

            // ✅ Disable change animations on TextViews
            ctPrice.setHasTransientState(false);
            ctChange.setHasTransientState(false);
        }
    }
}