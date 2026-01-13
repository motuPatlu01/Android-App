package org.example.theadnan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * SearchUserActivity: Allows users to look up other users by email.
 */
public class SearchUserActivity extends AppCompatActivity {

    private EditText etSearchEmail;
    private Button btnDoSearch;
    private CardView cardSearchResult;
    private ImageView ivSearchProfileImage;
    private TextView tvSearchName, tvSearchEmail, tvSearchProfession, tvSearchHobby, tvSearchAge;
    private AndroidDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        dbHelper = new AndroidDatabaseHelper(this);

        etSearchEmail = findViewById(R.id.etSearchEmail);
        btnDoSearch = findViewById(R.id.btnDoSearch);
        cardSearchResult = findViewById(R.id.cardSearchResult);
        ivSearchProfileImage = findViewById(R.id.ivSearchProfileImage);
        tvSearchName = findViewById(R.id.tvSearchName);
        tvSearchEmail = findViewById(R.id.tvSearchEmail);
        tvSearchProfession = findViewById(R.id.tvSearchProfession);
        tvSearchHobby = findViewById(R.id.tvSearchHobby);
        tvSearchAge = findViewById(R.id.tvSearchAge);

        btnDoSearch.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String email = etSearchEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT name, email, profession, hobby, age, profile_image FROM users WHERE email = ?", new String[]{email})) {
            if (c.moveToFirst()) {
                String name = c.getString(0);
                String userEmail = c.getString(1);
                String profession = c.getString(2);
                String hobby = c.getString(3);
                int age = c.getInt(4);
                String base64 = c.getString(5);

                tvSearchName.setText(name != null ? name : "N/A");
                tvSearchEmail.setText(userEmail);
                tvSearchProfession.setText("Profession: " + (profession != null ? profession : "N/A"));
                tvSearchHobby.setText("Hobby: " + (hobby != null ? hobby : "N/A"));
                tvSearchAge.setText("Age: " + age);

                if (base64 != null && !base64.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ivSearchProfileImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        ivSearchProfileImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    ivSearchProfileImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }

                cardSearchResult.setVisibility(View.VISIBLE);
            } else {
                cardSearchResult.setVisibility(View.GONE);
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
