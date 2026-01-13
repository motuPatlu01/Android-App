package org.example.theadnan;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Minimal RegisterActivity mapping RegisterController.
 */
public class RegisterActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private EditText editEmail, editName, editPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new AndroidDatabaseHelper(this);
        editEmail = findViewById(R.id.editRegEmail);
        editName = findViewById(R.id.editRegName);
        editPassword = findViewById(R.id.editRegPassword);
        btnRegister = findViewById(R.id.btnDoRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String name = editName.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("name", name);
        cv.put("password", password);
        long id = db.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
