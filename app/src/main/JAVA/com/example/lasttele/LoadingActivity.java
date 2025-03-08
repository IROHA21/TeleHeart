package com.example.lasttele;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView progressTextView;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        progressBar = findViewById(R.id.loadingProgressBar);
        progressTextView = findViewById(R.id.progressTextView);

        // Show initial progress
        progressBar.setProgress(1);
        progressTextView.setText("0/2000");

        // Get the selected contact ID from the intent
        String selectedContactId = getIntent().getStringExtra("selectedContactId");

        // Start the background task to fetch messages
        new Thread(() -> {
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");
            PyObject con = pyObj.callAttr("getconvo", selectedContactId);

            // Convert PyObject elements to String
            List<String> cont = new ArrayList<>();
            for (PyObject obj : con.asList()) {
                cont.add(obj.toString());
            }

            int totalMessages = cont.size();
            int totalCharacters = 0;

            for (int i = 0; i < totalMessages; i++) {
                totalCharacters += cont.get(i).length();

                // Update progress after processing each message
                final int progress = (int) ((i + 1) / (float) totalMessages * 100);
                final int currentIndex = i + 1;

                mainHandler.post(() -> {
                    progressBar.setProgress(progress);
                    progressTextView.setText(currentIndex + "/" + totalMessages);
                });

                // Simulate delay (remove this in production)
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Switch to ResultsActivity when done
            Intent intent = new Intent(LoadingActivity.this, ResultsActivity.class);
            intent.putExtra("totalCharacters", totalCharacters); // Pass totalCharacters
            intent.putStringArrayListExtra("messages", new ArrayList<>(cont)); // Pass the list of messages
            startActivity(intent);
            finish(); // Close the LoadingActivity
        }).start();
    }
}