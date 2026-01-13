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
 * MessagesActivity: Handles sending and viewing messages.
 */
public class MessagesActivity extends AppCompatActivity {

    private AndroidDatabaseHelper dbHelper;
    private EditText etReceiverEmail, etMessageContent;
    private Button btnSendMessage;
    private ListView listMessages;
    private ArrayList<String> messageList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        dbHelper = new AndroidDatabaseHelper(this);
        etReceiverEmail = findViewById(R.id.etReceiverEmail);
        etMessageContent = findViewById(R.id.etMessageContent);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        listMessages = findViewById(R.id.listMessages);

        messageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        listMessages.setAdapter(adapter);

        btnSendMessage.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void sendMessage() {
        String sender = Session.getCurrentEmail(this);
        String receiver = etReceiverEmail.getText().toString().trim();
        String content = etMessageContent.getText().toString().trim();

        if (receiver.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify receiver exists
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
        try (Cursor c = dbRead.rawQuery("SELECT email FROM users WHERE email = ?", new String[]{receiver})) {
            if (!c.moveToFirst()) {
                Toast.makeText(this, "Receiver not found", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("sender_email", sender);
        cv.put("receiver_email", receiver);
        cv.put("message", content);

        long id = dbWrite.insert("messages", null, cv);
        if (id != -1) {
            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
            etMessageContent.setText("");
            loadMessages();
        } else {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        String myEmail = Session.getCurrentEmail(this);
        if (myEmail == null) return;

        messageList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Load messages where I am either sender or receiver
        try (Cursor c = db.rawQuery(
                "SELECT sender_email, receiver_email, message, timestamp FROM messages " +
                "WHERE sender_email = ? OR receiver_email = ? ORDER BY id DESC",
                new String[]{myEmail, myEmail})) {
            
            while (c.moveToNext()) {
                String sender = c.getString(0);
                String receiver = c.getString(1);
                String msg = c.getString(2);
                String time = c.getString(3);

                String display;
                if (sender.equals(myEmail)) {
                    display = "To " + receiver + ": " + msg + " (" + time + ")";
                } else {
                    display = "From " + sender + ": " + msg + " (" + time + ")";
                }
                messageList.add(display);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
