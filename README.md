# Hush

**A calm, private YouTube client for Android — watch what you need, then take a breath.**

Hush is a personal rebuild of the open-source NewPipe lineage (NewPipe → PipePipe) around one idea:
your video app shouldn't be a slot machine. The Home screen offers a break instead of an endless
feed, quick offline games instead of shorts, and your library instead of recommendations — while
a full-featured, gesture-driven player stays one tap away.

These screenshots are fresh captures from the app on a headless Android emulator. Mobile is 411×914dp; tablets are 1280×800dp landscape and 800×1280dp portrait. The Watch and floating-video screenshots show real YouTube playback; separate local-fixture checks verify decoder continuity without relying on a live stream.

## Mobile screenshots

| Home | Search with floating video | Watch |
| :---: | :---: | :---: |
| ![Mobile Home](screenshots/latest/mobile-home.png) | ![Mobile search and floating video](screenshots/latest/mobile-floating-results.png) | ![Mobile watch](screenshots/latest/mobile-watch.png) |

## Tablet screenshots

| Landscape Home | Landscape floating video | Landscape Watch |
| :---: | :---: | :---: |
| ![Landscape Home](screenshots/latest/tablet-landscape-home.png) | ![Landscape search and floating video](screenshots/latest/tablet-landscape-floating-results.png) | ![Landscape Watch](screenshots/latest/tablet-landscape-watch.png) |

| Portrait Home | Portrait floating video | Portrait Watch |
| :---: | :---: | :---: |
| ![Portrait Home](screenshots/latest/tablet-portrait-home.png) | ![Portrait search and floating video](screenshots/latest/tablet-portrait-floating-results.png) | ![Portrait Watch](screenshots/latest/tablet-portrait-watch.png) |

Home's header, headline, search and shortcuts share one scrolling page. Collapsed video floats above browsing content and never reserves an empty section underneath the results. Landscape tablets use a navigation rail; portrait and compact windows use bottom navigation with equal side margins.

## Games and Breathe

These are actual in-app captures of isolated demonstration rounds and a running breathing session. The capture flow restores existing game saves and privacy settings. Active breathing exposes Pause/Resume and End controls while hiding the setup options.

### Mobile gameplay

| 2048 | Snake | Sudoku |
| :---: | :---: | :---: |
| ![2048 gameplay](screenshots/latest/mobile-game-2048.png) | ![Snake gameplay](screenshots/latest/mobile-game-snake.png) | ![Sudoku gameplay](screenshots/latest/mobile-game-sudoku.png) |

| Make 24 | Breathe setup | Active breathing |
| :---: | :---: | :---: |
| ![Make 24 gameplay](screenshots/latest/mobile-game-make24.png) | ![Breathe setup](screenshots/latest/mobile-breathe-setup.png) | ![Active breathing](screenshots/latest/mobile-breathe-active.png) |

### Landscape tablet gameplay

| 2048 | Snake | Sudoku |
| :---: | :---: | :---: |
| ![2048 gameplay](screenshots/latest/tablet-landscape-game-2048.png) | ![Snake gameplay](screenshots/latest/tablet-landscape-game-snake.png) | ![Sudoku gameplay](screenshots/latest/tablet-landscape-game-sudoku.png) |

| Make 24 | Breathe setup | Active breathing |
| :---: | :---: | :---: |
| ![Make 24 gameplay](screenshots/latest/tablet-landscape-game-make24.png) | ![Breathe setup](screenshots/latest/tablet-landscape-breathe-setup.png) | ![Active breathing](screenshots/latest/tablet-landscape-breathe-active.png) |

### Portrait tablet gameplay

| 2048 | Snake | Sudoku |
| :---: | :---: | :---: |
| ![2048 gameplay](screenshots/latest/tablet-portrait-game-2048.png) | ![Snake gameplay](screenshots/latest/tablet-portrait-game-snake.png) | ![Sudoku gameplay](screenshots/latest/tablet-portrait-game-sudoku.png) |

| Make 24 | Breathe setup | Active breathing |
| :---: | :---: | :---: |
| ![Make 24 gameplay](screenshots/latest/tablet-portrait-game-make24.png) | ![Breathe setup](screenshots/latest/tablet-portrait-breathe-setup.png) | ![Active breathing](screenshots/latest/tablet-portrait-breathe-active.png) |

## What's inside

**A quiet Home**
- *Take a break* card with **Breathe** and **Meditate** — the home never starts a session by itself.
- Quick games (2048, Snake, Sudoku, Make 24) as calm, tap-sized entry points.
- History, Saved, and Downloads in one grouped library card. No feed, no recommendations,
  no "up next" on Home — that content lives where you looked for it.
