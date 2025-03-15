package com.example.lasttele;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animated_background);


        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);

        // Initialize ImageViews
        white1 = findViewById(R.id.white1);
        blue1 = findViewById(R.id.blue1);
        white2 = findViewById(R.id.white2);
        blue2 = findViewById(R.id.blue2);

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

    public void onStartClick(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);


    }
}