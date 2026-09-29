package com.saga.arrowescape.ads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public final class AdsManager {
    public static final String TEST_BANNER = "ca-app-pub-3940256099942544/6300978111";
    public static final String TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712";
    public static final String TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917";

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private AdView bannerView;
    private boolean initialized;

    public void initialize(Context context, ViewGroup bannerContainer) {
        if (initialized) return;
        initialized = true;
        MobileAds.initialize(context, initializationStatus -> {
            loadBanner(context, bannerContainer);
            loadInterstitial(context);
            loadRewarded(context);
        });
    }

    private void loadBanner(Context context, ViewGroup container) {
        bannerView = new AdView(context);
        bannerView.setAdUnitId(TEST_BANNER);
        bannerView.setAdSize(AdSize.BANNER);
        container.removeAllViews();
        container.addView(bannerView);
        bannerView.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitial(Context context) {
        InterstitialAd.load(context, TEST_INTERSTITIAL, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd ad) {
                interstitialAd = ad;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                interstitialAd = null;
            }
        });
    }

    private void loadRewarded(Context context) {
        RewardedAd.load(context, TEST_REWARDED, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                rewardedAd = null;
            }
        });
    }

    public void showInterstitial(Activity activity, Runnable onFinished) {
        if (interstitialAd == null) {
            onFinished.run();
            loadInterstitial(activity);
            return;
        }
        InterstitialAd showing = interstitialAd;
        interstitialAd = null;
        showing.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadInterstitial(activity);
                onFinished.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                loadInterstitial(activity);
                onFinished.run();
            }
        });
        showing.show(activity);
    }

    public void showRewarded(Activity activity, Runnable onRewardEarned, Runnable onUnavailable) {
        if (rewardedAd == null) {
            onUnavailable.run();
            loadRewarded(activity);
            return;
        }
        RewardedAd showing = rewardedAd;
        rewardedAd = null;
        final boolean[] earned = {false};
        showing.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadRewarded(activity);
                if (!earned[0]) onUnavailable.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                loadRewarded(activity);
                onUnavailable.run();
            }
        });
        showing.show(activity, (RewardItem rewardItem) -> {
            earned[0] = true;
            onRewardEarned.run();
        });
    }

    public void destroy() {
        if (bannerView != null) {
            bannerView.destroy();
            bannerView = null;
        }
    }
}
