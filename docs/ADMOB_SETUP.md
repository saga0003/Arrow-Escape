# AdMob Production Setup

The repository ships with Google's official demo/test IDs so development traffic cannot accidentally monetize or create invalid-click risk.

## Current test IDs

- App ID: `ca-app-pub-3940256099942544~3347511713`
- Fixed banner: `ca-app-pub-3940256099942544/6300978111`
- Interstitial: `ca-app-pub-3940256099942544/1033173712`
- Rewarded: `ca-app-pub-3940256099942544/5224354917`

## Before production

1. Create Arrow Escape in AdMob.
2. Create one banner, one interstitial and one rewarded ad unit.
3. Replace the test application ID in `AndroidManifest.xml`.
4. Replace the three test ad-unit IDs in `AdsManager.java`.
5. Configure Privacy & messaging in AdMob for the regions where your app is distributed.
6. Test the production configuration through Play internal/closed testing.
7. Never intentionally click your own live ads.

## Current placements

- Banner: game-screen footer.
- Interstitial: after every fourth completed level.
- Rewarded: optional continue after all three lives are lost.

The full-screen placements occur at natural gameplay transitions instead of interrupting active play.
