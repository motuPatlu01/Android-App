package org.example.theadnan;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ListView listView;
    private TabLayout tabLayout;
    private AndroidDatabaseHelper dbHelper;
    private DataAdapter adapter;
    private List<AdminItem> itemList = new ArrayList<>();
    private int currentTab = 0; // 0 for Reports, 1 for Blocked Users

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new AndroidDatabaseHelper(this);
        listView = findViewById(R.id.listAdminData);
        tabLayout = findViewById(R.id.tabLayout);
        
        adapter = new DataAdapter();
        listView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadData();
    }

    private void loadData() {
        itemList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (currentTab == 0) {
            // Load Reports
            try (Cursor c = db.rawQuery("SELECT id, reporter_email, target_email, message FROM reports WHERE status = 'OPEN' ORDER BY id DESC", null)) {
                while (c.moveToNext()) {
                    itemList.add(new AdminItem(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), false));
                }
            }
        } else {
            // Load Blocked Users
            try (Cursor c = db.rawQuery("SELECT email, name FROM users WHERE blocked = 1", null)) {
                while (c.moveToNext()) {
                    itemList.add(new AdminItem(-1, "", c.getString(0), "Name: " + c.getString(1), true));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void markReviewed(int reportId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", "REVIEWED");
        db.update("reports", cv, "id = ?", new String[]{String.valueOf(reportId)});
        loadData();
    }

    private void blockUser(String email, int reportId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cvUser = new ContentValues();
        cvUser.put("blocked", 1);
        db.update("users", cvUser, "email = ?", new String[]{email});

        if (reportId != -1) {
            ContentValues cvReport = new ContentValues();
            cvReport.put("status", "ACTION_TAKEN");
            db.update("reports", cvReport, "id = ?", new String[]{String.valueOf(reportId)});
        }

        Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show();
        loadData();
    }

    private void unblockUser(String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("blocked", 0);
        db.update("users", cv, "email = ?", new String[]{email});
        Toast.makeText(this, "User unblocked", Toast.LENGTH_SHORT).show();
        loadData();
    }

    private class AdminItem {
        int id;
        String reporter, target, message;
        boolean isUser;

        AdminItem(int id, String reporter, String target, String message, boolean isUser) {
            this.id = id;
            this.reporter = reporter;
            this.target = target;
            this.message = message;
            this.isUser = isUser;
        }
    }

    private class DataAdapter extends BaseAdapter {
        @Override
        public int getCount() { return itemList.size(); }
        @Override
        public Object getItem(int i) { return itemList.get(i); }
        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(AdminActivity.this).inflate(R.layout.item_report, viewGroup, false);
            }
            AdminItem item = itemList.get(i);
            TextView title = view.findViewById(R.id.tvReportTitle);
            TextView reporter = view.findViewById(R.id.tvReporter);
            TextView target = view.findViewById(R.id.tvTarget);
            TextView message = view.findViewById(R.id.tvMessage);
            Button btnReviewed = view.findViewById(R.id.btnMarkReviewed);
            Button btnBlock = view.findViewById(R.id.btnBlockUser);

            if (item.isUser) {
                title.setText("Blocked User");
                reporter.setVisibility(View.GONE);
                target.setText("Email: " + item.target);
                message.setText(item.message);
                btnReviewed.setVisibility(View.GONE);
                btnBlock.setText("Unblock");
                btnBlock.setOnClickListener(v -> unblockUser(item.target));
            } else {
                title.setText("Report #" + item.id);
                reporter.setVisibility(View.VISIBLE);
                reporter.setText("By: " + item.reporter);
                target.setText("Target: " + item.target);
                message.setText("Reason: " + item.message);
                btnReviewed.setVisibility(View.VISIBLE);
                btnReviewed.setText("Reviewed");
                btnReviewed.setOnClickListener(v -> markReviewed(item.id));
                btnBlock.setText("Block Target");
                btnBlock.setOnClickListener(v -> blockUser(item.target, item.id));
            }

            return view;
        }
    }
}
