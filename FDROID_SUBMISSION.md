# F-Droid submission — Hush

Two ways in. The merge request is the route F-Droid maintainers prefer and the
one apps actually get added through (a stream of "New app:" MRs merges every
week, while RFP issues can sit for a long time). Do the MR.

Both need a gitlab.com account (sign in with GitHub works):
https://gitlab.com/users/sign_in

## Route A (recommended): direct merge request

1. Open https://gitlab.com/fdroid/fdroiddata and press **Fork** (top right).
   Keep defaults, create the fork under your account.
2. In your fork, open the **metadata** folder, then:
   **( + ) > This directory > New file**.
3. Name the file exactly `com.vidsagar.hush.yml` and paste the entire contents
   of [fdroid/com.vidsagar.hush.yml](fdroid/com.vidsagar.hush.yml) from this repo.
4. Commit to a new branch, suggested name: `hush`.
5. Open a merge request from the banner GitLab shows after committing.
   Title:
   ```
   New app: Hush
   ```
   IMPORTANT: GitLab will offer MR templates. Pick **"App inclusion"** and
   fill its checklist. A custom description gets the MR closed by maintainers
   ("Merge Request template is not followed"). Tick what applies; for items
   you cannot tick, add the reason inline (e.g. reproducible builds pending
   verification; fork pipelines cannot run without runners, ask maintainers
   to trigger CI). See MR !50203 for the filled example.
6. Submit, and answer maintainer questions promptly in the MR.

Expect review iterations (reviewers may tweak the recipe, e.g. output paths or
update-check settings); once merged, the app appears in the main repo about
24-48 hours later.

## Route B (fallback): RFP issue

Open https://gitlab.com/fdroid/fdroiddata/-/issues/new titled
`RFP: Hush (com.vidsagar.hush)`, and paste the sections below.

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

It is a substantially diverged fork of PipePipe/NewPipe (different application
ID, redesigned experience), maintained independently. Expected anti-feature:
NonFreeNet (interfaces with YouTube).

A draft build recipe is available at fdroid/com.vidsagar.hush.yml in the
repository (commit 6333dd9b91b1a895ae45c6d4d6807ed208bb98e2 is the v1.0.0 tag).

### Build notes

* Per-ABI APKs via Gradle splits, version codes 100 * base + ABI (101, 102,
  103, 104); the draft uses one Builds entry per ABI.
* versionName and base versionCode live in app/build.gradle.
* The only binary dependency is ffmpeg-kit, fetched at build time from the
  project's GitHub release and pinned by SHA-256 in ffmpeg/build.gradle.kts.
  The repository tree contains no prebuilt blobs.
* Fastlane metadata is at fastlane/metadata/android/en-US/.
* Development was assisted by AI tools with human review, on-device testing and
  unit tests for the game and breathing engines.

---

Notes (not part of the submission):

* F-Droid signs with its own key; users switching between the GitHub/IzzyOnDroid
  builds and the F-Droid build will need a one-time reinstall. Reproducible
  builds with AllowedAPKSigningKeys (as PipePipe does) can unify signatures
  later.
* Do not move the v1.0.0 tag while reviews are open; ship fixes as new tags
  with bumped versionCode.
