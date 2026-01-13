package org.example.theadnan;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Reports screen: handles submitting and viewing reports.
 */
public class ReportsActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private ListView listReports;
    private EditText etTargetEmail, etReportMessage;
    private Button btnSubmitReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new AndroidDatabaseHelper(this);
        listReports = findViewById(R.id.listReports);
        etTargetEmail = findViewById(R.id.etTargetEmail);
        etReportMessage = findViewById(R.id.etReportMessage);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);

        btnSubmitReport.setOnClickListener(v -> submitReport());

        loadReports();
    }

    private void submitReport() {
        String reporterEmail = Session.getCurrentEmail(this);
        String targetEmail = etTargetEmail.getText().toString().trim();
        String message = etReportMessage.getText().toString().trim();

        if (targetEmail.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("reporter_email", reporterEmail);
        cv.put("target_email", targetEmail);
        cv.put("message", message);
        cv.put("status", "OPEN");
        cv.put("created_at", String.valueOf(System.currentTimeMillis()));

        long id = db.insert("reports", null, cv);
        if (id != -1) {
            Toast.makeText(this, "Report submitted!", Toast.LENGTH_SHORT).show();
            etTargetEmail.setText("");
            etReportMessage.setText("");
            loadReports();
        } else {
            Toast.makeText(this, "Error submitting report", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadReports() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Typically only admins see all reports, but for this port we show them for debugging/visibility
        try (Cursor c = db.rawQuery("SELECT id, reporter_email, target_email, message, status FROM reports ORDER BY id DESC", null)) {
            ArrayList<String> items = new ArrayList<>();
            while (c.moveToNext()) {
                items.add("Report #" + c.getInt(0) + " by " + c.getString(1) + "\nTarget: " + c.getString(2) + "\nReason: " + c.getString(3) + "\nStatus: " + c.getString(4));
            }
            listReports.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        }
    }
}
