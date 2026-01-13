package org.example.theadnan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Simple Reports list screen.
 */
public class ReportsActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private ListView listReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new AndroidDatabaseHelper(this);
        listReports = findViewById(R.id.listReports);

        loadReports();
    }

    private void loadReports() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id, reporter_email, target_email, status FROM reports", null)) {
            ArrayList<String> items = new ArrayList<>();
            while (c.moveToNext()) {
                items.add("ID:" + c.getInt(0) + " " + c.getString(1) + "→" + c.getString(2) + " [" + c.getString(3) + "]");
            }
            listReports.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        }
    }
}
