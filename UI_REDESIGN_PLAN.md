# Hush — final UI redesign plan

Current implementation: [screen-by-screen alignment review](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/ALIGNMENT_REVIEW.md).

Date: 30 September 2026
Deliverable: design and implementation plan with embedded current screenshots and target mockups.
Status: the design package is complete (14 selected mockups and 70 individually generated icons). Android implementation is now in progress and installed on the emulator. See [implementation evidence and validation](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/IMPLEMENTATION_STATUS.md) for the source changes and actual app screenshots.

Companion: [complete icon placements and individual asset inventory](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/ICON_PLACEMENT_GUIDE.md), [mockup plus actual icon placement preview](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/icon-placement-preview.html). These define exact icon replacements and retained controls for all 14 selected mockups. Icon completion is recorded in the [manifest](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/manifest.json).

## 1. Design direction and scope

Carry the user's selected dark editorial home across the app: clear page identity, purposeful content, pine and sage colors, smaller consistent controls, and floating video that leaves navigation usable. This is a layout and interaction redesign, not a change to the app's underlying playlist, history, profile, download or game data.

This file consolidates the captured current screens and the selected target images. Ten new secondary-screen mockups cover Games, Breathe, Saved populated/empty, playlist detail, History, Profiles, Downloads populated/empty and Settings. Four previously selected mockups cover home, submitted results, watch and collapsed playback.

**Evidence boundary:** the installed debug app has Search/Games/Breathe navigation that is absent from this checkout. Before implementation, identify the source/revision that built that app. Screenshots establish visible behavior; local source provides a starting map, not a guarantee of the installed architecture. Populated playlists, playlist detail and populated downloads below are illustrated from implemented capabilities; their current populated states were not exercised.

## 2. Shared specification

| Element | Target |
| --- | --- |
| Page colors | Dark background #111B17; tonal surface #1C2922; primary text #EDF3EC; secondary #B3C3B6; sage accent #C4D8CB. Support existing light theme with corresponding semantic colors. |
| Typography | Existing Manrope/native font pipeline. Page titles 24–28sp, home headline about 30–32sp, row titles 15–17sp, secondary labels 12–14sp. Allow larger text to reflow. |
| Spacing | One measured horizontal gutter shared by headers, fields, lists and navigation: 22dp on phones, 24dp from 600dp, 32dp from 840dp; cap the centered content column at 1120dp. Prefer 8/12/16/24dp spacing increments. |
| Controls | Minimum 48dp interactive targets, consistent outline icon size/stroke. Native switches, segmented choices and clear pressed/selected states. |
| Surfaces | 16–20dp card corners, subtle tonal distinction and borders. Avoid making every list item a large card. |
| Bottom navigation | Search, Games, Breathe only; top-level pages only. About 72dp high, equal shared outer horizontal margins (22dp on phones), three equal targets, compact selected icon capsule, labels below. Safe gesture area remains outside content. |
| Subpages | Explicit title and back affordance. Saved, playlist detail, History, Downloads, Settings and watch do not inherit the top-level navigation. Back restores prior route and state. |
| Video | One 16:9 floating view above navigation, initially lower-right; roughly 184–208dp wide, draggable to safe corners. Pause/play, expand, close inside frame. Same session and playback position. |
| Modal sheets | Compact themed surfaces sized to actual content, scroll when needed. Background is dimmed and noninteractive. Modal controls must not be covered by video. |

The numeric specification is authoritative. Generated raster mockups have small size/alignment differences; implementation must align navigation to the exact shared gutter rather than copying pixel drift. Use the individually imagegen-generated PNG icon family described in the linked icon guide, and retain native game previews. Do not implement sample media, profile, file or count text as hard-coded data. The profile mockup's dimmed backdrop is illustrative; retain the selected actual home underneath.

## 3. Home

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-home-pip-concepts/current-emulator.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-home-alive-concepts/03-pine-editorial-refined.png) |

**Change:** replace the isolated search field and empty body with the selected tagline/ring composition, restrained recent queries, and compact History/Saved/Downloads shortcuts. Retain incognito and overflow. Inset navigation to the content gutter and replace the collapsed player strip with floating video.

