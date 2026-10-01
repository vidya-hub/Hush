# Hush tablet design plan

Date: 30 September 2026. Scope: tablet mockups and design documentation. No Android source changes were made in this phase.

Implementation was subsequently authorized. See [the Android implementation and emulator validation report](TABLET_IMPLEMENTATION.md) for the later coding phase.

34 selected screens were generated individually with the built-in imagegen tool. Landscape Home, portrait Home and landscape Watch established the reference style. Every later screen used one of these or a derived screen as a visual reference. Exact original and revision prompts are retained beside the images. The selected PNGs, hashes, dimensions and prompt history are recorded in the manifest.

All video scenes, search queries, uploader names, profile names, counts, timestamps, progress, game positions and metadata in generated images are **illustrative mockup content**. Native keyboards and status bars are illustrative Android UI. Generated pixels are visual references; the numeric dimensions, behavior and exact existing icon mappings below govern implementation. Use actual app data and localized strings. Do not hardcode these examples or recreate raster screenshots as views.

[Open the gallery](assets/hush-tablet-mockups/gallery.html) · [Image manifest](assets/hush-tablet-mockups/manifest.json) · [Existing 70-icon guide](ICON_PLACEMENT_GUIDE.md)

## Current app and headless tablet evidence

The app was inspected on a **headless Pixel Tablet AVD**, Hush_Tablet_API35_Headless, Android API 35, arm64, 2560×1600 physical pixels at 320dpi: a 1280×800dp reference window before system insets. The running emulator uses -no-window, -no-audio, -gpu swiftshader and 4096MB RAM. Existing phone/other emulator sessions were left untouched.

The original API 37 / 16KB tablet AVD failed cold boot: its 2G memory value was interpreted as 256MB; an explicit memory allocation and HVF enabled boot, but Android then repeatedly crashed in SurfaceFlinger/goldfish graphics. A separate API 35 AVD was created without wiping the original. API 35 booted successfully and produced the baseline captures.

| Capture configuration | Outcome | Evidence |
| --- | --- | --- |
| Landscape 1280×800dp | All 4 navigation/capture tests passed. Real-stream playback test failed before PiP assertions because YouTube playback did not start. | current-emulator/landscape/capture.log |
| Portrait 800×1280dp | All 4 navigation/capture tests passed. | current-emulator/portrait/capture.log |
| Portrait 200% font scale | 1 navigation/capture test passed. | current-emulator/portrait-200-percent/capture.log |
| Narrow 600×900dp | 1 navigation/capture test passed. | current-emulator/narrow-600dp/capture.log |

64 landscape screenshots, 64 portrait screenshots, 9 large-text screenshots and 9 narrow screenshots are saved under the gallery's current-emulator folder. The narrow captures use wm size to simulate an app-window size; they do **not** establish real OS multiwindow behavior. History in the fresh AVD is empty, so populated History and download mission states remain design scenarios. The temporary playlist test supplied and removed QA records. Tablet real-stream PiP transitions are **not runtime-verified** by this capture run.

Portrait and narrow captures use physical sizes 1600×2560 and 1200×1800 at density 320. Font scale was restored to 1.0 and wm size reset to the tablet default after captures. The headless tablet is left running.

### Code observations and concrete changes

| Area | Current source / observed tablet UI | Target |
| --- | --- | --- |
| Navigation | HushChrome builds a horizontal bottom navigation at every width. The landscape baseline shows a very wide bar. | Window-aware left rail in eligible landscape windows; centered bounded bottom bar otherwise. Preserve three destinations. |
| Home | SearchFragment and HushUi cap content at 1120dp, but the home search and shortcuts still span most of the tablet. | Bounded two-column landscape home; headline max 480dp and search group max 560dp. Portrait centered single column. |
| Search | SearchFragment hides the hero after submission and keeps the query editable; result width/grid already respond to measured width. | Preserve this behavior, normalize thumbnail proportions and readable card widths, reserve PiP clearance. |
| Playback | HushChrome already hosts a floating video surface; default bounds use 200dp width. Expanded player lifecycle is existing. | Tablet-sized single video, optional separate queue pane; collapse restores browsing context, no search on expanded Watch, no bottom strip for video. |
| Profiles | Current tablet dialog sits near the bottom, partly overlapping underlying navigation. | Centered bounded modal on wide windows; keyboard-aware bounds, independent body scrolling and aligned rows. |
| Library | LocalPlaylistFragment and list holders provide existing playlists, actions and reorder behavior. | Expanded master/detail selection and track pane; portrait stack, actions and grips stay accessible. |
| History | StatisticsPlaylistFragment shows privacy and history tabs; the large-text baseline demonstrates substantial header height. | Scroll privacy/header content with records when space is constrained; readable stacked rows at 200%. |
| Games | GameScreens already supplies native boards, state and adaptive tools. Snake currently uses board gestures and no visible tools pane. | Bounded boards, compact side controls; Snake side Resume/New game are alternate affordances for the existing tap/hold actions, not new mechanics. Preserve board gestures and accessibility actions. |
| Break | Existing breathing/session/orb code provides patterns, durations, sounds and pause/resume/end. | Separate visual and controls on wide windows; stack naturally and protect primary controls in narrow windows. |
| Downloads | Existing mission list supplies progress, failures, pause/resume/retry/completed actions. | Keep each mission's status and available action together, with readable bounded rows. |
| Settings | Baseline category list navigates into preferences as separate screens. | Expanded category/preferences panes; compact navigation remains a stack. No new preference keys or capability changes. |

Source touchpoints for a later implementation: HushChrome, FloatingBounds, HushUi, CompactPlaybackBar, SearchFragment, GameScreens/GameBoards/GameStateStore, StatisticsPlaylistFragment, LocalPlaylistFragment and local holders, ProfileStore and its dialog host, download mission adapter, SettingsActivity and preference fragments. This document does not authorize code changes during the mockup phase.

## Window and layout contract

Measure the **available app window in dp**, after horizontal system-bar and cutout insets. A portrait device in a wide window and a landscape device in split-screen must use the same rules as any other window with those dimensions. Physical device type alone never selects a layout.

| Condition | Navigation | Main layout |
| --- | --- | --- |
| Width ≥840dp and width > height | 96dp left rail; 16dp outer inset; 24dp gap to content | Expanded landscape panes; 32dp content gutters |
| Width ≥840dp and width ≤ height | Centered bottom bar on top-level pages | Portrait flow, panes only if their minimum widths fit |
| 600–839dp | Centered bottom bar on top-level pages | Single column / two media columns when readable; 24dp gutters |
| Below 600dp | Inset bottom bar on top-level pages | Compact single column; 22dp gutters, wrap/stack controls |
| Expanded Watch or fullscreen, any size | Hidden | Player-focused layout |
| Submitted search or a subpage without rail | Hidden bottom bar; explicit Back | Browsing stack |
| IME visible, any window | Hide bottom bar; retain wide rail if it does not interfere | Resize content to IME, reveal focused field |
| Modal visible | Underlying navigation inert beneath scrim | Modal owns focus; floating video hidden |

