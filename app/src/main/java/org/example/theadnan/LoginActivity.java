package org.example.theadnan;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity: authenticates against users table created by AndroidDatabaseHelper.
 */
public class LoginActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private EditText emailInput, passwordInput;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new AndroidDatabaseHelper(this);

        emailInput = findViewById(R.id.editEmail);
        passwordInput = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT password, is_admin, blocked FROM users WHERE email = ?", new String[]{email})) {
            if (c.moveToFirst()) {
                String dbPass = c.getString(0);
                int isAdmin = c.getInt(1);
                int blocked = c.getInt(2);
                if (blocked == 1) {
                    Toast.makeText(this, "Account blocked", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.equals(dbPass)) {
                    Session.saveSession(this, email, isAdmin == 1);
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
