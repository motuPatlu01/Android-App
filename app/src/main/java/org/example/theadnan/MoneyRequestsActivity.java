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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Money Requests screen: Handles sending money and managing requests.
 */
public class MoneyRequestsActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private ListView listRequests;
    private EditText etToEmail, etAmount;
    private Button btnSendRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        dbHelper = new AndroidDatabaseHelper(this);
        listRequests = findViewById(R.id.listRequests);
        etToEmail = findViewById(R.id.etToEmail);
        etAmount = findViewById(R.id.etAmount);
        btnSendRequest = findViewById(R.id.btnSendRequest);

        btnSendRequest.setOnClickListener(v -> sendMoney());

        listRequests.setOnItemClickListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            if (item.contains("[PENDING]") && item.contains("From:")) {
                String requestId = item.split(" #")[1].split(" ")[0];
                showFulfillDialog(requestId);
            }
        });

        loadRequests();
    }

    private void sendMoney() {
        String fromEmail = Session.getCurrentEmail(this);
        String toEmail = etToEmail.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (toEmail.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Check sender balance
            Cursor c = db.rawQuery("SELECT balance FROM users WHERE email = ?", new String[]{fromEmail});
            if (c.moveToFirst()) {
                double balance = c.getDouble(0);
                if (balance < amount) {
                    Toast.makeText(this, "Insufficient balance!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Deduct from sender
                db.execSQL("UPDATE users SET balance = balance - ? WHERE email = ?", new Object[]{amount, fromEmail});
                // Add to recipient
                db.execSQL("UPDATE users SET balance = balance + ? WHERE email = ?", new Object[]{amount, toEmail});

                // Record transaction
                ContentValues cv = new ContentValues();
                cv.put("from_email", fromEmail);
                cv.put("to_email", toEmail);
                cv.put("amount", amount);
                cv.put("status", "COMPLETED");
                cv.put("created_at", String.valueOf(System.currentTimeMillis()));
                db.insert("money_requests", null, cv);

                db.setTransactionSuccessful();
                Toast.makeText(this, "Money sent successfully!", Toast.LENGTH_SHORT).show();
                etToEmail.setText("");
                etAmount.setText("");
            }
            c.close();
        } catch (Exception e) {
            Toast.makeText(this, "Transaction failed", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            loadRequests();
        }
    }

    private void showFulfillDialog(String requestId) {
        new AlertDialog.Builder(this)
                .setTitle("Fulfill Request")
                .setMessage("Do you want to pay this request?")
                .setPositiveButton("Pay", (dialog, which) -> fulfillRequest(requestId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fulfillRequest(String requestId) {
        String myEmail = Session.getCurrentEmail(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT from_email, amount FROM money_requests WHERE id = ?", new String[]{requestId});
            if (c.moveToFirst()) {
                String requesterEmail = c.getString(0);
                double amount = c.getDouble(1);

                // Check my balance
                Cursor balanceCursor = db.rawQuery("SELECT balance FROM users WHERE email = ?", new String[]{myEmail});
                if (balanceCursor.moveToFirst()) {
                    double myBalance = balanceCursor.getDouble(0);
                    if (myBalance < amount) {
                        Toast.makeText(this, "Insufficient balance!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Transfer
                    db.execSQL("UPDATE users SET balance = balance - ? WHERE email = ?", new Object[]{amount, myEmail});
                    db.execSQL("UPDATE users SET balance = balance + ? WHERE email = ?", new Object[]{amount, requesterEmail});
                    db.execSQL("UPDATE money_requests SET status = 'COMPLETED' WHERE id = ?", new Object[]{requestId});

                    db.setTransactionSuccessful();
                    Toast.makeText(this, "Request paid!", Toast.LENGTH_SHORT).show();
                }
                balanceCursor.close();
            }
            c.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            loadRequests();
        }
    }

    private void loadRequests() {
        String email = Session.getCurrentEmail(this);
        if (email == null) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id, from_email, to_email, amount, status FROM money_requests WHERE from_email = ? OR to_email = ? ORDER BY id DESC",
                new String[]{email, email})) {
            ArrayList<String> items = new ArrayList<>();
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String from = c.getString(1);
                String to = c.getString(2);
                double amt = c.getDouble(3);
                String status = c.getString(4);
                
                String label = from.equals(email) ? "To: " + to : "From: " + from;
                items.add("Request #" + id + " " + label + " | $" + amt + " [" + status + "]");
            }
            listRequests.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        }
    }
}
