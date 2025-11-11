package com.example.bpsufixit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReportActivity extends AppCompatActivity {

    EditText locationEditText, categoryEditText, descriptionEditText;
    Button AsendReportButton;
    DatabaseReference databaseReports;

    private boolean isAdmin = false; // ✅ add flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        locationEditText = findViewById(R.id.locationEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        AsendReportButton = findViewById(R.id.AsendReportButton);

        // ✅ Get admin flag from previous activity
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        // ✅ Show Admin tab if admin is true
        TextView tvAdmin = findViewById(R.id.tvAdmin);
        if (tvAdmin != null) {
            tvAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        databaseReports = FirebaseDatabase.getInstance().getReference("reports");

        AsendReportButton.setOnClickListener(v -> {
            String loc = locationEditText.getText().toString().trim();
            String cat = categoryEditText.getText().toString().trim();
            String desc = descriptionEditText.getText().toString().trim();

            if (loc.isEmpty() || cat.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = databaseReports.push().getKey();
            Report report = new Report(id, loc, cat, desc, "Pending");

            String customId = "report_" + System.currentTimeMillis();
            databaseReports.child(customId).setValue(report);

            Toast.makeText(this, "Report Sent!", Toast.LENGTH_SHORT).show();

            // ✅ Clear fields
            locationEditText.setText("");
            categoryEditText.setText("");
            descriptionEditText.setText("");
        });

        setupHeaderNavigation();
    }

    private void setupHeaderNavigation() {
        TextView tvHome = findViewById(R.id.tvHome);
        TextView tvReport = findViewById(R.id.tvReport);
        TextView tvAdmin = findViewById(R.id.tvAdmin);

        // Disable Report since we're already here
        if (tvReport != null) {
            tvReport.setEnabled(false);
            tvReport.setAlpha(0.7f);
        }

        // ✅ Home navigation (keep admin flag)
        if (tvHome != null) {
            tvHome.setOnClickListener(v -> {
                Intent intent = new Intent(ReportActivity.this, MainActivity.class);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
            });
        }

        // ✅ Admin navigation (always show admin)
        if (tvAdmin != null) {
            tvAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(ReportActivity.this, AdminActivity.class);
                intent.putExtra("isAdmin", true);
                startActivity(intent);
            });
        }
    }
}
