package com.example.lasttele;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;

public class Firstscreen extends AppCompatActivity {

    private ImageView white1, blue1, white2, blue2;
    private int screenWidth, screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animated_background);

        // Get screen dimensions
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Initialize ImageViews
        white1 = findViewById(R.id.white1);
        blue1 = findViewById(R.id.blue1);
        white2 = findViewById(R.id.white2);
        blue2 = findViewById(R.id.blue2);

        // Load and start the animation
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.random_movement);
        white1.startAnimation(animation);
        blue1.startAnimation(animation);
        white2.startAnimation(animation);
        blue2.startAnimation(animation);

        // Start custom random movement animations for each image
        startRandomMovement(white1);
        startRandomMovement(blue1);
        startRandomMovement(white2);
        startRandomMovement(blue2);
    }

    private void startRandomMovement(View view) {
        // Random initial direction (angle in radians)
        double angle = Math.random() * 2 * Math.PI; // Random angle between 0 and 2π
        float speed = 10; // Pixels per update (adjust for faster/slower movement)

        // Use arrays to store velocity components (to make them effectively final)
        float[] velocity = new float[2];
        velocity[0] = (float) (speed * Math.cos(angle)); // velocityX
        velocity[1] = (float) (speed * Math.sin(angle)); // velocityY

        // ValueAnimator to update position
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(16); // ~60 FPS (1000ms / 60 ≈ 16ms per frame)

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

            // Apply new position
            view.setX(x);
            view.setY(y);
        });

        animator.start();
    }
}