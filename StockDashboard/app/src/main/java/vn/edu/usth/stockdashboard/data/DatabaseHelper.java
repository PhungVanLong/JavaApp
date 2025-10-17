package vn.edu.usth.stockdashboard.data;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "UserDatabase.db";
    public static final String TABLE_NAME = "users";
    public static final String COL_1 = "id";
    public static final String COL_2 = "username";
    public static final String COL_3 = "password";

    // Database cho Portfolio
    public static final String TABLE_PORTFOLIO = "portfolio";
    public static final String P_COL_1 = "P_id"; // id của item (prim key)
    public static final String P_COL_2 = "P_username"; // username liên kết với portfolio (foreign key)
    public static final String P_COL_3 = "ticker"; // Mã cổ phiếu
    public static final String P_COL_4 = "quantity"; // số lượng sở hữu
    public static final String P_COL_5 = "avg_price"; // giá mua trung bình

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng users
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT UNIQUE, " + // username
                COL_3 + " TEXT)";          // password
        db.execSQL(createTable);

        // Bảng portfolio
        String createPortfolioTable = "CREATE TABLE " + TABLE_PORTFOLIO + " (" +
                P_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                P_COL_2 + " TEXT, " + // username
                P_COL_3 + " TEXT, " + // ticker
                P_COL_4 + " REAL, " + // quantity
                P_COL_5 + " REAL, " + // avg_price
                "FOREIGN KEY(" + P_COL_2 + ") REFERENCES " + TABLE_NAME + "(" + COL_2 + ")" +
                ")";
        db.execSQL(createPortfolioTable);
        Log.d(TAG, "Table " + TABLE_PORTFOLIO + " created.");

        // Thêm user mẫu
        ContentValues testUser = new ContentValues();
        testUser.put(COL_2, "test");
        testUser.put(COL_3, "test");
        db.insert(TABLE_NAME, null, testUser);
        Log.d(TAG, "Test user inserted.");

        // Thêm stock mẫu vào portfolio
        addSamplePortfolio(db, "test", "AAPL", 5.0, 150.0);
        addSamplePortfolio(db, "test", "GOOG", 1.5, 2500.0);
    }

    private void addSamplePortfolio(SQLiteDatabase db, String username, String ticker, double quantity, double avgPrice) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(P_COL_2, username);
        contentValues.put(P_COL_3, ticker);
        contentValues.put(P_COL_4, quantity);
        contentValues.put(P_COL_5, avgPrice);
        db.insert(TABLE_PORTFOLIO, null, contentValues);
        Log.d(TAG, "Sample portfolio added for user: " + username);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PORTFOLIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, username);
        contentValues.put(COL_3, password);
        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }

    public String checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COL_2},
                COL_2 + "=? AND " + COL_3 + "=?",
                new String[]{username, password},
                null, null, null
        );

        String foundUsername = null;
        if (cursor.moveToFirst()) {
            foundUsername = cursor.getString(cursor.getColumnIndexOrThrow(COL_2));
        }

        cursor.close();
        return foundUsername;
    }

    public boolean hasStockInPortfolio(String username, String ticker) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PORTFOLIO,
                new String[]{P_COL_1},
                P_COL_2 + "=? AND " + P_COL_3 + "=?",
                new String[]{username, ticker},
                null, null, null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean updatePortfolioItem(String username, String ticker, double newQuantity, double newAvgPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(P_COL_4, newQuantity);
        contentValues.put(P_COL_5, newAvgPrice);

        int result = db.update(
                TABLE_PORTFOLIO,
                contentValues,
                P_COL_2 + "=? AND " + P_COL_3 + "=?",
                new String[]{username, ticker}
        );
        db.close();
        return result > 0;
    }

    public Cursor getPortfolioItem(String username, String ticker) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_PORTFOLIO,
                new String[]{P_COL_4, P_COL_5}, // quantity, avg_price
                P_COL_2 + "=? AND " + P_COL_3 + "=?",
                new String[]{username, ticker},
                null, null, null
        );
    }

    public boolean addPortfolioItem(String username, String symbol, double quantity, double buyPrice) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Kiểm tra xem stock đã có chưa
        Cursor cursor = db.rawQuery("SELECT " + P_COL_4 + ", " + P_COL_5 +
                        " FROM " + TABLE_PORTFOLIO + " WHERE " + P_COL_2 + "=? AND " + P_COL_3 + "=?",
                new String[]{username, symbol});

        if (cursor.moveToFirst()) {
            double oldQty = cursor.getDouble(0);
            double oldPrice = cursor.getDouble(1);

            double newQty = oldQty + quantity;
            double avgPrice = ((oldQty * oldPrice) + (quantity * buyPrice)) / newQty;

            ContentValues values = new ContentValues();
            values.put(P_COL_4, newQty);
            values.put(P_COL_5, avgPrice);

            int updated = db.update(TABLE_PORTFOLIO, values, P_COL_2 + "=? AND " + P_COL_3 + "=?",
                    new String[]{username, symbol});
            cursor.close();
            db.close();
            return updated > 0;
        } else {
            ContentValues values = new ContentValues();
            values.put(P_COL_2, username);
            values.put(P_COL_3, symbol);
            values.put(P_COL_4, quantity);
            values.put(P_COL_5, buyPrice);

            long result = db.insert(TABLE_PORTFOLIO, null, values);
            cursor.close();
            db.close();
            return result != -1;
        }
    }

    public boolean deletePortfolioItem(String username, String ticker) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(
                TABLE_PORTFOLIO,
                P_COL_2 + "=? AND " + P_COL_3 + "=?",
                new String[]{username, ticker}
        );
        db.close();
        return result > 0;
    }

    public Cursor getPortfolioForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {P_COL_3, P_COL_4, P_COL_5}; // ticker, quantity, avg_price

        Cursor cursor = db.query(
                TABLE_PORTFOLIO,
                columns,
                P_COL_2 + "=?",
                new String[]{username},
                null, null, null
        );
        return cursor;
    }
}
