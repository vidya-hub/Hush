# Gameplay and Breathe captures

The README now includes all four games plus breathing setup and an active breathing session in mobile, landscape-tablet and portrait-tablet layouts: 18 additional native screenshots. Demonstration rounds run through the real game engines and controllers in isolated incognito state; no user round is overwritten. The capture flow restores the private-round map, privacy preference, emulator dimensions, density and font scale.

Screenshot inspection found that Breathe updated its phase clock after Start/Pause/Resume without refreshing the surrounding controls. The primary action now refreshes the session controls once when the state changes. A native regression checks Pause after Start, Resume after Pause, Pause after Resume, and hidden setup options while active.

Reproduce the complete captures:

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
HUSH_GAMEPLAY=1 ANDROID_SERIAL=emulator-5558 python3 scripts/capture-latest-screens.py
```

`assets/hush-gameplay-proof/` contains the full-resolution captures and native test logs. `gameplay-tests.log` records the original demonstration capture checks; `tests.log` records the final breathing state-control regression. Selected images are embedded from `screenshots/latest/`.
