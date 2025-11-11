package com.example.bpsufixit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    TextView tvReportsList, tvAnnouncements;
    DatabaseReference databaseReports, databaseAnnouncements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tvReportsList = findViewById(R.id.tvReportsList);
        tvAnnouncements = findViewById(R.id.tvAnnouncements);
        Button btnAddAnnouncement = findViewById(R.id.btnAddAnnouncement);

        databaseReports = FirebaseDatabase.getInstance().getReference("reports");
        databaseAnnouncements = FirebaseDatabase.getInstance().getReference("announcements");

        loadReports();
        loadAnnouncements();

        btnAddAnnouncement.setOnClickListener(v -> showAnnouncementDialog(null, null));

        setupHeaderNavigation();
    }

    private void loadReports() {
        databaseReports.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder reports = new StringBuilder();
                for (DataSnapshot reportSnap : snapshot.getChildren()) {
                    Report report = reportSnap.getValue(Report.class);
                    if (report != null) {
                        reports.append("Location: ").append(report.location)
                                .append("\nCategory: ").append(report.category)
                                .append("\nDescription: ").append(report.description)
                                .append("\nStatus: ").append(report.status)
                                .append("\n\n");
                    }
                }

                tvReportsList.setText(reports.length() == 0 ? "No reports yet." : reports.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvReportsList.setText("Failed to load reports.");
            }
        });
    }

    private void loadAnnouncements() {
        databaseAnnouncements.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder announcements = new StringBuilder();
                for (DataSnapshot annSnap : snapshot.getChildren()) {
                    Announcement ann = annSnap.getValue(Announcement.class);
                    if (ann != null) {
                        announcements.append("🔹 ").append(ann.title)
                                .append("\n").append(ann.message)
                                .append("\n\n");
                    }
                }

                if (announcements.length() == 0) {
                    tvAnnouncements.setText("No announcements yet.");
                } else {
                    tvAnnouncements.setText(announcements.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvAnnouncements.setText("Failed to load announcements.");
            }
        });

    }

    /**
     * Shows dialog to add or an announcement.
     * Pass id == null and existing == null to create new.
     * Pass id != null and existing != null to edit.
     */
    private void showAnnouncementDialog(String id, Announcement existing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_announcement, null);
        EditText etTitle = view.findViewById(R.id.etAnnouncementTitle);
        EditText etMessage = view.findViewById(R.id.etAnnouncementMessage);

        // Use a final single-element array so we can mutate the id inside the inner class.
        final String[] idHolder = new String[1];
        idHolder[0] = id;

        if (existing != null) {
            etTitle.setText(existing.title);
            etMessage.setText(existing.message);

            // If editing, ensure the idHolder contains the existing id
            idHolder[0] = existing.id;
        }

        builder.setView(view);
        builder.setTitle(existing == null ? "Add Announcement" : "");
        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill out both fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // If creating new announcement, generate a key now
            if (idHolder[0] == null) {
                idHolder[0] = databaseAnnouncements.push().getKey();
            }

            if (idHolder[0] == null) {
                Toast.makeText(this, "Failed to generate announcement ID.", Toast.LENGTH_SHORT).show();
                return;
            }

            Announcement announcement = new Announcement(idHolder[0], title, message);
            databaseAnnouncements.child(idHolder[0]).setValue(announcement)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Announcement saved!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save.", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void setupHeaderNavigation() {
        TextView tvHome = findViewById(R.id.tvHome);
        TextView tvReport = findViewById(R.id.tvReport);
        TextView tvAdmin = findViewById(R.id.tvAdmin);

        if (tvAdmin != null) {
            tvAdmin.setEnabled(false);
            tvAdmin.setAlpha(0.7f);
        }

        if (tvHome != null) {
            tvHome.setOnClickListener(v -> {
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                intent.putExtra("isAdmin", true);
                startActivity(intent);
            });
        }

        if (tvReport != null) {
            tvReport.setOnClickListener(v -> {
                Intent intent = new Intent(AdminActivity.this, ReportActivity.class);
                intent.putExtra("isAdmin", true);
                startActivity(intent);
            });
        }
    }
}