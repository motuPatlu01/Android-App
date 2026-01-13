package org.example.theadnan;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates same database tables as desktop Database.java and ensures admin account.
 */
public class AndroidDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "users.db";
    private static final int DB_VERSION = 1;

    public AndroidDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String usersSql = """
            CREATE TABLE IF NOT EXISTS users (
                email TEXT PRIMARY KEY,
                name TEXT,
                age INTEGER,
                profession TEXT,
                hobby TEXT,
                password TEXT,
                balance REAL DEFAULT 0,
                is_admin INTEGER DEFAULT 0,
                blocked INTEGER DEFAULT 0
            );
            """;

        String notesSql = """
            CREATE TABLE IF NOT EXISTS notes (
                user_email TEXT NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at TEXT,
                PRIMARY KEY (user_email, title),
                FOREIGN KEY(user_email) REFERENCES users(email)
            );
            """;

        String requestsSql = """
            CREATE TABLE IF NOT EXISTS money_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                from_email TEXT NOT NULL,
                to_email TEXT NOT NULL,
                amount REAL NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT,
                FOREIGN KEY(from_email) REFERENCES users(email),
                FOREIGN KEY(to_email) REFERENCES users(email)
            );
            """;

        String reportsSql = """
            CREATE TABLE IF NOT EXISTS reports (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                reporter_email TEXT NOT NULL,
                target_email TEXT NOT NULL,
                message TEXT,
                status TEXT NOT NULL,
                created_at TEXT,
                FOREIGN KEY(reporter_email) REFERENCES users(email),
                FOREIGN KEY(target_email) REFERENCES users(email)
            );
            """;

        db.execSQL(usersSql);
        db.execSQL(notesSql);
        db.execSQL(requestsSql);
        db.execSQL(reportsSql);

        // Ensure admin exists
        try {
            ContentValues cv = new ContentValues();
            String adminEmail = "admin@example.app.com";
            String adminPassword = "AdminIsTheKing";
            cv.put("email", adminEmail);
            cv.put("name", "Admin");
            cv.put("age", 0);
            cv.put("profession", "admin");
            cv.put("hobby", "admin");
            cv.put("password", adminPassword);
            cv.put("balance", 0);
            cv.put("is_admin", 1);
            cv.put("blocked", 0);
            db.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_IGNORE);

            ContentValues update = new ContentValues();
            update.put("password", adminPassword);
            update.put("is_admin", 1);
            db.update("users", update, "email = ?", new String[]{adminEmail});
        } catch (Exception ignored) {}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implement migrations when needed
    }
}
