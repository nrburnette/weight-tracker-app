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
import java.util.List;
import java.util.Locale;
import java.util.Random;
import android.util.Log; // UPDATED enhancement 3 for DB Log

public class MainActivity extends AppCompatActivity {

    private TextView inspirationMessage, goalWeight, previousWeight;
    private EditText updateWeight;

    private Button buttonAddWeight;
    private RecyclerView recyclerView;
    private WeightAdapter weightAdapter;
    // private WeightDatabaseHelper dbHelper; // DEPRECATED

    //for storing goal weight and showing it
    private EditText goalWeightInput;
    private Button saveGoalButton;

    private Button deleteLastEntryButton;
    // private WeightDatabaseHelper getDbHelper; // DEPRECATED

    // UPDATED button for progress
    private Button buttonMyProgress;

    // UPDATED e3, field for local db
    private AppDatabase db;





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
        // UPDATED e3, for local db
        db = AppDatabase.getInstance(this);

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



        // Initialize database helper DEPRECATED
        // dbHelper = new WeightDatabaseHelper(this);

        //initialize weightList for later use DEPRECATED
        // ArrayList<WeightEntry> weightList = dbHelper.getAllWeights();

        // UPDATED enhancement 3 use Room DAO instead for db
        // initialize room db
        AppDatabase db = AppDatabase.getInstance(this);
        // fetch all weight entries from Room Dao
        List<WeightEntryEntity> weightList = db.weightDao().getAll();

        // UPDATED ENHANCEMENT 2 for TrendSummary.java
        TrendSummary summary = TrendAnalyzer.analyze(weightList);


        //delete last entry DEPRECATED
        // deleteLastEntryButton = findViewById(R.id.buttonDeleteWeight);
        // UPDATED enhancement 3 hooking up new delete to button
        deleteLastEntryButton = findViewById(R.id.buttonDeleteWeight);
        deleteLastEntryButton.setOnClickListener(view -> deleteLastEntry());

        //check goal weight for one SMS only

        if (goalWeight != null && !weightList.isEmpty() && !isGoalWeightMet) {
            // String latestWeight = weightList.get(0).getWeight(); // DEPRECATED
            String latestWeight = weightList.get(0).weight; // UPDATED e3, Get the most recent weight
            if (latestWeight.equals(savedGoal)) {
                sendGoalReachedSMS();
                weightPrefs.edit().putBoolean("goalWeightMet", true).apply(); // Mark goal as met
            }
        }

        // DEPRECATED
        //deleteLastEntryButton.setOnClickListener(view -> {  // UPDATED call resource string for last entry deleted
        //    dbHelper.deleteLastEntry();
        //    loadWeightData(); //refresh recycler view to show deletion
        //    Toast.makeText(MainActivity.this, R.string.last_entry_deleted, Toast.LENGTH_SHORT).show();
        //});





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
            // UPDATED ENHANCEMENT 2: start ProgressActivity
            startActivity(new Intent(MainActivity.this, ProgressActivity.class));
            //Toast.makeText(MainActivity.this, "Progress Tracking coming soon", Toast.LENGTH_SHORT).show();
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
    //UPDATED enhancement 3, d in CRUD moved to Room
    private void deleteLastEntry() {
        db.weightDao().deleteLast();
        Toast.makeText(this, R.string.last_entry_deleted, Toast.LENGTH_SHORT).show();
        loadWeightData();
    }


    // Load weight data from database and update RecyclerView
    private void loadWeightData() {
        //ArrayList<WeightEntry> weightList = dbHelper.getAllWeights(); //UPDATED enhancement 3 DEPRECATED

        // UPDATED enhancement 3 Read from Room
        AppDatabase db = AppDatabase.getInstance(this);
        List<WeightEntryEntity> rows = db.weightDao().getAll(); // UPDATED enhancement 3 oldest -> newest

        // UPDATED enhancement 3 map to existing UI model
        ArrayList<WeightEntry> listForUi = new ArrayList<>(rows.size());
        for (WeightEntryEntity e : rows) {
            // match WeightEntry String date, String weight constructor:
            listForUi.add(new WeightEntry(e.date, e.weight));
            // otherwise:
            // WeightEntry we = new WeightEntry();
            // we.setDate(e.date);
            // we.setWeight(e.weight);
            // listForUi.add(we);
        }

        // UPDATED enhancement 3 , UI expects most recent first, so reverse list ( Dao returns by date)
        java.util.Collections.reverse(listForUi); // newest at index 0
        // hook up RecyclerView adapter

        weightAdapter = new WeightAdapter(listForUi);
        // weightAdapter = new WeightAdapter(weightList);  // UPDATED Deprecated

        recyclerView.setAdapter(weightAdapter);

        // UPDATED enhancement 3 LOGCAT to confirm Room path is connecting, okay to delete
        android.util.Log.d("RoomTest", "Pulled" + rows.size() + " entries via Room");


        // Check if goal weight is met
        SharedPreferences weightPrefs = getSharedPreferences("WeightPrefs", MODE_PRIVATE);
        String goalWeight = weightPrefs.getString("goalWeight", null);

        // UPDATED enhancement 3, replaced weightList with listForUI
        if (goalWeight != null && !listForUi.isEmpty()) {
            String latestWeight = listForUi.get(0).getWeight(); // Get the most recent weight

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

    // UPDATED enhancement 3 replace addNewWeight with calls to Room Dao
    private void addNewWeight() {
        // Read and check input
        String weightStr = updateWeight.getText().toString().trim();
        if (weightStr.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_weight, Toast.LENGTH_SHORT).show();
            return;
        }

        // Build today's date string in the same db format
        String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());

        // insert using Room
        AppDatabase db = AppDatabase.getInstance(this);

        WeightEntryEntity e = new WeightEntryEntity();
        e.date = dateStr;          // yyyy-MM-dd
        e.weight = weightStr;      // weight remains as String

        db.weightDao().insert(e);  // Room insert

        // clear and refresh UI
        updateWeight.setText("");
        loadWeightData();          // re-query Room and update RecyclerView/summary
        Toast.makeText(this, R.string.weight_added, Toast.LENGTH_SHORT).show();
    }


    // to insert new weight DEPRECATED
    // private void addNewWeight() {
    //    String newWeight = updateWeight.getText().toString().trim();
    //    String currentDate = getCurrentDate(); //get today's date
    //    if (!newWeight.isEmpty()) {
    //        dbHelper.insertWeight(newWeight, currentDate); // Insert into database
    //        updateWeight.setText(""); // Clear input field
    //        loadWeightData(); // Refresh RecyclerView
    //        Toast.makeText(this, R.string.weight_added, Toast.LENGTH_SHORT).show();
    //    } else {
    //        Toast.makeText(this, R.string.please_enter_weight, Toast.LENGTH_SHORT).show();
    //    }
    //}

}
