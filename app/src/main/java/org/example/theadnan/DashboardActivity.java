package org.example.theadnan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * DashboardActivity: convert DashboardController's UI here.
 */
public class DashboardActivity extends AppCompatActivity {

    private Button btnNotes, btnRequests, btnReports, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnNotes = findViewById(R.id.btnNotes);
        btnRequests = findViewById(R.id.btnRequests);
        btnReports = findViewById(R.id.btnReports);
        btnLogout = findViewById(R.id.btnLogout);

        btnNotes.setOnClickListener(v -> startActivity(new Intent(this, NotesActivity.class)));
        btnRequests.setOnClickListener(v -> startActivity(new Intent(this, MoneyRequestsActivity.class)));
        btnReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        btnLogout.setOnClickListener(v -> {
            Session.clear(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
