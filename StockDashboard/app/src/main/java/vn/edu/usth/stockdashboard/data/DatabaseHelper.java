package vn.edu.usth.stockdashboard.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "UserDatabase.db";
    public static final String TABLE_NAME = "users";
    public static final String COL_1 = "id";
    public static final String COL_2 = "username";
    public static final String COL_3 = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT UNIQUE, " +
                COL_3 + " TEXT)";
        db.execSQL(createTable);

        // Thêm user mẫu để test
        ContentValues testUser = new ContentValues();
        testUser.put(COL_2, "test");
        testUser.put(COL_3, "test");
        db.insert(TABLE_NAME, null, testUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, username);
        contentValues.put(COL_3, password);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // return true if successful
    }

    public String checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COL_2}, // chỉ lấy cột username
                COL_2 + "=? AND " + COL_3 + "=?",
                new String[]{username, password},
                null, null, null
        );

        String foundUsername = null;
        if (cursor.moveToFirst()) {
            foundUsername = cursor.getString(cursor.getColumnIndexOrThrow(COL_2));
        }

        cursor.close();
        db.close();
        return foundUsername;
    }

}