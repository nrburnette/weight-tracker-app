package com.zybooks.projecttwonickburnetteweightlossoption;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView inspirationMessage, goalWeight, previousWeight;
    private EditText updateWeight;

    private Button buttonAddWeight;
    private RecyclerView recyclerView;
    private WeightAdapter weightAdapter;
    private WeightDatabaseHelper dbHelper;

    //for storing goal weight and showing it
    private EditText goalWeightInput;
    private Button saveGoalButton;

    private Button deleteLastEntryButton;
    private WeightDatabaseHelper getDbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        inspirationMessage = findViewById(R.id.inspirationMessage);
        goalWeightInput = findViewById(R.id.goalWeightInput);
        saveGoalButton = findViewById(R.id.saveGoalButton);
        goalWeight = findViewById(R.id.goalWeight);
        updateWeight = findViewById(R.id.updateWeight);
        buttonAddWeight = findViewById(R.id.buttonAddWeight);
        recyclerView = findViewById(R.id.recyclerView);

        //to display 'goal weight' using shared preferences
        SharedPreferences prefs = getSharedPreferences("WeightPrefs", MODE_PRIVATE);
        boolean isGoalWeightMet = prefs.getBoolean("goalWeightMet", false);
        String savedGoal = prefs.getString("goalWeight", "Not Set");
        goalWeight.setText("Goal Weight: " + savedGoal + " lbs");


        // Initialize database helper
        dbHelper = new WeightDatabaseHelper(this);

        //initialize weightList for later use
        ArrayList<WeightEntry> weightList = dbHelper.getAllWeights();

        //delete last entry
        deleteLastEntryButton = findViewById(R.id.buttonDeleteWeight);

        //check goal weight for one SMS only

        if (goalWeight != null && !weightList.isEmpty() && !isGoalWeightMet) {
            String latestWeight = weightList.get(0).getWeight(); // Get the most recent weight
            if (latestWeight.equals(savedGoal)) {
                sendGoalReachedSMS();
                prefs.edit().putBoolean("goalWeightMet", true).apply(); // Mark goal as met
            }
        }

        deleteLastEntryButton.setOnClickListener(view -> {
            dbHelper.deleteLastEntry();
            loadWeightData(); //refresh recycler view to show deletion
            Toast.makeText(MainActivity.this, "Last entry deleted!", Toast.LENGTH_SHORT).show();
        });

        //setup save goal button
        saveGoalButton.setOnClickListener(view -> {
            String goal = goalWeightInput.getText().toString();
            if (!goal.isEmpty()) {  //place goal weight in SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("goalWeight", goal);
                editor.putBoolean("goalWeightMet", false); //reset flag in case of a new goal
                editor.apply();
                //update UI
                goalWeight.setText("Goal Weight: " + goal + " lbs");
                Toast.makeText(this, "Goal weight saved!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Please enter a valid goal weight", Toast.LENGTH_SHORT).show();
            }
        });


        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Display in a 2-column grid
        loadWeightData(); // Load existing weight data

        // Set up button click listener to add weight
        buttonAddWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewWeight();
            }
        });
    }
    // Load weight data from database and update RecyclerView
    private void loadWeightData() {
        ArrayList<WeightEntry> weightList = dbHelper.getAllWeights();
        weightAdapter = new WeightAdapter(weightList);
        recyclerView.setAdapter(weightAdapter);

        // Check if goal weight is met
        SharedPreferences prefs = getSharedPreferences("WeightPrefs", MODE_PRIVATE);
        String goalWeight = prefs.getString("goalWeight", null);

        if (goalWeight != null && !weightList.isEmpty()) {
            String latestWeight = weightList.get(0).getWeight(); // Get the most recent weight

            if (latestWeight.equals(goalWeight)) {
                sendGoalReachedSMS(); // Trigger SMS when goal is met
            }
        }
    }

    // Method to trigger SMS
    private void sendGoalReachedSMS() {
        Intent intent = new Intent(MainActivity.this, SMSActivity.class);
        startActivity(intent);
    }

    //get current date
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date()); //returns current date
    }
    // to insert new weight
    private void addNewWeight() {
        String newWeight = updateWeight.getText().toString().trim();
        String currentDate = getCurrentDate(); //get today's date
        if (!newWeight.isEmpty()) {
            dbHelper.insertWeight(newWeight, currentDate); // Insert into database
            updateWeight.setText(""); // Clear input field
            loadWeightData(); // Refresh RecyclerView
            Toast.makeText(this, "Weight Added!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enter a weight", Toast.LENGTH_SHORT).show();
        }
    }

}
