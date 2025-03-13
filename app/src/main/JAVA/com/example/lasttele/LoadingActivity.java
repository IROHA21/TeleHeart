package com.example.lasttele;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView progressTextView;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean switcher;
    private static boolean isResultActivityStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        progressBar = findViewById(R.id.loadingProgressBar);


        // Show initial progress
        Intent intent2 = getIntent();
        switcher = intent2.getBooleanExtra("switcher", false);

        System.out.println("switcher check inside of loading : " + switcher );


        // Get the selected contact ID from the intent
        String selectedContactId = getIntent().getStringExtra("selectedContactId");
        String quantityStr = getIntent().getStringExtra("quantity");
        int quantity = Integer.parseInt(quantityStr); // Convert it back to an integer


        // Start the background task to fetch messages
        new Thread(() -> {
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");

            // Call getconvo first to ensure user_id is set
            PyObject con = pyObj.callAttr("getconvo", selectedContactId, quantity);

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
            intent.putExtra("selectedContactId", selectedContactId); // Pass the contact ID
            intent.putExtra("user_id", userId); // Pass the user ID
            startActivity(intent);
            isResultActivityStarted = true;
            finish();
             // Close the LoadingActivity
        }).start();
}
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Check if the user chose "Don't remember me"
        System.out.println("switcher destroy activated login");


        if (!switcher && !isResultActivityStarted){
            // Terminate the session and disconnect the client
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");
            PyObject result = pyObj.callAttr("terminate_and_disconnect");
            Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}
