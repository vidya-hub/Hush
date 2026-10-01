# Hush secondary-screen design review

Reviewed the running Android emulator and the checked-out client source on 2026-09-30. This is a design audit and mockup set, not an implementation change. Built-in imagegen generated the proposals; exact prompts are in `prompts.json`.

## Findings, in priority order

| Priority | Screen / concern | Evidence | Recommended change |
| --- | --- | --- | --- |
| High | Player over navigation | Captured Games, Breathe, History, Saved and Profiles all retain a full-width collapsed player over the navigation area. | Apply the already selected floating-video direction consistently. Reserve safe bounds above navigation. Use one player surface. Separate audio-only controls from live video. |
| High | Breathe setup | `current-breathe.png`: large orb and duplicate technique labels occupy most of the viewport; Sound cues and Start breathing are not visible in this capture. Source has a 280dp visual plus scrolling option controls. | Reduce the setup visual, group technique/duration/sound, keep Start visible above nav; active-session view may then give the ring more space. Retain scrolling at larger font sizes. |
| High | Saved empty state | `current-saved.png`: back arrow, no visible Saved title, only New Playlist text and an otherwise empty page. Source adapter always renders a creation row and hides the empty label. | Restore page title, provide a clear empty-state explanation and an actual Create playlist button using the existing creation action. Keep a consistent compact list when populated. |
| High | History orientation | `current-history.png`: no visible History header title; incognito controls dominate the upper page; watch/search tabs and video rows work but Clear history sits far below them. | Restore History title, compact the incognito summary, keep tabs/readable video rows, and place Clear history in a clearly labelled secondary action/overflow with its existing confirmation. Do not change actual privacy behavior. |
| Medium | Games hub | `current-games.png`: Make 24 has a taller card because its subtitle wraps; previews/title sizes and row heights vary; large empty lower region. | Equal card heights, consistent preview size, smaller secondary labels, accurate wrapping, shared header and navigation. Empty space can host floating playback without covering cards. No fake scores or streaks. |
| Medium | Individual games | Code inspection only: `GameScreens` supports four separate models/controllers and mixed game-specific controls. Live individual boards were not opened in this pass. | Audit each board at small window/large text; shared toolbar and control spacing, game-specific board preserved. Never let PiP obscure game input regions. Preserve save/pause/resume and existing accessible actions. |
| Medium | Profile switcher | `current-profiles.png`: functional selected profile card, but a single item sits in an oversized white dialog, with weak new-profile affordance. | Theme-matched compact sheet/dialog, obvious selection, comfortable New profile action; keep create/delete rules and long-name accessibility. Profile deletion must remain explicit. |
| Medium | Downloads | `current-downloads.png`: title and back are visible; empty state is only one sentence. Populated state not observed. Download form is source-inspected only. | A modest empty-state icon and brief guidance to the existing watch-page Download action. For populated items, readable file/status/progress and clear pause/retry/overflow; retain supported operations. Group filename/media type/quality in the creation form. |
| Lower | Settings | `current-settings.png`: titles/category groups/search work. Dense inherited icon vocabulary and repeated Appearance/Account labels diverge from selected visual style. | Harmonize icon strokes, spacing, titles and grouping without moving or removing preferences casually. Preserve settings search and capability labels. |

## Mockups

- [01-breathe-setup.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/01-breathe-setup.png): Compact setup ring, technique/duration/sound, visible Start.
- [02-games-hub.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/02-games-hub.png): Consistent card heights and previews, inset navigation and safe floating video.
- [03-saved-playlists.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/03-saved-playlists.png): Populated Saved with readable playlist rows and creation action.
- [04-history.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/04-history.png): Clear title, compact privacy summary, watch/search tabs and readable history.
- [05-profiles.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/05-profiles.png): Compact themed profile switcher with selected state and creation action.
- [06-playlist-detail.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/06-playlist-detail.png): Playlist identity, play controls, reorder and item menus.
- [07-saved-empty.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/07-saved-empty.png): Meaningful empty state and existing Create playlist action.
- [08-downloads.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/08-downloads.png): Readable populated missions, progress, statuses and conditional actions.
- [09-settings.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/09-settings.png): Consistent headers, icons and category spacing.
- [10-downloads-empty.png](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/PipePipeClient/assets/hush-secondary-screen-review/10-downloads-empty.png): Useful empty state linking back to the watch Download action.

All are dark-theme concepts matching the selected home. Use real app data and support the existing light theme. Raster dimensions are approximate; the numeric specification is authoritative. See the [final plan](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/UI_REDESIGN_PLAN.md) and [individual icon guide](/Users/vidyasagar/ProjectSpace/debyt/PipePipe/ICON_PLACEMENT_GUIDE.md).

## Sources inspected

- `hush/games/GamesFragment.java`: four hub cards, responsive columns, current compact audio bar.
- `hush/games/GameScreens.java`: controllers and per-game tools, pause/resume/save.
- `fragments/list/search/BreakSessionFragment.java`: visual heights, technique selection, 1/3/5 minute duration, sound cues, Start/Pause/Resume states.
- `local/library/HistoryLibraryFragment.java` and `fragment_history_library.xml`: incognito, watch/search tabs, Clear history confirmation.
- `local/library/SavedLibraryFragment.java`, `LibraryChrome.java`, `fragment_library_list.xml`, `item_playlist_row.xml`: title plumbing, creation row, counts and empty-state behavior.
- `sheet_profiles.xml` and `item_profile_cell.xml`: selection grid and create/delete controls.
- `activity_downloader.xml`, `download_dialog.xml`, mission layouts: download activity/form structure.
- `res/xml/main_settings.xml`: existing preference categories and destinations.

The installed build has bottom-navigation IDs and behavior absent from this checkout; this mismatch is already recorded in the earlier design folders. Captured running screens establish visible issues; source analysis explains the local layouts, but is not proof that the installed build was produced from this checkout. Title/navigation issues must be diagnosed in the matching source before implementation.

No profile/history/download changes were made. No session was started, no game was played, and playback was not intentionally stopped. Supporting settings forms, populated downloads and active breathing states remain unverified; the review does not claim an exhaustive app QA pass.