**States:** recent queries use real per-profile history and disappear in incognito or when empty. Now-playing title/motif and video appear only during relevant playback; no fake active waveform while idle. Preserve home layout when video closes.

## 4. Submitted search and watch

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-home-pip-concepts/current-results.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-search-player-flow/01-search-results.png) |

**Search change:** on submission, show a compact editable query toolbar at the top. Preserve query, filter and scroll state. Remove the home headline/shortcuts and duplicated bottom search dock. Show only supported filters and the current list/grid control. Back returns to home; editing the query opens suggestions/keyboard as appropriate.

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/current-watch-or-results.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-search-player-flow/02-watch-page.png) |

**Watch change:** search UI disappears completely. Prioritize the actual video, title, channel, Save/Background/Download and overflow. Group description and queue clearly. Existing optional metadata and settings remain accessible; data absent from the illustrative image is not an instruction to delete that capability.

**Collapse behavior:** pull down from the player card's handle/header or tap collapse. Animate the video toward the floating position and restore the exact prior browsing page, not always home. Fullscreen player gestures must not accidentally trigger this route transition.

![After swiping down: results and continuing floating playback](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-search-player-flow/03-results-floating-video.png)

**Acceptance:** same stream/queue/position, no restart, no second player/decoder, no bottom strip. Expand returns to watch. Close follows existing close/stop semantics. Preserve safe bounds during keyboard, rotation and navigation changes. OS PiP after leaving the app is a separate behavior from this in-app floating view.

## 5. Saved library — empty and populated

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/current-saved.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/07-saved-empty.png) |

**Current problem:** the Saved title is missing in the running UI; a plain New Playlist text action sits on an otherwise blank page.

**Change:** visible Saved toolbar, meaningful empty-state explanation, and an accessible Create playlist button using the existing creation flow. The button opens name entry with Create/Cancel; validate a trimmed nonempty name. Distinguish loading, empty and error states; a failed load must not look like an empty library.

![Saved library with real playlist rows — populated target](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/03-saved-playlists.png)

**Populated state:** visible New playlist action; consistent compact rows with abstract native playlist mark, name, actual video count and open affordance. Names may wrap/reflow at large text. Avoid fabricated cover art or cloud-sync features. Existing playlist management actions remain reachable.

**Source map:** SavedLibraryFragment, LibraryChrome, fragment_library_list.xml, item_library_header.xml, item_playlist_row.xml. The local adapter currently always shows its creation row and suppresses the empty view; restructure those states.

## 6. Playlist detail

Current populated detail was not captured because the observed Saved library was empty. The target is grounded in the local playlist implementation.

![Playlist detail target](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/06-playlist-detail.png)

**Change:** one compact title/count block, clear Play all and Background actions, and consistent media rows. Keep reorder grips and row overflow; preserve existing rename, remove, reorder and other supported playlist operations.

**States:** empty playlist gets add-videos guidance instead of disabled unexplained rows. Remove destructive operations only after the existing confirmation where required. Video overlay stays clear of drag grips and primary actions. Provide an accessible reorder route in addition to touch dragging.

**Source map:** LocalPlaylistFragment, local_playlist_header.xml, playlist_control.xml, list_stream_playlist_item.xml and associated adapters.

## 7. Profiles

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/current-profiles.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/05-profiles.png) |

**Current problem:** one profile occupies an oversized white dialog with unused grid area; selection and creation do not share the desired visual style.

**Change:** compact theme-matched sheet, Profiles title, small explanation, current-profile row with checkmark, and obvious New profile action. Use real names/avatar data and indicate selection independently of color. Multiple profiles form a scrollable compact list rather than stretching the modal.

**Behavior:** switching restores the correct profile's history, playback position and login state according to existing rules. New profile opens inline/name-entry UI; maintain existing delete availability/protection and confirmation. Do not invent extra users. Close/back/outside dismiss; background is inert. Suppress or reposition video visually behind the modal while preserving the actual playback session.

**Source map:** profile sheet/controller code, sheet_profiles.xml, item_profile_cell.xml, row_profile.xml.