Reference windows: landscape 1280×800dp; portrait 800×1280dp; narrow 600×900dp. Test width boundaries 599/600 and 839/840dp, also 1024×600, 1280×600 and 900×1280. A wide portrait window may retain panes if minimum widths fit; navigation remains a bottom bar by the explicit rail rule. At large text, switch pane content to a stack when minimum readable widths cannot be maintained.

The bottom bar width is min(560dp, availableWidth − 2×gutter). Center it horizontally; both margins equal (availableWidth − barWidth)/2. Three equal segments, minimum 48dp touch targets, normal height 72dp with natural growth at large text. Place 12dp above bottom safe inset. Search, Games and Breathe are the only destinations. Use search, games and hush-mark assets, labels below, selected tonal container plus state semantics. There is no separate Home destination. Rail has the same labels and selection, 72dp-or-larger destinations with 16dp vertical gaps. The decorative rail brand is not a fourth destination.

Content panes use a 24dp gap. Home content maximum width 1120dp; landscape hero max480dp, search/library group max560dp. Portrait Home max560dp centered. Library selector 280dp; Settings categories 260dp; preference text max640dp. Pane minima: selector240dp + detail440dp; Watch video560dp + related280dp. Include rail, gaps and gutters in fit calculations, then stack when they do not fit. Do not shrink text to maintain two panes.

Use native typography: body16sp/24sp line height, supporting14sp/20, action16sp/20, page title28sp/36, Home tagline40sp/48 normal or32sp/40 in narrow/short keyboard windows. Scale sp with user font settings; wrap and grow containers. At ≥150% font scale use single-column media and stacked metadata where needed; at200% use the History composition shown. Never apply fixed-height containers to wrapping labels. All interactive targets ≥48×48dp with separate hit rectangles; game cells retain board behavior and expose native accessible actions.

Card corners16dp, field/button corners14–20dp, navigation24–28dp. Component padding16–24dp; section gaps24dp. Leading edges of header, body and actions align to the same gutter. Thumbnail images are always16:9 (actual content letterboxed as needed), square playlist covers/avatars are explicit exceptions. Native switches share a trailing edge; toggles may move below long labels. Reorder grips and row menus each receive48dp targets. Decorative rings are not loading indicators.

Dark: pine #101D17, surface #1C2B22, primary text #EDF3EC, secondary #B3C3B6, sage #C4D8CB, button ink #111B17, error #F18B82. Light: warm ivory #F6F3EB, surface #EDEAE2, ink #1C2922, selected sage #DDEBE0. Use semantic app theme attributes. Confirm text contrast in implementation; selected/failed/disabled states also have labels and shapes. Reduced motion removes ornamental pulsing and transition animation; breathing phase/timer remains understandable through text.

## Navigation and playback contract

Search opens Home at the top level. History, Saved and Downloads open from Home shortcuts/overflow. Games and Breathe retain their top-level selections in both rail and bottom bar. Wide browsing subpages retain the Search rail; compact subpages use Back, without adding a bottom bar. Header menus appear once; per-row menus are separate controls.

After search submission, the same query remains visible and editable in a compact toolbar with Back, clear, supported filter/overflow and the existing list/grid options. Hide the Home hero/shortcuts. Keyboard reopening does not reset results or playback; keep prior results behind suggestions and restore their scroll when editing ends. Back first dismisses IME, then returns from results to Home with its previous query/history state. Clear edits the field; it does not clear History.

Expanded Watch hides search, rail and bottom navigation. At sufficient width, left video/metadata and right related/queue panes scroll independently; video stays16:9. Up next / Queue selection and queue order survive resizing. Portrait uses one video followed by metadata/actions/tabs; the media surface remains visible while the user scrolls the body, with native player controls. Fullscreen is immersive with16:9 letterboxing inside the window and controls over the single video. Back exits fullscreen to Watch; Back/downward-collapse from Watch restores browsing. Exit-fullscreen glyph differs from expand-to-Watch.

Drag downward from the player/card drag region or activate chevron-down to collapse Watch into **one in-app floating video**. Preserve the same player/decoder, current time, play/pause state, queue, caption settings and browsing page/query/selection/scroll. Never instantiate a second stream or retain a hidden duplicate video surface. Tapping Expand restores Watch; Close ends that playback session and removes its surface. Play/Pause only changes playback state. In-app floating playback is distinct from Android OS PiP and the existing external Popup permission path.

Normal tablet floating width300dp; narrow width240dp; below600dp use min(208dp, availableWidth − 2×gutter). Height = width×9/16; shrink only as needed to fit the safe rectangle. Expand upper-start, Close upper-end, Play/Pause center; each48dp with nonoverlapping targets and contrast scrims. Clip media to16dp corners. Initial anchor lower-end, inset by the content gutter, 12dp above navigation/gesture-safe bottom. User dragging snaps to a safe corner after release. Preserve the anchor through rotation; recompute bounds on every resize/inset/IME change and clamp, not absolute old pixels.

Keep PiP outside game boards/keypads, breathing Pause/End/Start controls, row menus and fields. Reserve bottom clearance of PiP height+24dp on affected browsing panes, so items can scroll fully above it. Prefer the empty right margin or a reserved lower corner when panes permit. With IME open, move it above the keyboard only if all targets remain reachable; otherwise hide its video presentation temporarily while retaining the existing session state. Modal scrims hide floating presentation. Restore it after dismissal without duplicating playback. If the window cannot fit a48dp-safe video, show a bounded audio control presentation until space returns.

Audio-only Home uses a compact in-flow card after search, recent queries and shortcuts: actual artwork, title, Audio only, Play/Pause, Queue, Close. It never coexists with floating video. Long titles wrap two lines; controls wrap below metadata below600dp/large text. No fullscreen glyph in audio-only mode. Existing background audio may continue while navigating.

## Per-area pane and state behavior

**Home:** Idle shows no fake recommendation feed or now-playing. Recent chips show actual local queries and wrap; hide them in incognito if current history policy requires it. Library shortcuts remain equal where all labels fit; stack as full-width labeled rows at200%/very narrow widths. On short landscape/IME windows align Home content toward the top and let hero+body scroll together rather than vertically centering content out of reach.

**Search:** Expanded grids use min card width240dp, max360dp, up to3columns at1280dp with rail. Portrait800dp uses2columns. At200% text or insufficient width use1column. Titles wrap2lines normally,3or natural height at large text; menu occupies its own trailing hit area. Duration/live badges have contrast containers. Retain native filter options; do not invent recommendation categories. Empty successful results and network errors are different states.

**Saved/playlists:** Expanded selector and contents each scroll independently; selected playlist visible/announced. Compact opens detail as a stack and Back returns to prior selector scroll. New playlist modal uses one name field, validation and Create/Cancel. Play all, Background and Popup map to existing playback modes; unavailable permission paths show explanatory handling. Reorder uses existing semantics with an accessible move alternative; cancel returns original order. Long playlist headings wrap; action row wraps below heading when needed. Removal confirmation names the playlist/item, distinguishes removal from deleting downloaded media and uses existing data rules.

