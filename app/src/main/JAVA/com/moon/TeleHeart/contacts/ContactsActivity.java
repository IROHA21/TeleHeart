package com.moon.TeleHeart.contacts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.moon.TeleHeart.ads.AdManager;
import com.moon.TeleHeart.ads.AdUtils;
import com.moon.TeleHeart.loading.LoadingActivity;
import com.moon.TeleHeart.R;
import com.moon.TeleHeart.ads.YandexAdManager;
import com.moon.TeleHeart.firstscreen.Firstscreen;
import com.moon.TeleHeart.myadapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactsActivity extends AppCompatActivity {
    private myadapter adapter;
    private String selectedContactId;
    private EditText quantity; // Added EditText for quantity
    private ProgressBar progressBar;
    private Handler backgroundHandler;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean switcher;

    private static final String PREF_FIRST_LAUNCH4 = "isFirstLaunch4";
    private static final String PREFS_NAME4 = "switchon";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLocaleFromPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_main);

        SharedPreferences settings4 = getSharedPreferences(PREFS_NAME4, MODE_PRIVATE);
        boolean isFirstLaunch3 = settings4.getBoolean(PREF_FIRST_LAUNCH4, true);

        if (isFirstLaunch3) {
            // Launch popactivity only if it's the first launch
            startActivity(new Intent(ContactsActivity.this, popnum.class));

            // Set the flag to false so this doesn't happen again
            SharedPreferences.Editor editor = settings4.edit();
            editor.putBoolean(PREF_FIRST_LAUNCH4, false);
            editor.apply();
        }



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



        Intent intent = getIntent();
        switcher = intent.getBooleanExtra("switcher", false);

        System.out.println("switcher check inside of contact : " + switcher );

        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }


        // Find views

        quantity = findViewById(R.id.quantity); // Get EditText from layout

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

                // Get the quantity value from EditText
                String quantityValue = quantity.getText().toString().trim();

                // Validate input
                if (quantityValue.isEmpty()) {
                    Toast.makeText(this, "Quantity cannot be empty", Toast.LENGTH_SHORT).show();
                    return; // Stop execution
                }

                int quantityInt;
                try {
                    quantityInt = Integer.parseInt(quantityValue);
                    if (quantityInt > 100000 || quantityInt < 100  ) {
                        Toast.makeText(this, R.string.quantity, Toast.LENGTH_SHORT).show();
                        return; // Stop execution
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return; // Stop execution
                }
                Intent intent2 = new Intent(ContactsActivity.this, LoadingActivity.class);

                // If all validations pass, proceed to the next screen

                intent2.putExtra("selectedContactId", selectedContactId);
                intent2.putExtra("quantity", String.valueOf(quantityInt)); // Store it as a String
                intent2.putExtra("switcher", switcher); // Pass the user ID


                startActivity(intent2);
            } else {
                Toast.makeText(this, "No contact selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onbut(View view) {

        System.out.println(selectedContactId);
        
    }

    public void onbuttonclick(View view) {
        Intent intent = new Intent(this, Firstscreen.class);
        startActivity(intent);
    }
    public void uperlefts (View view){
        startActivity((new Intent(ContactsActivity.this, popnum.class)));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Check if the user chose "Don't remember me"
        System.out.println("switcher destroy activated contacts");


        if (!switcher) {
            // Terminate the session and disconnect the client
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");
            PyObject result = pyObj.callAttr("terminate_and_disconnect");
            Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
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




    }
    // Add these to MainActivity.java and all other activities
    private void setLocaleFromPreferences() {
        String languageCode = getSavedLanguage();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private String getSavedLanguage() {
        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return preferences.getString("language", "en");
    }



}

