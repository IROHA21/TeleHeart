package com.moon.TeleHeart.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.moon.TeleHeart.R;
import com.moon.TeleHeart.login.rememberaccount;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {



    private TextView cacheStatusTextView;
    private Button manualDisconnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLocaleFromPreferences();  // MUST BE FIRST LINE
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        manualDisconnectButton = findViewById(R.id.manualDisconnectButton);
        manualDisconnectButton.setOnClickListener(this::ondisclick);

        Button clearCacheButton = findViewById(R.id.clearCacheButton);
        cacheStatusTextView = findViewById(R.id.cacheStatusTextView);

        // Display current data size
        updateDataSize();

        // Set click listener for the clear data button
        clearCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAppData();
                updateDataSize();
                Toast.makeText(SettingsActivity.this, getString(R.string.All_app_data_cleared), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to clear all app data
    private void clearAppData() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                PackageManager packageManager = getPackageManager();
                Method clearApplicationUserData = packageManager.getClass()
                        .getMethod("clearApplicationUserData", String.class, Class.forName("android.content.pm.IPackageDataObserver"));
                clearApplicationUserData.invoke(packageManager, getPackageName(), null);
            } else {
                // Fallback for older SDK versions
                deleteDir(getDataDir());
                deleteDir(getCacheDir());
                deleteDir(getFilesDir());
                deleteDir(getExternalCacheDir());
                Toast.makeText(this,getString(R.string.App_data_cleared), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.Failed_to_clear_app_data), Toast.LENGTH_SHORT).show();
        }
    }

    // Recursive method to delete a directory
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    // Method to update the data size TextView
    private void updateDataSize() {
        long dataSize = getDataSize(getDataDir());
        cacheStatusTextView.setText(getString(R.string.Data_size) + formatSize(dataSize));
    }

    // Method to calculate data size
    private long getDataSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += getDataSize(file);
                }
            }
        }
        return size;
    }

    // Method to format size in KB or MB
    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return (size / 1024) + " KB";
        } else {
            return (size / (1024 * 1024)) + " MB";
        }
    }
    public void ondisclick(View view) {
        startActivity(new Intent(SettingsActivity.this, rememberaccount.class));
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