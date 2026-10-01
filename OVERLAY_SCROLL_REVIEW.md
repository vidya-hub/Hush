# Full-page scrolling and video overlay

The publishing branch starts at Hush commit `b31bc4c8f1fe466653e53db24054eda8e5e0dfdb`, preserving the standalone included extractor, reproducible ffmpeg fetch, release documentation and Android background picture-in-picture support.

The Home brand/header now shares the ScrollView with its headline, search field, recent queries and shortcuts. Collapsed video no longer adds its own height to the fragment holder's bottom inset. Browsing therefore extends underneath the floating 16:9 video instead of ending above an empty reserved section. Navigation, keyboard and system-bar insets still determine the safe page window and overlay placement. The existing decoder remains attached through collapse, navigation and expansion.

Native regressions assert that showing the overlay leaves the page height unchanged, that the page extends beneath the video, and that the Home header belongs to the scrolling content. Integration checks exercise collapse, search, game navigation, expansion, fullscreen and close with the same decoder and advancing playback.

Fresh README screenshots cover 411×914dp mobile, 1280×800dp landscape tablet and 800×1280dp portrait tablet. Raw capture sets and test logs are under `assets/hush-overlay-proof/` (nine native checks) and `assets/hush-live-overlay-proof/` (three real YouTube checks). The full debug/test build and 38 JVM tests pass in the standalone publishing checkout.

To reproduce using a headless ARM64 tablet emulator:

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest
ANDROID_SERIAL=emulator-5558 python3 scripts/capture-latest-screens.py
# Optional real YouTube playback smoke test and captures:
HUSH_REAL_VIDEO=1 ANDROID_SERIAL=emulator-5558 python3 scripts/capture-latest-screens.py
```

The script restores the previous emulator dimensions, density and font scale. No changes are pushed to upstream PipePipe repositories.
