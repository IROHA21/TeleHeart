package com.example.lasttele;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.lang.reflect.Method;

public class SettingsActivity extends AppCompatActivity {

    private TextView cacheStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
                Toast.makeText(SettingsActivity.this, "All app data cleared", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "App data cleared (fallback)", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to clear app data", Toast.LENGTH_SHORT).show();
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
        cacheStatusTextView.setText("Data size: " + formatSize(dataSize));
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
}