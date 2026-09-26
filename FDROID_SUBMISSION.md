# F-Droid RFP submission — Hush

File at: **https://gitlab.com/fdroid/fdroiddata/-/issues/new**
(needs a gitlab.com account; you can sign in with GitHub)

Title the issue:

```
RFP: Hush (com.vidsagar.hush)
```

Then paste this body (matches the format of current RFPs, including a draft
build recipe so maintainers only have to verify, not write):

---

### App Name

Hush

### Package Name

com.vidsagar.hush

### Repository URL

https://github.com/vidya-hub/Hush

### License

GPL-3.0-or-later

### Summary

A calm YouTube client with breathing breaks, meditation and offline mini games

### Description

Hush is a calm, private YouTube client for Android. Instead of an endless feed,
the Home screen offers a break: a Breathe and Meditate card with guided breathing
(Calm 4/6, Box, 4/7/8) and a quiet meditation timer, quick offline games (2048,
Snake, Sudoku, Make 24), and your own library of History, Saved and Downloads.
The player is fully gesture driven (10 second double-tap skipping, preview
scrubbing, pinch zoom, Fit/Zoom geometry) with background and popup playback,
downloads, and local playlists. No account needed, no tracking; data stays on
the device with per-profile storage and an incognito mode.

It is a substantially diverged fork of PipePipe/NewPipe (a different application
ID and a redesigned experience: no feed on Home, break sessions, offline games,
unified player geometry), maintained independently at the URL above.

Anti-Features expected: NonFreeNet (interfaces with YouTube).

### Build Recipe (com.vidsagar.hush.yml)

```yaml
Categories:
  - Internet
  - Multimedia
  - Games
License: GPL-3.0-or-later
AuthorName: vidya-hub
SourceCode: https://github.com/vidya-hub/Hush
IssueTracker: https://github.com/vidya-hub/Hush/issues
Changelog: https://github.com/vidya-hub/Hush/releases

AutoName: Hush
Summary: A calm YouTube client with breathing breaks, meditation and offline mini games
Description: |-
  Hush is a calm, private YouTube client for Android. Instead of an endless
  feed, the Home screen offers a break: guided breathing and meditation
  sessions, quick offline games (2048, Snake, Sudoku, Make 24), and your own
  library of History, Saved and Downloads. The player is fully gesture driven
  with background and popup playback, downloads, and local playlists. No
  account needed, no tracking; data stays on the device.

RepoType: git
Repo: https://github.com/vidya-hub/Hush.git

Builds:
  - versionName: '1.0.0'
    versionCode: 104
    commit: v1.0.0
    subdir: app
    gradle:
      - yes
    prebuild: |
      # first build downloads the ffmpeg-kit AAR pinned by SHA-256
      # from the v1.0.0 GitHub release (see ffmpeg/build.gradle.kts);
      # no binary blobs are committed to the repository
      :

AutoUpdateMode: Version v%v
UpdateCheckMode: Tags
CurrentVersion: '1.0.0'
CurrentVersionCode: 104
```

### Build notes

* Per-ABI APKs via Gradle splits (arm64-v8a, armeabi-v7a, x86, x86_64), version
  codes follow 100 * base + ABI (101, 102, 103, 104 for base 1); the draft above
  uses the arm64 output, maintainers may prefer one Builds entry per ABI.
* versionName and the base versionCode live in app/build.gradle
  (`def appVersionName`, `def baseVersionCode`).
* The only binary dependency is ffmpeg-kit, fetched at build time from the
  project's GitHub release and pinned by SHA-256 in ffmpeg/build.gradle.kts
  (same approach the NewPipe family uses). The repository tree itself contains
  no prebuilt blobs.
* Fastlane metadata (descriptions, icon, screenshots, changelogs per
  versionCode) is at fastlane/metadata/android/en-US/.
* Development was assisted by AI tools with human review, on-device testing and
  unit tests for the game and breathing engines (also disclosed in our
  IzzyOnDroid request).

---

Notes for you (not part of the submission):

* Timeline: expect weeks. A maintainer verifies the recipe, builds the tag in
  their infra, and merges; the app appears in the main repo about 24-48h after
  the build cycle.
* F-Droid signs with its own key. Users moving between your GitHub/IzzyOnDroid
  builds and the F-Droid build will need a reinstall (unless reproducible
  builds with signature sharing are set up later, as PipePipe did).
* Answer maintainer questions in the issue promptly; they may patch the recipe
  (e.g. per-ABI Builds entries) and will check for blobs, trackers and license
  compliance.
