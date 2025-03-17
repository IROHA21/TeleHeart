package com.example.lasttele;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Firstscreen extends AppCompatActivity {

    private ImageView white1, blue1, white2, blue2;
    private int screenWidth, screenHeight;
    private Random random = new Random();
    private int[] imageResources = {R.drawable.e2, R.drawable.e3, R.drawable.e4, R.drawable.e5,
            R.drawable.e6, R.drawable.e7, R.drawable.e8, R.drawable.e9,
            R.drawable.e10, R.drawable.e11};
    private int maxImages = 20; // Maximum number of images (including the initial 4)

    // Map to store ValueAnimators for each view
    private final Map<View, ValueAnimator> animatorMap = new HashMap<>();

    // SharedPreferences key for saving the selected language
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String LANGUAGE_KEY = "language";

    // UI elements
    private TextView welcomeMessage;
    private Button startButton, tutorialButton;
    private Spinner languageSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the locale based on the saved preference
        setLocaleFromPreferences();

        setContentView(R.layout.animated_background);

        // Initialize UI elements
        welcomeMessage = findViewById(R.id.welcome_message);
        startButton = findViewById(R.id.start_button);
        tutorialButton = findViewById(R.id.tutorial_button);
        languageSpinner = findViewById(R.id.language_spinner);

        // Update UI texts based on the current language
        updateUITexts();

        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);

        // Initialize ImageViews
        white1 = findViewById(R.id.white1);
        blue1 = findViewById(R.id.blue1);
        white2 = findViewById(R.id.white2);
        blue2 = findViewById(R.id.blue2);

        // Set up the language spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.languages,
                R.layout.custom_spinner_item // Use custom layout for the selected item
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item); // Use custom layout for dropdown items
        languageSpinner.setAdapter(adapter);

        // Set the spinner to the current language
        String currentLanguage = getSavedLanguage();
        if (currentLanguage.equals("ru")) {
            languageSpinner.setSelection(1); // Select Russian
        } else {
            languageSpinner.setSelection(0); // Select English
        }

        // Set the spinner listener
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                String newLanguageCode = selectedLanguage.equals("Русский") ? "ru" : "en";

                // Only restart the activity if the language has changed
                if (!newLanguageCode.equals(getSavedLanguage())) {
                    saveLanguage(newLanguageCode); // Save the new language
                    setLocale(newLanguageCode); // Set the new locale
                    restartActivity(); // Restart the activity to apply the new language
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Get screen dimensions after the layout is drawn
        View rootView = findViewById(R.id.root_layout);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            screenWidth = rootView.getWidth();
            screenHeight = rootView.getHeight();

            // Start animations after screen dimensions are known
            startRandomMovement(white1);
            startRandomMovement(blue1);
            startRandomMovement(white2);
            startRandomMovement(blue2);
        });

        // Set click listeners for the images
        setOnClickListeners(white1);
        setOnClickListeners(blue1);
        setOnClickListeners(white2);
        setOnClickListeners(blue2);
    }

    // Helper method to update UI texts based on the current language
    private void updateUITexts() {
        welcomeMessage.setText(getString(R.string.Welcome_to_TeleHeart));
        startButton.setText(getString(R.string.Start));
        tutorialButton.setText(getString(R.string.Tutorial));
    }

    // Helper method to change the app's locale
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    // Helper method to set the locale from SharedPreferences
    private void setLocaleFromPreferences() {
        String languageCode = getSavedLanguage();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    // Helper method to save the selected language to SharedPreferences
    private void saveLanguage(String languageCode) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LANGUAGE_KEY, languageCode);
        editor.apply();
    }

    // Helper method to get the saved language from SharedPreferences
    private String getSavedLanguage() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getString(LANGUAGE_KEY, "en"); // Default to English
    }

    // Helper method to restart the activity
    private void restartActivity() {
        Intent intent = getIntent();
        finish(); // Finish the current activity
        startActivity(intent); // Start the activity again
    }

    // Rest of your existing methods (e.g., setOnClickListeners, startRandomMovement, etc.)
    private void setOnClickListeners(ImageView imageView) {
        imageView.setOnClickListener(v -> {
            // Check if we've reached the maximum number of images
            RelativeLayout rootLayout = findViewById(R.id.root_layout);
            if (rootLayout.getChildCount() >= maxImages) {
                Log.d("SpawnedImages", "Maximum images reached: " + maxImages);
                return; // Stop spawning new images
            }

            // Create a new ImageView at the clicked location
            ImageView newImage = new ImageView(this);

            // Double the size of the image (e.g., 48x48 becomes 96x96)
            int imageSize = 96; // New size (2 times bigger)
            newImage.setLayoutParams(new RelativeLayout.LayoutParams(imageSize, imageSize)); // Set size

            // Set a random image from the resources
            newImage.setImageResource(imageResources[random.nextInt(imageResources.length)]); // Random image

            // Position the new image at the clicked location
            newImage.setX(imageView.getX());
            newImage.setY(imageView.getY());

            // Add the new ImageView to the layout
            rootLayout.addView(newImage);

            // Start random movement for the new image
            startRandomMovement(newImage);

            Log.d("SpawnedImages", "Total images: " + rootLayout.getChildCount());
        });
    }

    private void startRandomMovement(View view) {
        // Stop any existing animator for this view
        if (animatorMap.containsKey(view)) {
            ValueAnimator existingAnimator = animatorMap.get(view);
            if (existingAnimator != null) {
                existingAnimator.cancel();
            }
        }

        // Random initial direction (angle in radians)
        double angle = Math.random() * 2 * Math.PI; // Random angle between 0 and 2π
        float speed = 7; // Pixels per update (adjust for faster/slower movement)

        // Use arrays to store velocity components (to make them effectively final)
        float[] velocity = new float[2];
        velocity[0] = (float) (speed * Math.cos(angle)); // velocityX
        velocity[1] = (float) (speed * Math.sin(angle)); // velocityY

        // Create a ValueAnimator for smooth animation
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(16); // ~60 FPS

        animator.addUpdateListener(animation -> {
            // Update position
            float x = view.getX() + velocity[0];
            float y = view.getY() + velocity[1];

            // Check for collisions with screen edges
            if (x <= 0 || x >= screenWidth - view.getWidth()) {
                velocity[0] *= -1; // Reverse X direction
            }
            if (y <= 0 || y >= screenHeight - view.getHeight()) {
                velocity[1] *= -1; // Reverse Y direction
            }

            // Update the view's position
            view.setX(x);
            view.setY(y);
        });

        // Store the animator in the map
        animatorMap.put(view, animator);

        // Start the animation
        animator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up all animators
        for (ValueAnimator animator : animatorMap.values()) {
            if (animator != null) {
                animator.cancel();
            }
        }
        animatorMap.clear();
    }

    public void onStartClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}