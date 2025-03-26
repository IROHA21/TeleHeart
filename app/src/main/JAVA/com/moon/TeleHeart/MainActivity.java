package com.moon.TeleHeart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    EditText editTextPhone2, editTextCode, codeid;
    SwitchCompat switch1;
    Button verifyid;
    Button button123;
    ImageButton resendButton;

    boolean switcher;
    private int clicks = 0;
    private final int MAX_CLICKS = 1;

    private TextView timerTextView; // Declare the TextView


    private long currentCooldownDuration = 10000; // Initial cooldown duration (10 seconds)

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_FIRST_LAUNCH = "isFirstLaunch";
    private static final String PREF_FIRST_LAUNCH2 = "isFirstLaunch2";
    private static final String PREFS_NAME2 = "switchon";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences settings2 = getSharedPreferences(PREFS_NAME2, MODE_PRIVATE);

        // Check if it's the first launch
        boolean isFirstLaunch = settings.getBoolean(PREF_FIRST_LAUNCH, true);
        boolean isFirstLaunch2 = settings2.getBoolean(PREF_FIRST_LAUNCH2, true);

        if (isFirstLaunch) {
            // Launch popactivity only if it's the first launch
            startActivity(new Intent(MainActivity.this, popactivity.class));

            // Set the flag to false so this doesn't happen again
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PREF_FIRST_LAUNCH, false);
            editor.apply();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(com.google.android.gms.ads.initialization.InitializationStatus initializationStatus) {
                // SDK initialized, ready to load ads
            }
        });

        // Load the interstitial ad in advance
        if (AdUtils.isUserInCISOrRussia(this)) { // Pass 'this' as the Context
            // Use Yandex Ads for CIS countries
            YandexAdManager.getInstance().loadInterstitialAd(this);
        } else {
            // Use Google Ads for the rest of the world
            AdManager.getInstance().loadInterstitialAd(this);
        }


        switcher = false;

        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Initialize views
        editTextPhone2 = findViewById(R.id.editTextPhone2);
        switch1 = findViewById(R.id.switch1);
        editTextCode = findViewById(R.id.codeid);
        codeid = findViewById(R.id.codeid); // Initialize codeid here

        button123 = findViewById(R.id.button123);
        button123.setOnClickListener(this::onBtnClick);

        verifyid = findViewById(R.id.verifyid);
        verifyid.setOnClickListener(this::onCodeClick);

        timerTextView = findViewById(R.id.timerTextView); // Initialize the TextView

        resendButton = findViewById(R.id.resend);
        resendButton.setOnClickListener(this::onBtnClick);

        // Add OnCheckedChangeListener to switch1
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the switcher variable
                switcher = isChecked;

                if (isChecked) {
                    if (isFirstLaunch2) {
                        // Launch popactivity only if it's the first launch

                        // Launch rememberaccount activity when the switch is turned ON
                        startActivity(new Intent(MainActivity.this, rememberaccount.class));

                        // Set the flag to false so this doesn't happen again
                        SharedPreferences.Editor editor = settings2.edit();
                        editor.putBoolean(PREF_FIRST_LAUNCH2, false);
                        editor.apply();
                    }
                    // Switch is ON

                } else {
                    // Switch is OFF

                }
            }
        });
    }

    public void onBtnClick(View view) {



        int buttonId = view.getId();



        // Common logic for both buttons
        String phone = editTextPhone2.getText().toString().trim();
        ProgressBar progressBar = findViewById(R.id.progressBar2);


        if (phone.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_phone_number, Toast.LENGTH_SHORT).show();
        }
        else if (!phone.startsWith("+")) {
            Toast.makeText(this, R.string.phone_must_start_with_plus, Toast.LENGTH_SHORT).show();
        }
        else if (!phone.substring(1).matches("[0-9]+")) {
            Toast.makeText(this, R.string.phone_must_contain_only_numbers, Toast.LENGTH_SHORT).show();
        }
        else if (phone.length() < 8) {
            Toast.makeText(this, R.string.phone_too_short, Toast.LENGTH_SHORT).show();
        }
        else if (phone.length() > 15) {
            Toast.makeText(this, R.string.phone_too_long, Toast.LENGTH_SHORT).show();
        }
        else {
            // All conditions passed - show progress bar
            progressBar.setVisibility(View.VISIBLE);
            // Continue with your phone number processing


            if (buttonId == R.id.button123) {
                // Specific logic for button123
                clicks++;
                if (clicks >= MAX_CLICKS) {
                    button123.setEnabled(false);
                    resendButton.setEnabled(false);
                    startCooldownTimer();
                }
            } else if (buttonId == R.id.resend) {
                clicks++;
                if (clicks >= MAX_CLICKS) {
                    resendButton.setEnabled(false);
                    startCooldownTimer();
                }
            }

        // Create a new thread to handle the logic
        Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            // Background thread logic
            boolean switcher = switch1.isChecked();

            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");  // Ensure "helloworld.py" is in "src/main/python"

            // Get the internal storage path
            String internalStoragePath = getFilesDir().getAbsolutePath();

            // Set the session file path based on the phone number
            String sessionFilePath = internalStoragePath + "/session_" + phone;
            pyObj.callAttr("set_session_path", sessionFilePath);

            // Retrieve the previous phone number from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String previousPhoneNumber = sharedPreferences.getString("last_phone_number", "");

            // Check if the entered phone number matches the previous phone number
            if (phone.equals(previousPhoneNumber)) {
                // If the phone numbers match, restore the session
                PyObject restoreResult = pyObj.callAttr("restoreSession");
                handler.post(() -> {
                    System.out.println("Session Restore Result: " + restoreResult);
                });

                // If the session is already authorized, skip OTP and go to ContactsActivity
                if (restoreResult.toString().equals("Session restored. Already authorized.")) {
                    handler.post(() -> {
                        System.out.println("switcher check : " + switcher);
                        Intent intent = new Intent(this, ContactsActivity.class);
                        intent.putExtra("switcher", switcher); // Pass the user ID
                        startActivity(intent);
                        finish(); // Optional: Closes the current activity so user can't go back with back button
                    });
                    return; // Exit the method to avoid sending OTP unnecessarily
                }
            } else {
                // If the phone numbers don't match, inform the user
                handler.post(() -> {
                    System.out.println("Phone number does not match the previous one. S0ending OTP to the new number.");
                    codeid.setVisibility(View.VISIBLE);
                    verifyid.setVisibility(View.VISIBLE);
                    button123.setVisibility(View.INVISIBLE);
                    resendButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);


                });
            }

            // Check the state of the switch
            if (switch1.isChecked()) {
                // If the switch is on, save the current phone number to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("last_phone_number", phone);
                editor.apply();
            } else {
                // If the switch is off, clear the saved phone number (optional)
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("last_phone_number");
                editor.apply();
            }

            // Call the Python function to send OTP
            PyObject result = pyObj.callAttr("phoneNumber", phone);
            String resultString = result.toString();
            // Display the result (success or error message)
            handler.post(() -> {

                if (resultString.equals("Code sent check your telegram")) {
                    Toast.makeText(this, R.string.Code_sent_check_your_telegram, Toast.LENGTH_SHORT).show();
                } else if(resultString.equals("Error: database is locked")||resultString.equals("Error: Cannot send requests while disconnected")) {
                    Toast.makeText(this, R.string.try_again, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "OTP Result: " + result.toString(), Toast.LENGTH_SHORT).show();
                    System.out.println("OTP Result: " + result);
                }
            });


            System.out.println("before" + resultString);
            String check = "Code sent check your telegram";
            if (resultString.equals(check)) {
                handler.post(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    codeid.setVisibility(View.VISIBLE);
                    verifyid.setVisibility(View.VISIBLE);
                    button123.setVisibility(View.INVISIBLE);
                    resendButton.setVisibility(View.VISIBLE);

                });
            }

            if (resultString.equals("Already authorized. No need for OTP.")) {
                handler.post(() -> {
                    System.out.println("switcher check no need: " + switcher);
                    Intent intent = new Intent(this, ContactsActivity.class);
                    intent.putExtra("switcher", switcher); // Pass the user ID
                    startActivity(intent);
                    finish(); // Optional: Closes the current activity so user can't go back with back button
                });
            }
        }).start();
        }
    }

    public void onCodeClick(View view) {
        ProgressBar progressBar = findViewById(R.id.progressBar2);

        progressBar.setVisibility(View.VISIBLE);
        // Create a new thread to handle the logic
        Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            // Background thread logic
            boolean switcher = switch1.isChecked();
            String phone = editTextPhone2.getText().toString().trim();
            String code = editTextCode.getText().toString().trim();

            if (code.isEmpty()) {
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, "Please enter the code you received.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                });
                return;
            }

            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");

            PyObject result = pyObj.callAttr("otpCode", code, phone);
            String resultString = result.toString();
            System.out.println("resultstring : " + resultString);

            handler.post(() -> {
                if (resultString.equals("Error: Two-steps verification is enabled and a password is required (caused by SignInRequest)")) {
                    Toast.makeText(MainActivity.this, "you need to disable two steps verification", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, popdisconnect.class));
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    if (resultString.equals("Logged in successfully.")) {
                        Toast.makeText(MainActivity.this, R.string.Logged_in_successfully, Toast.LENGTH_SHORT).show();
                        System.out.println(result);
                    } else {
                        Toast.makeText(MainActivity.this, "Result: " + result.toString(), Toast.LENGTH_SHORT).show();
                        System.out.println(result);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }

                // If login is successful, switch to ContactsActivity
                if (resultString.equals("Logged in successfully.")) {
                    System.out.println("switcher value in before sending " + switcher);
                    Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                    intent.putExtra("switcher", switcher); // Pass the user ID

                    Intent intent2 = new Intent(MainActivity.this, LoadingActivity.class);
                    intent2.putExtra("switcher", switcher); // Pass the user ID

                    Intent intent3 = new Intent(MainActivity.this, ResultsActivity.class);
                    intent3.putExtra("switcher", switcher); // Pass the user ID

                    startActivity(intent);
                    finish();
                }
            });
        }).start();
    }
    private void startCooldownTimer() {
        timerTextView.setVisibility(View.VISIBLE); // Make the TextView visible

        new CountDownTimer(currentCooldownDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Convert milliseconds to minutes and seconds
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                // Format the time as "MM:SS"
                String timeRemaining = String.format("%02d:%02d", minutes, seconds);

                // Update the TextView

                timerTextView.setText(getString(R.string.tries)+ " "+ timeRemaining);
            }

            @Override
            public void onFinish() {
                // Re-enable the button and reset the click count
                button123.setEnabled(true);
                resendButton.setEnabled(true);

                clicks = clicks-1; // Reset the click count

                // Double the cooldown duration for the next time
                currentCooldownDuration *= 2;

                // Hide the timer TextView
                timerTextView.setVisibility(View.INVISIBLE);
            }
        }.start();
    }
    public void infobtn (View view){
        startActivity(new Intent(MainActivity.this, rememberaccount.class));

    }
    public void uperleft (View view){
        startActivity((new Intent(MainActivity.this, popactivity.class)));

    }





}