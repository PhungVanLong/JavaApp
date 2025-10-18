package vn.edu.usth.stockdashboard.data.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import vn.edu.usth.stockdashboard.data.DatabaseHelper;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class PortfolioManager {

    public static void addStock(Context context, StockItem stock, String P_username) {
        if (context == null || stock == null || P_username == null) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PORTFOLIO,
                new String[]{DatabaseHelper.P_COL_1},
                DatabaseHelper.P_COL_2 + "=? AND " + DatabaseHelper.P_COL_3 + "=?",
                new String[]{P_username, stock.getSymbol()},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            db.delete(DatabaseHelper.TABLE_PORTFOLIO,
                    DatabaseHelper.P_COL_2 + "=? AND " + DatabaseHelper.P_COL_3 + "=?",
                    new String[]{P_username, stock.getSymbol()});
        }

        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.P_COL_2, P_username);
        values.put(DatabaseHelper.P_COL_3, stock.getSymbol());
        values.put(DatabaseHelper.P_COL_4, stock.getQuantity());
        values.put(DatabaseHelper.P_COL_5, stock.getPrice());

        db.insert(DatabaseHelper.TABLE_PORTFOLIO, null, values);

        db.close();
    }

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
