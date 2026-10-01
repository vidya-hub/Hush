# Hush: minimal bottom search home concepts

Generated with the built-in imagegen tool on 2026-09-30. These are design proposals, not implemented screens. Full generation prompts are in `prompts.json`.

## Code inspected

- `app/src/main/res/layout/fragment_search.xml`: header, scrolling home content, fixed 56dp search field, separate 56dp incognito button, and optional playback row **below** search.
- `app/src/main/java/org/schabi/newpipe/fragments/list/search/HomeDashboard.java`: break card, 2048/Snake cards, Sudoku row, library rows, theme-dependent search styling, responsive arrangements.
- `app/src/main/java/org/schabi/newpipe/fragments/list/search/SearchFragment.java`: profile switching, overflow actions, search/results states, incognito hints, background playback visibility, responsive dock width capped at 720dp.
- `app/src/main/java/org/schabi/newpipe/hush/ui/CompactPlaybackBar.java`: compact audio control used on game and break screens.
- `app/src/main/java/org/schabi/newpipe/hush/ui/HushUi.java`: shared colors, widths, safe-inset helpers, touch feedback.
- `app/src/main/res/values/colors.xml`, `strings.xml`, and existing home mockups: current pine/paper palette, Hush branding, labels and prior concepts.

## Design decisions

Keep search anchored at the bottom, outside scrolling content. Use a tonal surface and ordinary body typography to make it a quiet utility field. Preserve a separate incognito action and profile/overflow access. Compact the upper sections so they do not compete with search or force library access far down the page. No feed or recent-history content on home.

The current `HomeDashboard.styleSearch()` uses a solid pine fill in light mode and a bright cream fill in dark mode. The proposed tonal fill is a visual change, while dock position and search interaction stay consistent with the code.

For active audio, preserve current XML order: scrolling home, search dock, playback row, system gesture area. Only show the playback row when background audio is active and the existing player sheet is hidden, matching `refreshNowPlaying()`.

Raster mockups illustrate hierarchy and styling, not exact measurable Android dimensions. An implementation should retain scrolling for shorter windows, existing wide-screen breakpoints, large-text stacking, 48dp touch targets, safe insets, keyboard/results behavior, and incognito selected state.

Visual review: labels, section order, profile/overflow, minimal search placement, separate incognito action, and dark playback order are present. Generated game previews are illustrative and do not consistently reproduce the real 4x4 2048 board; production should reuse `GamePreview`. The compact image shows a filled incognito button; reserve that emphasis for its selected state and use a tonal idle button. Generated logo and action icons are proposals; production should reuse the existing drawable assets.

## Deliverables

- `01-light-balanced.png`: warm paper home with a subdued bottom search field.
- `02-dark-playing.png`: dark counterpart with optional playback beneath search.
- `03-light-compact.png`: more compact alternative with reduced game previews and flatter library access.

Recommended direction: use the compact alternative's content density with the light/dark pair's tonal bottom dock. Production Java/XML files were not changed.
