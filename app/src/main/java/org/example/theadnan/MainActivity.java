package org.example.theadnan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity: placeholder that launches Dashboard.
 */
public class MainActivity extends AppCompatActivity {

    private TextView txtWelcome;
    private Button btnDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtWelcome = findViewById(R.id.txtWelcome);
        btnDashboard = findViewById(R.id.btnOpenDashboard);

        String email = Session.getCurrentEmail(this);
        txtWelcome.setText("Welcome, " + (email == null ? "user" : email));

        btnDashboard.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
    }
}