## 8. History

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/current-history.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/04-history.png) |

**Current problem:** missing visible title, oversized privacy area, inconsistent media rows, and a distant Clear history action.

**Change:** visible History title; compact, correctly described incognito control; Watch history/Search history tabs; aligned readable rows; distinct secondary Clear history action with existing confirmation. Incognito helper copy must reflect whether it is enabled.

**States:** Watch history renders actual media records and existing resume behavior; Search history uses compact query rows that resubmit the chosen query. Empty watch/search histories have different explanations. Clear history affects the active tab and must retain the confirmation and record deletion rules. Preserve profile isolation and incognito behavior. Optional floating video never blocks destructive/privacy controls.

**Source map:** HistoryLibraryFragment, fragment_history_library.xml, VideoActions, HistoryRecordManager and LibraryChrome.

## 9. Games

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/current-games.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/02-games-hub.png) |

**Change:** four equal-height cards, consistent preview scale and subtitle wrapping, quieter header, shared inset navigation. Keep 2048/Snake/Sudoku/Make 24 models unchanged. Floating playback uses free space below cards rather than covering navigation.

**Individual games:** apply the same title/back/control spacing; preserve board inputs, pause/resume/save, per-game actions and accessible alternatives. Keep floating video out of active board/input bounds. Large text and narrow windows fall back to a single column. Game previews in raster images are illustrative; reuse GamePreview.

**Source map:** GamesFragment, GamePreview, GameScreens, GameBoards, GameModels, GameStateStore.

## 10. Breathe and Meditate

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/current-breathe.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/01-breathe-setup.png) |

**Current problem:** the 280dp visual and repeated technique label consume the page; Sound cues/Start are not visible in the captured initial viewport, and playback overlaps navigation.

**Change:** smaller setup ring, clearer technique/duration/sound group, and Start visible above the navigation on ordinary phone sizes. Proposed Breathe/Meditate selector exposes the two existing tools; no guided-audio service is added.

**Session states:** setup shows technique, 1/3/5 minute choice, cues, and Start. Active state prioritizes phase/timer with Pause and End. Paused state shows Resume; completion shows existing Again action. Meditation uses its quiet timer and applicable cues, hiding breath-technique selection. Honor existing lifecycle/pause behavior.

**Layout:** large text still scrolls; Start must remain reachable and never sit beneath navigation/player. Idle setup should not show a fake active countdown. Respect reduced motion. Coexistence of external playback and session cues needs explicit verification.

**Source map:** BreakSessionFragment, BreakController, BreathingOrbView, BreathingSession, BreathingSound.

## 11. Downloads

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/current-downloads.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/10-downloads-empty.png) |

**Empty-state change:** title/back retained; add a small download mark and concrete guidance to the existing watch-page Download action. Back to home is a proposed navigation affordance, not a new download capability.

![Downloads populated target](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/08-downloads.png)

**Populated change:** separate existing pending/finished groups. Show filenames, real status/size/progress, and accessible pause/resume/overflow. Error/retry and indeterminate progress remain meaningful. File deletion follows current confirmation rules. The source's download cards already provide a useful base; refine rather than replace mission logic.

**Creation form:** group filename, supported video/audio/subtitle choices and quality, retain destination/permission behavior and advanced options. No dedicated creation-form mockup was generated; this is a follow-on layout requirement.

**Source map:** download activity/dialog, activity_downloader.xml, download_dialog.xml, missions.xml, mission_item_linear.xml and MissionAdapter. Pending/finished example data is illustrative; current live evidence covered the empty state only.

## 12. Settings

| Current running app | Target design |
| --- | --- |
| ![Current screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/current-settings.png) | ![Proposed screen](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/09-settings.png) |

**Change:** compact grouped rows, consistent outline icons, clear hierarchy and chevrons. Retain settings search and all existing preference destinations. A settings-search icon is distinct from YouTube search and remains appropriate here.

**Behavior:** preserve preference keys, values, deep links, account flows and destructive backup/history operations. Categories below the screenshot remain scrollable; do not silently remove advanced choices. Apply theme and text scaling consistently to child settings pages.

