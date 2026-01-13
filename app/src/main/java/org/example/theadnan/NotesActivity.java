package org.example.theadnan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Simple Notes list screen.
 */
public class NotesActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private ListView listNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        dbHelper = new AndroidDatabaseHelper(this);
        listNotes = findViewById(R.id.listNotes);

        loadNotes();
    }

    private void loadNotes() {
        String email = Session.getCurrentEmail(this);
        if (email == null) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT title FROM notes WHERE user_email = ?", new String[]{email})) {
            ArrayList<String> titles = new ArrayList<>();
            while (c.moveToNext()) {
                titles.add(c.getString(0));
            }
            listNotes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles));
        }
    }
}
