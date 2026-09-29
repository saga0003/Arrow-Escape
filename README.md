# Arrow Escape

Arrow Escape is a lightweight offline-first Android puzzle game built around a simple rule: tap an arrow only when its path to the edge is clear.

## v1 features

- Procedurally generated, guaranteed-solvable levels
- Difficulty scaling from 4×4 up to 8×8 boards
- Local level, coin and streak progress
- Three-heart mistake system
- Retry and rewarded-continue flow
- AdMob integration using Google's official **test** ad IDs
- UMP consent/privacy flow for global distribution
- Android 16 / API 36 target
- Release-ready Gradle configuration
- GitHub Actions build workflow
- Unit tests that validate the first 500 generated levels

## Package

`com.saga.arrowescape`

## Build

Prerequisites:

- JDK 17+
- Android SDK Platform 36
- Android Build Tools 36.0.0+

```bash
gradle testDebugUnitTest assembleDebug bundleRelease
```

Outputs:

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`

> The repository does not contain a release keystore. Before a Play Store production upload, configure your private upload key as described in `docs/RELEASE_SIGNING.md`.

## Ads

The source intentionally uses Google's official test AdMob application/ad-unit IDs. Do not replace them until the app exists in your AdMob account and you are ready for production traffic.

See `docs/ADMOB_SETUP.md`.

## Privacy

The game has no account/login system and stores gameplay progress locally. Google Mobile Ads may process advertising/device information. See `docs/PRIVACY_POLICY.md` and complete Google Play Data Safety based on your final production SDK configuration.
