package org.example.theadnan.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.example.theadnan.AndroidDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MoneyRequestService {

    private final AndroidDatabaseHelper dbHelper;

    public MoneyRequestService(AndroidDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long createRequest(String from, String to, double amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("from_email", from);
        cv.put("to_email", to);
        cv.put("amount", amount);
        cv.put("status", "PENDING");
        return db.insert("money_requests", null, cv);
    }

    public List<String> listFor(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id, from_email, to_email, amount, status FROM money_requests WHERE from_email = ? OR to_email = ?", new String[]{email, email})) {
            ArrayList<String> out = new ArrayList<>();
            while (c.moveToNext()) out.add("ID:" + c.getInt(0) + " " + c.getString(1) + "→" + c.getString(2));
            return out;
        }
    }
}
