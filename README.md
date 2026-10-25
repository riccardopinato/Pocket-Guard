# Pockara

Android anti-pickpocket utility built with Kotlin and Jetpack Compose.

Pockara is the visible commercial brand. The Android application ID remains
`com.riccardopinato.pocketguard` so signed upgrades remain compatible with previous test releases.

Current product line:
- Smart Protection with calibrated Pocket/Bag modes
- Journey Guard
- owner verification
- local PIN / optional biometric disarm
- local event history and device diagnostics
- Free + Pro Lifetime monetization through Google Play Billing
- no third-party ads at launch

## Release identity

- Application ID: `com.riccardopinato.pocketguard`
- Current version: `0.5.1` (`versionCode 7`)
- Permanent signing identity is managed only through GitHub Actions secrets.
- The private keystore is intentionally not stored in this repository.

## Monetization

Google Play one-time product:
- `pro_lifetime`

The current pre-launch build keeps `PRELAUNCH_PRO_PREVIEW=true` so sideload QA can test all Pro features before the Play product is configured.

See:
- `docs/MONETIZATION.txt`
- `docs/PLAY_CONSOLE_SETUP.txt`
- `docs/PRIVACY_POLICY.txt`
- `docs/PLAY_DATA_SAFETY.txt`

## Development

- `Development checks` builds and tests debug code on push/PR.
- `Signed Android release` is the controlled release lane and produces signed APK/AAB artifacts.
