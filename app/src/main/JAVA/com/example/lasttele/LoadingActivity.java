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
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView progressTextView;
    private TextView messagesProgressTextView;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean switcher;
    private static boolean isResultActivityStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        // Show the interstitial ad
        if (AdUtils.isUserInCISOrRussia(this)) {
            // Use Yandex Ads for CIS countries
            YandexAdManager.getInstance().showInterstitialAd(this, new AdDismissListener() {
                @Override
                public void onAdDismissed() {
                    startBackgroundTask(); // Start the background task after the ad is dismissed
                }
            });
        } else {
            // Use Google Ads for the rest of the world
            AdManager.getInstance().showInterstitialAd(this, new AdDismissListener() {
                @Override
                public void onAdDismissed() {
                    startBackgroundTask(); // Start the background task after the ad is dismissed
                }
            });
        }

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // SDK initialized, ready to load ads
            }
        });

        // Initialize UI elements
        progressBar = findViewById(R.id.loadingProgressBar);
        progressTextView = findViewById(R.id.progressTextView);
        messagesProgressTextView = findViewById(R.id.messagesProgressTextView);

        // Get the selected contact ID and quantity from the intent
        String selectedContactId = getIntent().getStringExtra("selectedContactId");
        String quantityStr = getIntent().getStringExtra("quantity");
        int quantity = Integer.parseInt(quantityStr);

        // Calculate the total time
        double totalTimeSeconds = 0.01067 * quantity - 0.68;
        int totalTimeMillis = (int) (totalTimeSeconds * 1000);

        // Start updating the ProgressBar
        startProgressBar(totalTimeMillis, quantity);
    }

    private void startBackgroundTask() {
        // Start the background task to fetch messages
        new Thread(() -> {
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");

            // Call getconvo first to ensure user_id is set
            PyObject con = pyObj.callAttr("getconvo", getIntent().getStringExtra("selectedContactId"), Integer.parseInt(getIntent().getStringExtra("quantity")));

            if (con == null) {
                System.out.println("getconvo returned null");
                return;
            }

            // Now call get_user_id_sync to retrieve the user_id
            PyObject idu = pyObj.callAttr("get_user_id_sync");

            if (idu == null) {
                System.out.println("get_user_id_sync returned null");
                return;
            }

            // Convert PyObject to String directly
            String userId = idu.toString();  // No need to call .get("user_id")

            // Convert PyObject elements to String
            List<String> messages = new ArrayList<>();
            for (PyObject obj : con.asList()) {
                messages.add(obj.toString());
            }

            // Save messages to the database
            DatabaseHelper dbHelper = new DatabaseHelper(LoadingActivity.this);
            dbHelper.deleteAllMessages(); // Clear old data before saving new messages
            dbHelper.saveMessages(messages); // Save new messages

            // Switch to ResultsActivity when done
            Intent intent = new Intent(LoadingActivity.this, ResultsActivity.class);
            intent.putExtra("selectedContactId", getIntent().getStringExtra("selectedContactId"));
            intent.putExtra("user_id", userId);
            intent.putExtra("switcher", getIntent().getBooleanExtra("switcher", false));
            startActivity(intent);
            isResultActivityStarted = true;
            finish(); // Close the LoadingActivity
        }).start();
    }

    private void startProgressBar(int totalTimeMillis, int quantity) {
        final int totalProgress = 100; // ProgressBar max value
        final int interval = 100; // Update interval in milliseconds
        final int steps = totalTimeMillis / interval; // Number of steps to reach 100%

        // Update the ProgressBar incrementally
        new Thread(() -> {
            for (int i = 0; i <= steps; i++) {
                final int progress = (i * totalProgress) / steps;
                final int messagesProgress = (i * quantity) / steps; // Calculate the messages progress

                // Update the UI on the main thread
                mainHandler.post(() -> {
                    progressBar.setProgress(progress);
                    progressTextView.setText(progress + "%"); // Update the percentage TextView
                    messagesProgressTextView.setText(messagesProgress + "/" + quantity); // Update the messages progress TextView
                });

                try {
                    Thread.sleep(interval); // Wait for the interval
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}