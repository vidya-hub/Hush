# Hush minimal home with floating video

Design proposals generated with built-in imagegen on 2026-09-30. Android implementation was not changed. Exact prompts are saved in `prompts.json`.

## Observed running UI

The connected emulator was running `com.vidsagar.hush.debug`, version 1.0.2 (304). `current-emulator.png` captures its actual home: Hush header, incognito and overflow, YouTube search, recent-search chips, three bottom navigation destinations (Search, Games, Breathe), and a collapsed player overlapping the navigation. `current-results.png` is a subsequent search-results snapshot, not a Games screen.

The installed UI differs from this checkout: the source `fragment_search.xml` still includes a dashboard and bottom search dock, and the three tab IDs observed in the emulator hierarchy were not found in this source tree. Therefore these mockups follow the observed installed UI for product structure. A later implementation should first locate/synchronize the source that produced the installed build. We did not rebuild or reinstall the app.

## Proposed home

Keep the existing three destinations: Search, Games, Breathe. Add small labels so their meanings stay clear. Keep incognito and overflow in the header. Use one understated search input and otherwise leave the home sparse; no break, game, or library dashboard cards. Recent search chips are optional and should disappear for empty history or incognito.

Replace the collapsed bottom video strip with a floating 16:9 video window, initially at the lower right, above the navigation. Close, pause/play and expand belong inside the window. The navigation must remain fully visible and tappable. Show only one player surface. The video scene and chip text are illustrative, not actual new app data.

Suggested interaction: drag and snap to safe corners; keep clear of navigation, search and keyboard; tap expand to return to the watch view; closing stops/closes playback according to the established player action. Hide the window when playback ends. If playback is audio-only, do not pretend it has a live video surface; decide its separate control treatment during implementation.

## Player code inspected

`VideoDetailFragment` currently uses a collapsed bottom sheet and reserves its peek height beneath the content. `SearchFragment.refreshNowPlaying()` shows another compact row only for background audio when the player sheet is hidden. `NavigationHelper.playOnPopupPlayer()` and `Player.initPopup()` use a separate WindowManager overlay player requiring popup permission.

The proposed window is an **in-app floating video surface**. It is distinct from OS picture-in-picture after leaving the application and from the existing global popup overlay. It should reuse the active player/session rather than start a second stream. The inspected checkout does not establish that this behavior is already implemented.

## Deliverables

- `01-light-floating-video.png`: recommended paper-and-pine home with floating playback.
- `02-dark-floating-video.png`: matching dark theme with navigation clear of playback.
- `03-light-idle.png`: empty-history, inactive-playback home.

Visual checks cover supplied labels, exactly three destinations, uncluttered home, and floating video clear of navigation. Raster mockups are approximate layout proposals; exact safe areas, contrast, 48dp controls, dragging, keyboard and orientation behavior require validation in the Android implementation.
