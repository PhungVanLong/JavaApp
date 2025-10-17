package vn.edu.usth.stockdashboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class StockDialog extends DialogFragment {

    public interface OnPortfolioUpdatedListener {
        void onPortfolioUpdated();
    }

    private OnPortfolioUpdatedListener listener;

    public void setOnPortfolioUpdatedListener(OnPortfolioUpdatedListener listener) {
        this.listener = listener;
    }

    public static StockDialog newInstance(StockItem stockItem, String username) {
        StockDialog dialog = new StockDialog();
        Bundle args = new Bundle();
        args.putString("symbol", stockItem.getSymbol());
        args.putDouble("price", stockItem.getPrice());
        args.putString("username", username);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_stock, null);

        String symbol = getArguments().getString("symbol");
        double price = getArguments().getDouble("price");
        String username = getArguments().getString("username");

        EditText etQuantity = view.findViewById(R.id.etQuantity);
        EditText etPrice = view.findViewById(R.id.etPrice);
        etPrice.setText(String.valueOf(price));

        DatabaseHelper db = new DatabaseHelper(getContext());

        return new AlertDialog.Builder(requireContext())
                .setTitle("Manage " + symbol)
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String qtyStr = etQuantity.getText().toString();
                    String priceStr = etPrice.getText().toString();

                    if (qtyStr.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double qty = Double.parseDouble(qtyStr);
                        double buyPrice = Double.parseDouble(priceStr);

                        boolean success = db.addPortfolioItem(username, symbol, qty, buyPrice);
                        if (success) {
                            Toast.makeText(getContext(), "Added to portfolio", Toast.LENGTH_SHORT).show();
                            if (listener != null) listener.onPortfolioUpdated();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Remove", (d, w) -> {
                    boolean removed = db.deletePortfolioItem(username, symbol);
                    if (removed) {
                        Toast.makeText(getContext(), "Removed from portfolio", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onPortfolioUpdated();
                    } else {
                        Toast.makeText(getContext(), "Not found in portfolio", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}
