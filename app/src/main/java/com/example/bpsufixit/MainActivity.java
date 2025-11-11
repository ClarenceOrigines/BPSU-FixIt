package com.example.bpsufixit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    TextView mainReports, mainAnnouncement;
    DatabaseReference databaseReports, databaseAnnouncements;
    private boolean isAdmin = false; // ✅ store flag globally


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainReports = findViewById(R.id.mainReports);
        mainAnnouncement = findViewById(R.id.mainAnnouncement);

        databaseReports = FirebaseDatabase.getInstance().getReference("reports");
        databaseAnnouncements = FirebaseDatabase.getInstance().getReference("announcements");

        setupHeaderNavigation();

        // ✅ Check if user is admin (from LoginActivity or AdminActivity)
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        // ✅ Find views
        TextView tvAdmin = findViewById(R.id.tvAdmin);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        // ✅ Show/hide admin text
        if (tvAdmin != null) {
            tvAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        loadAnnouncements();
        loadReports();

        // ✅ Hide login button if admin is visible
        if (btnLogin != null) {
            if (isAdmin) {
                btnLogin.setVisibility(View.GONE); // 👈 hides button for admin
            } else {
                btnLogin.setVisibility(View.VISIBLE); // 👈 show it for normal users
                btnLogin.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                });
            }
        }
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
                    mainAnnouncement.setText("No announcements yet.");
                } else {
                    mainAnnouncement.setText(announcements.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainAnnouncement.setText("Failed to load announcements.");
            }
        });

    }

    private void loadReports() {
        databaseReports.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder updatedReports = new StringBuilder();

                for (DataSnapshot reportSnap : snapshot.getChildren()) {
                    Report report = reportSnap.getValue(Report.class);
                    if (report != null && report.status != null) {
                        if(!report.status.equalsIgnoreCase("Pending")) {
                            updatedReports.append("Location: ").append(report.location)
                                    .append("\nCategory: ").append(report.category)
                                    .append("\nDescription: ").append(report.description)
                                    .append("\nStatus: ").append(report.status)
                                    .append("\n\n");
                        }
                    }
                }
                if (updatedReports.length() == 0) {
                    mainReports.setText("No updated reports yet.");
                } else {
                    mainReports.setText(updatedReports.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainReports.setText("Failed to load reports.");
            }
        });
    }

    private void setupHeaderNavigation() {
        TextView tvHome = findViewById(R.id.tvHome);
        TextView tvReport = findViewById(R.id.tvReport);
        TextView tvAdmin = findViewById(R.id.tvAdmin);

        // Disable Home since we're in MainActivity
        if (tvHome != null) {
            tvHome.setEnabled(false);
            tvHome.setAlpha(0.7f);
        }

        if (tvReport != null) {
            tvReport.setOnClickListener(v -> {
                // ✅ Keep admin flag when going to ReportActivity
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
            });
        }

        if (tvAdmin != null) {
            tvAdmin.setOnClickListener(v -> {
                // ✅ Always pass admin flag so Admin tab stays visible
                Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                intent.putExtra("isAdmin", true);
                startActivity(intent);
            });
        }
    }
}
