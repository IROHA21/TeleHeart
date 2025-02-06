package com.example.lasttele;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    EditText editTextPhone2;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        editTextPhone2 = findViewById(R.id.editTextPhone2);
        Button button123 = findViewById(R.id.button123);

        button123.setOnClickListener(v -> onBtnClick());
    }

    public void onBtnClick() {
        phone = editTextPhone2.getText().toString().trim();

        if (phone.isEmpty()) {
            return; // Don't proceed if phone number is empty
        }

        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("helloworld");  // Ensure "helloworld.py" is in "src/main/python"

        // Call Python function
        PyObject result = pyObj.callAttr("phoneNumber", phone);

        // Find the TextView to display result
        TextView txtResult = findViewById(R.id.txtmessage);
        txtResult.setText("Result: " + result.toString());
    }
}
