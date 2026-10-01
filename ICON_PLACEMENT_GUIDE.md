# Hush — generated icons and page placement

Date: 30 September 2026. This accompanies [the final redesign plan](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/UI_REDESIGN_PLAN.md) and [the placement preview](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/icon-placement-preview.html).

## Deliverables and authority

70 distinct icon identities are inventoried. Every delivered PNG is generated individually through built-in imagegen, one subject per call, sequentially. No sprite sheet, contact sheet generation, CLI batch, downloaded icon pack, or hand-drawn vector substitute is used. The manifest records completion and the alpha bounds of each saved file. Exact prompts are stored individually and consolidated in [prompts.json](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/prompts.json).

The 14 selected mockups define the visual direction. This guide defines icon identity, placement and state where the generated raster mockups differ. The HTML companion places the actual PNGs into page components and shows them beside the selected mockup; it is a review artifact, not the Android implementation. The original raster mockups and PNG pixels are preserved.

## Shared placement and rendering

| Property | Requirement |
| --- | --- |
| Gutter | 22dp leading/trailing for header, body and inset navigation. Use start/end so RTL layouts mirror correctly. |
| Action glyph | 24dp optical bounding box; 48dp minimum touch target. 18dp inline query/metadata glyph; 28dp Breathe mark; 32dp playlist/file tile glyph; 48dp empty-state decoration. |
| Optical fitting | Use manifest alpha bounds to center and uniformly fit the glyph; do not squeeze non-square PNG canvases or stretch their aspect ratio. Preserve original alpha. The HTML uses CSS masks and measured bounds, not rewritten PNG pixels. |
| Color | Tint alpha at display time: normal #EDF3EC, secondary #B3C3B6, accent #C4D8CB, primary-button foreground #111B17, destructive #F18B82. Both the initial sage masters and later black masters receive the same semantic tint. |
| States | Selected actions use a tonal/filled container plus state announcement. Pressed and keyboard focus use a visible native state layer/ring. Disabled controls expose disabled semantics and keep explanatory text where needed. Color alone never identifies selection or errors. |
| RTL | Mirror back/forward navigation and row disclosure as appropriate. Do not automatically mirror letters, brand mark, text/CC, playback triangles, or media art. Verify directional controls with native layout rules. |
| Accessibility | Decorative marks are hidden from accessibility. Buttons get localized action labels, not filenames. Label Play/Pause, Mute/Unmute, Expand to watch/Enter fullscreen, and Add/Remove saved according to actual state. Entire row/button is the target, not the narrow glyph. |
| Source integration | Keep masters in this design folder. At implementation, import the individually generated PNG artwork through the matched Android build's bitmap drawable pipeline; decode/downsample appropriately for display density and cache, apply theme tint and optical inset. Do not load 1254px masters unbounded on the UI thread. Preserve existing preference keys and click listeners. |
| Native UI | Status-bar Wi-Fi, battery, clock, OS gesture indicator, native switches, radio/checkbox state, seek/progress bars, sheet handles, real avatars/initials, text/numbers, game boards and live breathing timer animation remain native/data-driven. They are not omitted custom icon assets. |

## State substitutions

- Save uses bookmark; already saved uses bookmark-filled. The accessible action says what activating it will do.
- A playing video uses pause; paused uses play. Downloads use pause or resume according to mission state. Breathing sessions use Pause/Resume/End, with stop only for End.
- Expanded watch collapses using chevron-down. Floating playback expands back to watch using expand. In fullscreen the same corner position uses fullscreen-exit and the correct label. Existing popup action uses pip.
- Shuffle and repeat retain state descriptions. Repeat-one uses its separate generated numeral glyph; repeat-off uses repeat with an explicit off state. Do not invent separate toggles unsupported by the player.
- speaker/mute and comments/comments-off substitute according to actual player state. Sleep timer keeps a label/value for its duration. Captions and speed keep selected language/rate text beside the icon.
- Current profile and finished mission reuse check. Profile initials remain actual text; profile is the generic fallback only.
- Failed/loading/empty states are distinct. error and retry never replace readable error messages. A decorative empty download ring is not real progress.

## Page-by-page mapping

### Home

