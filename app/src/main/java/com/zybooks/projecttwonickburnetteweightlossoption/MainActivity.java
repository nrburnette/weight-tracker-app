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
import java.util.Random;

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

    // UPDATED button for progress
    private Button buttonMyProgress;



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
        // UPDATED adding new button for my progress
        buttonMyProgress = findViewById(R.id.buttonMyProgress);

        //to display 'goal weight' using shared preferences // UPDATED renamed prefs to weightPrefs
        SharedPreferences weightPrefs = getSharedPreferences("WeightPrefs", MODE_PRIVATE);


        // UPDATED call SharedPreferences again to grab from LoginActivity
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String firstName = userPrefs.getString(
                getString(R.string.pref_first_name_key),
                getString(R.string.default_first_name) // UPDATED default name is 'Friend'
        );

        // UPDATED load inspiration message array from strings.xml
        String[] templates = getResources().getStringArray(R.array.inspiration_messages);
        // UPDATED pick a random inspiration message
        int idx = new Random().nextInt(templates.length);

        // UPDATED formats the selected message with the user's first name or default 'Friend'
        String message = String.format(Locale.getDefault(), templates[idx], firstName);

        // UPDATED now we can set the text message
        inspirationMessage.setText(message);

        boolean isGoalWeightMet = weightPrefs.getBoolean("goalWeightMet", false);

        // UPDATED: call resource and placeholder to use string.xml for strings and key
        String savedGoal = weightPrefs.getString(
                getString(R.string.goal_weight_key),
                getString(R.string.goal_not_set)
        );

        // UPDATED: call resource and placeholder to call (%1$s) for label
        goalWeight.setText(getString(R.string.goal_weight_label, savedGoal));



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
                weightPrefs.edit().putBoolean("goalWeightMet", true).apply(); // Mark goal as met
            }
        }

        deleteLastEntryButton.setOnClickListener(view -> {  // UPDATED call resource string for last entry deleted
            dbHelper.deleteLastEntry();
            loadWeightData(); //refresh recycler view to show deletion
            Toast.makeText(MainActivity.this, R.string.last_entry_deleted, Toast.LENGTH_SHORT).show();
        });

        //setup save goal button
        saveGoalButton.setOnClickListener(view -> {
            String goal = goalWeightInput.getText().toString();
            if (!goal.isEmpty()) {  //place goal weight in SharedPreferences
                SharedPreferences.Editor editor = weightPrefs.edit();
                editor.putString("goalWeight", goal);
                editor.putBoolean("goalWeightMet", false); //reset flag in case of a new goal
                editor.apply();
                //UPDATED: call resource strings instead of hardcoded strings
                // update UI with goal and weight
                goalWeight.setText(getString(R.string.goal_weight_label, goal));
                Toast.makeText(this, R.string.goal_saved_confirmation, Toast.LENGTH_SHORT).show();

            } else { // UPDATED: call resource strings instead of hardcoded strings
                Toast.makeText(this, R.string.enter_valid_goal_weight, Toast.LENGTH_SHORT).show();
            }
        });

        // UPDATED setup My Progress button, initialized above
        buttonMyProgress.setOnClickListener(v -> {
            // PLACEHOLDER
            Toast.makeText(MainActivity.this, "Progress Tracking coming soon", Toast.LENGTH_SHORT).show();
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
        SharedPreferences weightPrefs = getSharedPreferences("WeightPrefs", MODE_PRIVATE);
        String goalWeight = weightPrefs.getString("goalWeight", null);

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
            Toast.makeText(this, R.string.weight_added, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.please_enter_weight, Toast.LENGTH_SHORT).show();
        }
    }

}
