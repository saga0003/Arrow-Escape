package com.saga.arrowescape.privacy;

import android.app.Activity;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public final class ConsentManager {

    public interface Callback {
        void onConsentResolved(boolean canRequestAds);
    }

    private final ConsentInformation consentInformation;

    public ConsentManager(Activity activity) {
        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
    }

    public void requestConsent(Activity activity, Callback callback) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        formError -> callback.onConsentResolved(consentInformation.canRequestAds())
                ),
                requestConsentError -> callback.onConsentResolved(consentInformation.canRequestAds())
        );
    }

    public void showPrivacyOptions(Activity activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> {
            // The SDK persists the user's choice.
        });
    }
}
