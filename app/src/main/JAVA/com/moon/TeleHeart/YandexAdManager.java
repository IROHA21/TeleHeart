package com.moon.TeleHeart;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;

public class YandexAdManager {
    private static YandexAdManager instance; // Singleton instance
    private InterstitialAd interstitialAd; // The ad instance
    private InterstitialAdLoader interstitialAdLoader; // The ad loader
    private static final String TAG = "YandexAdManager";
    private static final String DEMO_AD_UNIT_ID = "demo-rewarded-yandex"; // Replace with your real ad unit ID

    private AdDismissListener adDismissListener; // Listener for ad dismissal
    private AdLoadFailureListener adLoadFailureListener; // Listener for ad loading failure

    private YandexAdManager() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized YandexAdManager getInstance() {
        if (instance == null) {
            instance = new YandexAdManager();
        }
        return instance;
    }

    // Set the AdLoadFailureListener
    public void setAdLoadFailureListener(AdLoadFailureListener listener) {
        this.adLoadFailureListener = listener;
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

                    // Notify the failure listener
                    if (adLoadFailureListener != null) {
                        adLoadFailureListener.onAdFailedToLoad(adRequestError.getCode());
                    }
                }
            });

            // Create ad request configuration
            AdRequestConfiguration adRequestConfiguration = new AdRequestConfiguration.Builder(DEMO_AD_UNIT_ID).build();
            interstitialAdLoader.loadAd(adRequestConfiguration);
        });
    }

    public void showInterstitialAd(Activity activity, AdDismissListener dismissListener, AdLoadFailureListener loadFailureListener) {
        this.adDismissListener = dismissListener; // Set the dismiss listener
        this.adLoadFailureListener = loadFailureListener; // Set the load failure listener

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
                    if (adDismissListener != null) {
                        adDismissListener.onAdDismissed(); // Notify listener
                    }
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

            // Notify the failure listener if the ad is not ready
            if (adLoadFailureListener != null) {
                adLoadFailureListener.onAdFailedToLoad(0); // Use 0 as a generic error code
            }
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
    public void loadBannerAd(BannerAdView bannerAdView) {
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
    }
}