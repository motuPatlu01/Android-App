package org.example.theadnan.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.example.theadnan.AndroidDatabaseHelper;

/**
 * Lightweight AuthService adapted for Android.
 */
public class AuthService {

    private final AndroidDatabaseHelper dbHelper;

    public AuthService(AndroidDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean register(String email, String name, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("name", name);
        cv.put("password", password);
        long id = db.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return id != -1;
    }

    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT password FROM users WHERE email = ?", new String[]{email})) {
            if (c.moveToFirst()) {
                return password.equals(c.getString(0));
            }
            return false;
        }
    }
}
