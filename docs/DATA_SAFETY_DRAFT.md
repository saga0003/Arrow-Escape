# Google Play Data Safety — Draft for Arrow Escape

This draft is based on the current app code plus Google Mobile Ads SDK 25.5.0. Re-check it in Play Console immediately before publication because SDK behavior and the final AdMob configuration can change.

## App-owned data

Arrow Escape has no developer-operated account, login, profile database, contacts access, precise location access, photo/media access, microphone access, or cloud gameplay database. Level, coin and streak progress are stored locally on the device.

## Third-party advertising SDK

Google states that the Google Mobile Ads SDK automatically collects and shares data used for advertising, analytics and fraud prevention, including:

- IP address, which can be used to estimate general location.
- User product interactions such as app launches, taps and video views.
- Diagnostic/performance information.
- Device and account identifiers, including the Android advertising ID and app set ID where applicable.

Google states that this data is encrypted in transit.

## Likely Play Console disclosures for the current production design

When answering the Data Safety form, account for the Google Mobile Ads SDK rather than answering “no data collected” merely because Arrow Escape itself has no account database.

Likely relevant data categories include:
- Approximate location derived from IP address.
- App interactions.
- Diagnostics.
- Device or other identifiers.

Likely purposes include:
- Advertising or marketing.
- Analytics.
- Fraud prevention, security and compliance.

Whether each category should be marked as collected, shared, optional or required must be answered according to the final AdMob settings, consent configuration, Google Play definitions and any additional SDKs added before release.

## Encryption

Google Mobile Ads documents TLS encryption for data it collects.

## Account deletion

Arrow Escape does not create a developer-operated user account, so an account-deletion flow is not applicable to the current v1 architecture. Local game progress can be removed by clearing app storage or uninstalling the app.

## Before submitting

1. Confirm the final Google Mobile Ads SDK version.
2. Re-read Google's current Mobile Ads Play data disclosure page.
3. Confirm whether any analytics, crash-reporting or other SDK has been added.
4. Make the privacy policy consistent with the final Data Safety answers.
5. Complete the Play Console form using the final production build, not this draft alone.
