# Pocket Guard

Android anti-pickpocket utility built with Kotlin, Jetpack Compose and Material 3.

Current product line: Premium visual UI + Smart Protection with Pocket/Bag modes, calibration, Journey Guard,
owner verification, PIN/biometric disarm, local history and device diagnostics.

## Release identity

- Application ID: `com.riccardopinato.pocketguard`
- Current version: `0.4.0` (`versionCode 4`)
- Permanent signing identity is managed only through GitHub Actions secrets.
- The private keystore is intentionally not stored in this repository.

## Development

- `Development checks` builds and tests debug code on push/PR.
- `Signed Android release` is the controlled release lane and produces signed APK/AAB artifacts.
