package org.example.theadnan;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * DashboardActivity: Displays user profile, balance and handles navigation.
 */
public class DashboardActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;

    private TextView tvBalance, tvProfileName, tvProfileEmail;
    private ImageView ivProfileImage;
    private View sectionProfile, sectionBalance, sectionUserInfo;
    private Button btnNotes, btnRequests, btnMessages, btnReports, btnLogout, btnUpdateBalance, btnCurrencyConverter, btnWeather, btnAdminPanel, btnSearchUser;
    private AndroidDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new AndroidDatabaseHelper(this);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvBalance = findViewById(R.id.tvBalance);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        
        sectionProfile = findViewById(R.id.sectionProfile);
        sectionBalance = findViewById(R.id.sectionBalance);
        sectionUserInfo = findViewById(R.id.sectionUserInfo);

        btnNotes = findViewById(R.id.btnNotes);
        btnRequests = findViewById(R.id.btnRequests);
        btnMessages = findViewById(R.id.btnMessages);
        btnReports = findViewById(R.id.btnReports);
        btnLogout = findViewById(R.id.btnLogout);
        btnUpdateBalance = findViewById(R.id.btnUpdateBalance);
        btnCurrencyConverter = findViewById(R.id.btnCurrencyConverter);
        btnWeather = findViewById(R.id.btnWeather);
        btnAdminPanel = findViewById(R.id.btnAdminPanel);
        btnSearchUser = findViewById(R.id.btnSearchUser);

        findViewById(R.id.tvChangePhoto).setOnClickListener(v -> openGallery());
        findViewById(R.id.cardProfile).setOnClickListener(v -> showEditProfileDialog());

        boolean isAdmin = Session.isAdmin(this);
        if (isAdmin) {
            // Admin Specific Dashboard
            btnAdminPanel.setVisibility(View.VISIBLE);
            btnNotes.setVisibility(View.GONE);
            btnRequests.setVisibility(View.GONE);
            btnReports.setVisibility(View.GONE);
            
            // Hide personal/bank info for Admin
            sectionProfile.setVisibility(View.GONE);
            sectionBalance.setVisibility(View.GONE);
            sectionUserInfo.setVisibility(View.GONE);
            
            // Show Admin Title
            TextView welcomeHeader = new TextView(this);
            welcomeHeader.setText("Admin Dashboard");
            welcomeHeader.setTextSize(24);
            welcomeHeader.setTextColor(0xFFFFD700); // Gold
            welcomeHeader.setPadding(0, 0, 0, 48);
            ((android.widget.LinearLayout)sectionProfile.getParent()).addView(welcomeHeader, 0);
        }

        btnNotes.setOnClickListener(v -> startActivity(new Intent(this, NotesActivity.class)));
        btnRequests.setOnClickListener(v -> startActivity(new Intent(this, MoneyRequestsActivity.class)));
        btnMessages.setOnClickListener(v -> startActivity(new Intent(this, MessagesActivity.class)));
        btnReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        btnCurrencyConverter.setOnClickListener(v -> startActivity(new Intent(this, CurrencyConverterActivity.class)));
        btnWeather.setOnClickListener(v -> startActivity(new Intent(this, WeatherActivity.class)));
        btnAdminPanel.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));
        btnSearchUser.setOnClickListener(v -> startActivity(new Intent(this, SearchUserActivity.class)));
        
        btnLogout.setOnClickListener(v -> {
            Session.clear(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnUpdateBalance.setOnClickListener(v -> showUpdateBalanceDialog());

        if (!isAdmin) {
            loadUserProfile();
        }
    }

    private void showEditProfileDialog() {
        String email = Session.getCurrentEmail(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String name = "", profession = "", hobby = "";
        int age = 0;

        try (Cursor c = db.rawQuery("SELECT name, age, profession, hobby FROM users WHERE email = ?", new String[]{email})) {
            if (c.moveToFirst()) {
                name = c.getString(0);
                age = c.getInt(1);
                profession = c.getString(2);
                hobby = c.getString(3);
            }
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = view.findViewById(R.id.etEditName);
        EditText etAge = view.findViewById(R.id.etEditAge);
        EditText etProfession = view.findViewById(R.id.etEditProfession);
        EditText etHobby = view.findViewById(R.id.etEditHobby);

        etName.setText(name);
        etAge.setText(String.valueOf(age));
        etProfession.setText(profession);
        etHobby.setText(hobby);

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    updateProfileInfo(
                        etName.getText().toString(),
                        etAge.getText().toString(),
                        etProfession.getText().toString(),
                        etHobby.getText().toString()
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProfileInfo(String name, String ageStr, String profession, String hobby) {
        String email = Session.getCurrentEmail(this);
        int age = 0;
        try { age = Integer.parseInt(ageStr); } catch (Exception ignored) {}

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("age", age);
        cv.put("profession", (profession.isEmpty() ? "N/A" : profession));
        cv.put("hobby", (hobby.isEmpty() ? "N/A" : hobby));
        
        db.update("users", cv, "email = ?", new String[]{email});
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        loadUserProfile();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivProfileImage.setImageBitmap(bitmap);
                saveProfileImage(bitmap);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfileImage(Bitmap bitmap) {
        String email = Session.getCurrentEmail(this);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("profile_image", base64);
        db.update("users", cv, "email = ?", new String[]{email});
    }

    private void loadUserProfile() {
        String email = Session.getCurrentEmail(this);
        if (email == null) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT name, age, profession, hobby, balance, profile_image FROM users WHERE email = ?", new String[]{email})) {
            if (c.moveToFirst()) {
                String name = c.getString(0);
                double balance = c.getDouble(4);
                String base64 = c.getString(5);

                tvProfileName.setText("Name: " + (name != null ? name : "N/A"));
                tvProfileEmail.setText("Email: " + email);
                tvBalance.setText(String.format("$%.2f", balance));

                if (base64 != null && !base64.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ivProfileImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        ivProfileImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    ivProfileImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Session.isAdmin(this)) {
            loadUserProfile();
        }
    }

    private void showUpdateBalanceDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_balance, null);
        EditText etAmount = view.findViewById(R.id.etDialogAmount);
        EditText etPassword = view.findViewById(R.id.etDialogPassword);

        new AlertDialog.Builder(this)
                .setTitle("Update Balance")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString();
                    String password = etPassword.getText().toString();
                    updateBalance(amountStr, password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBalance(String amountStr, String password) {
        String email = Session.getCurrentEmail(this);
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT password, balance FROM users WHERE email = ?", new String[]{email})) {
            if (c.moveToFirst()) {
                String dbPassword = c.getString(0);
                double currentBalance = c.getDouble(1);
                if (dbPassword != null && dbPassword.equals(password)) {
                    double newBalance = currentBalance + amount;
                    if (newBalance < 0) {
                        Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ContentValues cv = new ContentValues();
                    cv.put("balance", newBalance);
                    db.update("users", cv, "email = ?", new String[]{email});
                    loadUserProfile();
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
