package com.example.lasttele;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;

public class YandexBannerAdManager {
    private static YandexBannerAdManager instance;
    private BannerAdView bannerAdView;
    private static final String TAG = "YandexBannerAdManager";

    private YandexBannerAdManager() {}

    public static synchronized YandexBannerAdManager getInstance() {
        if (instance == null) {
            instance = new YandexBannerAdManager();
        }
        return instance;
    }

    public void loadBannerAd(Context context, ViewGroup adContainer, String adUnitId) {
        // Clean up previous banner if exists
        destroyBanner();

        bannerAdView = new BannerAdView(context);
        bannerAdView.setAdUnitId(adUnitId);

        // Set up the event listener
        bannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Banner ad loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                Log.e(TAG, "Banner ad failed to load: " + adRequestError.getDescription());
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Banner ad clicked");
            }

            @Override
            public void onLeftApplication() {
                Log.d(TAG, "Banner ad left application");
            }

            @Override
            public void onReturnedToApplication() {
                Log.d(TAG, "Banner ad returned to application");
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
                Log.d(TAG, "Banner ad impression recorded");
            }
        });

        // Add the banner to your container
        adContainer.addView(bannerAdView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Load the ad
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
    }

    public void destroyBanner() {
        if (bannerAdView != null) {
            bannerAdView.setBannerAdEventListener(null);
            bannerAdView.destroy();
            bannerAdView = null;
        }
    }
}