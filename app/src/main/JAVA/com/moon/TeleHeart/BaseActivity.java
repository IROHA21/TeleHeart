package com.moon.TeleHeart;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = newBase.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String language = preferences.getString("language", "en");
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure the title is updated
        String title = getTitle().toString();
    }
}