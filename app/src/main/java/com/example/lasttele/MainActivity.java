package com.example.lasttele;

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

    EditText editTextPhone2;
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

        Button button123 = findViewById(R.id.button123);
        button123.setOnClickListener(this::onBtnClick); // Using View view
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
}