**Profiles:** Modal width min(520dp, window−2gutters), height at most availableHeight−48dp including IME-safe space. Header and footer stay reachable; body scrolls independently. Avatars/monograms64dp; rows72dpmin with name wrapping and current-selection check in separate trailing area. Personal/default protected profile keeps its existing deletion restriction; show Delete disabled with reason rather than deleting it. Create trims input, rejects blank/duplicate names according to ProfileStore, preserves typed text on validation. Create/Cancel/Close and IME Done are aligned. Back first closes IME, then modal. Switch only through existing profile behavior, retaining per-profile data; no login/email/sync added. Delete non-default profile uses a named confirmation, Cancel safe default, and existing fallback profile selection.

**History:** Local profile recording explanation and Incognito switch are always readable. Enabling incognito does not delete prior records; show them with Existing history is still available. Watch/Search tabs use native localized labels. At constrained height/large text the privacy header and tabs are part of the scrolling content, with only the small page toolbar remaining. Record rows stack thumbnail then title/metadata at200%. Clear menu opens existing scoped confirmation; no accidental one-tap clear. Search row replay submits the query; its remove control has an explicit label.

**Games:** Hub order2048,Snake,Sudoku,Make24. Boards max480dp square, Snake max520dp, Sudoku min legible-cell size with native accessibility support; no endless stretching. When board+tools do not fit, put controls below inside one vertical scroll. Keep board gestures intact and exclude PiP from their input area.2048 shows Score/Best/Undo/New game and disables Undo when unavailable. Snake retains swipe-to-steer, tap pause/resume, paused hold restart and accessibility directions; side Resume/New game only invoke those existing state actions. Paused overlay title and hint have16dp gap. Sudoku keeps Easy/Medium, timer, Pause/Resume,1–9keypad, Pencil/Erase/Hint/New puzzle; disable editing while paused. Make24 keeps operand selection, operation order, Swap, Undo, Hint, Skip, Next round; disable Swap until two operands selected and Next round according to current rules. Do not treat an intermediate24 as success until all numbers are consumed. Rotation/resizing restores saved board/session, never starts a new round. Game-over/solved messages remain native and expose existing restart/next actions.

**Breathe/Meditate:** Wide orb/timer max400dp with360dpcontrol pane; portrait stack max560dp. Pattern and duration controls only before session. Retain all existing patterns even though Calm4in6out is illustrated. Start fixes pattern/duration; active/paused states disable mode switching. Sound cues remain changeable if existing behavior allows. Pause freezes session/phase, Resume continues, End returns to setup according to existing state handling. Completion says Session complete, exposes Done and Start again; no wellness statistics. IME/rotation does not reset or duplicate timers. Preserve monotonic elapsed time and lifecycle pause rules; motion-off uses phase text/countdown.

**Downloads:** Mission rows show actual media type, progress, speed/status and matching action together. Wide status/action columns align; compact status/progress moves below title and action stays in its48dp slot. Paused differs from failure; Retry only when meaningful. Completed actions use existing file/open semantics. Long filenames wrap. Download format/destination/permission dialog remains existing capability, bounded520dp, scrollable. Android permission/directory picker remains native; returning cancellation preserves the current page and does not create a fake mission.

**Settings:** Expanded categories260dp with independent scroll; selected preferences pane max readable text640dp, actual current values and existing keys. Compact category tap pushes preference page. Switching panes preserves category and scroll. Global settings search belongs to Settings, not Watch. Keep current subcategories including Feed/notifications, content filters, YouTube account and player options; image30 is representative, not a reduction of the settings tree. Backup uses local data behavior, not implied cloud sync. Dialog lists/switches have natural height, summaries wrap and value columns stack at large text.

## Supporting state coverage matrix

These states supplement the34 primary images; do not infer new capabilities from sample labels.

| State | Applies to / reference | Required presentation and transition |
| --- | --- | --- |
| Initial loading | Search05/06, Watch09/10, library13, History18 | Native progress within content; accessible loading label; keep header/back. Decorative Home ring is never progress. |
| Pagination loading | Search05/06, History18 | Footer progress; existing content and scroll stay stable. |
| Load/network error | Search05/08, Watch09/10 | Readable message + Retry using error/retry icons; query/context retained; empty success is not an error. |
| Retry pending/repeated failure | Same | Disable duplicate requests, native progress, return actionable message if retry fails. |
| Long title/uploader/query | Search06, playlist15, Downloads28 | Wrap within bounded text; no overlap with menus; query scrolls horizontally during editing; no forced shrunken font. |
| No recent queries | Home01/02 | Omit chip row, close gap naturally; no sample queries. |
| Video buffering | Watch09/10/12, PiP03/05/11 | Progress over ONE surface; retain Close/Back; controls reflect state. |
| Video paused/ended | Same | Play glyph replaces Pause; Replay/next only if supported; preserve queue/context. |
| Live stream | Search05, Watch09 | Live label from metadata; hide fabricated duration; seek/DVR affordances reflect real stream. |
| Captions/quality/speed unavailable | Watch09/12 | Hide unsupported menu item or disable with reason; no invented options. |
| Audio-only transition | Home04/Watch09 | Remove video surface once; use compact controls, keep player/queue/time. |
| Empty/removed queue | Watch09 | Readable empty queue; selection preserved; related results independent. |
| OS PiP/Popup permission denied | Playback11/library13 | Existing permission rationale/settings route; no duplicate surface; stay on prior page after cancel. |
| Playlist creation / blank/duplicate | Library13/14/15 | Name field + validation + Create/Cancel; preserve text and focus. |
| Playlist rename |15 | Bounded existing rename dialog with current name, wraps and saves by existing rules. |
| Playlist removal/item removal |15 | Named scoped confirmation; Cancel default; refresh selection/empty state after accepted removal. |
| Reorder start/commit/cancel |15 | Visible grip, accessible move actions, stable scroll, retained order across resize. |
| No profiles beyond default |16 | One current row; New profile; protected Delete disabled/reason. |
| Profile deletion |16 | Named confirmation; close IME first, body scroll, fallback selection; no cloud/account implication. |
| Invalid profile name |17 | Inline localized validation above keyboard; Create unavailable until valid. |
| History empty |18/19 | Distinct Watch/Search guidance; Incognito explanation remains accessible. |
| History clear |18/19 | Existing scoped confirmation and storage policy; incognito toggle does not clear records. |
| Game loading/restoring |20–24 | Restore real board state; neutral short progress only if needed, no random fake scores. |
| Game paused/solved/game over |21–24 | Existing state labels and valid controls; no clipped board/overlay text. |
| Undo/Hint/Swap unavailable |21/23/24 | Disabled native semantics, state-specific explanation, no fake interaction. |
| Breathing paused/resumed |26/27 | Frozen/current timer and phase; no reset on Resume or resize. |
| Breathing completion |25–27 | Session complete, Done/Start again, no fabricated health stats. |
| Background/lifecycle interruption | Games/Breathe/Playback | Apply existing pause rules; persist state once, avoid duplicate timers/players. |
| Download permission/destination cancel |28/29 | Native permission/picker; concise rationale and existing alternate action; keep page state. |
| Download retry/failure/disk full |28 | Local row message and appropriate Retry/Change destination action only if supported. |
| Download unavailable format/deleted file |28/29 | Disable relevant action with reason; do not show completed play for nonexistent file. |
| Settings unavailable values |30 | Current capability-based visibility, readable reason, retain existing defaults. |
| Rotation/resizing | All | Preserve query, selection, scroll anchor, modal input, player and game/session state; recompute panes/insets. |
| Keyboard open/closed |07/17/31 | Focus above IME, bottomnav suppressed, modal grows/scrolls, restore context after dismissal. |
| Large text/RTL/focus/reduced motion |32/all | Natural heights, start/end mirroring,48dp targets, visible focus, accessible labels and selected states. |
| Short windows / bottom insets | All | Content scrolls; nav/primary actions remain reachable; PiP clamps to safe bounds. |

