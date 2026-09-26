# IzzyOnDroid submission — Hush

Paste-ready content for a new issue at:
**https://codeberg.org/IzzyOnDroid/repodata/issues/new**
(their old GitLab tracker is archived/read-only — submissions moved to Codeberg)

---

**App name:** Hush

**Package name:** com.vidsagar.hush

**Source repo:** https://github.com/vidya-hub/Hush

**License:** GPL-3.0-or-later

**Release feed:** https://github.com/vidya-hub/Hush/releases
(latest: https://github.com/vidya-hub/Hush/releases/tag/v1.0.0 — per-ABI APKs, ~20 MiB each, developer-signed)

**Summary:** A calm, private YouTube client with breathing breaks and offline games

**Full description:**

Hush is a calm, private YouTube client for Android. Instead of an endless feed, the
Home screen offers a break: a Breathe and Meditate card with guided breathing
(Calm 4/6, Box, 4-7-8) and a quiet meditation timer, quick offline games (2048,
Snake, Sudoku, Make 24), and your own library of History, Saved and Downloads.
The player is fully gesture driven (±10 s double-tap, preview scrubbing, pinch
zoom, Fit/Zoom geometry) with background and popup playback, downloads, and local
playlists. No account needed, no tracking — subscriptions, history and profiles
stay on the device, with per-profile data and an incognito mode that writes nothing.

**Why include it:** A materially diverged fork of PipePipe/NewPipe focused on
digital wellbeing — the only client in this family whose home is a break, not a
feed. Fully FOSS (GPL-3.0), no trackers or ads, builds from tagged source with a
clean, blob-free tree; the single binary dependency (ffmpeg-kit AAR) is attached
to each release and pinned by SHA-256 in the repository.

**AntiFeatures to declare:** NonFreeNet (interfaces with YouTube)

**Fastlane metadata:** included in the repo (`fastlane/metadata/android/en-US/`
with short/full descriptions, icon, 8 screenshots and changelogs)

**Author:** vidya-hub

**Donations:** none

---

Notes for you (not part of the submission):

- APK sizes are ~20 MiB, under the repo's usual 30 MB per-app limit.
- IzzyOnDroid fetches updates from the GitHub release feed automatically once
  accepted — future updates just need a new tag + release with a bumped
  `versionCode`.
- Keep every APK signed with the same key (keystore lives, gitignored, in the
  Hush working copy `keystore/` and in `PipePipe/PipePipeClient/keystore/`).
- For the official F-Droid repo later: file an RFP at
  https://gitlab.com/fdroid/fdroiddata — the repo is already blob-free and
  builds from a clean clone.
