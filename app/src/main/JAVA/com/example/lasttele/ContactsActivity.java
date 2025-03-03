package com.example.lasttele;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_main);

        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Find the TextView and Button
        TextView textView2 = findViewById(R.id.textView2);


        // Set up button click listener

        // Get Python instance and module
        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("helloworld");

        // Call Python function
        PyObject result = pyObj.callAttr("get_chats");

        // Convert PyObject to List<String>
        List<String> chats = new ArrayList<>();
        for (PyObject item : result.asList()) {
            chats.add(item.toString());
        }

        // Find the TextView


        // Display chats
        if (!chats.isEmpty()) {
            StringBuilder chatText = new StringBuilder();
            for (String chat : chats) {
                chatText.append(chat).append("\n\n");
            }
            textView2.setText(chatText.toString());
        } else {
            textView2.setText("No chats found.");
        }
    }
}
    // Method to handle button click