## Icon authority and placements

Retain all70 individually imagegen-generated PNG masters and exact prompts in assets/hush-icon-system. No new tablet icon identities are needed. The gallery displays actual assets beside the corresponding screens and inventories all70. Raster-generated mockup glyphs are approximations; the linked PNG asset is the implementation authority.

Use HushIcons and its measured alpha bounds for uniform optical fit.24dp optical action glyph inside48dp hit target;18dp inline glyph;28dp Breathe mark;32dp library/file glyph;48dp empty-state decoration. Tint by theme at runtime, preserving original alpha; do not stretch full PNG canvases or use their incidental whitespace for alignment. Status bars, switches, seekbars, timers, letters/numbers and game boards remain native.

Selection uses tonal container and selected semantics. Play/Pause, Save/Saved, Mute/Speaker, Repeat/Repeat-one, Captions and fullscreen state choose the existing matching asset and accessible label. Profiles use initials or actual avatar with profile fallback; check marks indicate current profile. Hide decorative ring/artwork from accessibility. Keep localized action labels;48dp controls include the entire labeled button/row.

Screen families: Home/navigation—hush-mark/search/games/incognito/more-vertical/history/bookmark/download/breath-ring; search—back/close/content-filter/list/grid/live/more-vertical; Watch—chevron-down/play/pause/expand/fullscreen-exit/bookmark/bookmark-filled/headphones/download/more-horizontal/queue/captions/speed/settings; playlists—playlist/add/edit/trash/drag/play/headphones/pip; profiles—profile/check/add/trash/close; History—history/incognito/back/trash/more-vertical; games—games/undo/retry/play/pause/notes/erase/hint/swap; break—hush-mark/breath-ring/speaker/mute/play/pause/stop; downloads—file-video/file-audio/file-subtitle/download/pause/play/retry/error/check/folder/more-vertical; Settings—headphones/gestures/download/history/feed/backup/appearance/profile/globe/settings/search/chevron-right.

All remaining player/settings icons keep their existing placements in the70-icon guide, including external player/cast, comments states, previous/next, shuffle/repeat, sleep timer, brightness and notification preferences. Their absence from a primary composition does not remove the capability. The complete inventory below records every identity.

## Implementation handoff and acceptance

This is a design handoff. Android implementation is a later phase. Apply changes in this order: responsive shell and shared insets; Home/query flow; single-player Watch/PiP transitions; library/profile/history panes; games and break layout; downloads/settings; accessibility and regression QA. Preserve current data operations, preference keys and player lifecycle.

Acceptance for implementation: all34 references represented; widths599/600/839/840and reference windows verified; Home and Watch light/dark; keyboard and actual OS split-screen; rotation while editing/playing/session active;200% text and RTL; focus/screen-reader labels; one video surface; no search on Watch; PiP does not cover game/primary controls; no clipped labels/duplicated menus; all70icons resolve with tint/optical bounds. Real-stream playback, failure/retry, populated History/downloads and actual multiwindow checks remain required runtime QA, not established by mockups.

The34 selected images were inspected individually for text, labels, controls, alignment and unsupported features. Targeted revisions corrected aspect ratio, Breathe mark, missing Background/Popup, bottom strip instead of PiP, query toolbar duplication, PiP metadata overlap, portrait thumbnails, profile keyboard letters, local-history wording, Sudoku clues/action name, Make24 Swap/Next round and clipped Settings category. Earlier variants and their exact prompts are retained for audit, excluded from the selected gallery.

## Primary mockup coverage and selected images


[Headless emulator runbook](assets/hush-tablet-mockups/HEADLESS_EMULATOR.md) · [Artifact verification](assets/hush-tablet-mockups/verification.json)

