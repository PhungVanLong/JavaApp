package vn.edu.usth.stockdashboard.data.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class PortfolioManager {

    /**
     * Thêm hoặc cập nhật một mã cổ phiếu/crypto vào danh mục đầu tư.
     * Nếu ticker đã tồn tại cho user → cập nhật quantity + avg_price.
     * Nếu chưa tồn tại → thêm mới.
     */
    public static void addStock(Context context, StockItem stock, String username) {
        if (context == null || stock == null || username == null) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT quantity, avg_price FROM " + DatabaseHelper.TABLE_PORTFOLIO +
                        " WHERE username = ? AND ticker = ?",
                new String[]{username, stock.getSymbol()}
        );

        if (cursor != null && cursor.moveToFirst()) {
            // --- Đã có ticker này trong danh mục ---
            int oldQuantity = cursor.getInt(0);
            double oldAvgPrice = cursor.getDouble(1);

            double newQuantity = oldQuantity + stock.getQuantity();
            double newAvgPrice = ((oldAvgPrice * oldQuantity) + (stock.getPrice() * stock.getQuantity())) / newQuantity;

            ContentValues values = new ContentValues();
            values.put("quantity", newQuantity);
            values.put("avg_price", newAvgPrice);

            db.update(DatabaseHelper.TABLE_PORTFOLIO, values,
                    "username = ? AND ticker = ?",
                    new String[]{username, stock.getSymbol()});

        } else {
            // --- Chưa có -> thêm mới ---
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("ticker", stock.getSymbol());
            values.put("quantity", stock.getQuantity());
            values.put("avg_price", stock.getPrice());

            db.insert(DatabaseHelper.TABLE_PORTFOLIO, null, values);
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    /**
     * Lấy danh mục đầu tư của một user (toàn bộ cổ phiếu/crypto đã mua).
     * @return Cursor gồm 3 cột: ticker, quantity, avg_price
     */
    public static Cursor getPortfolioForUser(Context context, String username) {
        if (context == null || username == null) return null;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.rawQuery(
                "SELECT ticker, quantity, avg_price FROM " + DatabaseHelper.TABLE_PORTFOLIO +
                        " WHERE username = ?",
                new String[]{username}
        );
    }

    /**
     * Cập nhật lại thông tin mã đã có trong danh mục (ví dụ đổi giá trung bình).
     */
    public static void updateStockIfExists(Context context, StockItem stock, String username) {
        if (context == null || stock == null || username == null) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("quantity", stock.getQuantity());
        values.put("avg_price", stock.getPrice());

        db.update(DatabaseHelper.TABLE_PORTFOLIO, values,
                "username = ? AND ticker = ?",
                new String[]{username, stock.getSymbol()});

        db.close();
    }

    /**
     * Xóa toàn bộ danh mục của user (nếu cần reset).
     */
    public static void clearPortfolio(Context context, String username) {
        if (context == null || username == null) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DatabaseHelper.TABLE_PORTFOLIO,
                "username = ?",
                new String[]{username});

        db.close();
    }
}
