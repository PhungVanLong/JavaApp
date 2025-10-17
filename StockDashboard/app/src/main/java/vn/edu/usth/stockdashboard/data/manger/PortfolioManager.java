package vn.edu.usth.stockdashboard.data.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class PortfolioManager {

    /**
     * Thêm hoặc thay thế một stock/crypto trong portfolio.
     * Nếu ticker trùng → xóa item cũ và insert item mới.
     */
    public static void addStock(Context context, StockItem stock, String P_username) {
        if (context == null || stock == null || P_username == null) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1️⃣ Kiểm tra tồn tại
        Cursor cursor = db.query(DatabaseHelper.TABLE_PORTFOLIO,
                new String[]{DatabaseHelper.P_COL_1},
                DatabaseHelper.P_COL_2 + "=? AND " + DatabaseHelper.P_COL_3 + "=?",
                new String[]{P_username, stock.getSymbol()},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Nếu tồn tại → xóa cũ
            db.delete(DatabaseHelper.TABLE_PORTFOLIO,
                    DatabaseHelper.P_COL_2 + "=? AND " + DatabaseHelper.P_COL_3 + "=?",
                    new String[]{P_username, stock.getSymbol()});
        }

        if (cursor != null) cursor.close();

        // 2️⃣ Thêm item mới
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.P_COL_2, P_username);
        values.put(DatabaseHelper.P_COL_3, stock.getSymbol());
        values.put(DatabaseHelper.P_COL_4, stock.getQuantity());
        values.put(DatabaseHelper.P_COL_5, stock.getPrice());

        db.insert(DatabaseHelper.TABLE_PORTFOLIO, null, values);

        db.close();
    }

    /**
     * Lấy portfolio của user
     * @return Cursor gồm 3 cột: ticker, quantity, avg_price
     */
    public static Cursor getPortfolioForUser(Context context, String P_username) {
        if (context == null || P_username == null) return null;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(DatabaseHelper.TABLE_PORTFOLIO,
                new String[]{DatabaseHelper.P_COL_3, DatabaseHelper.P_COL_4, DatabaseHelper.P_COL_5},
                DatabaseHelper.P_COL_2 + "=?",
                new String[]{P_username},
                null, null, null);
    }

    /**
     * Xóa toàn bộ portfolio của user
     */
    public static void clearPortfolio(Context context, String P_username) {
        if (context == null || P_username == null) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DatabaseHelper.TABLE_PORTFOLIO,
                DatabaseHelper.P_COL_2 + "=?",
                new String[]{P_username});

        db.close();
    }
}