- A compact search field inside the Home page scroll, with recent queries and library shortcuts. Search results keep a query editing bar; expanded Watch has no search field.

**Breathe & Meditate**
- Clock-driven breathing engine: Calm (4 in / 6 out), Box (4·4·4·4), and 4·7·8, with a phase ring,
  per-phase countdown, and soft local cues you can mute.
- A quiet meditation timer with start/completion chimes and a still visual.
- 1, 3, or 5 minutes; sessions end exactly at the chosen time. Backgrounding pauses the session,
  rotation keeps it, and your player keeps playing underneath — cues never pause or seek media.
- Reduced-motion friendly: with system animations disabled, phase text and timing update without
  orb motion.

**Offline games** (no ads, no sounds, no network)
- **2048** — swipe to merge, undo, best score, Continue or Finish at 2048.
- **Snake** — swipe to steer, tap the board to pause/resume; fully touch-driven, no D-pad.
- **Sudoku** — bundled one-solution Easy/Medium puzzles, pencil notes, conflict highlighting,
  hints, and a pauseable timer.
- **Make 24** — fraction-exact arithmetic rounds, every round verified solvable, with hints and undo.
- Game progress is saved per profile, paused when you leave, and never written in incognito.
- Snake buffers rapid corner turns and uses a frame-synchronized, interpolated movement loop. 2048 commits swipes at the movement threshold and keeps ordered input during its animation. Sudoku selects on the first tap; Make 24 retains its operand tiles through selection changes.
- Background audio has compact controls; collapsed video is a draggable overlay that keeps its decoder while browsing.

**A proper player**
- Collapsing Watch retains the browsing context and moves the same video surface into an overlay. It clears system bars, keyboard and navigation without shrinking the page.
- Android picture-in-picture remains available when leaving expanded Watch on supported devices.
- Unified Fit/Zoom video geometry for every surface (embedded, fullscreen, popup), honoring pixel
  aspect ratio and rotation — circles stay circles.
- Single tap for controls, side double-tap for ±10 s, swipe-to-seek with thumbnail previews,
  swipe-to-fullscreen, pinch crop in fullscreen, side brightness/volume gestures.
- One Playback sheet in both orientations: Quality, Speed, Captions, and Display in one place.
- Legacy "Fill" preferences migrate to Fit — stretched video is gone by design.

**Privacy & profiles**
- No Google account, no play services required, no telemetry. Subscriptions and history stay local.
- Multiple local profiles (each with its own sessions), and an incognito mode that writes nothing.

**Design**
- Warm paper / dark pine Material 3 themes that follow the system setting.
- [Manrope](app/src/main/assets/manrope_ofl.html) for type (OFL licensed, bundled), 48 dp touch targets,
  and layouts that reflow — not clip — at large font sizes and narrow screens.

## Building

Requirements: JDK 21 (verified), Android SDK with API 37 installed. This is a standard Gradle project — open it in Android Studio,
or create `local.properties` with `sdk.dir=/path/to/android-sdk` and build from the command line.
The ffmpeg-kit AAR is not committed; the first build downloads it from the project's GitHub
release and verifies a pinned SHA-256 checksum, so a clean clone builds out of the box:

```bash
# debug APK
./gradlew :app:assembleDebug

# signed release (expects keystore.properties, see app/build.gradle for the expected keys)
./gradlew :app:assembleRelease

# unit tests (breathing engine + game engines)
./gradlew :app:testDebugUnitTest
```

The extractor lives in `extractor/` as an included Gradle build; no extra setup is needed.

## Project layout

| Path | What it is |
| --- | --- |
| `app/` | The Android application (player, home, break sheet, games, storage) |
| `extractor/` | The streaming extractor (YouTube-only fork of NewPipeExtractor) |
| `ffmpeg/` | Native ffmpeg module used by the app |
| `screenshots/` | Real screen captures used in this README |

## Credits & license

Hush is a derivative work of two great open-source projects and is licensed under the
**GPL-3.0** (see [LICENSE](LICENSE)):

- [NewPipe](https://github.com/TeamNewPipe/NewPipe) and the NewPipeExtractor — the foundation.
- [PipePipe](https://github.com/InfinityLoop1308/PipePipe) by InfinityLoop1308 — the fork this
  project started from, including its extractor work.
- [Manrope](https://github.com/google/fonts/blob/main/ofl/manrope/OFL.txt) typeface, under the
  SIL Open Font License.

Hush is not affiliated with YouTube, Google, or any service it interfaces with. Use it
respectfully within the terms of the services you access.
