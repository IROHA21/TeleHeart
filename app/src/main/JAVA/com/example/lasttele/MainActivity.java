package com.example.lasttele;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
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

    EditText editTextPhone2, editTextCode;
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
        String phone = editTextPhone2.getText().toString().trim();
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE); // Hide progress bar if phone is empty
            return;
        }

        if (switch1.isChecked()) {
            // If the switch is on, save the current phone number to SharedPreferences
            switcher = true;
        }

        // Create and start a HandlerThread for background work
        HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();

        // Create a Handler associated with the HandlerThread's Looper
        Handler backgroundHandler = new Handler(handlerThread.getLooper());

        // Post the background work to the HandlerThread
        backgroundHandler.post(() -> {
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");

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

                // Post UI updates to the main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    System.out.println("check in : " + "Session Restore Result: " + restoreResult);
                    if (!restoreResult.toString().equals("Error: The key is not registered in the system (caused by GetDialogsRequest)")) {
                        Toast.makeText(MainActivity.this, "Session Restore Result: " + restoreResult.toString(), Toast.LENGTH_SHORT).show();
                    }


                    // If the session is already authorized, skip OTP and go to ContactsActivity
                    if (restoreResult.toString().equals("Session restored. Already authorized.")) {
                        System.out.println("switcher check : " + switcher);
                        Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                        intent.putExtra("switcher", switcher); // Pass the user ID
                        startActivity(intent);
                        finish(); // Optional: Closes the current activity so user can't go back with back button
                        handlerThread.quit(); // Quit the HandlerThread
                        return; // Exit the method to avoid sending OTP unnecessarily
                    }
                });
            } else {
                // If the phone numbers don't match, inform the user
                new Handler(Looper.getMainLooper()).post(() -> {
                    System.out.println("Phone number does not match the previous one. Sending OTP to the new number.");

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

            // Post the result to the main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(MainActivity.this, "OTP Result: " + result.toString(), Toast.LENGTH_SHORT).show();
                System.out.println("check in 2 : " + "OTP Result: " + result);
                String resultString = result.toString();
                System.out.println("before" + resultString);
                String check = "Code sent check your telegram";

                if (resultString.equals(check)) {
                    progressBar.setVisibility(View.INVISIBLE);
                }

                if (resultString.equals("Already authorized. No need for OTP.")) {
                    System.out.println("switcher check no need: " + switcher);
                    Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                    intent.putExtra("switcher", switcher); // Pass the user ID
                    startActivity(intent);
                    finish(); // Optional: Closes the current activity so user can't go back with back button
                }

                handlerThread.quit(); // Quit the HandlerThread after all work is done
            });
        });
    }

    public void onCodeClick(View view) {
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE); // Show the progress bar

        if (switch1.isChecked()) {
            // If the switch is on, save the current phone number to SharedPreferences
            switcher = true;
        }

        String phone = editTextPhone2.getText().toString().trim();
        String code = editTextCode.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter the code you received.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE); // Hide progress bar if code is empty
            return;
        }

        // Create and start a HandlerThread for background work
        HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();

        // Create a Handler associated with the HandlerThread's Looper
        Handler backgroundHandler = new Handler(handlerThread.getLooper());

        // Post the background work to the HandlerThread
        backgroundHandler.post(() -> {
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");

            // Call the Python function to verify the OTP
            PyObject result = pyObj.callAttr("otpCode", code, phone);
            String resultString = result.toString();

            // Post the result to the main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(MainActivity.this,  resultString, Toast.LENGTH_SHORT).show();
                System.out.println("check in 3 : " + "Result: " + resultString);

                // If login is successful, switch to ContactsActivity
                if (resultString.equals("Logged in successfully.")) {
                    System.out.println("switcher value in before sending " + switcher);
                    Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                    intent.putExtra("switcher", switcher); // Pass the user ID

                    // Optional: Start other activities if needed
                    Intent intent2 = new Intent(MainActivity.this, LoadingActivity.class);
                    intent2.putExtra("switcher", switcher); // Pass the user ID

                    Intent intent3 = new Intent(MainActivity.this, ResultsActivity.class);
                    intent3.putExtra("switcher", switcher); // Pass the user ID

                    startActivity(intent);
                }

                progressBar.setVisibility(View.INVISIBLE); // Hide the progress bar
                handlerThread.quit(); // Quit the HandlerThread after all work is done
            });
        });
    }





}