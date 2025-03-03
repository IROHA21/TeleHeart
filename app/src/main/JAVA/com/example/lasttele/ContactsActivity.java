package com.example.lasttele;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);


        List<contactList> items = new ArrayList<contactList>();
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));
        items.add(new contactList("jhon wick", R.drawable.group, 123456));





        // Set up button click listener

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new myadapter(getApplicationContext(), items));

        // Get Python instance and module
    }

}

