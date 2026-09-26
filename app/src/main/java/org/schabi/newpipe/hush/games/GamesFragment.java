package org.schabi.newpipe.hush.games;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.schabi.newpipe.BaseFragment;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.player.PlayerService;
import org.schabi.newpipe.player.helper.PlayerHolder;
import org.schabi.newpipe.player.playqueue.PlayQueue;
import org.schabi.newpipe.player.playqueue.PlayQueueItem;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.PicassoHelper;

import java.util.Random;

/** Four small offline games and their shared navigation/playback chrome. */
public final class GamesFragment extends BaseFragment {
    private static final String ARG_GAME = "game";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private GameStateStore store;
    private String game;
    private LinearLayout root;
    private LinearLayout content;
    private ScrollView scroll;
    private LinearLayout playerBar;
    private TextView playerTitle;
    private ImageView playerArt;
    private ImageView playerToggle;
    private int ink;
    private int muted;
    private int surface;
    private int card;
    private int border;
    private int pine;
    private int accent;

    private GameModels.Twenty48 twenty48;
    private GameModels.Snake snake;
    private GameModels.Sudoku sudoku;
    private GameModels.Make24 make24;
    private GridLayout twentyBoard;
    private TextView scoreView;
    private TextView bestView;
    private MaterialButton undoButton;
    private SnakeBoard snakeBoard;
    private SudokuBoard sudokuBoard;
    private TextView sudokuClock;
    private GridLayout sudokuKeys;
    private LinearLayout sudokuTools;
    private boolean pencil;
    private MaterialButton pencilButton;
    private LinearLayout makeTerms;
    private TextView makeTrail;
    private MaterialButton makeNext;
    private int firstTerm = -1;
    private int secondTerm = -1;
    private int snakeTicks;
    private boolean overShown;

    private final Runnable gameTick = new Runnable() {
        @Override public void run() {
            if (!isAdded() || getView() == null) return;
            if ("snake".equals(game) && snake != null && snake.alive && !snake.paused) {
                snake.tick();
                snakeTicks++;
                if (snakeTicks % 5 == 0 || !snake.alive) save();
                renderSnake();
                handler.postDelayed(this, 170);
            } else if ("sudoku".equals(game) && sudoku != null && !sudoku.paused) {
                renderSudokuTimer();
                handler.postDelayed(this, 1000);
            }
        }
    };
    private final Runnable playerTick = new Runnable() {
        @Override public void run() {
            if (!isAdded() || getView() == null) return;
            renderPlayer();
            handler.postDelayed(this, 1200);
        }
    };

    public static GamesFragment newInstance(@Nullable final String selectedGame) {
        final GamesFragment fragment = new GamesFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_GAME, selectedGame);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(@Nullable final Bundle state) {
        super.onCreate(state);
        game = getArguments() == null ? null : getArguments().getString(ARG_GAME);
    }

