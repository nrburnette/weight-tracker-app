package com.zybooks.projecttwonickburnetteweightlossoption;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText enterUsername, enterPassword, firstNameEditText; // UPDATED
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Ties to activity_login.xml

        // Initialize UI elements from activity_login.xml
        enterUsername = findViewById(R.id.enterUsername);
        enterPassword = findViewById(R.id.enterPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        // Updated initialize new UI first name variable
        firstNameEditText = findViewById(R.id.firstNameEditText);

        // Check if the user is already logged in

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);


        if (isLoggedIn) {
            goToMainActivity(); // Skip login if already authenticated
        }

        // Handle login button click
        buttonLogin.setOnClickListener(v -> {
            String username = enterUsername.getText().toString().trim();
            String password = enterPassword.getText().toString().trim();
            // UPDATED saving the first name to be used in the inspiration message
            String enteredFirstName = firstNameEditText.getText().toString().trim();

            if (validateLogin(username, password)) {
                // Save login state
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                // UPDATED to save the enteredFirstName if login successful
                editor.putString(getString(R.string.pref_first_name_key), enteredFirstName);
                editor.apply();

                goToMainActivity(); // Redirect after successful login
            } else {
                Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validate login credentials (Replace with real credentials later)
    private boolean validateLogin(String username, String password) {
        return username.equals("admin") && password.equals("password");
    }

    // Redirect user to MainActivity
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close LoginActivity so the user can’t go back
    }
}
