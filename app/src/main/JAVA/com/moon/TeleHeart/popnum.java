
package com.moon.TeleHeart;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class popnum extends FragmentActivity implements nummessages1.OnCloseButtonClickListener{

    private ViewPager2 viewPager5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setLocaleFromPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.numpop);

        // Make the background transparent
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Get screen dimensions
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        // Set popup window size
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.7));

        // Center the popup window
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);

        // Initialize ViewPager2
        viewPager5 = findViewById(R.id.viewPager5);

        // Create a list of fragments
        List<Fragment> accountList5 = new ArrayList<>();



        // Create and set up FragmentThree
        nummessages1 nummessages1 = new nummessages1();
        nummessages1.setOnCloseButtonClickListener(this); // Set the listener
        accountList5.add(nummessages1);

        // Set up the adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, accountList5);
        viewPager5.setAdapter(adapter);
    }

    // Handle the close button click
    @Override
    public void onCloseButtonClicked() {
        finish(); //000000000000000 Close the popup window
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