package com.example.lasttele;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

public class MyService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Retrieve switcher value from Intent or SharedPreferences
        boolean switcher = false;
        if (intent != null) {
            switcher = intent.getBooleanExtra("switcher", false);
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            switcher = sharedPreferences.getBoolean("switcher", false);
        }

        Log.d("MyService", "Service started with switcher: " + switcher);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("MyService", "App just got removed from Recents!");

        // Retrieve switcher value from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean switcher = sharedPreferences.getBoolean("switcher", false);

        // Terminate session only if switcher is false
        if (!switcher) {
            Log.d("MyService", "Terminating session because switcher is false.");
            Python py = Python.getInstance();
            PyObject pyObj = py.getModule("helloworld");
            PyObject result = pyObj.callAttr("terminate_and_disconnect");
            Log.d("MyService", "Session terminated: " + result.toString());
        }

        // Stop the service
        stopSelf();
    }
}