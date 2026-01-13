package org.example.theadnan.services;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.example.theadnan.AndroidDatabaseHelper;

public class ReportService {
    private final AndroidDatabaseHelper dbHelper;

    public ReportService(AndroidDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long submitReport(String reporter, String target, String message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("reporter_email", reporter);
        cv.put("target_email", target);
        cv.put("message", message);
        cv.put("status", "PENDING");
        return db.insert("reports", null, cv);
    }
}
