package com.example.lasttele;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    EditText editTextPhone2, editTextCode;
    TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        editTextPhone2 = findViewById(R.id.editTextPhone2);
        txtResult = findViewById(R.id.txtmessage);
        editTextCode = findViewById(R.id.codeid);

        Button button123 = findViewById(R.id.button123);
        button123.setOnClickListener(this::onBtnClick);

        Button verifyid = findViewById(R.id.verifyid);
        verifyid.setOnClickListener(this::onCodeClick);
    }

    public void onBtnClick(View view) {
        String phone = editTextPhone2.getText().toString().trim();

        if (phone.isEmpty()) {
            txtResult.setText("Please enter a phone number!");
            return;
        }

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
            txtResult.setText("Session Restore Result: " + restoreResult.toString());

            // If the session is already authorized, skip OTP and go to ContactsActivity
            if (restoreResult.toString().equals("Session restored. Already authorized.")) {
                Intent intent = new Intent(this, ContactsActivity.class);
                startActivity(intent);
                finish(); // Optional: Closes the current activity so user can't go back with back button
                return; // Exit the method to avoid sending OTP unnecessarily
            }
        } else {
            // If the phone numbers don't match, inform the user
            txtResult.setText("Phone number does not match the previous one. Sending OTP to the new number.");
        }

        // Save the current phone number to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("last_phone_number", phone);
        editor.apply();

        // Call the Python function to send OTP
        PyObject result = pyObj.callAttr("phoneNumber", phone);

        // Display the result (success or error message)
        txtResult.setText("OTP Result: " + result.toString());

        String resultString = result.toString();
        if (resultString.equals("Already authorized. No need for OTP.")) {
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
            finish(); // Optional: Closes the current activity so user can't go back with back button
        }
    }

    public void onCodeClick(View view) {
        String phone = editTextPhone2.getText().toString().trim();
        String code = editTextCode.getText().toString().trim();
        if (code.isEmpty()) {
            txtResult.setText("Please enter the code you received.");
            return;
        }

        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("helloworld");

        PyObject result = pyObj.callAttr("otpCode", code, phone);

        txtResult.setText("Result: " + result.toString());

        String resultString = result.toString();

        // If login is successful, switch to ContactsActivity
        if (resultString.equals("Logged in successfully.")) {
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
            finish(); // Optional: Closes the current activity so user can't go back with back button
        }
    }
}