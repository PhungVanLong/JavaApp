package vn.edu.usth.stockdashboard.data.manager;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class PortfolioManager {

    private static DatabaseHelper dbHelper;

    // Khởi tạo DatabaseHelper
    public static void init(Context context) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context.getApplicationContext());
        }
    }

    // Thêm stock vào portfolio (cập nhật nếu đã tồn tại)
    public static void addStock(Context context, StockItem stock, String username) {
        init(context);
        new Thread(() -> {
            double avgPrice = stock.getQuantity() > 0 ? stock.getInvestedValue() / stock.getQuantity() : 0;
            dbHelper.addPortfolioItem(username, stock.getSymbol(), stock.getQuantity(), avgPrice);
            Log.d("PortfolioManager", "Stock added: " + stock.getSymbol() + ", qty=" + stock.getQuantity());
        }).start();
    }

    // Xóa stock khỏi portfolio
    public static void removeStock(Context context, String symbol, String username) {
        init(context);
        new Thread(() -> {
            boolean deleted = dbHelper.deletePortfolioItem(username, symbol);
            Log.d("PortfolioManager", "Stock removed: " + symbol + ", success=" + deleted);
        }).start();
    }

    // Lấy toàn bộ portfolio của user
    public static List<StockItem> getPortfolio(Context context, String username) {
        init(context);
        List<StockItem> list = new ArrayList<>();
        Cursor cursor = dbHelper.getPortfolioForUser(username);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String symbol = cursor.getString(cursor.getColumnIndexOrThrow("ticker"));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
                double avgPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("avg_price"));

                StockItem item = new StockItem(symbol, quantity * avgPrice, quantity * avgPrice);
                item.setQuantity((int) quantity);
                list.add(item);
            }
            cursor.close();
        }
        return list;
    }

    // Kiểm tra stock đã tồn tại
    public static boolean hasStock(Context context, String symbol, String username) {
        init(context);
        return dbHelper.hasStockInPortfolio(username, symbol);
    }
}
