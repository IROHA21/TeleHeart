package com.example.lasttele;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {
    private static AdManager instance; // Singleton instance
    private InterstitialAd interstitialAd; // The ad instance
    private static final String TAG = "AdManager";

    private AdDismissListener adDismissListener; // Listener for ad dismissal
    private AdLoadFailureListener adLoadFailureListener; // Listener for ad loading failure

    private AdManager() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    // Set the AdLoadFailureListener
    public void setAdLoadFailureListener(AdLoadFailureListener listener) {
        this.adLoadFailureListener = listener;
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

                        // Notify the failure listener
                        if (adLoadFailureListener != null) {
                            adLoadFailureListener.onAdFailedToLoad(loadAdError.getCode());
                        }
                    }
                });
    }

    public void showInterstitialAd(Activity activity, AdDismissListener listener) {
        this.adDismissListener = listener; // Set the listener
        if (interstitialAd != null) {
            interstitialAd.show(activity);
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    if (adDismissListener != null) {
                        adDismissListener.onAdDismissed(); // Notify listener
                    }
                    interstitialAd = null; // Clear the ad after showing it
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    Log.e(TAG, "Ad failed to show: " + adError.getMessage());
                }
            });
        } else {
            Log.d(TAG, "Interstitial ad is not ready yet.");

            // Notify the failure listener if the ad is not ready
            if (adLoadFailureListener != null) {
                adLoadFailureListener.onAdFailedToLoad(0); // Use 0 as a generic error code
            }
        }
    }
  
}