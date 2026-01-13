package org.example.theadnan;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * RegisterActivity: Handles user registration with additional fields.
 */
public class RegisterActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private EditText editEmail, editName, editAge, editProfession, editHobby, editPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new AndroidDatabaseHelper(this);
        editEmail = findViewById(R.id.editRegEmail);
        editName = findViewById(R.id.editRegName);
        editAge = findViewById(R.id.editRegAge);
        editProfession = findViewById(R.id.editRegProfession);
        editHobby = findViewById(R.id.editRegHobby);
        editPassword = findViewById(R.id.editRegPassword);
        btnRegister = findViewById(R.id.btnDoRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String name = editName.getText().toString().trim();
        String ageStr = editAge.getText().toString().trim();
        String profession = editProfession.getText().toString().trim();
        String hobby = editHobby.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Email, Name and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = 0;
        try {
            if (!ageStr.isEmpty()) age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("name", name);
        cv.put("age", age);
        cv.put("profession", (profession.isEmpty() ? "N/A" : profession));
        cv.put("hobby", (hobby.isEmpty() ? "N/A" : hobby));
        cv.put("password", password);
        cv.put("balance", 0.0); // Initial balance

        long id = db.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