**Source map:** main_settings.xml, settings layouts, MainSettingsFragment and the existing per-category fragments.

## 13. Implementation sequence

| Step | Deliverable | Completion check |
| --- | --- | --- |
| 0 | Locate/synchronize the source that produced the installed three-tab UI; capture baseline build/revision. | Same baseline screens reproduced from that source. |
| 1 | Shared semantic tokens, typography, gutters, icon treatment, list rows and header/sheet components. | Light/dark and large-text layouts reviewed; no missing page titles. |
| 2 | Top-level navigation and route state; remove unintended nav/search chrome from subpages. | Home, results, watch and every reviewed subpage show exactly their intended chrome. |
| 3 | Expanded-to-floating playback transition with safe-position handling. | Continuous video, one surface/session, restored results state, accessible controls and clear nav. |
| 4 | Saved empty/populated, playlist detail, profile sheet and History. | Real data works; create/switch/reorder/remove/clear behaviors preserved. |
| 5 | Games and Breathe/Meditate layouts and lifecycle states. | All game inputs and session controls remain usable with/without playback. |
| 6 | Downloads and Settings presentation. | Existing operations/preferences and accessibility remain intact. |
| 7 | Integrated UI and playback regression pass. | Acceptance matrix below passes on the matched source/build. |

## 14. Acceptance matrix

- Verify light/dark themes, a narrow phone, ordinary phone, tablet/wide layout, landscape and 200% text scaling.
- Equal horizontal margins on bottom navigation; no tab, Start, toggle, row menu or game input covered by video.
- Query editing, submit, keyboard open/close, no-results/error/loading states and back navigation preserve appropriate state.
- No YouTube search on watch; no duplicate video surface or bottom mini-player strip alongside floating video.
- Down-swipe/expand/pause/close, fullscreen, rotation, live/on-demand streams, process recreation and app-background behavior work without playback restart.
- Distinguish audio-only from video mode; retain meaningful audio controls without pretending a thumbnail is playing video.
- Saved empty/loading/error/populated, long playlist names, creation, reorder and existing removal behavior work.
- Profile switching does not leak data; current selection is announced; creation/deletion rules remain intact.
- History tabs, incognito, resume positions and confirmed clear actions retain existing semantics.
- All four game models preserve state; breathing/meditation setup, active, paused and completion states work with sound options.
- Downloads pending/paused/failed/finished and permission/destination errors remain actionable; preferences retain saved values.
- Screen-reader labels/focus, 48dp targets, contrast and reduced motion are checked. Generated thumbnails/metadata are replaced by actual app content.

## 15. Assets and provenance

All current screenshots are captured emulator evidence. Target mockups were generated using built-in imagegen and saved in this workspace. Generated UI text and geometry can vary; use this specification, the individual generated glyphs and native game/status components during implementation. The original design phase changed no Android code or user data. The subsequent implementation changes Android source; its runtime checks and temporary test-fixture cleanup are documented in IMPLEMENTATION_STATUS.md.

New prompts: /Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/prompts.json
Earlier selected home/flow prompts: the adjacent hush-home-alive-concepts and hush-search-player-flow folders.
Detailed capture/source notes: /Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-secondary-screen-review/README.md

## 16. Individual icon assets and exact page placement

Every icon below is generated in a separate built-in imagegen call, with a separate transparent PNG and prompt. Original output pixels are preserved. The [placement guide](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/ICON_PLACEMENT_GUIDE.md) expands these mappings beside every selected mockup. The [placement preview](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/icon-placement-preview.html) renders the actual assets at their intended size; it is a design reference, not an Android implementation.

### Page mapping

