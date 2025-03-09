package com.example.lasttele;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private myadapter adapter;
    private String selectedContactId;
    private TextView textViewMessages;
    private ProgressBar progressBar;
    private Handler backgroundHandler;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_main);

        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Find the ProgressBar

        progressBar = findViewById(R.id.progressBar);

        // Initialize HandlerThread for background tasks
        HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        // Get Python instance and module
        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("helloworld");

        PyObject result = pyObj.callAttr("get_chats");

        List<String> chats = new ArrayList<>();
        for (PyObject item : result.asList()) {
            chats.add(item.toString());
        }

        // Process each string in the array
        List<contactList> items = new ArrayList<>();
        for (String chat : chats) {
            String[] parts = chat.split(", chat id: ");
            String chatName = parts[0].replace("chatname: ", "");
            long chatId = Long.parseLong(parts[1]);

            if (chatId < 0) {
                continue;
            }

            int imageResource = R.drawable.individual;
            items.add(new contactList(chatName, imageResource, chatId));
        }

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new myadapter(this, items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);



        // Set up button click listener
        Button buttonSelect = findViewById(R.id.buttonselect);
        buttonSelect.setOnClickListener(v -> {
            long selectedId = adapter.getSelectedContactId();
            if (selectedId != -1) {
                selectedContactId = String.valueOf(selectedId);

                // Start LoadingActivity with the selected contact ID
                Intent intent = new Intent(ContactsActivity.this, LoadingActivity.class);
                intent.putExtra("selectedContactId", selectedContactId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No contact selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onbut(View view) {
        System.out.println(selectedContactId);
    }

    public void onbuttonclick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}