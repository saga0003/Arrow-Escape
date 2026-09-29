package com.saga.arrowescape;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.saga.arrowescape.ads.AdsManager;
import com.saga.arrowescape.game.GameView;
import com.saga.arrowescape.privacy.ConsentManager;

public final class MainActivity extends Activity implements GameView.Listener {

    private GameView gameView;
    private FrameLayout adContainer;
    private AdsManager adsManager;
    private ConsentManager consentManager;
    private boolean adsAllowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0B1020);
        applySystemBarInsets(root);

        gameView = new GameView(this);
        gameView.setListener(this);
        root.addView(gameView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        adContainer = new FrameLayout(this);
        adContainer.setForegroundGravity(Gravity.CENTER);
        root.addView(adContainer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)));
        setContentView(root);

        adsManager = new AdsManager();
        consentManager = new ConsentManager(this);
        consentManager.requestConsent(this, canRequestAds -> {
            adsAllowed = canRequestAds;
            if (canRequestAds) adsManager.initialize(getApplicationContext(), adContainer);
        });
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(0xFF0B1020);
        window.setNavigationBarColor(0xFF0B1020);
    }

    private void applySystemBarInsets(View root) {
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int left, top, right, bottom;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                left = bars.left;
                top = bars.top;
                right = bars.right;
                bottom = bars.bottom;
            } else {
                left = insets.getSystemWindowInsetLeft();
                top = insets.getSystemWindowInsetTop();
                right = insets.getSystemWindowInsetRight();
                bottom = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(left, top, right, bottom);
            return insets;
        });
    }

    @Override
    public void onLevelFinished(int completedLevel) {
        Runnable next = gameView::advanceToNextLevel;
        if (adsAllowed && completedLevel > 0 && completedLevel % 4 == 0) adsManager.showInterstitial(this, next);
        else next.run();
    }

    @Override
    public void onRewardedContinueRequested() {
        if (!adsAllowed) {
            gameView.grantRewardedContinue();
            return;
        }
        adsManager.showRewarded(this, gameView::grantRewardedContinue, gameView::grantRewardedContinue);
    }

    @Override
    public void onPrivacyRequested() {
        consentManager.showPrivacyOptions(this);
    }

    @Override
    protected void onDestroy() {
        if (adsManager != null) adsManager.destroy();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
