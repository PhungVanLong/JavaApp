package vn.edu.usth.stockdashboard.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.data.model.CryptoItem;

public class CryptoAdapter extends RecyclerView.Adapter<CryptoAdapter.ViewHolder> {
    private final List<CryptoItem> cryptoList;
    private final OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(CryptoItem item);
    }


    public CryptoAdapter(List<CryptoItem> cryptoList, OnItemClickListener listener) {
        this.cryptoList = cryptoList;
        this.listener = listener;
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
        CryptoItem item = cryptoList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cryptoList.size();
    }

    public void updateList(List<CryptoItem> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CryptoDiffCallback(this.cryptoList, newList));
        this.cryptoList.clear();
        this.cryptoList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }
    // ✅ ViewHolder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ctSymbol, ctPrice, ctChange, ctTime;

        ViewHolder(View itemView) {
            super(itemView);
            ctSymbol = itemView.findViewById(R.id.ctSymbol);
            ctPrice = itemView.findViewById(R.id.ctPrice);
            ctChange = itemView.findViewById(R.id.ctChange);
            ctTime = itemView.findViewById(R.id.ctTime);
        }

        void bind(CryptoItem item, OnItemClickListener listener) {
            ctSymbol.setText(item.getSymbol().toUpperCase().replace("USDT", "/USDT"));
            ctTime.setText(item.getTime());
            ctPrice.setText(String.format("$%.2f", item.getPrice()));

            String changeText;
            if (item.getChangePercent() > 0) {
                changeText = String.format("▲ +%.2f%%", item.getChangePercent());
            } else if (item.getChangePercent() < 0) {
                changeText = String.format("▼ %.2f%%", item.getChangePercent());
            } else {
                changeText = String.format("%.2f%%", item.getChangePercent());
            }
            ctChange.setText(changeText);

            int color = item.isPriceUp() ? Color.parseColor("#4CAF50")
                    : item.isPriceDown() ? Color.parseColor("#F44336")
                    : Color.parseColor("#9E9E9E");

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(color);
            bg.setCornerRadius(8f);
            ctPrice.setBackground(bg);
            ctChange.setTextColor(color);

            // ✅ Gán sự kiện click Add to Portfolio
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
    static class CryptoDiffCallback extends DiffUtil.Callback {

        private final List<CryptoItem> oldList;
        private final List<CryptoItem> newList;

        public CryptoDiffCallback(List<CryptoItem> oldList, List<CryptoItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getSymbol()
                    .equals(newList.get(newItemPosition).getSymbol());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CryptoItem oldItem = oldList.get(oldItemPosition);
            CryptoItem newItem = newList.get(newItemPosition);
            return oldItem.getPrice() == newItem.getPrice()
                    && oldItem.getChangePercent() == newItem.getChangePercent();
        }
    }
}