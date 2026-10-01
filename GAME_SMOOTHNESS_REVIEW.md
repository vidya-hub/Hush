# Hush game responsiveness review

This pass changes the Android game engines, controllers and board rendering. It preserves the tablet layout work, existing icons, game speed and game capabilities. The updated debug build is installed on the headless tablet emulator, `emulator-5558`.

## Reproduced problems and fixes

| Game | Reproduction and current change |
|---|---|
| Snake | Two quick legal corner turns dropped the second turn. A bounded two-turn buffer now validates against the last queued direction and consumes one turn per simulation step. The buffer survives saved-round restoration. |
| Snake | Movement rebuilt control text/icons and requested 16 page layouts in a three-second baseline run. Controls now update only when score or round state changes. Drawing uses reusable segment arrays, bounds and clipping/grid paths. |
| Snake | Delayed callbacks accumulated scheduling drift. One Choreographer callback now drives interpolation with a fixed 170 ms simulation clock. Ordinary timing jitter preserves the step boundary; a long suspension avoids replaying a lethal movement backlog. Pause/reset removes the callback. |
| Snake | Growth could introduce a jumping segment. The extra head now interpolates from the previous head while the old tail remains in place. Filling the entire board is recognized as a win, including saved-round restoration. Invalid saved bodies are rejected. |
| Snake | Cancelled gestures could still trigger a tap or delayed hold restart. Cancellation and additional pointers invalidate the gesture, release parent interception and cancel hold feedback. New-game confirmation pauses movement and preserves the round when cancelled. |
| 2048 | A swipe waited for finger release, and subsequent moves could overwrite pending input. Swipes now commit on reaching the movement threshold; a bounded ordered queue retains subsequent inputs during a shorter 200 ms slide/merge/spawn animation. Cancellation prevents replay. Pause, undo, reset and resize clear pending input. Win/loss dialogs stop queued actions. |
| Sudoku | The first tap resumed the game but discarded the intended cell. It now resumes and selects in one action. Cancelled and outside releases cannot select or resume. The timer updates its label rather than rebuilding game controls every second; difficulty/new-puzzle dialogs pause elapsed time. |
| Make 24 | Selecting an operand tore down and recreated the operand rows. Existing tiles now retain their identity through selection changes; only an actual change to the terms rebuilds the rows. Undo availability no longer serializes the whole saved round merely to enable a button. |

## Verification

The initial quick-turn JVM regression and four native interaction regressions failed against the original game source. Baseline logs and metrics are retained in `assets/hush-game-smoothness-proof/baseline/`.

The full JVM suite contains 38 passing tests, including 17 game-engine tests and three fixed-step-clock tests. Coverage includes 400 seeded 2048 boards with all directions and undo, invalid/no-op moves, saved undo, queued Snake turns and reversal rejection, tail-cell movement, a full-board Snake win, invalid saved bodies, uniquely solvable Sudoku variants with immutable clues, elapsed-time pause/resume, solvable Make 24 rounds, exact fractions and division-by-zero handling.

Nine native regressions run in each reference window: landscape 1280×800dp, portrait 800×1280dp and narrow 600×900dp. They exercise swipe timing, cancelled gestures, interrupted animation, first-cell selection, retained operand views, Snake hold cancellation, modal pause/resume, growth interpolation and autonomous Snake movement. Screenshots are actual emulator captures, not generated mockups; they can include animation or pause transitions. The fixtures use isolated incognito game state and restore the prior state and privacy preference after each test.

The emulator uses SwiftShader software rendering. Total frame times varied substantially with host load, so they are recorded as observations rather than used to assert a device FPS target. An early landscape run failed an overly environment-sensitive frame-count threshold; the retained `host-load-diagnostics/` logs show that failure. The test now checks that display callbacks occur and that movement does not relayout the page, while deterministic clock tests check cadence and stall behavior. Zero page layouts during continuous movement is the directly comparable improvement; this run does not establish 60 FPS on a physical tablet.

The three-second metrics below include initial baseline and final observations. They should not be read as a controlled FPS benchmark.

| Run | Native checks | Movement page layouts | Frames sampled | P95 total ms | P95 draw ms |
|---|---:|---:|---:|---:|---:|
| Original landscape | 4 failed (expected reproduction) | 16 | 140 | 94.64 | Not captured |
| Final landscape | 9 passed | 0 | 48 | 230.80 | 0.388 |
| Final portrait | 9 passed | 0 | 46 | 300.81 | 0.556 |
| Final narrow | 9 passed | 0 | 81 | 172.71 | 0.460 |


## Reproduce and inspect

Build with Java 21:

```sh
JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home \
gradlew -p PipePipeClient :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest
python3 .zcode/run-game-smoothness-qa.py
```

The QA script targets only emulator-5558, installs the debug/test APKs, runs the native regressions in all three windows and exports captures plus raw metrics. It restores the tablet window and font scale afterwards. Emulator-5554 is untouched.

Production changes are limited to `GameModels.java`, `GameBoards.java`, `GameScreens.java` and the new `GameStepClock.java`. The pre-change source snapshot is under `.zcode/game-smoothness-baseline/`; `.zcode/game-smoothness.patch` isolates these production changes from the earlier tablet spacing work.

[Open the native capture gallery](assets/hush-game-smoothness-proof/gallery.html)