<!-- GENERATED COVERAGE -->
| # | Selected screen | Window | Scenario | Final prompt |
| --- | --- | --- | --- | --- |
| 01 | [Home · landscape idle](assets/hush-tablet-mockups/01-home-landscape.png) | landscape | standard | [01-home-landscape-v2.prompt.txt](assets/hush-tablet-mockups/01-home-landscape-v2.prompt.txt) |
| 02 | [Home · portrait idle](assets/hush-tablet-mockups/02-home-portrait.png) | portrait | standard | [02-home-portrait-v2.prompt.txt](assets/hush-tablet-mockups/02-home-portrait-v2.prompt.txt) |
| 03 | [Home · floating video](assets/hush-tablet-mockups/03-home-floating.png) | landscape | floating-video | [03-home-floating.prompt.txt](assets/hush-tablet-mockups/03-home-floating.prompt.txt) |
| 04 | [Home · audio only](assets/hush-tablet-mockups/04-home-audio.png) | landscape | audio-only | [04-home-audio-v2.prompt.txt](assets/hush-tablet-mockups/04-home-audio-v2.prompt.txt) |
| 05 | [Search · landscape results + floating video](assets/hush-tablet-mockups/05-search-landscape-floating.png) | landscape | floating-video | [05-search-landscape-floating-v3.prompt.txt](assets/hush-tablet-mockups/05-search-landscape-floating-v3.prompt.txt) |
| 06 | [Search · portrait results](assets/hush-tablet-mockups/06-search-portrait.png) | portrait | standard | [06-search-portrait-v3.prompt.txt](assets/hush-tablet-mockups/06-search-portrait-v3.prompt.txt) |
| 07 | [Search · keyboard editing](assets/hush-tablet-mockups/07-search-keyboard.png) | compact | keyboard | [07-search-keyboard.prompt.txt](assets/hush-tablet-mockups/07-search-keyboard.prompt.txt) |
| 08 | [Search · no results](assets/hush-tablet-mockups/08-search-empty.png) | landscape | empty | [08-search-empty.prompt.txt](assets/hush-tablet-mockups/08-search-empty.prompt.txt) |
| 09 | [Watch · landscape panes](assets/hush-tablet-mockups/09-watch-landscape.png) | landscape | standard | [09-watch-landscape-v2.prompt.txt](assets/hush-tablet-mockups/09-watch-landscape-v2.prompt.txt) |
| 10 | [Watch · portrait](assets/hush-tablet-mockups/10-watch-portrait.png) | portrait | standard | [10-watch-portrait.prompt.txt](assets/hush-tablet-mockups/10-watch-portrait.prompt.txt) |
| 11 | [Playback · collapsed over Saved](assets/hush-tablet-mockups/11-playback-collapsed.png) | landscape | floating-video | [11-playback-collapsed.prompt.txt](assets/hush-tablet-mockups/11-playback-collapsed.prompt.txt) |
| 12 | [Watch · fullscreen](assets/hush-tablet-mockups/12-watch-fullscreen.png) | landscape | standard | [12-watch-fullscreen.prompt.txt](assets/hush-tablet-mockups/12-watch-fullscreen.prompt.txt) |
| 13 | [Saved · populated master/detail](assets/hush-tablet-mockups/13-saved-populated.png) | landscape | standard | [13-saved-populated-v2.prompt.txt](assets/hush-tablet-mockups/13-saved-populated-v2.prompt.txt) |
| 14 | [Saved · empty](assets/hush-tablet-mockups/14-saved-empty.png) | portrait | empty | [14-saved-empty.prompt.txt](assets/hush-tablet-mockups/14-saved-empty.prompt.txt) |
| 15 | [Playlist · long title and reorder](assets/hush-tablet-mockups/15-playlist-detail.png) | landscape | standard | [15-playlist-detail.prompt.txt](assets/hush-tablet-mockups/15-playlist-detail.prompt.txt) |
| 16 | [Profiles · switching](assets/hush-tablet-mockups/16-profile-switch.png) | landscape | standard | [16-profile-switch.prompt.txt](assets/hush-tablet-mockups/16-profile-switch.prompt.txt) |
| 17 | [Profiles · creation with keyboard](assets/hush-tablet-mockups/17-profile-create.png) | portrait | keyboard | [17-profile-create-v2.prompt.txt](assets/hush-tablet-mockups/17-profile-create-v2.prompt.txt) |
| 18 | [History · landscape watch history](assets/hush-tablet-mockups/18-history-watch.png) | landscape | standard | [18-history-watch-v2.prompt.txt](assets/hush-tablet-mockups/18-history-watch-v2.prompt.txt) |
| 19 | [History · portrait incognito search history](assets/hush-tablet-mockups/19-history-search-private.png) | portrait | standard | [19-history-search-private.prompt.txt](assets/hush-tablet-mockups/19-history-search-private.prompt.txt) |
| 20 | [Games · hub](assets/hush-tablet-mockups/20-games-hub.png) | landscape | standard | [20-games-hub-v2.prompt.txt](assets/hush-tablet-mockups/20-games-hub-v2.prompt.txt) |
| 21 | [2048 · board and tools](assets/hush-tablet-mockups/21-game-2048.png) | landscape | standard | [21-game-2048.prompt.txt](assets/hush-tablet-mockups/21-game-2048.prompt.txt) |
| 22 | [Snake · paused](assets/hush-tablet-mockups/22-game-snake.png) | landscape | standard | [22-game-snake.prompt.txt](assets/hush-tablet-mockups/22-game-snake.prompt.txt) |
| 23 | [Sudoku · board and keypad](assets/hush-tablet-mockups/23-game-sudoku.png) | landscape | standard | [23-game-sudoku-v2.prompt.txt](assets/hush-tablet-mockups/23-game-sudoku-v2.prompt.txt) |
| 24 | [Make 24 · selected operands](assets/hush-tablet-mockups/24-game-make24.png) | landscape | standard | [24-game-make24-v2.prompt.txt](assets/hush-tablet-mockups/24-game-make24-v2.prompt.txt) |
| 25 | [Breathe · setup](assets/hush-tablet-mockups/25-breathe-setup.png) | landscape | standard | [25-breathe-setup.prompt.txt](assets/hush-tablet-mockups/25-breathe-setup.prompt.txt) |
| 26 | [Breathe · active portrait](assets/hush-tablet-mockups/26-breathe-active.png) | portrait | standard | [26-breathe-active.prompt.txt](assets/hush-tablet-mockups/26-breathe-active.prompt.txt) |
| 27 | [Meditate · paused](assets/hush-tablet-mockups/27-meditate-paused.png) | landscape | standard | [27-meditate-paused.prompt.txt](assets/hush-tablet-mockups/27-meditate-paused.prompt.txt) |
| 28 | [Downloads · mixed mission states](assets/hush-tablet-mockups/28-downloads-populated.png) | landscape | standard | [28-downloads-populated.prompt.txt](assets/hush-tablet-mockups/28-downloads-populated.prompt.txt) |
| 29 | [Downloads · empty](assets/hush-tablet-mockups/29-downloads-empty.png) | portrait | empty | [29-downloads-empty.prompt.txt](assets/hush-tablet-mockups/29-downloads-empty.prompt.txt) |
| 30 | [Settings · categories and preferences](assets/hush-tablet-mockups/30-settings-master-detail.png) | landscape | standard | [30-settings-master-detail-v2.prompt.txt](assets/hush-tablet-mockups/30-settings-master-detail-v2.prompt.txt) |
| 31 | [Home · narrow window with keyboard](assets/hush-tablet-mockups/31-split-home-keyboard.png) | compact | keyboard | [31-split-home-keyboard.prompt.txt](assets/hush-tablet-mockups/31-split-home-keyboard.prompt.txt) |
| 32 | [History · 200% text](assets/hush-tablet-mockups/32-history-large-text.png) | portrait | large-text | [32-history-large-text.prompt.txt](assets/hush-tablet-mockups/32-history-large-text.prompt.txt) |
| 33 | [Home · landscape light](assets/hush-tablet-mockups/33-home-landscape-light.png) | landscape | light-theme | [33-home-landscape-light.prompt.txt](assets/hush-tablet-mockups/33-home-landscape-light.prompt.txt) |
| 34 | [Watch · landscape light](assets/hush-tablet-mockups/34-watch-landscape-light.png) | landscape | light-theme | [34-watch-landscape-light.prompt.txt](assets/hush-tablet-mockups/34-watch-landscape-light.prompt.txt) |

### Home

Bounded headline/search columns replace the current stretched tablet flow; compact equal-margin bottom navigation in portrait.

#### Home · landscape idle

![Home · landscape idle — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/01-home-landscape.png)

Anchor revised to16:10 and lowercase h Breathe glyph. Compact search/library group; no fake feed.

