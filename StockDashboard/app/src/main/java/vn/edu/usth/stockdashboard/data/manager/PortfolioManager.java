package vn.edu.usth.stockdashboard.data.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.stockdashboard.data.model.StockItem;

public class PortfolioManager {

    private static final String PREF_NAME = "portfolio_pref";
    private static final String KEY_PORTFOLIO = "portfolio_list";
    private static final Gson gson = new Gson();

    // Lấy danh sách stock trong portfolio
    public static List<StockItem> getPortfolio(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PORTFOLIO, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<StockItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Thêm stock vào portfolio
    public static void addStock(Context context, StockItem stock) {
        List<StockItem> list = getPortfolio(context);

        // Nếu stock đã tồn tại, cập nhật quantity & giá trị
        boolean updated = false;
        for (StockItem s : list) {
            if (s.getSymbol().equalsIgnoreCase(stock.getSymbol())) {
                s.setQuantity(s.getQuantity() + stock.getQuantity());
                s.setInvestedValue(s.getInvestedValue() + stock.getInvestedValue());
                s.setCurrentValue(s.getCurrentValue() + stock.getCurrentValue());
                updated = true;
                break;
            }
        }

        if (!updated) list.add(stock);

        savePortfolio(context, list);
    }

    // Xóa stock khỏi portfolio
    public static void removeStock(Context context, String symbol) {
        List<StockItem> list = getPortfolio(context);
        list.removeIf(s -> s.getSymbol().equalsIgnoreCase(symbol));
        savePortfolio(context, list);
    }

    // Lưu portfolio vào SharedPreferences
    private static void savePortfolio(Context context, List<StockItem> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_PORTFOLIO, gson.toJson(list));
        editor.apply();
        Log.d("PortfolioManager", "Portfolio saved: " + list.size() + " items");
    }

    // Lấy tổng giá trị portfolio
    public static double getTotalInvested(Context context) {
        List<StockItem> list = getPortfolio(context);
        double total = 0;
        for (StockItem s : list) {
            total += s.getInvestedValue();
        }
        return total;
    }
}
