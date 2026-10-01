# Headless tablet capture runbook

The selected design images are generated mockups. `current-emulator/` contains actual screenshots from the installed debug APK, captured on a separate API 35 tablet emulator. No Android source was changed during this design phase.

## Running instance

- AVD: `Hush_Tablet_API35_Headless` (Pixel Tablet, ARM64, Google APIs)
- Serial: `emulator-5558`
- Reference display: 2560×1600 physical pixels, density 320 → 1280×800dp
- Startup log: `/tmp/hush-tablet-api35-headless-5558.log`
- API 37 attempts failed during boot/rendering; API 35 booted successfully with software graphics.
- Existing `Exp` emulator and the connected physical tablet were left untouched.

## Launch and inspect

Run this launch command only when this AVD is stopped. Do not launch a duplicate instance.

```sh
/Users/vidyasagar/Library/Android/sdk/emulator/emulator \
  -avd Hush_Tablet_API35_Headless -port 5558 \
  -no-window -no-audio -no-boot-anim -no-snapshot \
  -gpu swiftshader -memory 4096 \
  > /tmp/hush-tablet-api35-headless-5558.log 2>&1
```

```sh
/Users/vidyasagar/Library/Android/sdk/platform-tools/adb -s emulator-5558 shell getprop sys.boot_completed
/Users/vidyasagar/Library/Android/sdk/platform-tools/adb -s emulator-5558 shell wm size
/Users/vidyasagar/Library/Android/sdk/platform-tools/adb -s emulator-5558 shell wm density
```

The installed app is `com.vidsagar.hush.debug`, using the existing `Hush_5.3.1-arm64-v8a-debug.apk` and its existing instrumentation APK. APK checksum and repository revision are recorded in `verification.json`.

## Captured configurations

| Folder | Window | Verification |
| --- | --- | --- |
| `current-emulator/landscape` | 1280×800dp | Four navigation tests passed; real playback test failed to start a stream |
| `current-emulator/portrait` | 800×1280dp | Four navigation tests passed |
| `current-emulator/portrait-200-percent` | 800×1280dp, font scale 2.0 | Capture/navigation test passed |
| `current-emulator/narrow-600dp` | 600×900dp | Capture/navigation test passed |

There are 146 baseline screenshots. Each folder includes its capture log. Narrow width uses `wm size` to simulate the available window; it is not an actual OS split-screen capture. Empty profile history/download screens reflect this fresh test instance.

Use targeted commands when reproducing narrow size and large text:

```sh
/Users/vidyasagar/Library/Android/sdk/platform-tools/adb -s emulator-5558 shell wm size 1200x1800
/Users/vidyasagar/Library/Android/sdk/platform-tools/adb -s emulator-5558 shell settings put system font_scale 2.0
```

Restore after captures:

```sh
/Users/vidyasagar/Library/Android/sdk/platform-tools/adb -s emulator-5558 shell wm size reset
/Users/vidyasagar/Library/Android/sdk/platform-tools/adb -s emulator-5558 shell settings put system font_scale 1.0
```

These overrides were restored at completion. The headless emulator remains running. To stop only this test instance later:

```sh
/Users/vidyasagar/Library/Android/sdk/platform-tools/adb -s emulator-5558 emu kill
```

## Limit of verification

Navigation and screen capture results describe the current installed app. The generated tablet layouts are a specification for a later implementation phase. The playback test could not start its real YouTube stream before its PiP assertions; decoder continuity, collapse/expand, live playback and fullscreen restoration still require runtime verification with a working stream.
