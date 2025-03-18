package com.example.lasttele;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    EditText editTextPhone2, editTextCode, codeid;
    SwitchCompat switch1;

    boolean switcher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switcher  = false;
        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }


        editTextPhone2 = findViewById(R.id.editTextPhone2);
        switch1 = findViewById(R.id.switch1);
        editTextCode = findViewById(R.id.codeid);

        Button button123 = findViewById(R.id.button123);
        button123.setOnClickListener(this::onBtnClick);

        Button verifyid = findViewById(R.id.verifyid);
        verifyid.setOnClickListener(this::onCodeClick);





    }

    public void onBtnClick(View view) {
        // Get the phone number and show the progress bar
        String phone = editTextPhone2.getText().toString().trim();
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        if (phone.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new thread to handle the logic
        Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            // Background thread logic
            boolean switcher = switch1.isChecked();

            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");  // Ensure "helloworld.py" is in "src/main/python"

            // Get the internal storage path
            String internalStoragePath = getFilesDir().getAbsolutePath();

            // Set the session file path based on the phone number
            String sessionFilePath = internalStoragePath + "/session_" + phone;
            pyObj.callAttr("set_session_path", sessionFilePath);

            // Retrieve the previous phone number from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String previousPhoneNumber = sharedPreferences.getString("last_phone_number", "");

            // Check if the entered phone number matches the previous phone number
            if (phone.equals(previousPhoneNumber)) {
                // If the phone numbers match, restore the session
                PyObject restoreResult = pyObj.callAttr("restoreSession");
                handler.post(() -> {
                    Toast.makeText(this, "Session Restore Result: " + restoreResult.toString(), Toast.LENGTH_SHORT).show();
                });

                // If the session is already authorized, skip OTP and go to ContactsActivity
                if (restoreResult.toString().equals("Session restored. Already authorized.")) {
                    handler.post(() -> {
                        System.out.println("switcher check : " + switcher);
                        Intent intent = new Intent(this, ContactsActivity.class);
                        intent.putExtra("switcher", switcher); // Pass the user ID
                        startActivity(intent);
                        finish(); // Optional: Closes the current activity so user can't go back with back button
                    });
                    return; // Exit the method to avoid sending OTP unnecessarily
                }
            } else {
                // If the phone numbers don't match, inform the user
                handler.post(() -> {
                    Toast.makeText(this, "Phone number does not match the previous one. Sending OTP to the new number.", Toast.LENGTH_SHORT).show();


                });
            }

            // Check the state of the switch
            if (switch1.isChecked()) {
                // If the switch is on, save the current phone number to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("last_phone_number", phone);
                editor.apply();
            } else {
                // If the switch is off, clear the saved phone number (optional)
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("last_phone_number");
                editor.apply();
            }

            // Call the Python function to send OTP
            PyObject result = pyObj.callAttr("phoneNumber", phone);

            // Display the result (success or error message)
            handler.post(() -> {
                Toast.makeText(this, "OTP Result: " + result.toString(), Toast.LENGTH_SHORT).show();
            });

            String resultString = result.toString();
            System.out.println("before" + resultString);
            String check = "Code sent check your telegram";
            if (resultString.equals(check)) {
                handler.post(() -> {
                    progressBar.setVisibility(View.INVISIBLE);

                });
            }

            if (resultString.equals("Already authorized. No need for OTP.")) {
                handler.post(() -> {
                    System.out.println("switcher check no need: " + switcher);
                    Intent intent = new Intent(this, ContactsActivity.class);
                    intent.putExtra("switcher", switcher); // Pass the user ID
                    startActivity(intent);
                    finish(); // Optional: Closes the current activity so user can't go back with back button
                });
            }
        }).start();
    }

    public void onCodeClick(View view) {

        if (switch1.isChecked()) {
            // If the switch is on, save the current phone number to SharedPreferences
            switcher = true;
        }
        String phone = editTextPhone2.getText().toString().trim();
        String code = editTextCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter the code you received.", Toast.LENGTH_SHORT).show();

            return;
        }

        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("helloworld");

        PyObject result = pyObj.callAttr("otpCode", code, phone);

        Toast.makeText(this, "Result: " + result.toString(), Toast.LENGTH_SHORT).show();


        String resultString = result.toString();

        // If login is successful, switch to ContactsActivity
        if (resultString.equals("Logged in successfully.")) {

            System.out.println("switcher value in before sending " + switcher);
            Intent intent = new Intent(this, ContactsActivity.class);
            intent.putExtra("switcher", switcher); // Pass the user ID


            Intent intent2 = new Intent(this, LoadingActivity.class);
            intent2.putExtra("switcher", switcher); // Pass the user ID

            Intent intent3 = new Intent(this, ResultsActivity.class);
            intent3.putExtra("switcher", switcher); // Pass the user ID

            startActivity(intent);

        }


    }





}