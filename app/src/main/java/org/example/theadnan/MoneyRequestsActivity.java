package org.example.theadnan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Simple Money Requests list screen (incoming/outgoing).
 */
public class MoneyRequestsActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private ListView listRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        dbHelper = new AndroidDatabaseHelper(this);
        listRequests = findViewById(R.id.listRequests);

        loadRequests();
    }

    private void loadRequests() {
        String email = Session.getCurrentEmail(this);
        if (email == null) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id, from_email, to_email, amount, status FROM money_requests WHERE from_email = ? OR to_email = ?",
                new String[]{email, email})) {
            ArrayList<String> items = new ArrayList<>();
            while (c.moveToNext()) {
                items.add("ID:" + c.getInt(0) + " " + c.getString(1) + "→" + c.getString(2) + " $" + c.getDouble(3) + " [" + c.getString(4) + "]");
            }
            listRequests.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        }
    }
}