![Selected Home mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-home-alive-concepts/03-pine-editorial-refined.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Header | [hush-mark.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [incognito.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Brand at leading edge, privacy and overflow trailing. Profile switching remains reachable in overflow. |
| Hero and current playback | [breath-ring.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [waveform.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png) | Ring is decorative at 44dp. Waveform at 32dp appears only for actual playback and is not a fake loading state. |
| Search and recent queries | [search.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [history.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png) | 24dp field-leading search; 18dp recent-query history. No permanent bottom search dock. |
| Library shortcuts | [history.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [bookmark.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [download.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png) | Three equal History, Saved and Downloads actions, with 24dp glyphs and visible labels. |
| Audio-only playback | [music.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [queue.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png) | When actual mode is audio-only, use compact labelled controls above navigation, not a fake video frame. Never coexist with video PiP. |
| Bottom navigation | [search.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [hush-mark.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png) | Search/Games/Breathe; Search selected. Three equal 48dp-or-larger targets, 72dp container, 22dp outer gutters. Label below glyph; no extra Home tab. |
| Floating video | [expand.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Expand upper-left, Close upper-right, Play/Pause center. Each is 48dp; no overlap. 184–208dp-wide 16:9 frame, 22dp outer gutter, 12dp above nav or gesture-safe bottom. Draggable safe corners; keep primary controls clear. |

### Submitted search

![Selected Submitted search mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-search-player-flow/01-search-results.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Editable query toolbar | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Back leading; clear query within field trailing; overflow outside field. Search stays editable after submission. |
| Display and filtering | [list.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png), [grid.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png), [content-filter.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png) | List/grid toggle above results; filter only in supported filter menu. Selected display mode announced. |
| Result rows | [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [live.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png) | Overflow trailing each media row. Live icon appears only with verified live metadata. Duration is text. |

### Watch / expanded player

![Selected Watch / expanded player mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-search-player-flow/02-watch-page.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Player overlay | [chevron-down.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [expand.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [fullscreen-exit.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png) | Collapse upper-left, overflow upper-right, play OR pause center, fullscreen lower-right. Exit-fullscreen replaces enter only in fullscreen. |
| Media actions | [bookmark.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [bookmark-filled.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png), [headphones.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [download.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [more-horizontal.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png) | Save OR saved, Background, Download and overflow below title/channel. Search and bottom navigation absent. |
| Details and queue | [chevron-down.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [queue.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Disclosure trailing each section; queue leading title; row overflow trailing. Expanded/collapsed state announced. |
| Existing player menu | [share.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [pip.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png), [previous.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/previous.png), [next.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/next.png), [shuffle.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/shuffle.png), [repeat.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat.png), [repeat-one.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat-one.png), [add.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [captions.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png), [speed.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png), [speaker.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png), [mute.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png), [globe.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.png), [cast.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/cast.png), [sleep-timer.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/sleep-timer.png), [comments.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments.png), [comments-off.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments-off.png), [brightness.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/brightness.png), [info.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png), [settings.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png) | Retain existing menus and conditional supported actions. Do not put every menu icon on the primary watch toolbar or introduce unavailable services. |

### Results after player down-swipe

![Selected Results after player down-swipe mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-search-player-flow/03-results-floating-video.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Restored query and results | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [list.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png), [grid.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png), [content-filter.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png), [live.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png) | Reuse submitted-search components and preserve query, filters and scroll. Only display current mode glyph and supported items. |
| Floating video | [expand.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Expand upper-left, Close upper-right, Play/Pause center. Each is 48dp; no overlap. 184–208dp-wide 16:9 frame, 22dp outer gutter, 12dp above nav or gesture-safe bottom. Draggable safe corners; keep primary controls clear. |

### Saved / populated

![Selected Saved / populated mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/03-saved-playlists.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Toolbar | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Saved title between back and overflow. |
| Creation | [add.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png) | 24dp leading glyph in labelled New playlist action. |
| Playlist rows | [playlist.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png), [chevron-right.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | 32dp mark in 48dp tonal tile leading; chevron trailing. Reuse one mark, actual names/counts, no artificial artwork variations. |
| Existing management menu | [edit.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png), [trash.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [share.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png) | Rename and remove remain in existing actions; share only where supported by the current playlist implementation. |

### Playlist detail

![Selected Playlist detail mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/06-playlist-detail.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Toolbar and identity | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [playlist.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png) | Back leading, overflow trailing; same playlist mark at 48dp in header. |
| Playback | [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [headphones.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png) | Leading glyphs inside labelled Play all and Background actions. |
| Rows | [drag.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Reorder at start and overflow at end; 48dp targets. Accessible move-up/down alternative. |
| Existing playlist actions | [edit.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png), [trash.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [share.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [pip.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png), [shuffle.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/shuffle.png) | Only expose existing supported operations. Popup playback uses pip, not the expand glyph. |
| Floating video | [expand.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Expand upper-left, Close upper-right, Play/Pause center. Each is 48dp; no overlap. 184–208dp-wide 16:9 frame, 22dp outer gutter, 12dp above nav or gesture-safe bottom. Draggable safe corners; keep primary controls clear. |

### Profiles sheet

![Selected Profiles sheet mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/05-profiles.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Sheet header | [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Close at upper-right in 48dp target. Handle is a native rounded line, not an icon asset. |
| Profile rows | [profile.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png), [check.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png) | Default avatar leading only when no actual avatar/initials; check trailing current profile. Initials remain real text. |
| Creation and deletion | [add.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [trash.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png) | Add leading New profile row; delete only in allowed existing profile management flow with confirmation. |

### History

![Selected History mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/04-history.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Toolbar | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Visible History title and existing overflow. |
| Privacy | [incognito.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png) | 24dp leading privacy mark; native switch trailing, accurate enabled/disabled helper text. |
| Watch and search history | [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [history.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png) | Watch-row overflow trailing; search-query rows use history leading. Empty-state mark uses history at 48dp. |
| Clear history | [trash.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png) | 24dp leading destructive glyph beside Clear history. Confirm and clear only the active tab under existing rules. |
| Floating video | [expand.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Expand upper-left, Close upper-right, Play/Pause center. Each is 48dp; no overlap. 184–208dp-wide 16:9 frame, 22dp outer gutter, 12dp above nav or gesture-safe bottom. Draggable safe corners; keep primary controls clear. |

### Games hub

![Selected Games hub mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/02-games-hub.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Game choices | Native preview / text | Four native GamePreview boards are illustrations/data, not navigation icons. Keep all four actual models. |
| Individual game controls | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [retry.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [info.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Use only the controls implemented for each game. Back and help in toolbar; pause/resume/restart in labelled action area; board stays clear of PiP. |
| Game-specific tools | [undo.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png), [erase.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png), [hint.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png), [notes.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png), [swap.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png) | 2048 Undo/New game; Sudoku pause/resume, notes, erase, hint, New puzzle; Make24 swap/undo/hint and text-only Skip/Next round. Snake retains gesture/accessibility steering without adding buttons. |
| Bottom navigation | [search.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [hush-mark.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png) | Search/Games/Breathe; Games selected. Three equal 48dp-or-larger targets, 72dp container, 22dp outer gutters. Label below glyph; no extra Home tab. |
| Floating video | [expand.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Expand upper-left, Close upper-right, Play/Pause center. Each is 48dp; no overlap. 184–208dp-wide 16:9 frame, 22dp outer gutter, 12dp above nav or gesture-safe bottom. Draggable safe corners; keep primary controls clear. |

### Breathe / Meditate

![Selected Breathe / Meditate mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/01-breathe-setup.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Technique identity | [breath-ring.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [chevron-right.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | 32dp decorative technique mark leading the row; 24dp disclosure trailing. Phase ring/timer remains a native animation. |
| Session states | [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [stop.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.png), [retry.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png) | Active Pause/End; paused Resume; completed Again. Setup Start breathing remains a labelled text action. |
| Sound state | [speaker.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png), [mute.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png) | Use in supported sound/volume control presentation; do not replace the native Sound cues switch with an icon-only control. |
| Bottom navigation | [search.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [hush-mark.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png) | Search/Games/Breathe; Breathe selected. Three equal 48dp-or-larger targets, 72dp container, 22dp outer gutters. Label below glyph; no extra Home tab. |

### Saved / empty

![Selected Saved / empty mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/07-saved-empty.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Toolbar | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Same Saved toolbar as populated state. |
| Empty state and creation | [playlist.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png), [add.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png) | 48dp decorative empty mark, 24dp leading Create playlist glyph. Loading/error must not render this empty state. |
| Load error | [error.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png), [retry.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png) | Separate readable error text and labelled Retry action. |

### Downloads / populated

![Selected Downloads / populated mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/08-downloads.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Toolbar | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Title/back/overflow; no top-level bottom navigation. |
| File identity | [file-video.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png), [file-audio.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png), [file-subtitle.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png) | 32dp glyph in 48dp tonal tile, based on actual media type. |
| Mission controls | [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [check.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png) | Pending pause; paused resume; completed check. Each mission shows real status/progress. |
| Failure and existing menu | [error.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png), [retry.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [folder.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png), [trash.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [share.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [info.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Failures show readable reason and Retry where possible; menu actions depend on mission support and permissions. |

### Settings

![Selected Settings mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/09-settings.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Toolbar | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png) | Settings title; settings search at trailing edge. This search is not YouTube search. |
| Playback group | [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [gestures.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/gestures.png), [download.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [chevron-right.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | Player, Gestures, Download leading glyphs; row chevrons trailing. |
| Library group | [history.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [feed.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/feed.png), [backup.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/backup.png), [chevron-right.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | History/cache, Feed, Backup; preserve preference destinations. |
| Appearance, Account, Advanced | [appearance.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/appearance.png), [profile.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png), [globe.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.png), [content-filter.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png), [settings.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png), [chevron-right.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | Appearance; Account; Content via globe; Content filter; Advanced. Match existing hierarchy and retain all preferences. |
| Child settings actions | [notifications.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notifications.png), [folder.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png), [trash.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [info.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Only existing notification/destination/clear/explanatory controls; no new preference values. |

### Downloads / empty

![Selected Downloads / empty mockup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/10-downloads-empty.png)

| Region | Individual files | Exact placement / behavior |
| --- | --- | --- |
| Toolbar | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Same Downloads toolbar. |
| Empty state | [download.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png) | 48dp decorative download mark. Surrounding progress ring is a native static decoration, not actual progress. |
| Creation form | [folder.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png), [file-video.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png), [file-audio.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png), [file-subtitle.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png), [chevron-down.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [error.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png) | Retain supported media/quality/destination controls and permission validation. Form is specified; no dedicated raster mockup yet. |

## Complete individual inventory

The same file is reused where its meaning is shared. Each row is a separate generation, not a crop from an atlas.

| # | Generated PNG | Uses |
| --- | --- | --- |
| 1 | [hush-mark.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png) | Home brand; Breathe navigation |
| 2 | [search.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png) | Home field/nav; results; Settings search |
| 3 | [games.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png) | Games navigation |
| 4 | [incognito.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png) | Home privacy; History privacy |
| 5 | [more-vertical.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Toolbars and media-row overflow |
| 6 | [more-horizontal.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png) | Watch action overflow |
| 7 | [history.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png) | Recent queries; History shortcut; History/cache setting |
| 8 | [bookmark.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png) | Saved shortcut; watch Save |
| 9 | [bookmark-filled.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png) | Watch already-saved state |
| 10 | [download.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png) | Home Downloads; watch Download; downloads empty; settings |
| 11 | [back.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png) | Subpage toolbars; search results |
| 12 | [chevron-right.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | Playlist rows; technique row; Settings rows |
| 13 | [chevron-down.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png) | Watch collapse; Description and Queue disclosure |
| 14 | [close.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Clear query; close video; dismiss profile sheet |
| 15 | [play.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png) | Video paused; Play all; download resume |
| 16 | [pause.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png) | Playing video; download pause; active session pause |
| 17 | [expand.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png) | Floating-video expand; fullscreen entry |
| 18 | [fullscreen-exit.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png) | Fullscreen exit |
| 19 | [headphones.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png) | Background audio; player settings |
| 20 | [playlist.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png) | Saved rows and empty state; playlist detail header |
| 21 | [add.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png) | New playlist; New profile; add to playlist |
| 22 | [check.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png) | Current profile; completed download; selected options |
| 23 | [profile.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png) | Default avatar; Account; profile entry |
| 24 | [drag.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.png) | Playlist reorder grip |
| 25 | [trash.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png) | Clear history; remove/delete confirmation |
| 26 | [edit.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png) | Rename playlist; rename profile where supported |
| 27 | [retry.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png) | Retry load/download; restart game/session |
| 28 | [speaker.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png) | Enabled sound cues; volume |
| 29 | [mute.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png) | Muted video; sound cues disabled |
| 30 | [settings.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png) | Player/settings menu; Advanced settings |
| 31 | [gestures.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/gestures.png) | Gesture settings |
| 32 | [feed.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/feed.png) | Feed settings |
| 33 | [backup.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/backup.png) | Backup settings |
| 34 | [appearance.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/appearance.png) | Appearance settings |
| 35 | [content-filter.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png) | Content-filter settings; supported search filters |
| 36 | [list.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png) | Results list view |
| 37 | [grid.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png) | Results grid view |
| 38 | [share.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png) | Existing watch/player Share menu |
| 39 | [queue.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png) | Watch Queue; player queue |
| 40 | [shuffle.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/shuffle.png) | Player queue shuffle |
| 41 | [repeat.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat.png) | Repeat all; repeat off uses state treatment |
| 42 | [repeat-one.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat-one.png) | Repeat one state |
| 43 | [stop.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.png) | End breathing/meditation; supported stop actions |
| 44 | [info.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Description/info; supported explanatory menus |
| 45 | [error.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png) | Load/download failure |
| 46 | [folder.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png) | Download destination; open supported file directory |
| 47 | [file-video.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png) | Video download file |
| 48 | [file-audio.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png) | Audio download file |
| 49 | [file-subtitle.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png) | Subtitle download file |
| 50 | [live.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png) | Live result badge only for live streams |
| 51 | [waveform.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png) | Home actual now-playing ornament |
| 52 | [breath-ring.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png) | Home decorative motif; Breathe technique mark |
| 53 | [pip.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png) | Existing popup/PiP menu action |
| 54 | [previous.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/previous.png) | Existing previous track control |
| 55 | [next.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/next.png) | Existing next track control |
| 56 | [captions.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png) | Player subtitles/captions |
| 57 | [speed.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png) | Player playback-speed menu |
| 58 | [globe.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.png) | Open in browser; Content/language settings |
| 59 | [cast.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/cast.png) | Existing external-player/cast action |
| 60 | [sleep-timer.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/sleep-timer.png) | Existing sleep-timer menu |
| 61 | [comments.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments.png) | Existing comments/bullet-comments enabled |
| 62 | [comments-off.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments-off.png) | Existing bullet-comments disabled |
| 63 | [brightness.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/brightness.png) | Existing player brightness gesture indicator |
| 64 | [notifications.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notifications.png) | Existing Feed/notification settings subpage |
| 65 | [undo.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png) | 2048 and Make24 Undo |
| 66 | [erase.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png) | Sudoku Erase |
| 67 | [hint.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png) | Sudoku and Make24 Hint |
| 68 | [notes.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png) | Sudoku pencil-notes mode |
| 69 | [swap.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png) | Make24 swap selected operands |
| 70 | [music.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png) | Existing audio-only compact playback identity |

## Integration and acceptance

1. Match the source/revision to the installed app before changing code. Resolve missing header titles and player/nav overlap there.
2. Apply tokens and the generated icon resolver; use measured optical bounds and uniform scale. Do not replace the individually generated assets with legacy SVGs without a later design decision.
3. Wire page placements and action-specific labels without changing data operations. Maintain profile protection, history confirmations and playlist reorder behavior.
4. Keep one playback session/surface through watch-to-floating transitions. Restore the previous page/query/scroll; no YouTube search on watch. No player strip simultaneously occupying navigation.
5. For menus retained from source, show only supported/available actions. Cast/external player, comments and subtitles remain capability-specific. Download source/checksum/open/error controls preserve existing behavior; globe/check/info can be reused there.
6. Verify each glyph at 18/24/28/32/48dp in light/dark themes, high density, RTL, large text and screen-reader focus. Inspect transparency/edges and verify no invisible black source art is shown without tint in dark mode.
7. Verify no missing files/prompts, no unknown IDs in the page map, and coverage of every inventoried identity. Review the complete visible gallery and all 14 page references. Native status/animation/data assets are explicitly outside custom-icon generation.

Individual-game boards, active breathing, populated download/playlist data and child settings forms still need implementation QA; mockups do not establish those runtime states. Android code and user data have not been changed by this package.


## Delivery verification

- 70/70 distinct icon PNGs and 70 exact individual prompt files are present. All masters have a real alpha channel with fully transparent background pixels and visible ink. SHA-256 and optical bounds are recorded in the manifest.
- All 70 IDs are mapped to screen regions; all 14 selected mockup pages loaded successfully in the browser preview with no pending glyphs. All linked local files/images in the plan and guide exist.
- Reviewed the gallery at 24/32px and home controls with the actual PNGs. Refined History and Incognito individually; their earlier outputs/prompts are preserved under `superseded/`.
- This validates the design package, not Android behavior. Matching-build diagnosis and the runtime acceptance matrix remain implementation tasks. Individual game boards, active breathing and child settings still need their runtime review.

![Verified home mockup and actual generated icon placements](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/verified-home-placement.jpg)

[Full placement and gallery proof](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/verified-full-preview.jpg)
