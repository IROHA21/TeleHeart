
package com.moon.TeleHeart.result;

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

import com.moon.TeleHeart.R;
import com.moon.TeleHeart.ViewPagerAdapter;
import com.moon.TeleHeart.login.disconnect1;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class popdisconnect extends FragmentActivity implements disconnect1.OnCloseButtonClickListener{

    private ViewPager2 viewPager3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setLocaleFromPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discopop);

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
        viewPager3 = findViewById(R.id.viewPager3);

        // Create a list of fragments
        List<Fragment> accountList2 = new ArrayList<>();



        // Create and set up FragmentThree
        disconnect1 disconnect1 = new disconnect1();
        disconnect1.setOnCloseButtonClickListener(this); // Set the listener
        accountList2.add(disconnect1);

        // Set up the adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, accountList2);
        viewPager3.setAdapter(adapter);
    }

    // Handle the close button click
    @Override
    public void onCloseButtonClicked() {
        finish();
        // Close the popup window
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
