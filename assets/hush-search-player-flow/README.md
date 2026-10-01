# Hush: search, watch and swipe-to-floating-player flow

Raster mockups generated with built-in imagegen on 2026-09-30. These propose behavior; no Android implementation changes. Exact generation/edit prompts are saved in `prompts.json`.

| State | Search treatment | Playback treatment | Navigation |
| --- | --- | --- | --- |
| Home | Existing home search field | Floating video when active | Search, Games, Breathe with matching horizontal gutters |
| Submitted results | Compact editable query in top toolbar | No player before opening a result | Back, filters, view toggle; top-level tabs hidden |
| Watch page | No search field or search toolbar | One expanded video surface; collapse chevron and drag handle | Watch actions, description and queue |
| Swipe down from watch | Restore prior page and query/scroll state | Same stream continues in floating 16:9 window | Prior page's navigation is restored |

## Images

1. `01-search-results.png`: submitted query remains editable at the top; no bottom search dock.
2. `02-watch-page.png`: search disappears completely; the video and playback actions own the page.
3. `03-results-floating-video.png`: after downward swipe, return to the results and keep watching in a floating window.
4. `04-home-floating-video.png`: the user's already selected home design, copied from `hush-home-alive-concepts/03-pine-editorial-refined.png`; navigation remains inset and clear of playback.

## Proposed interaction

- Submit a query: switch home to results, preserve query, and move search into a compact results toolbar. Tapping the query edits it. Show clear/query/filter controls as appropriate, rather than duplicating search in two locations.
- Open a result: transition to watch page and hide the browsing search UI. The query and results stay saved beneath this screen.
- Pull down on the watch card's header/drag region, or tap collapse: animate the existing video surface from its expanded bounds toward the last floating position. Dismiss watch chrome and restore the exact prior browsing page. If that page was home, show home; if results, retain query, filters and scroll position. Do not force every collapse to home.
- Playback continues at its current position, with the same session/queue; do not restart or create a second decoder/player. One playback surface is visible at a time.
- Drag the floating window between safe corners. Keep it clear of header/search, home navigation, gesture areas, keyboard, and touch-critical controls. The results example illustrates an overlay over scrollable content; dragging lets the user uncover it.
- Expand the floating window to return to the watch page. Pause/play stays inside the window. Close ends/closes playback according to the established player action.
- The watch page's `Swipe down to keep watching` text is optional first-use guidance, not a permanent page footer. A vertical swipe during fullscreen playback must retain the player's existing fullscreen gestures instead of collapsing the page.

This is **in-app floating video**, separate from OS picture-in-picture when leaving Hush and from the checkout's existing global popup overlay. Keyboard, fullscreen, live streams, orientation, accessibility and audio-only playback need real implementation validation. The examples use illustrative search results and queue content. The actual queue may be empty. The library/filter/action UI should use the app's real data and existing supported actions.

The installed UI/source mismatch observed during the earlier emulator inspection remains documented in `hush-home-pip-concepts/README.md`; implementation should target the source that produced the installed navigation UI.
