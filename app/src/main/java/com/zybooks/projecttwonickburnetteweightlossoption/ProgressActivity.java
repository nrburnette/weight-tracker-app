package com.zybooks.projecttwonickburnetteweightlossoption;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;


public class ProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        TextView progressSummaryText = findViewById(R.id.progressSummaryText);

        // Initialize  dbHelper
        WeightDatabaseHelper dbHelper = new WeightDatabaseHelper(this);

        // Load entries
        ArrayList<WeightEntry> weightList = dbHelper.getAllWeights();

        // Analyze trend
        TrendSummary summary = TrendAnalyzer.analyze(weightList);

        if (!summary.hasEnoughData) {
            progressSummaryText.setText(getString(R.string.not_enough_data));
        } else {
            String overall = getString(
                    R.string.progress_overall,
                    summary.totalChange,
                    summary.totalChangePct,
                    getString(R.string.unit_of_measure)
            );
            String weeklyPace = getString(
                    R.string.progress_last_four_weeks,
                    summary.weeklyPace,
                    getString(R.string.unit_of_measure)
            );
            progressSummaryText.setText(overall + "\n" + "\n"+ weeklyPace);     // UPDATED ENHANCEMENT 2 show trend results in text view
        }
        // Create button to return
        Button btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(v -> {
            // return to MainActivity
            finish();
        });


    }

}