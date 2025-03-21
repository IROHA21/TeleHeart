package com.example.lasttele;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;

public class YandexAdManager {
    private static YandexAdManager instance; // Singleton instance
    private InterstitialAd interstitialAd; // The ad instance
    private InterstitialAdLoader interstitialAdLoader; // The ad loader
    private static final String TAG = "YandexAdManager";
    private static final String DEMO_AD_UNIT_ID = "demo-interstitial-yandex"; // Replace with your real ad unit ID

    private YandexAdManager() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized YandexAdManager getInstance() {
        if (instance == null) {
            instance = new YandexAdManager();
        }
        return instance;
    }

    public void loadInterstitialAd(Context context) {
        MobileAds.initialize(context, () -> {
            Log.d(TAG, "Yandex SDK initialized");
            interstitialAdLoader = new InterstitialAdLoader(context);
            interstitialAdLoader.setAdLoadListener(new InterstitialAdLoadListener() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    YandexAdManager.this.interstitialAd = interstitialAd;
                    Log.d(TAG, "Yandex Interstitial ad loaded.");
                }

                @Override
                public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                    interstitialAd = null; // Clear the ad if loading fails
                    Log.d(TAG, "Yandex Interstitial ad failed to load: " + adRequestError.getDescription());
                }
            });

            // Create ad request configuration
            AdRequestConfiguration adRequestConfiguration = new AdRequestConfiguration.Builder(DEMO_AD_UNIT_ID).build();
            interstitialAdLoader.loadAd(adRequestConfiguration);
        });
    }

    public void showInterstitialAd(Activity activity) {
        if (interstitialAd != null) {
            interstitialAd.setAdEventListener(new InterstitialAdEventListener() {
                @Override
                public void onAdShown() {
                    Log.d(TAG, "Yandex Ad shown");
                }

                @Override
                public void onAdFailedToShow(@NonNull AdError adError) {
                    Log.e(TAG, "Yandex Ad failed to show: " + adError.getDescription());
                }

                @Override
                public void onAdDismissed() {
                    Log.d(TAG, "Yandex Ad dismissed");
                    // Clean up and reload a new ad
                    interstitialAd.setAdEventListener(null);
                    interstitialAd = null;
                    loadInterstitialAd(activity); // Preload the next ad
                }

                @Override
                public void onAdClicked() {
                    Log.d(TAG, "Yandex Ad clicked");
                }

                @Override
                public void onAdImpression(@Nullable ImpressionData impressionData) {
                    Log.d(TAG, "Yandex Ad impression recorded");
                }
            });
            interstitialAd.show(activity);
        } else {
            Log.d(TAG, "Yandex Interstitial ad is not ready yet.");
        }
    }

    /**
     * Clean up resources to avoid memory leaks.
     */
    public void onDestroy() {
        if (interstitialAdLoader != null) {
            interstitialAdLoader.setAdLoadListener(null);
            interstitialAdLoader = null;
        }
        if (interstitialAd != null) {
            interstitialAd.setAdEventListener(null);
            interstitialAd = null;
        }
    }
}