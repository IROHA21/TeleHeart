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

    private EditText editTextPhone2, editTextCode;
    private SwitchCompat switch1;
    private boolean switcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Initialize views
        editTextPhone2 = findViewById(R.id.editTextPhone2);
        switch1 = findViewById(R.id.switch1);
        editTextCode = findViewById(R.id.codeid);

        Button button123 = findViewById(R.id.button123);
        button123.setOnClickListener(this::onBtnClick);

        Button verifyid = findViewById(R.id.verifyid);
        verifyid.setOnClickListener(this::onCodeClick);

        // Initialize switcher
        switcher = false;
    }

    public void onBtnClick(View view) {
        String phone = editTextPhone2.getText().toString().trim();
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        // Update switcher based on switch state
        switcher = switch1.isChecked();

        // Save switcher to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("switcher", switcher);
        editor.apply();

        // Start the service with the switcher value
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.putExtra("switcher", switcher);
        startService(serviceIntent);

        // Background task to handle session and OTP
        HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();
        Handler backgroundHandler = new Handler(handlerThread.getLooper());

        backgroundHandler.post(() -> {
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");

            // Set session path
            String internalStoragePath = getFilesDir().getAbsolutePath();
            String sessionFilePath = internalStoragePath + "/session_" + phone;
            pyObj.callAttr("set_session_path", sessionFilePath);

            // Restore session if phone number matches
            String previousPhoneNumber = sharedPreferences.getString("last_phone_number", "");
            if (phone.equals(previousPhoneNumber)) {
                PyObject restoreResult = pyObj.callAttr("restoreSession");

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!restoreResult.toString().equals("Error: The key is not registered in the system (caused by GetDialogsRequest)")) {
                        Toast.makeText(this, "Session Restore Result: " + restoreResult.toString(), Toast.LENGTH_SHORT).show();
                    }

                    if (restoreResult.toString().equals("Session restored. Already authorized.")) {
                        Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                        intent.putExtra("switcher", switcher);
                        startActivity(intent);
                        finish();
                        handlerThread.quit();
                        return;
                    }
                });
            }

            // Save or clear phone number based on switch state
            if (switcher) {
                editor.putString("last_phone_number", phone);
            } else {
                editor.remove("last_phone_number");
            }
            editor.apply();

            // Send OTP
            PyObject result = pyObj.callAttr("phoneNumber", phone);

            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(this, "OTP Result: " + result.toString(), Toast.LENGTH_SHORT).show();

                if (result.toString().equals("Already authorized. No need for OTP.")) {
                    Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                    intent.putExtra("switcher", switcher);
                    startActivity(intent);
                    finish();
                }

                handlerThread.quit();
            });
        });
    }

    public void onCodeClick(View view) {
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        String phone = editTextPhone2.getText().toString().trim();
        String code = editTextCode.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter the code you received.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        // Update switcher based on switch state
        switcher = switch1.isChecked();

        // Save switcher to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("switcher", switcher);
        editor.apply();

        // Start the service with the switcher value
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.putExtra("switcher", switcher);
        startService(serviceIntent);

        // Background task to verify OTP
        HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();
        Handler backgroundHandler = new Handler(handlerThread.getLooper());

        backgroundHandler.post(() -> {
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");

            PyObject result = pyObj.callAttr("otpCode", code, phone);

            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();

                if (result.toString().equals("Logged in successfully.")) {
                    Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                    intent.putExtra("switcher", switcher);
                    startActivity(intent);
                }

                progressBar.setVisibility(View.INVISIBLE);
                handlerThread.quit();
            });
        });
    }
}