    @Nullable @Override
    public View onCreateView(@NonNull final android.view.LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        final Context c = requireContext();
        store = new GameStateStore(c);
        ink = color(com.google.android.material.R.attr.colorOnSurface);
        muted = color(com.google.android.material.R.attr.colorOnSurfaceVariant);
        surface = color(com.google.android.material.R.attr.colorSurface);
        final boolean dark = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        card = dark ? 0xFF203D30 : 0xFFF1F6F2;
        border = dark ? 0xFF496858 : 0xFFD8E3DA;
        pine = color(R.attr.colorPrimary);
        accent = ContextCompat.getColor(c, R.color.m3_light_primary);
        root = column();
        root.setBackgroundColor(surface);
        root.addView(header(), new LinearLayout.LayoutParams(-1, dp(66)));
        scroll = new ScrollView(c);
        scroll.setClipToPadding(false);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        content = column();
        if (game != null) content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(20), dp(12), dp(20), dp(28));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        buildPlayerBar();
        root.addView(playerBar, new LinearLayout.LayoutParams(-1, dp(72)));
        if (game == null) buildHub();
        else if ("2048".equals(game)) build2048();
        else if ("snake".equals(game)) buildSnake();
        else if ("sudoku".equals(game)) buildSudoku();
        else buildMake24();
        return root;
    }

    @Override public void onResume() {
        super.onResume();
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setSearchChrome(true);
        }
        handler.removeCallbacks(playerTick);
        handler.post(playerTick);
        renderPlayer();
        // Leaving always pauses games. Resumption requires an explicit tap.
        if ("sudoku".equals(game)) renderSudokuTimer();
    }

    @Override public void onPause() {
        if (snake != null) snake.paused = true;
        if (sudoku != null) sudoku.pause(SystemClock.elapsedRealtime());
        handler.removeCallbacks(gameTick);
        handler.removeCallbacks(playerTick);
        save();
        super.onPause();
    }

    @Override public void onDestroyView() {
        handler.removeCallbacks(gameTick);
        handler.removeCallbacks(playerTick);
        super.onDestroyView();
    }

    private View header() {
        final LinearLayout line = row();
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setPadding(dp(12), 0, dp(12), 0);
        final TextView back = text("‹", 36, ink, false);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription(getString(R.string.back));
        back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        line.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        final int title = game == null ? R.string.hush_all_games :
                "2048".equals(game) ? R.string.hush_game_2048 :
                "snake".equals(game) ? R.string.hush_game_snake :
                "sudoku".equals(game) ? R.string.hush_game_sudoku : R.string.hush_game_make24;
        final TextView heading = text(getString(title), 25, ink, true);
        line.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        return line;
    }

    private void buildHub() {
        content.addView(text(getString(R.string.hush_quick_games), 21, ink, true), top(8));
        final boolean twoColumns = getResources().getConfiguration().screenWidthDp >= 360
                && getResources().getConfiguration().fontScale <= 1.2f;
        final View[] cards = {
                hubCard("2048", R.string.hush_game_2048, R.string.hush_game_2048_hint,
                        "2  4\n8 16", twoColumns),
                hubCard("snake", R.string.hush_game_snake, R.string.hush_game_snake_hint,
                        "■ ■ ●", twoColumns),
                hubCard("sudoku", R.string.hush_game_sudoku, R.string.hush_game_sudoku_hint,
                        "1 2 3", twoColumns),
                hubCard("make24", R.string.hush_game_make24, R.string.hush_game_make24_hint,
                        "6 ÷ 2 × 8", twoColumns)
        };
        if (twoColumns) {
            for (int i = 0; i < cards.length; i += 2) {
                final LinearLayout row = row();
                final LinearLayout.LayoutParams first = new LinearLayout.LayoutParams(0, -1, 1);
                first.rightMargin = dp(6);
                row.addView(cards[i], first);
                final LinearLayout.LayoutParams second = new LinearLayout.LayoutParams(0, -1, 1);
                second.leftMargin = dp(6);
                row.addView(cards[i + 1], second);
                content.addView(row, top(12));
            }
        } else {
            for (final View cardView : cards) content.addView(cardView, top(12));
        }
    }

    private View hubCard(final String id, final int title, final int subtitle,
                         final String symbol, final boolean compact) {
        final LinearLayout cardView = compact ? column() : row();
        cardView.setGravity(Gravity.CENTER_VERTICAL);
        cardView.setPadding(dp(18), dp(12), dp(18), dp(12));
        cardView.setMinimumHeight(dp(compact ? 128 : 92));
        cardView.setBackground(shape(card, 20, border));
        final LinearLayout labels = column();
        labels.addView(text(getString(title), 20, ink, true));
        labels.addView(text(getString(subtitle), 13, muted, false));
        cardView.addView(labels, compact ? new LinearLayout.LayoutParams(-1, 0, 1)
                : new LinearLayout.LayoutParams(0, -2, 1));
        final TextView preview = text(symbol, 15, pine, true);
        preview.setTypeface(android.graphics.Typeface.MONOSPACE);
        if (compact) preview.setGravity(Gravity.END);
        cardView.addView(preview);
        cardView.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragment_holder, newInstance(id))
                .addToBackStack(null).commit());
        return cardView;
    }

    private void build2048() {
        twenty48 = new GameModels.Twenty48(random);
        twenty48.load(store.read("2048"));
        final LinearLayout stats = row();
        scoreView = text("", 17, ink, true);
        bestView = text("", 17, ink, true);
        stats.addView(scoreView, new LinearLayout.LayoutParams(0, dp(58), 1));
        stats.addView(bestView, new LinearLayout.LayoutParams(0, dp(58), 1));
        final MaterialButton reset = button(R.string.hush_new_game);
        reset.setOnClickListener(v -> confirmNew(() -> {
            twenty48.reset(); save(); render2048();
        }));
        stats.addView(reset);
        content.addView(stats, top(10));
        twentyBoard = new GridLayout(requireContext());
        twentyBoard.setColumnCount(4);
        twentyBoard.setRowCount(4);
        twentyBoard.setPadding(dp(6), dp(6), dp(6), dp(6));
        twentyBoard.setBackground(shape(card, 20, border));
        final int tile = Math.max(dp(44), Math.min(dp(92),
                (getResources().getDisplayMetrics().widthPixels - dp(52)) / 4));
        for (int i = 0; i < 16; i++) {
            final TextView cell = text("", 25, ink, true);
            cell.setGravity(Gravity.CENTER);
            final GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(i / 4), GridLayout.spec(i % 4));
            lp.width = tile - dp(5);
            lp.height = tile - dp(5);
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
            twentyBoard.addView(cell, lp);
        }
        content.addView(twentyBoard, top(24));
        final float[] touch = new float[2];
        twentyBoard.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                touch[0] = event.getX(); touch[1] = event.getY(); return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                final float dx = event.getX() - touch[0];
                final float dy = event.getY() - touch[1];
                if (Math.max(Math.abs(dx), Math.abs(dy)) > dp(24)) {
                    final int direction = Math.abs(dx) > Math.abs(dy)
                            ? dx > 0 ? 1 : 0 : dy > 0 ? 3 : 2;
                    if (twenty48.move(direction)) {
                        save(); render2048();
                        if (twenty48.won && !twenty48.acknowledged) {
                            twenty48.acknowledged = true;
                            save();
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.hush_you_win)
                                    .setPositiveButton(R.string.hush_continue, null)
                                    .setNegativeButton(R.string.hush_finish, (dialog, which) -> {
                                        twenty48.gameOver = true;
                                        overShown = false;
                                        save();
                                        render2048();
                                    }).show();
                        }
                    }
                }
                return true;
            }
            return true;
        });
        final LinearLayout actions = row();
        final TextView instruction = text(getString(R.string.hush_swipe_move), 14, muted, false);
        actions.addView(instruction, new LinearLayout.LayoutParams(0, dp(50), 1));
        undoButton = button(R.string.hush_undo);
        undoButton.setOnClickListener(v -> { if (twenty48.undo()) { save(); render2048(); } });
        actions.addView(undoButton);
        content.addView(actions, top(12));
        render2048();
    }

    private void render2048() {
        if (twentyBoard == null) return;
        scoreView.setText(getString(R.string.hush_score) + "\n" + twenty48.score);
        bestView.setText(getString(R.string.hush_best) + "\n" + twenty48.best);
        for (int i = 0; i < 16; i++) {
            final TextView tile = (TextView) twentyBoard.getChildAt(i);
            final int value = twenty48.board[i];
            tile.setText(value == 0 ? "" : Integer.toString(value));
            final int fill = value == 0 ? surface : value < 8 ? 0xFFE9EFE9
                    : value < 32 ? 0xFFD6E3D6 : value < 128 ? 0xFFB8D0BC
                    : value < 512 ? 0xFF8EAE96 : 0xFF557C64;
            tile.setBackground(shape(fill, 11, 0));
            tile.setTextColor(value >= 512 ? 0xFFFFFFFF : 0xFF183528);
            tile.setContentDescription(value == 0 ? "Empty" : Integer.toString(value));
        }
        undoButton.setEnabled(twenty48.save().has("undo"));
        if (twenty48.gameOver && !overShown) {
            overShown = true;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.hush_game_over)
                    .setPositiveButton(R.string.hush_new_game, (dialog, which) -> {
                        twenty48.reset(); overShown = false; save(); render2048();
                    }).setNegativeButton(R.string.close, null).show();
        }
    }

    private void buildSnake() {
        snake = new GameModels.Snake(random);
        snake.load(store.read("snake"));
        final LinearLayout stats = row();
        scoreView = text("", 17, ink, true);
        bestView = text("", 17, ink, true);
        stats.addView(scoreView, new LinearLayout.LayoutParams(0, dp(56), 1));
        stats.addView(bestView, new LinearLayout.LayoutParams(0, dp(56), 1));
        final MaterialButton reset = button(R.string.hush_new_game);
        reset.setOnClickListener(v -> confirmNew(() -> {
            snake.reset(); save(); renderSnake();
        }));
        stats.addView(reset);
        content.addView(stats, top(10));
        snakeBoard = new SnakeBoard(requireContext());
        snakeBoard.setBackground(shape(card, 18, border));
        snakeBoard.setClipToOutline(true);
        content.addView(snakeBoard, top(16));
        final TextView hint = text(getString(R.string.hush_swipe_to_steer), 14, muted, false);
        hint.setGravity(Gravity.CENTER);
        content.addView(hint, top(12));
        final float[] touch = new float[2];
        snakeBoard.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                touch[0] = event.getX(); touch[1] = event.getY(); return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                final float dx = event.getX() - touch[0];
                final float dy = event.getY() - touch[1];
                if (Math.max(Math.abs(dx), Math.abs(dy)) > dp(18)) {
                    snake.turn(Math.abs(dx) > Math.abs(dy) ? dx > 0 ? 1 : 3 : dy > 0 ? 2 : 0);
                    // the first swipe of a resting round also starts it
                    if (snake.alive && snake.paused) {
                        snake.paused = false;
                        handler.removeCallbacks(gameTick);
                        handler.post(gameTick);
                    }
                } else if (!snake.alive || snake.paused) {
                    if (!snake.alive) {
                        snake.reset();
                    }
                    snake.paused = false;
                    handler.removeCallbacks(gameTick);
                    handler.post(gameTick);
                } else {
                    snake.paused = true;
                    handler.removeCallbacks(gameTick);
                }
                save(); renderSnake();
                return true;
            }
            return true;
        });
        renderSnake();
    }

    private void renderSnake() {
        if (snakeBoard == null) return;
        scoreView.setText(getString(R.string.hush_score) + "\n" + snake.score);
        bestView.setText(getString(R.string.hush_best) + "\n" + snake.best);
        snakeBoard.invalidate();
    }

    private void buildSudoku() {
        sudoku = new GameModels.Sudoku(random);
        sudoku.load(store.read("sudoku"));
        final LinearLayout stats = row();
        final MaterialButton difficulty = button(0);
        difficulty.setText(sudoku.difficulty == 0 ? R.string.hush_easy : R.string.hush_medium);
        difficulty.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setItems(new String[]{getString(R.string.hush_easy),
                        getString(R.string.hush_medium)}, (dialog, which) -> {
                    sudoku.newPuzzle(which); save(); buildGameContent();
                }).show());
        stats.addView(difficulty);
        sudokuClock = text("", 16, ink, true);
        sudokuClock.setGravity(Gravity.CENTER);
        sudokuClock.setBackground(shape(card, 18, border));
        sudokuClock.setMinWidth(dp(64));
        sudokuClock.setMinimumHeight(dp(48));
        sudokuClock.setOnClickListener(v -> {
            if (sudoku.paused) {
                sudoku.resume(SystemClock.elapsedRealtime());
                handler.removeCallbacks(gameTick);
                handler.post(gameTick);
            } else {
                sudoku.pause(SystemClock.elapsedRealtime());
                handler.removeCallbacks(gameTick);
            }
            save(); renderSudokuTimer();
        });
        stats.addView(sudokuClock, new LinearLayout.LayoutParams(0, dp(48), 1));
        final MaterialButton reset = button(R.string.hush_new_puzzle);
        reset.setOnClickListener(v -> confirmNew(() -> {
            sudoku.newPuzzle(sudoku.difficulty); save(); buildGameContent();
        }));
        stats.addView(reset);
        content.addView(stats, top(10));
        sudokuBoard = new SudokuBoard(requireContext());
        sudokuBoard.setBackground(shape(card, 20, border));
        sudokuBoard.setClipToOutline(true);
        content.addView(sudokuBoard, top(14));
        sudokuBoard.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                if (sudoku.paused && !sudoku.complete()) {
                    sudoku.resume(SystemClock.elapsedRealtime());
                    handler.removeCallbacks(gameTick);
                    handler.post(gameTick);
                    save(); renderSudokuTimer();
                    return true;
                }
                final float cell = sudokuBoard.getWidth() / 9f;
                final int col = Math.min(8, Math.max(0, (int) (event.getX() / cell)));
                final int row = Math.min(8, Math.max(0, (int) (event.getY() / cell)));
                sudoku.selected = row * 9 + col;
                sudokuBoard.invalidate();
                return true;
            }
            return true;
        });
        sudokuKeys = new GridLayout(requireContext());
        sudokuKeys.setColumnCount(5);
        for (int number = 1; number <= 9; number++) {
            final int value = number;
            final MaterialButton key = button(0);
            key.setText(Integer.toString(number));
            final GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = Math.max(dp(48), (getResources().getDisplayMetrics().widthPixels
                    - dp(84)) / 5);
            lp.height = dp(55);
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
            sudokuKeys.addView(key, lp);
            key.setOnClickListener(v -> {
                if (sudoku.paused) return;
                sudoku.set(sudoku.selected, value, pencil);
                if (sudoku.complete()) sudoku.pause(SystemClock.elapsedRealtime());
                save(); sudokuBoard.invalidate();
                if (sudoku.complete()) new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.hush_round_solved)
                        .setPositiveButton(R.string.hush_new_puzzle, (dialog, which) -> {
                            sudoku.newPuzzle(sudoku.difficulty); save(); buildGameContent();
                        }).show();
            });
        }
        content.addView(sudokuKeys, top(14));
        sudokuTools = row();
        pencilButton = button(R.string.hush_pencil);
        pencilButton.setOnClickListener(v -> {
            if (sudoku.paused) return;
            pencil = !pencil;
            pencilButton.setAlpha(pencil ? 1f : 0.62f);
        });
        sudokuTools.addView(pencilButton, new LinearLayout.LayoutParams(0, dp(50), 1));
        final MaterialButton erase = button(R.string.hush_erase);
        erase.setOnClickListener(v -> {
            if (sudoku.paused) return;
            sudoku.erase(sudoku.selected); save(); sudokuBoard.invalidate();
        });
        sudokuTools.addView(erase, new LinearLayout.LayoutParams(0, dp(50), 1));
        final MaterialButton hint = button(R.string.hush_hint);
        hint.setOnClickListener(v -> {
            if (sudoku.paused) return;
            sudoku.hint(); save(); sudokuBoard.invalidate();
        });
        sudokuTools.addView(hint, new LinearLayout.LayoutParams(0, dp(50), 1));
        content.addView(sudokuTools, top(10));
        pencilButton.setAlpha(0.62f);
        renderSudokuTimer();
    }

    private void renderSudokuTimer() {
        if (sudokuClock == null || sudoku == null) return;
        final long elapsed = sudoku.elapsed(SystemClock.elapsedRealtime()) / 1000;
        sudokuClock.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d",
                elapsed / 60, elapsed % 60));
        sudokuClock.setContentDescription(getString(sudoku.paused
                ? R.string.hush_tap_to_resume : R.string.hush_paused));
        final float keys = sudoku.paused && !sudoku.complete() ? 0.45f : 1f;
        if (sudokuKeys != null) sudokuKeys.setAlpha(keys);
        if (sudokuTools != null) sudokuTools.setAlpha(keys);
        if (sudokuBoard != null) sudokuBoard.invalidate();
    }

    private void buildMake24() {
        make24 = new GameModels.Make24(random);
        make24.load(store.read("make24"));
        final TextView goal = text(getString(R.string.hush_goal_24), 21, ink, true);
        goal.setGravity(Gravity.CENTER);
        content.addView(goal, top(22));
        makeTerms = row();
        makeTerms.setGravity(Gravity.CENTER);
        content.addView(makeTerms, top(28));
        final LinearLayout operations = row();
        for (int op = 0; op < 4; op++) {
            final int chosen = op;
            final MaterialButton button = button(0);
            button.setText(Character.toString(GameModels.Make24.OPERATIONS[op]));
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(56), 1);
            if (op > 0) lp.setMarginStart(dp(7));
            operations.addView(button, lp);
            button.setOnClickListener(v -> {
                if (!make24.combine(firstTerm, secondTerm, chosen)) {
                    makeTrail.setText(R.string.hush_invalid_move);
                    return;
                }
                firstTerm = -1; secondTerm = -1;
                save(); renderMake24();
            });
        }
        content.addView(operations, top(24));
        makeTrail = text("", 16, muted, false);
        makeTrail.setGravity(Gravity.CENTER);
        content.addView(makeTrail, top(18));
        final LinearLayout tools = row();
        final MaterialButton undo = button(R.string.hush_undo);
        undo.setOnClickListener(v -> { if (make24.undo()) { save(); renderMake24(); } });
        tools.addView(undo, new LinearLayout.LayoutParams(0, dp(50), 1));
        final MaterialButton hint = button(R.string.hush_hint);
        hint.setOnClickListener(v -> makeTrail.setText(make24.hint()));
        tools.addView(hint, new LinearLayout.LayoutParams(0, dp(50), 1));
        final MaterialButton skip = button(R.string.hush_skip);
        skip.setOnClickListener(v -> { make24.next(); save(); renderMake24(); });
        tools.addView(skip, new LinearLayout.LayoutParams(0, dp(50), 1));
        content.addView(tools, top(18));
        makeNext = button(R.string.hush_next_round);
        makeNext.setOnClickListener(v -> { make24.next(); save(); renderMake24(); });
        content.addView(makeNext, top(12));
        renderMake24();
    }

    private void renderMake24() {
        if (makeTerms == null) return;
        makeTerms.removeAllViews();
        for (int i = 0; i < make24.terms.size(); i++) {
            final int index = i;
            final GameModels.Make24.Term term = make24.terms.get(i);
            final TextView tile = text(term.toString(), 22, index == firstTerm
                    || index == secondTerm ? 0xFFFFFFFF : ink, true);
            tile.setGravity(Gravity.CENTER);
            tile.setBackground(shape(index == firstTerm || index == secondTerm ? pine : card,
                    20, border));
            tile.setContentDescription(term.expression);
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(84), 1);
            if (i > 0) lp.setMarginStart(dp(8));
            makeTerms.addView(tile, lp);
            tile.setOnClickListener(v -> {
                if (firstTerm == index) firstTerm = -1;
                else if (secondTerm == index) secondTerm = -1;
                else if (firstTerm < 0) firstTerm = index;
                else secondTerm = index;
                renderMake24();
            });
        }
        makeTrail.setText(make24.solved ? getString(R.string.hush_round_solved) :
                make24.terms.size() < 4 ? make24.terms.get(make24.terms.size() - 1).expression
                        : getString(R.string.hush_goal_24));
        makeNext.setVisibility(make24.solved ? View.VISIBLE : View.GONE);
    }

    private void buildGameContent() {
        handler.removeCallbacks(gameTick);
        content.removeAllViews();
        twentyBoard = null; snakeBoard = null; sudokuBoard = null; makeTerms = null;
        if ("sudoku".equals(game)) buildSudoku();
        else if ("2048".equals(game)) build2048();
        else if ("snake".equals(game)) buildSnake();
        else if ("make24".equals(game)) buildMake24();
    }

    private void save() {
        if (store == null || game == null) return;
        if ("2048".equals(game) && twenty48 != null) store.write(game, twenty48.save());
        else if ("snake".equals(game) && snake != null) store.write(game, snake.save());
        else if ("sudoku".equals(game) && sudoku != null) store.write(game,
                sudoku.save(SystemClock.elapsedRealtime()));
        else if ("make24".equals(game) && make24 != null) store.write(game, make24.save());
    }

    private void confirmNew(final Runnable action) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.hush_new_game)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.hush_new_game, (dialog, which) -> action.run())
                .show();
    }

    private void buildPlayerBar() {
        playerBar = row();
        playerBar.setGravity(Gravity.CENTER_VERTICAL);
        playerBar.setPadding(dp(16), dp(8), dp(10), dp(8));
        playerBar.setBackground(shape(card, 20, border));
        playerArt = new ImageView(requireContext());
        playerArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        playerBar.addView(playerArt, new LinearLayout.LayoutParams(dp(48), dp(48)));
        playerTitle = text("", 15, ink, true);
        playerTitle.setSingleLine(true);
        playerTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        final LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, -2, 1);
        titleLp.setMarginStart(dp(12));
        playerBar.addView(playerTitle, titleLp);
        playerToggle = new ImageView(requireContext());
        playerToggle.setPadding(dp(12), dp(12), dp(12), dp(12));
        playerToggle.setImageTintList(ColorStateList.valueOf(ink));
        playerToggle.setContentDescription(getString(R.string.play_audio));
        playerBar.addView(playerToggle, new LinearLayout.LayoutParams(dp(48), dp(48)));
        playerToggle.setOnClickListener(v -> requireContext().sendBroadcast(
                new Intent(PlayerService.ACTION_PLAY_PAUSE)));
        playerTitle.setOnClickListener(v -> openPlayer());
        playerArt.setOnClickListener(v -> openPlayer());
    }

    private void renderPlayer() {
        if (playerBar == null || !isAdded()) return;
        final PlayerHolder holder = PlayerHolder.getInstance();
        final View playerSheet = requireActivity().findViewById(R.id.fragment_player_holder);
        boolean sheetVisible = false;
        if (playerSheet != null) {
            try {
                sheetVisible = BottomSheetBehavior.from(playerSheet).getState()
                        != BottomSheetBehavior.STATE_HIDDEN;
            } catch (final IllegalArgumentException ignored) { }
        }
        final boolean show = holder.isBackgroundAudio() && !sheetVisible;
        playerBar.setVisibility(show ? View.VISIBLE : View.GONE);
        scroll.setPadding(0, 0, 0, sheetVisible ? dp(80) : 0);
        if (!show) return;
        final PlayQueue queue = holder.getPlayQueue();
        final PlayQueueItem item = queue == null ? null : queue.getItem();
        if (item != null) {
            playerTitle.setText(item.getTitle());
            if (item.getThumbnailUrl() != null) {
                PicassoHelper.loadThumbnail(item.getThumbnailUrl()).into(playerArt);
            }
        }
        playerToggle.setImageResource(holder.isPlaying() ? R.drawable.ic_pause
                : R.drawable.ic_play_arrow);
    }

    private void openPlayer() {
        final PlayQueue queue = PlayerHolder.getInstance().getPlayQueue();
        final PlayQueueItem item = queue == null ? null : queue.getItem();
        if (item != null) NavigationHelper.openVideoDetailFragment(requireContext(),
                requireActivity().getSupportFragmentManager(), item.getServiceId(), item.getUrl(),
                item.getTitle(), queue, true);
    }

    private class SnakeBoard extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        SnakeBoard(final Context context) { super(context); }
        @Override protected void onMeasure(final int widthSpec, final int heightSpec) {
            final int width = MeasureSpec.getSize(widthSpec);
            setMeasuredDimension(width, width);
        }
        @Override protected void onDraw(final Canvas canvas) {
            super.onDraw(canvas);
            if (snake == null) return;
            final float cell = getWidth() / (float) GameModels.Snake.SIZE;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(border);
            for (int i = 0; i <= GameModels.Snake.SIZE; i++) {
                canvas.drawLine(i * cell, 0, i * cell, getHeight(), paint);
                canvas.drawLine(0, i * cell, getWidth(), i * cell, paint);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFE3886D);
            drawCell(canvas, snake.food, cell, paint);
            paint.setColor(pine);
            for (final int segment : snake.body) drawCell(canvas, segment, cell, paint);
            if (snake.paused || !snake.alive) {
                drawBoardOverlay(canvas, paint, !snake.alive
                        ? getString(R.string.hush_game_over)
                        : snake.body.size() == 3 ? getString(R.string.hush_tap_to_start)
                        : getString(R.string.hush_paused),
                        !snake.alive ? getString(R.string.hush_tap_to_play)
                                : snake.body.size() == 3
                                ? getString(R.string.hush_swipe_to_steer)
                                : getString(R.string.hush_tap_to_resume));
            }
        }
        private void drawCell(final Canvas canvas, final int position, final float size,
                              final Paint paint) {
            final float x = position % GameModels.Snake.SIZE * size;
            final float y = position / GameModels.Snake.SIZE * size;
            canvas.drawRoundRect(new RectF(x + dp(1), y + dp(1), x + size - dp(1),
                    y + size - dp(1)), dp(4), dp(4), paint);
        }
    }

    private class SudokuBoard extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        SudokuBoard(final Context context) { super(context); setContentDescription("Sudoku board"); }
        @Override protected void onMeasure(final int widthSpec, final int heightSpec) {
            final int width = MeasureSpec.getSize(widthSpec);
            setMeasuredDimension(width, width);
        }
        @Override protected void onDraw(final Canvas canvas) {
            super.onDraw(canvas);
            if (sudoku == null) return;
            final float side = getWidth() / 9f;
            canvas.drawColor(surface);
            for (int i = 0; i < 81; i++) {
                final int row = i / 9;
                final int col = i % 9;
                final float x = col * side;
                final float y = row * side;
                if (i == sudoku.selected) {
                    paint.setColor(0xFFC5DCCD);
                    canvas.drawRect(x, y, x + side, y + side, paint);
                } else if (sudoku.selected >= 0 && (row == sudoku.selected / 9
                        || col == sudoku.selected % 9 || row / 3 == sudoku.selected / 27
                        && col / 3 == sudoku.selected % 9 / 3)) {
                    paint.setColor(card);
                    canvas.drawRect(x, y, x + side, y + side, paint);
                }
                if (sudoku.cells[i] != 0) {
                    paint.setColor(sudoku.conflict(i) ? 0xFFC94D42
                            : sudoku.clues[i] != 0 ? ink : pine);
                    paint.setTextSize(side * 0.51f);
                    paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(Integer.toString(sudoku.cells[i]), x + side / 2,
                            y + side * 0.68f, paint);
                } else if (sudoku.notes[i] != 0) {
                    paint.setColor(muted);
                    paint.setTextSize(side * 0.18f);
                    paint.setTypeface(android.graphics.Typeface.DEFAULT);
                    paint.setTextAlign(Paint.Align.CENTER);
                    for (int digit = 1; digit <= 9; digit++) {
                        if ((sudoku.notes[i] & 1 << digit) != 0) {
                            canvas.drawText(Integer.toString(digit), x + (digit - 1) % 3
                                            * side / 3 + side / 6,
                                    y + (digit - 1) / 3 * side / 3 + side / 4, paint);
                        }
                    }
                }
            }
            for (int line = 0; line <= 9; line++) {
                paint.setColor(line % 3 == 0 ? pine : border);
                paint.setStrokeWidth(line % 3 == 0 ? dp(2) : dp(1));
                canvas.drawLine(line * side, 0, line * side, getHeight(), paint);
                canvas.drawLine(0, line * side, getWidth(), line * side, paint);
            }
            if (sudoku.paused && !sudoku.complete()) {
                drawBoardOverlay(canvas, paint, getString(R.string.hush_paused),
                        getString(R.string.hush_tap_to_resume));
            }
        }
    }

    /** Semi-transparent scrim with a title and hint line for paused/over board states. */
    private void drawBoardOverlay(final Canvas canvas, final Paint paint,
                                  final String title, final String hint) {
        final int side = Math.min(canvas.getWidth(), canvas.getHeight());
        paint.setStyle(Paint.Style.FILL);
        paint.setColor((surface & 0x00FFFFFF) | 0xB8000000);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        paint.setColor(ink);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(side * 0.072f);
        canvas.drawText(title, canvas.getWidth() / 2f,
                canvas.getHeight() / 2f - dp(6), paint);
        paint.setColor(muted);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setTextSize(side * 0.042f);
        canvas.drawText(hint, canvas.getWidth() / 2f,
                canvas.getHeight() / 2f + dp(22), paint);
    }

    private LinearLayout column() {
        final LinearLayout view = new LinearLayout(requireContext());
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    private LinearLayout row() {
        final LinearLayout view = new LinearLayout(requireContext());
        view.setOrientation(LinearLayout.HORIZONTAL);
        return view;
    }

    private LinearLayout.LayoutParams top(final int margin) {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(margin);
        return lp;
    }

    private TextView text(final String text, final int size, final int color,
                          final boolean bold) {
        final TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        view.setTextColor(color);
        if (bold) view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        return view;
    }

    private MaterialButton button(final int text) {
        final MaterialButton result = new MaterialButton(requireContext());
        if (text != 0) result.setText(text);
        result.setCornerRadius(dp(18));
        result.setInsetTop(0);
        result.setInsetBottom(0);
        result.setMinimumHeight(dp(48));
        result.setBackgroundTintList(ColorStateList.valueOf(card));
        result.setTextColor(ink);
        result.setStrokeColor(ColorStateList.valueOf(border));
        result.setStrokeWidth(dp(1));
        // let labels wrap at large font sizes instead of truncating
        result.setMaxLines(2);
        result.setSingleLine(false);
        return result;
    }

    private GradientDrawable shape(final int fill, final int radius, final int stroke) {
        final GradientDrawable result = new GradientDrawable();
        result.setColor(fill);
        result.setCornerRadius(dp(radius));
        if (stroke != 0) result.setStroke(dp(1), stroke);
        return result;
    }

    private int color(final int attr) {
        final TypedValue value = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, value, true);
        return value.resourceId != 0 ? ContextCompat.getColor(requireContext(), value.resourceId)
                : value.data;
    }

    private int dp(final int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
