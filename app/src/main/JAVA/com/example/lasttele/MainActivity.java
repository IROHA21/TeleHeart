package com.example.lasttele;

import android.content.Intent;
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

    EditText editTextPhone2,editTextCode;

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
        button123.setOnClickListener(this::onBtnClick); // Using View view

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

        // Call the Python function to send OTP
        PyObject result = pyObj.callAttr("phoneNumber", phone);

        // Display the result (success or error message)
        txtResult.setText("Result: " + result.toString());






    }

    public void onCodeClick(View view){
        String phone = editTextPhone2.getText().toString().trim();
        String code = editTextCode.getText().toString().trim();
        if (code.isEmpty()){
            txtResult.setText("please enter the code you recieved");
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