Exact prompt: [01-home-landscape-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/01-home-landscape-v2.prompt.txt). Prompt history: [01-home-landscape.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/01-home-landscape.prompt.txt), [01-home-landscape-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/01-home-landscape-v2.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [music](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [waveform](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png).

#### Home · portrait idle

![Home · portrait idle — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/02-home-portrait.png)

Bottom bar revised into one centered bounded group with equal margins and three destinations.

Exact prompt: [02-home-portrait-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/02-home-portrait-v2.prompt.txt). Prompt history: [02-home-portrait.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/02-home-portrait.prompt.txt), [02-home-portrait-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/02-home-portrait-v2.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [music](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [waveform](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png).

#### Home · floating video

![Home · floating video — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/03-home-floating.png)

Single warm study video floats over Home; navigation/search remain clear. Implementation width300dp governs raster approximation.

Exact prompt: [03-home-floating.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/03-home-floating.prompt.txt). Prompt history: [03-home-floating.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/03-home-floating.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [music](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [waveform](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png).

#### Home · audio only

![Home · audio only — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/04-home-audio.png)

Audio card reordered after recent queries and shortcuts; no video/fullscreen affordance.

Exact prompt: [04-home-audio-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/04-home-audio-v2.prompt.txt). Prompt history: [04-home-audio.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/04-home-audio.prompt.txt), [04-home-audio-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/04-home-audio-v2.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [music](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [waveform](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png).


### Search

Preserve existing editable submitted query; normalize card proportions, readable grids and one safe floating surface.

#### Search · landscape results + floating video

![Search · landscape results + floating video — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/05-search-landscape-floating.png)

Wide player strip replaced by true16:9 PiP; duplicate header removed; metadata clearance revised.

Exact prompt: [05-search-landscape-floating-v3.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/05-search-landscape-floating-v3.prompt.txt). Prompt history: [05-search-landscape-floating.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/05-search-landscape-floating.prompt.txt), [05-search-landscape-floating-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/05-search-landscape-floating-v2.prompt.txt), [05-search-landscape-floating-v3.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/05-search-landscape-floating-v3.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [list](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png), [grid](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png), [live](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png).

#### Search · portrait results

![Search · portrait results — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/06-search-portrait.png)

Portrait grid rebuilt with landscape16:9 thumbnails, complete toolbar and row menus.

Exact prompt: [06-search-portrait-v3.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/06-search-portrait-v3.prompt.txt). Prompt history: [06-search-portrait.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/06-search-portrait.prompt.txt), [06-search-portrait-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/06-search-portrait-v2.prompt.txt), [06-search-portrait-v3.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/06-search-portrait-v3.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [list](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png), [grid](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png), [live](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png).

#### Search · keyboard editing

![Search · keyboard editing — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/07-search-keyboard.png)

Focused query/suggestions remain above illustrative native QWERTY keyboard; bottomnav hidden.

Exact prompt: [07-search-keyboard.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/07-search-keyboard.prompt.txt). Prompt history: [07-search-keyboard.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/07-search-keyboard.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [list](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png), [grid](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png), [live](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png).

#### Search · no results

![Search · no results — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/08-search-empty.png)

Successful no-results state with editable query and Edit search; no failure banner.

Exact prompt: [08-search-empty.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/08-search-empty.prompt.txt). Prompt history: [08-search-empty.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/08-search-empty.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [list](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png), [grid](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png), [live](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png).


### Playback

Preserve existing single player and browsing context; introduce tablet panes and responsive floating bounds; hide search on Watch.

#### Watch · landscape panes

![Watch · landscape panes — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/09-watch-landscape.png)

Background restored alongside Save/Download/More; one video, related/queue pane, no search/nav.

Exact prompt: [09-watch-landscape-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/09-watch-landscape-v2.prompt.txt). Prompt history: [09-watch-landscape.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/09-watch-landscape.prompt.txt), [09-watch-landscape-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/09-watch-landscape-v2.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [fullscreen-exit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [bookmark-filled](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [captions](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png), [speed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png), [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png), [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png).

#### Watch · portrait

![Watch · portrait — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/10-watch-portrait.png)

One portrait video then metadata/actions and Up next/Queue/Info; no landscape panes squeezed into portrait.

Exact prompt: [10-watch-portrait.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/10-watch-portrait.prompt.txt). Prompt history: [10-watch-portrait.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/10-watch-portrait.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [fullscreen-exit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [bookmark-filled](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [captions](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png), [speed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png), [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png), [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png).

#### Playback · collapsed over Saved

![Playback · collapsed over Saved — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/11-playback-collapsed.png)

Saved context retained beneath one floating video. Exact overlay control positions follow playback contract.

Exact prompt: [11-playback-collapsed.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/11-playback-collapsed.prompt.txt). Prompt history: [11-playback-collapsed.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/11-playback-collapsed.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [fullscreen-exit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [bookmark-filled](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [captions](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png), [speed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png), [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png), [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png).

#### Watch · fullscreen

![Watch · fullscreen — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/12-watch-fullscreen.png)

Immersive16:9 video letterboxed in16:10 with overlay controls; no browsing chrome.

Exact prompt: [12-watch-fullscreen.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/12-watch-fullscreen.prompt.txt). Prompt history: [12-watch-fullscreen.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/12-watch-fullscreen.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [fullscreen-exit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [bookmark-filled](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [captions](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png), [speed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png), [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png), [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png).


### Library

Replace separate wide library browsing with selector/content panes; preserve local playlists, playback modes and reorder operations.

#### Saved · populated master/detail

![Saved · populated master/detail — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/13-saved-populated.png)

Popup restored; master/detail list and four accessible reorder rows.

Exact prompt: [13-saved-populated-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/13-saved-populated-v2.prompt.txt). Prompt history: [13-saved-populated.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/13-saved-populated.prompt.txt), [13-saved-populated-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/13-saved-populated-v2.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [playlist](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png), [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [edit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [drag](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png).

#### Saved · empty

![Saved · empty — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/14-saved-empty.png)

Empty library guidance and Create playlist; no illustrative playlists presented as actual data.

Exact prompt: [14-saved-empty.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/14-saved-empty.prompt.txt). Prompt history: [14-saved-empty.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/14-saved-empty.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [playlist](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png), [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [edit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [drag](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png).

#### Playlist · long title and reorder

![Playlist · long title and reorder — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/15-playlist-detail.png)

Long playlist title wraps; all playback modes and reorder grips retained.

Exact prompt: [15-playlist-detail.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/15-playlist-detail.prompt.txt). Prompt history: [15-playlist-detail.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/15-playlist-detail.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [playlist](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png), [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [edit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [drag](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png).


### Profiles

Replace bottom-anchored tablet dialog with bounded centered/IME-aware modal, aligned avatars and explicit selection.

#### Profiles · switching

![Profiles · switching — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/16-profile-switch.png)

Centered profile modal with aligned monograms, current check and protected Delete state.

Exact prompt: [16-profile-switch.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/16-profile-switch.prompt.txt). Prompt history: [16-profile-switch.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/16-profile-switch.prompt.txt).

Actual icon references: [profile](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png), [check](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png), [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png).

#### Profiles · creation with keyboard

![Profiles · creation with keyboard — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/17-profile-create.png)

Creation controls remain above IME; keyboard letter order corrected individually.

Exact prompt: [17-profile-create-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/17-profile-create-v2.prompt.txt). Prompt history: [17-profile-create.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/17-profile-create.prompt.txt), [17-profile-create-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/17-profile-create-v2.prompt.txt).

Actual icon references: [profile](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png), [check](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png), [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png).


### History

Keep local profile privacy controls; reduce constrained-height header footprint and reflow media at large text.

#### History · landscape watch history

![History · landscape watch history — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/18-history-watch.png)

Privacy copy corrected from account to local profile; readable watch rows and scoped overflow.

Exact prompt: [18-history-watch-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/18-history-watch-v2.prompt.txt). Prompt history: [18-history-watch.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/18-history-watch.prompt.txt), [18-history-watch-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/18-history-watch-v2.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png).

#### History · portrait incognito search history

![History · portrait incognito search history — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/19-history-search-private.png)

Incognito on stops new records without deleting existing search history.

Exact prompt: [19-history-search-private.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/19-history-search-private.prompt.txt). Prompt history: [19-history-search-private.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/19-history-search-private.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png).


### Games

Keep native boards and existing game models; bound boards and place matching controls beside them where space permits.

#### Games · hub

![Games · hub — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/20-games-hub.png)

Four native game previews; invalid repeated Sudoku clues corrected to sparse valid clues.

Exact prompt: [20-games-hub-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/20-games-hub-v2.prompt.txt). Prompt history: [20-games-hub.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/20-games-hub.prompt.txt), [20-games-hub-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/20-games-hub-v2.prompt.txt).

Actual icon references: [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [undo](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png), [notes](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png), [erase](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png), [hint](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png), [swap](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png).

#### 2048 · board and tools

![2048 · board and tools — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/21-game-2048.png)

Bounded4x4 board and existing Score/Best/Undo/New game.

Exact prompt: [21-game-2048.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/21-game-2048.prompt.txt). Prompt history: [21-game-2048.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/21-game-2048.prompt.txt).

Actual icon references: [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [undo](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png), [notes](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png), [erase](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png), [hint](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png), [swap](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png).

#### Snake · paused

![Snake · paused — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/22-game-snake.png)

Paused overlay has distinct title/hint; side actions reuse existing board resume/restart gestures.

Exact prompt: [22-game-snake.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/22-game-snake.prompt.txt). Prompt history: [22-game-snake.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/22-game-snake.prompt.txt).

Actual icon references: [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [undo](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png), [notes](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png), [erase](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png), [hint](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png), [swap](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png).

#### Sudoku · board and keypad

![Sudoku · board and keypad — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/23-game-sudoku.png)

Exactly9x9 sparse valid grid and1–9 keypad; primary label corrected to New puzzle.

Exact prompt: [23-game-sudoku-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/23-game-sudoku-v2.prompt.txt). Prompt history: [23-game-sudoku.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/23-game-sudoku.prompt.txt), [23-game-sudoku-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/23-game-sudoku-v2.prompt.txt).

Actual icon references: [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [undo](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png), [notes](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png), [erase](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png), [hint](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png), [swap](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png).

#### Make 24 · selected operands

![Make 24 · selected operands — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/24-game-make24.png)

Swap restored and primary corrected to Next round; partial operand selection avoids false24 success.

Exact prompt: [24-game-make24-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/24-game-make24-v2.prompt.txt). Prompt history: [24-game-make24.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/24-game-make24.prompt.txt), [24-game-make24-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/24-game-make24-v2.prompt.txt).

Actual icon references: [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [undo](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png), [notes](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png), [erase](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png), [hint](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png), [swap](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png).


### Break

Keep existing patterns/session state; use balanced visual/control panes and accessible portrait session actions.

#### Breathe · setup

![Breathe · setup — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/25-breathe-setup.png)

Setup visual/control panes, current pattern/duration/sound controls.

Exact prompt: [25-breathe-setup.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/25-breathe-setup.prompt.txt). Prompt history: [25-breathe-setup.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/25-breathe-setup.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [speaker](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png), [mute](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [stop](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.png).

#### Breathe · active portrait

![Breathe · active portrait — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/26-breathe-active.png)

Active phase/countdown and reachable Pause/End above equal-margin bottomnav.

Exact prompt: [26-breathe-active.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/26-breathe-active.prompt.txt). Prompt history: [26-breathe-active.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/26-breathe-active.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [speaker](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png), [mute](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [stop](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.png).

#### Meditate · paused

![Meditate · paused — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/27-meditate-paused.png)

Paused meditation timer, locked duration/modes, Resume/End.

Exact prompt: [27-meditate-paused.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/27-meditate-paused.prompt.txt). Prompt history: [27-meditate-paused.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/27-meditate-paused.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [speaker](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png), [mute](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [stop](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.png).


### Downloads

Keep real mission operations; bring progress/status/action together and wrap rows at narrow width.

#### Downloads · mixed mission states

![Downloads · mixed mission states — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/28-downloads-populated.png)

Downloading/Paused/Failed/Completed are separate labeled states, each with matching available action.

Exact prompt: [28-downloads-populated.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/28-downloads-populated.prompt.txt). Prompt history: [28-downloads-populated.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/28-downloads-populated.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [file-video](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png), [file-audio](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png), [file-subtitle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [error](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png), [check](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png), [folder](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png).

#### Downloads · empty

![Downloads · empty — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/29-downloads-empty.png)

Bounded empty guidance pointing to existing Watch Download action.

Exact prompt: [29-downloads-empty.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/29-downloads-empty.prompt.txt). Prompt history: [29-downloads-empty.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/29-downloads-empty.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [file-video](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png), [file-audio](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png), [file-subtitle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png), [error](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png), [check](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png), [folder](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png).


### Settings

Replace sequential category/preference screens with expanded selection/detail panes, keeping current preference keys.

#### Settings · categories and preferences

![Settings · categories and preferences — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/30-settings-master-detail.png)

Clipped Advanced category corrected; all categories visible; local Backup glyph, aligned preferences.

Exact prompt: [30-settings-master-detail-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/30-settings-master-detail-v2.prompt.txt). Prompt history: [30-settings-master-detail.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/30-settings-master-detail.prompt.txt), [30-settings-master-detail-v2.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/30-settings-master-detail-v2.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [gestures](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/gestures.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [feed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/feed.png), [backup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/backup.png), [appearance](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/appearance.png), [profile](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png), [globe](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.png), [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png).


### Responsive

Use measured window width and natural text growth; resize for IME, hide bottom navigation and keep scroll/focus reachable.

#### Home · narrow window with keyboard

![Home · narrow window with keyboard — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/31-split-home-keyboard.png)

600dp window reflow and illustrative IME; query remains visible, bar hidden, scrolling explicit.

Exact prompt: [31-split-home-keyboard.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/31-split-home-keyboard.prompt.txt). Prompt history: [31-split-home-keyboard.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/31-split-home-keyboard.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png).

#### History · 200% text

![History · 200% text — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/32-history-large-text.png)

200% text reflow with scrollable header/tabs and stacked media; next thumbnail indicates scrolling.

Exact prompt: [32-history-large-text.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/32-history-large-text.prompt.txt). Prompt history: [32-history-large-text.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/32-history-large-text.prompt.txt).

Actual icon references: [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png), [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png).


### Light

Preserve selected Home/Watch structures exactly; use semantic warm light colors and actual alpha-tinted icons.

#### Home · landscape light

![Home · landscape light — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/33-home-landscape-light.png)

Selected Home structure retained with warm light colors.

Exact prompt: [33-home-landscape-light.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/33-home-landscape-light.prompt.txt). Prompt history: [33-home-landscape-light.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/33-home-landscape-light.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png).

#### Watch · landscape light

![Watch · landscape light — illustrative target design](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/34-watch-landscape-light.png)

Selected Watch structure retained with light surfaces and single full-color video.

Exact prompt: [34-watch-landscape-light.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/34-watch-landscape-light.prompt.txt). Prompt history: [34-watch-landscape-light.prompt.txt](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/34-watch-landscape-light.prompt.txt).

Actual icon references: [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png), [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png), [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png), [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png), [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png), [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png), [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png), [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png), [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png), [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png), [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png), [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png), [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png), [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png), [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png).

## Current emulator comparison images

These are actual app captures, not generated target designs. All stored screenshots remain available in the gallery.

### Current · Landscape Home

![Current emulator Landscape Home](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/current-emulator/landscape/dark-home.png)


### Current · Landscape profiles

![Current emulator Landscape profiles](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/current-emulator/landscape/dark-profiles.png)


### Current · Landscape Saved

![Current emulator Landscape Saved](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/current-emulator/landscape/saved-populated-qa.png)


### Current · Landscape Snake

![Current emulator Landscape Snake](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/current-emulator/landscape/game-snake.png)


### Current · Landscape Settings

![Current emulator Landscape Settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/current-emulator/landscape/settings.png)


### Current · Portrait Home

![Current emulator Portrait Home](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/current-emulator/portrait/dark-home.png)


### Current · History at200%text

![Current emulator History at200%text](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/current-emulator/portrait-200-percent/history.png)


### Current · Narrow Home

![Current emulator Narrow Home](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-tablet-mockups/current-emulator/narrow-600dp/home.png)

## Complete retained icon inventory

All70 identities retain their existing usage. The per-screen references above identify tablet families; use the existing guide for capability-specific menus.

| # | Asset | Existing placement |
| --- | --- | --- |
| 1 | [hush-mark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hush-mark.png) | Home brand; Breathe navigation |
| 2 | [search](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/search.png) | Home field/nav; results; Settings search |
| 3 | [games](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/games.png) | Games navigation |
| 4 | [incognito](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/incognito.png) | Home privacy; History privacy |
| 5 | [more-vertical](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-vertical.png) | Toolbars and media-row overflow |
| 6 | [more-horizontal](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/more-horizontal.png) | Watch action overflow |
| 7 | [history](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/history.png) | Recent queries; History shortcut; History/cache setting |
| 8 | [bookmark](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark.png) | Saved shortcut; watch Save |
| 9 | [bookmark-filled](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/bookmark-filled.png) | Watch already-saved state |
| 10 | [download](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/download.png) | Home Downloads; watch Download; downloads empty; settings |
| 11 | [back](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/back.png) | Subpage toolbars; search results |
| 12 | [chevron-right](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-right.png) | Playlist rows; technique row; Settings rows |
| 13 | [chevron-down](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/chevron-down.png) | Watch collapse; Description and Queue disclosure |
| 14 | [close](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/close.png) | Clear query; close video; dismiss profile sheet |
| 15 | [play](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/play.png) | Video paused; Play all; download resume |
| 16 | [pause](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pause.png) | Playing video; download pause; active session pause |
| 17 | [expand](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/expand.png) | Floating-video expand; fullscreen entry |
| 18 | [fullscreen-exit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/fullscreen-exit.png) | Fullscreen exit |
| 19 | [headphones](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/headphones.png) | Background audio; player settings |
| 20 | [playlist](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/playlist.png) | Saved rows and empty state; playlist detail header |
| 21 | [add](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/add.png) | New playlist; New profile; add to playlist |
| 22 | [check](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/check.png) | Current profile; completed download; selected options |
| 23 | [profile](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/profile.png) | Default avatar; Account; profile entry |
| 24 | [drag](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/drag.png) | Playlist reorder grip |
| 25 | [trash](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/trash.png) | Clear history; remove/delete confirmation |
| 26 | [edit](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/edit.png) | Rename playlist; rename profile where supported |
| 27 | [retry](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/retry.png) | Retry load/download; restart game/session |
| 28 | [speaker](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speaker.png) | Enabled sound cues; volume |
| 29 | [mute](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/mute.png) | Muted video; sound cues disabled |
| 30 | [settings](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/settings.png) | Player/settings menu; Advanced settings |
| 31 | [gestures](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/gestures.png) | Gesture settings |
| 32 | [feed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/feed.png) | Feed settings |
| 33 | [backup](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/backup.png) | Backup settings |
| 34 | [appearance](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/appearance.png) | Appearance settings |
| 35 | [content-filter](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/content-filter.png) | Content-filter settings; supported search filters |
| 36 | [list](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/list.png) | Results list view |
| 37 | [grid](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/grid.png) | Results grid view |
| 38 | [share](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/share.png) | Existing watch/player Share menu |
| 39 | [queue](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/queue.png) | Watch Queue; player queue |
| 40 | [shuffle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/shuffle.png) | Player queue shuffle |
| 41 | [repeat](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat.png) | Repeat all; repeat off uses state treatment |
| 42 | [repeat-one](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/repeat-one.png) | Repeat one state |
| 43 | [stop](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/stop.png) | End breathing/meditation; supported stop actions |
| 44 | [info](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/info.png) | Description/info; supported explanatory menus |
| 45 | [error](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/error.png) | Load/download failure |
| 46 | [folder](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/folder.png) | Download destination; open supported file directory |
| 47 | [file-video](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-video.png) | Video download file |
| 48 | [file-audio](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-audio.png) | Audio download file |
| 49 | [file-subtitle](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/file-subtitle.png) | Subtitle download file |
| 50 | [live](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/live.png) | Live result badge only for live streams |
| 51 | [waveform](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/waveform.png) | Home actual now-playing ornament |
| 52 | [breath-ring](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/breath-ring.png) | Home decorative motif; Breathe technique mark |
| 53 | [pip](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/pip.png) | Existing popup/PiP menu action |
| 54 | [previous](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/previous.png) | Existing previous track control |
| 55 | [next](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/next.png) | Existing next track control |
| 56 | [captions](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/captions.png) | Player subtitles/captions |
| 57 | [speed](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/speed.png) | Player playback-speed menu |
| 58 | [globe](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/globe.png) | Open in browser; Content/language settings |
| 59 | [cast](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/cast.png) | Existing external-player/cast action |
| 60 | [sleep-timer](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/sleep-timer.png) | Existing sleep-timer menu |
| 61 | [comments](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments.png) | Existing comments/bullet-comments enabled |
| 62 | [comments-off](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/comments-off.png) | Existing bullet-comments disabled |
| 63 | [brightness](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/brightness.png) | Existing player brightness gesture indicator |
| 64 | [notifications](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notifications.png) | Existing Feed/notification settings subpage |
| 65 | [undo](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/undo.png) | 2048 and Make24 Undo |
| 66 | [erase](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/erase.png) | Sudoku Erase |
| 67 | [hint](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/hint.png) | Sudoku and Make24 Hint |
| 68 | [notes](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/notes.png) | Sudoku pencil-notes mode |
| 69 | [swap](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/swap.png) | Make24 swap selected operands |
| 70 | [music](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/assets/hush-icon-system/music.png) | Existing audio-only compact playback identity |