| Page | Region and individual assets | Placement / behavior |
| --- | --- | --- |
| Home | Header: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Brand at leading edge, privacy and overflow trailing. Profile switching remains reachable in overflow. |
| Home | Hero and current playback: [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [waveform](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png) | Ring is decorative at 44dp. Waveform at 32dp appears only for actual playback and is not a fake loading state. |
| Home | Search and recent queries: [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png) | 24dp field-leading search; 18dp recent-query history. No permanent bottom search dock. |
| Home | Library shortcuts: [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png) | Three equal History, Saved and Downloads actions, with 24dp glyphs and visible labels. |
| Home | Audio-only playback: [music](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png) | When actual mode is audio-only, use compact labelled controls above navigation, not a fake video frame. Never coexist with video PiP. |
| Submitted search | Editable query toolbar: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Back leading; clear query within field trailing; overflow outside field. Search stays editable after submission. |
| Submitted search | Display and filtering: [list](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png), [grid](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png), [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png) | List/grid toggle above results; filter only in supported filter menu. Selected display mode announced. |
| Submitted search | Result rows: [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [live](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png) | Overflow trailing each media row. Live icon appears only with verified live metadata. Duration is text. |
| Watch / expanded player | Player overlay: [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [fullscreen-exit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png) | Collapse upper-left, overflow upper-right, play OR pause center, fullscreen lower-right. Exit-fullscreen replaces enter only in fullscreen. |
| Watch / expanded player | Media actions: [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [bookmark-filled](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png) | Save OR saved, Background, Download and overflow below title/channel. Search and bottom navigation absent. |
| Watch / expanded player | Details and queue: [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Disclosure trailing each section; queue leading title; row overflow trailing. Expanded/collapsed state announced. |
| Watch / expanded player | Existing player menu: [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png), [previous](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/previous.png), [next](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/next.png), [shuffle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/shuffle.png), [repeat](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat.png), [repeat-one](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat-one.png), [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [captions](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png), [speed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png), [speaker](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png), [mute](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png), [globe](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.png), [cast](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/cast.png), [sleep-timer](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/sleep-timer.png), [comments](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments.png), [comments-off](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments-off.png), [brightness](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/brightness.png), [info](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png), [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png) | Retain existing menus and conditional supported actions. Do not put every menu icon on the primary watch toolbar or introduce unavailable services. |
| Results after player down-swipe | Restored query and results: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [list](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png), [grid](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png), [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png), [live](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png) | Reuse submitted-search components and preserve query, filters and scroll. Only display current mode glyph and supported items. |
| Saved / populated | Toolbar: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Saved title between back and overflow. |
| Saved / populated | Creation: [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png) | 24dp leading glyph in labelled New playlist action. |
| Saved / populated | Playlist rows: [playlist](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png), [chevron-right](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | 32dp mark in 48dp tonal tile leading; chevron trailing. Reuse one mark, actual names/counts, no artificial artwork variations. |
| Saved / populated | Existing management menu: [edit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png) | Rename and remove remain in existing actions; share only where supported by the current playlist implementation. |
| Playlist detail | Toolbar and identity: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [playlist](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png) | Back leading, overflow trailing; same playlist mark at 48dp in header. |
| Playlist detail | Playback: [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png) | Leading glyphs inside labelled Play all and Background actions. |
| Playlist detail | Rows: [drag](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Reorder at start and overflow at end; 48dp targets. Accessible move-up/down alternative. |
| Playlist detail | Existing playlist actions: [edit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png), [shuffle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/shuffle.png) | Only expose existing supported operations. Popup playback uses pip, not the expand glyph. |
| Profiles sheet | Sheet header: [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Close at upper-right in 48dp target. Handle is a native rounded line, not an icon asset. |
| Profiles sheet | Profile rows: [profile](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png), [check](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png) | Default avatar leading only when no actual avatar/initials; check trailing current profile. Initials remain real text. |
| Profiles sheet | Creation and deletion: [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png) | Add leading New profile row; delete only in allowed existing profile management flow with confirmation. |
| History | Toolbar: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Visible History title and existing overflow. |
| History | Privacy: [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png) | 24dp leading privacy mark; native switch trailing, accurate enabled/disabled helper text. |
| History | Watch and search history: [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png) | Watch-row overflow trailing; search-query rows use history leading. Empty-state mark uses history at 48dp. |
| History | Clear history: [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png) | 24dp leading destructive glyph beside Clear history. Confirm and clear only the active tab under existing rules. |
| Games hub | Game choices:  | Four native GamePreview boards are illustrations/data, not navigation icons. Keep all four actual models. |
| Games hub | Individual game controls: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [info](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Use only the controls implemented for each game. Back and help in toolbar; pause/resume/restart in labelled action area; board stays clear of PiP. |
| Games hub | Game-specific tools: [undo](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png), [erase](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png), [hint](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png), [notes](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png), [swap](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png) | 2048 Undo/New game; Sudoku pause/resume, notes, erase, hint, New puzzle; Make24 swap/undo/hint and text-only Skip/Next round. Snake retains gesture/accessibility steering without adding buttons. |
| Breathe / Meditate | Technique identity: [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [chevron-right](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | 32dp decorative technique mark leading the row; 24dp disclosure trailing. Phase ring/timer remains a native animation. |
| Breathe / Meditate | Session states: [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [stop](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png) | Active Pause/End; paused Resume; completed Again. Setup Start breathing remains a labelled text action. |
| Breathe / Meditate | Sound state: [speaker](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png), [mute](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png) | Use in supported sound/volume control presentation; do not replace the native Sound cues switch with an icon-only control. |
| Saved / empty | Toolbar: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Same Saved toolbar as populated state. |
| Saved / empty | Empty state and creation: [playlist](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png), [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png) | 48dp decorative empty mark, 24dp leading Create playlist glyph. Loading/error must not render this empty state. |
| Saved / empty | Load error: [error](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png) | Separate readable error text and labelled Retry action. |
| Downloads / populated | Toolbar: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Title/back/overflow; no top-level bottom navigation. |
| Downloads / populated | File identity: [file-video](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png), [file-audio](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png), [file-subtitle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png) | 32dp glyph in 48dp tonal tile, based on actual media type. |
| Downloads / populated | Mission controls: [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [check](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png) | Pending pause; paused resume; completed check. Each mission shows real status/progress. |
| Downloads / populated | Failure and existing menu: [error](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [folder](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [info](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Failures show readable reason and Retry where possible; menu actions depend on mission support and permissions. |
| Settings | Toolbar: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png) | Settings title; settings search at trailing edge. This search is not YouTube search. |
| Settings | Playback group: [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [gestures](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/gestures.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [chevron-right](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | Player, Gestures, Download leading glyphs; row chevrons trailing. |
| Settings | Library group: [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [feed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/feed.png), [backup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/backup.png), [chevron-right](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | History/cache, Feed, Backup; preserve preference destinations. |
| Settings | Appearance, Account, Advanced: [appearance](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/appearance.png), [profile](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png), [globe](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.png), [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png), [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png), [chevron-right](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | Appearance; Account; Content via globe; Content filter; Advanced. Match existing hierarchy and retain all preferences. |
| Settings | Child settings actions: [notifications](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notifications.png), [folder](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [info](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Only existing notification/destination/clear/explanatory controls; no new preference values. |
| Downloads / empty | Toolbar: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Same Downloads toolbar. |
| Downloads / empty | Empty state: [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png) | 48dp decorative download mark. Surrounding progress ring is a native static decoration, not actual progress. |
| Downloads / empty | Creation form: [folder](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png), [file-video](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png), [file-audio](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png), [file-subtitle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png), [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [error](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png) | Retain supported media/quality/destination controls and permission validation. Form is specified; no dedicated raster mockup yet. |
| Home / Games / Breathe | Bottom navigation: [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png) | Three equal targets, 22dp outer margins, selected capsule and label. |

### Complete asset inventory

| # | Individual generated icon | Usage | Exact prompt |
| --- | --- | --- | --- |
| 1 | [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png) | Home brand; Breathe navigation | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.prompt.txt) |
| 2 | [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png) | Home field/nav; results; Settings search | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.prompt.txt) |
| 3 | [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png) | Games navigation | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.prompt.txt) |
| 4 | [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png) | Home privacy; History privacy | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.prompt.txt) |
| 5 | [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Toolbars and media-row overflow | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.prompt.txt) |
| 6 | [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png) | Watch action overflow | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.prompt.txt) |
| 7 | [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png) | Recent queries; History shortcut; History/cache setting | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.prompt.txt) |
| 8 | [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png) | Saved shortcut; watch Save | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.prompt.txt) |
| 9 | [bookmark-filled](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png) | Watch already-saved state | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.prompt.txt) |
| 10 | [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png) | Home Downloads; watch Download; downloads empty; settings | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.prompt.txt) |
| 11 | [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png) | Subpage toolbars; search results | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.prompt.txt) |
| 12 | [chevron-right](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | Playlist rows; technique row; Settings rows | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.prompt.txt) |
| 13 | [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png) | Watch collapse; Description and Queue disclosure | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.prompt.txt) |
| 14 | [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Clear query; close video; dismiss profile sheet | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.prompt.txt) |
| 15 | [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png) | Video paused; Play all; download resume | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.prompt.txt) |
| 16 | [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png) | Playing video; download pause; active session pause | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.prompt.txt) |
| 17 | [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png) | Floating-video expand; fullscreen entry | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.prompt.txt) |
| 18 | [fullscreen-exit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png) | Fullscreen exit | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.prompt.txt) |
| 19 | [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png) | Background audio; player settings | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.prompt.txt) |
| 20 | [playlist](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png) | Saved rows and empty state; playlist detail header | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.prompt.txt) |
| 21 | [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png) | New playlist; New profile; add to playlist | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.prompt.txt) |
| 22 | [check](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png) | Current profile; completed download; selected options | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.prompt.txt) |
| 23 | [profile](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png) | Default avatar; Account; profile entry | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.prompt.txt) |
| 24 | [drag](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.png) | Playlist reorder grip | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.prompt.txt) |
| 25 | [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png) | Clear history; remove/delete confirmation | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.prompt.txt) |
| 26 | [edit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png) | Rename playlist; rename profile where supported | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.prompt.txt) |
| 27 | [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png) | Retry load/download; restart game/session | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.prompt.txt) |
| 28 | [speaker](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png) | Enabled sound cues; volume | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.prompt.txt) |
| 29 | [mute](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png) | Muted video; sound cues disabled | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.prompt.txt) |
| 30 | [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png) | Player/settings menu; Advanced settings | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.prompt.txt) |
| 31 | [gestures](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/gestures.png) | Gesture settings | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/gestures.prompt.txt) |
| 32 | [feed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/feed.png) | Feed settings | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/feed.prompt.txt) |
| 33 | [backup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/backup.png) | Backup settings | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/backup.prompt.txt) |
| 34 | [appearance](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/appearance.png) | Appearance settings | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/appearance.prompt.txt) |
| 35 | [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png) | Content-filter settings; supported search filters | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.prompt.txt) |
| 36 | [list](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png) | Results list view | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.prompt.txt) |
| 37 | [grid](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png) | Results grid view | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.prompt.txt) |
| 38 | [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png) | Existing watch/player Share menu | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.prompt.txt) |
| 39 | [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png) | Watch Queue; player queue | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.prompt.txt) |
| 40 | [shuffle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/shuffle.png) | Player queue shuffle | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/shuffle.prompt.txt) |
| 41 | [repeat](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat.png) | Repeat all; repeat off uses state treatment | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat.prompt.txt) |
| 42 | [repeat-one](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat-one.png) | Repeat one state | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat-one.prompt.txt) |
| 43 | [stop](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.png) | End breathing/meditation; supported stop actions | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.prompt.txt) |
| 44 | [info](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Description/info; supported explanatory menus | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.prompt.txt) |
| 45 | [error](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png) | Load/download failure | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.prompt.txt) |
| 46 | [folder](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png) | Download destination; open supported file directory | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.prompt.txt) |
| 47 | [file-video](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png) | Video download file | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.prompt.txt) |
| 48 | [file-audio](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png) | Audio download file | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.prompt.txt) |
| 49 | [file-subtitle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png) | Subtitle download file | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.prompt.txt) |
| 50 | [live](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png) | Live result badge only for live streams | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.prompt.txt) |
| 51 | [waveform](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png) | Home actual now-playing ornament | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.prompt.txt) |
| 52 | [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png) | Home decorative motif; Breathe technique mark | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.prompt.txt) |
| 53 | [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png) | Existing popup/PiP menu action | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.prompt.txt) |
| 54 | [previous](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/previous.png) | Existing previous track control | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/previous.prompt.txt) |
| 55 | [next](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/next.png) | Existing next track control | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/next.prompt.txt) |
| 56 | [captions](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png) | Player subtitles/captions | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.prompt.txt) |
| 57 | [speed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png) | Player playback-speed menu | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.prompt.txt) |
| 58 | [globe](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.png) | Open in browser; Content/language settings | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.prompt.txt) |
| 59 | [cast](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/cast.png) | Existing external-player/cast action | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/cast.prompt.txt) |
| 60 | [sleep-timer](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/sleep-timer.png) | Existing sleep-timer menu | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/sleep-timer.prompt.txt) |
| 61 | [comments](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments.png) | Existing comments/bullet-comments enabled | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments.prompt.txt) |
| 62 | [comments-off](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments-off.png) | Existing bullet-comments disabled | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments-off.prompt.txt) |
| 63 | [brightness](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/brightness.png) | Existing player brightness gesture indicator | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/brightness.prompt.txt) |
| 64 | [notifications](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notifications.png) | Existing Feed/notification settings subpage | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notifications.prompt.txt) |
| 65 | [undo](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png) | 2048 and Make24 Undo | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.prompt.txt) |
| 66 | [erase](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png) | Sudoku Erase | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.prompt.txt) |
| 67 | [hint](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png) | Sudoku and Make24 Hint | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.prompt.txt) |
| 68 | [notes](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png) | Sudoku pencil-notes mode | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.prompt.txt) |
| 69 | [swap](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png) | Make24 swap selected operands | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.prompt.txt) |
| 70 | [music](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png) | Existing audio-only compact playback identity | [Prompt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.prompt.txt) |

