package com.example.lasttele;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_screen);

        TextView resultsTextView = findViewById(R.id.resultsTextView);

        // Get the total characters from the intent
        int totalCharacters = getIntent().getIntExtra("totalCharacters", 0);

        // Display the total characters
        resultsTextView.setText("Total characters: " + totalCharacters);
    }
}