package org.example.theadnan;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Notes screen: handles creating, viewing, updating, and deleting notes with text, drawing, or photo.
 */
public class NotesActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private AndroidDatabaseHelper dbHelper;
    private ListView listNotes;
    private EditText etNoteTitle, etNoteContent;
    private DrawingView drawingView;
    private TabLayout tabLayout;
    private Button btnSaveNote, btnDeleteNote, btnErase, btnTakePhoto;
    private ImageView ivScanPreview;
    private ImageButton btnBlack, btnRed, btnBlue, btnGreen;
    private String selectedNoteTitle = null;
    private Bitmap scannedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        dbHelper = new AndroidDatabaseHelper(this);
        listNotes = findViewById(R.id.listNotes);
        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);
        drawingView = findViewById(R.id.drawingView);
        tabLayout = findViewById(R.id.noteTabLayout);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnDeleteNote = findViewById(R.id.btnDeleteNote);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        ivScanPreview = findViewById(R.id.ivScanPreview);
        
        btnErase = findViewById(R.id.btnErase);
        btnBlack = findViewById(R.id.btnColorBlack);
        btnRed = findViewById(R.id.btnColorRed);
        btnBlue = findViewById(R.id.btnColorBlue);
        btnGreen = findViewById(R.id.btnColorGreen);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                etNoteContent.setVisibility(View.GONE);
                findViewById(R.id.drawContainer).setVisibility(View.GONE);
                findViewById(R.id.scanContainer).setVisibility(View.GONE);

                if (tab.getPosition() == 0) etNoteContent.setVisibility(View.VISIBLE);
                else if (tab.getPosition() == 1) findViewById(R.id.drawContainer).setVisibility(View.VISIBLE);
                else findViewById(R.id.scanContainer).setVisibility(View.VISIBLE);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnSaveNote.setOnClickListener(v -> saveNote());
        btnDeleteNote.setOnClickListener(v -> {
            if (selectedNoteTitle != null) showDeleteConfirmation(selectedNoteTitle);
        });

        btnTakePhoto.setOnClickListener(v -> dispatchTakePictureIntent());

        // Drawing controls
        btnErase.setOnClickListener(v -> drawingView.setErase(true));
        btnBlack.setOnClickListener(v -> drawingView.setColor(Color.BLACK));
        btnRed.setOnClickListener(v -> drawingView.setColor(Color.RED));
        btnBlue.setOnClickListener(v -> drawingView.setColor(Color.BLUE));
        btnGreen.setOnClickListener(v -> drawingView.setColor(Color.GREEN));

        listNotes.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item != null) loadNoteIntoEditor(item.toString());
        });

        loadNotes();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            scannedBitmap = (Bitmap) extras.get("data");
            ivScanPreview.setImageBitmap(scannedBitmap);
        }
    }

    private void saveNote() {
        String email = Session.getCurrentEmail(this);
        if (email == null) return;

        String title = etNoteTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = "";
        int tab = tabLayout.getSelectedTabPosition();
        if (tab == 0) {
            content = etNoteContent.getText().toString().trim();
        } else if (tab == 1) {
            Bitmap b = drawingView.getBitmap();
            if (b != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.PNG, 100, baos);
                content = "DRAWING:" + Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            }
        } else if (tab == 2) {
            if (scannedBitmap == null) {
                Toast.makeText(this, "Take a photo first", Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scannedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            content = "PHOTO:" + Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_email", email);
        cv.put("title", title);
        cv.put("content", content);
        cv.put("created_at", String.valueOf(System.currentTimeMillis()));

        if (selectedNoteTitle != null && !selectedNoteTitle.equals(title)) {
            db.delete("notes", "user_email = ? AND title = ?", new String[]{email, selectedNoteTitle});
        }
        db.insertWithOnConflict("notes", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
        resetEditor();
        loadNotes();
    }

    private void loadNoteIntoEditor(String title) {
        String email = Session.getCurrentEmail(this);
        if (email == null) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT content FROM notes WHERE user_email = ? AND title = ?", new String[]{email, title})) {
            if (c.moveToFirst()) {
                etNoteTitle.setText(title);
                String content = c.getString(0);
                
                resetEditorUI();
                
                if (content != null && content.startsWith("DRAWING:")) {
                    try {
                        byte[] bytes = Base64.decode(content.substring(8), Base64.DEFAULT);
                        drawingView.setBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        tabLayout.getTabAt(1).select();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error loading drawing", Toast.LENGTH_SHORT).show();
                    }
                } else if (content != null && content.startsWith("PHOTO:")) {
                    try {
                        byte[] bytes = Base64.decode(content.substring(6), Base64.DEFAULT);
                        scannedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ivScanPreview.setImageBitmap(scannedBitmap);
                        tabLayout.getTabAt(2).select();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    etNoteContent.setText(content != null ? content : "");
                    tabLayout.getTabAt(0).select();
                }
                selectedNoteTitle = title;
                btnDeleteNote.setVisibility(View.VISIBLE);
                btnSaveNote.setText("Update");
            }
        }
    }

    private void showDeleteConfirmation(String title) {
        new AlertDialog.Builder(this).setTitle("Delete").setMessage("Delete note?")
                .setPositiveButton("Delete", (d, w) -> {
                    dbHelper.getWritableDatabase().delete("notes", "user_email = ? AND title = ?", new String[]{Session.getCurrentEmail(this), title});
                    resetEditor();
                    loadNotes();
                }).setNegativeButton("Cancel", null).show();
    }

    private void resetEditor() {
        etNoteTitle.setText("");
        resetEditorUI();
        selectedNoteTitle = null;
        btnSaveNote.setText("Save");
        btnDeleteNote.setVisibility(View.GONE);
        tabLayout.getTabAt(0).select();
    }

    private void resetEditorUI() {
        etNoteContent.setText("");
        drawingView.clear();
        ivScanPreview.setImageResource(android.R.drawable.ic_menu_camera);
        scannedBitmap = null;
    }

    private void loadNotes() {
        String email = Session.getCurrentEmail(this);
        if (email == null) return;
        
        ArrayList<String> notes = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT title FROM notes WHERE user_email = ? ORDER BY created_at DESC", new String[]{email})) {
            while (c.moveToNext()) notes.add(c.getString(0));
        }
        listNotes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notes));
    }
}