### Integration rules

- Use a shared 24dp glyph slot with 48dp touch targets, 18dp secondary glyphs and 28dp primary play/pause glyphs. Normalize transparent bounds uniformly without stretching or editing the master PNG.
- Tint alpha masks through semantic colors; show selected navigation with its capsule and label. Support light/dark, disabled, pressed and destructive states without generating new colored copies.
- Replace icon-bearing controls in matched Android source with a central bitmap resolver and cached decoding. Keep actual game boards, media thumbnails, text, avatars and OS-owned status symbols native/data-driven.
- Back/forward directional glyphs mirror in RTL; media playback and brand glyphs retain their meaning. Decorative motifs are excluded from accessibility focus. Action glyphs have meaningful labels and state announcements.
- Watch has no search bar. Collapse/down-swipe restores the previous route and starts in-app floating playback without restarting the session. The existing external popup/system PiP action stays distinct from this in-app transition.
- Clear history, playlist removal, profile deletion and download cancellation retain their current confirmations and supported behavior. Icon replacement does not authorize adding or removing operations.
- Compare all generated glyphs at actual size before integration. Raster mockups may show slightly different drawn symbols; the individual PNG family plus the placement guide is the final asset reference.

## Delivery verification

- 70/70 distinct icon PNGs and 70 exact individual prompt files are present. All masters have a real alpha channel with fully transparent background pixels and visible ink. SHA-256 and optical bounds are recorded in the manifest.
- All 70 IDs are mapped to screen regions; all 14 selected mockup pages loaded successfully in the browser preview with no pending glyphs. All linked local files/images in the plan and guide exist.
- Reviewed the gallery at 24/32px and home controls with the actual PNGs. Refined History and Incognito individually; their earlier outputs/prompts are preserved under `superseded/`.
- This validates the design package, not Android behavior. Matching-build diagnosis and the runtime acceptance matrix remain implementation tasks. Individual game boards, active breathing and child settings still need their runtime review.

![Verified home mockup and actual generated icon placements](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/verified-home-placement.jpg)

[Full placement and gallery proof](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/verified-full-preview.jpg)
