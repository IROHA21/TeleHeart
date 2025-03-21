package com.example.lasttele;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {
    private static AdManager instance; // Singleton instance
    private InterstitialAd interstitialAd; // The ad instance
    private static final String TAG = "AdManager";

    private AdManager() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    public void loadInterstitialAd(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        AdManager.this.interstitialAd = interstitialAd; // Store the loaded ad
                        Log.d(TAG, "Interstitial ad loaded.");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAd = null; // Clear the ad if loading fails
                        Log.d(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                    }
                });
    }

    public InterstitialAd getInterstitialAd() {
        return interstitialAd; // Return the loaded ad
    }

    public void showInterstitialAd(Activity activity) {
        if (interstitialAd != null) {
            interstitialAd.show(activity); // Show the ad
            interstitialAd = null; // Clear the ad after showing it
        } else {
            Log.d(TAG, "Interstitial ad is not ready yet.");
        }
    }
}