package org.example.theadnan.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.example.theadnan.AndroidDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotesService {

    private final AndroidDatabaseHelper dbHelper;

    public NotesService(AndroidDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean addNote(String userEmail, String title, String content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_email", userEmail);
        cv.put("title", title);
        cv.put("content", content);
        long id = db.insertWithOnConflict("notes", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return id != -1;
    }

    public List<String> listTitles(String userEmail) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT title FROM notes WHERE user_email = ?", new String[]{userEmail})) {
            ArrayList<String> out = new ArrayList<>();
            while (c.moveToNext()) out.add(c.getString(0));
            return out;
        }
    }
}
