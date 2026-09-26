# IzzyOnDroid submission — Hush

Submit at: **https://codeberg.org/IzzyOnDroid/repodata/issues/new**
Pick the "New app" template, then replace the whole body with the block below.

Their tracker applies an LLM triage label to every request (llm/none, llm/minimal,
llm/moderate, llm/substantial). Disclose honestly: in recent history every
llm/substantial request was declined, while llm/moderate and llm/minimal requests
that documented human review passed. Keep the disclosure specific.

---

### Guidelines

- [x] I am the developer of the app. (If not, please explain the developer's stance on this inclusion request in the Further Notices section.)
- [x] The app complies with the [App Inclusion Policy](https://izzyondroid.org/docs/general/AppInclusionPolicy/).
- [x] The app is not already listed in the repo or issue tracker.
- [x] The [Fastlane](https://izzyondroid.org/docs/general/Fastlane/) folder is available in the app's repo.

### Link to the source code

https://github.com/vidya-hub/Hush

### Link to app in another app store

_No response_

### License used

GPL-3.0-or-later

### Categories

Internet, Multimedia, Games

### Summary

A calm YouTube client with breathing breaks, meditation and offline mini games

### Description

Hush is a calm, private YouTube client for Android. Instead of an endless feed, the
Home screen offers a break: a Breathe and Meditate card with guided breathing
(Calm 4/6, Box, 4/7/8) and a quiet meditation timer, quick offline games (2048,
Snake, Sudoku, Make 24), and your own library of History, Saved and Downloads.
The player is fully gesture driven (10 second double-tap skipping, preview
scrubbing, pinch zoom, Fit/Zoom geometry) with background and popup playback,
downloads, and local playlists. No account needed, no tracking. Subscriptions,
history and profiles stay on the device, with per-profile data and an incognito
mode that writes nothing.

## FEATURES

* Guided breathing (Calm 4 in/6 out, Box, 4/7/8) and a quiet meditation timer
* Four offline games: 2048, Snake, Sudoku and Make 24, all touch controlled
* Gesture player: Fit/Zoom video geometry, double-tap skipping, preview scrubbing
* Background and popup playback, downloads, local playlists and history
* Warm paper and dark pine themes following the system setting
* Multiple local profiles and an incognito mode that writes nothing

### Build instructions

Requirements: JDK 17+, Android SDK (compileSdk 37). The first build downloads the
ffmpeg-kit AAR from the project's GitHub release and verifies a pinned SHA-256.

* git clone https://github.com/vidya-hub/Hush.git
* cd Hush
* ./gradlew :app:assembleRelease

Release builds expect keystore.properties with signing details (see app/build.gradle);
assembleDebug works without it. Output: app/build/outputs/apk/release/Hush_1.0.0-<abi>-release.apk

### Assistance Level

Moderate – Used for specific tasks or modules

### "AI" Tool(s)

GLM (Z.ai coding agent)

### What did the tools help with, and how?

The AI agent implemented the Hush-specific modules on top of the inherited,
human-written NewPipe/PipePipe code base: the home screen and break sheet, the
four games, the theme work and player cleanups. Each module followed a written
spec, and the developer reviewed every change, built the app, and manually
verified behavior on a device at each step (including unit tests for the game
and breathing engines). The core client, player and extractor code is the
pre-existing human-written NewPipe/PipePipe lineage under GPL-3.0.

### AI Accountability

- [x] The human developer(s) reviewed and edited all "AI"-generated outputs
- [x] The human developer(s) ran manual tests and manually verified all changes

### Further Notices

Per-ABI APKs attached to each GitHub release (arm64-v8a, armeabi-v7a, x86, x86_64,
about 20 MiB each), all developer-signed with the same key. Version codes follow
the 100 * base + ABI scheme (101, 102, 103, 104 for base 1). Release tags use
v<versionName> (current: v1.0.0 for versionName 1.0.0). Fastlane metadata with
changelogs per version code is under fastlane/metadata/android/en-US/.

---

Notes (not part of the submission):

- APKs are ~20 MiB, under the 30 MB per-app limit.
- Updates sync automatically from GitHub releases once accepted; bump versionCode
  and versionName for each new tag.
- Keep all APKs signed with the same key (keystore/ in the working copy, gitignored).
- Official F-Droid later: RFP at https://gitlab.com/fdroid/fdroiddata.
