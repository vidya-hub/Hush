package org.schabi.newpipe.fragments.list.search;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import org.schabi.newpipe.R;
import org.schabi.newpipe.hush.ui.HushUi;
import org.schabi.newpipe.hush.ui.GamePreview;
import org.schabi.newpipe.databinding.FragmentSearchBinding;

/** The resting Home content. Search and playback remain outside this scrolling view. */
final class HomeDashboard {
    interface Actions {
        void breathe();
        void meditate();
        void game(String game);
        void allGames();
        void history();
        void saved();
        void downloads();
    }

    private final Context context;
    private final Actions actions;
    private final boolean dark;
    private final int ink;
    private final int muted;
    private final int card;
    private final int border;
    private final int pine;

    private HomeDashboard(final Context context, final Actions actions) {
        this.context = context;
        this.actions = actions;
        dark = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        ink = color(com.google.android.material.R.attr.colorOnSurface);
        muted = color(com.google.android.material.R.attr.colorOnSurfaceVariant);
        card = color(com.google.android.material.R.attr.colorSurfaceContainer);
        border = color(com.google.android.material.R.attr.colorOutlineVariant);
        pine = ContextCompat.getColor(context, R.color.m3_light_primary);
    }

    static View build(final Context context, final Actions actions) {
        return new HomeDashboard(context, actions).create();
    }

    static void styleSearch(final FragmentSearchBinding binding, final Context context) {
        final HomeDashboard palette = new HomeDashboard(context, null);
        final int fill = palette.dark ? 0xFFF6F5F2 : palette.pine;
        final int foreground = palette.dark ? palette.pine : 0xFFF6F5F2;
        binding.homeSearchGroup.setBackground(palette.shape(fill, 28, 0));
        binding.homeSearchEditText.setTextColor(foreground);
        binding.homeSearchEditText.setHintTextColor(foreground);
        binding.homeSearchLeading.setImageTintList(ColorStateList.valueOf(foreground));
        binding.homeSearchClear.setImageTintList(ColorStateList.valueOf(foreground));
        binding.homeViewToggle.setImageTintList(ColorStateList.valueOf(foreground));
    }

    private View create() {
        final LinearLayout root = vertical();
        root.setPadding(dp(20), dp(20), dp(20), dp(28));
        final LinearLayout games = vertical();
        final LinearLayout personal = vertical();
        personal.addView(heading(R.string.hush_take_break, 0));
        personal.addView(breakCard(), fullTop(12));

        final boolean largeText = context.getResources().getConfiguration().fontScale > 1.3f;
        final LinearLayout gameHeading = largeText ? vertical() : horizontal();
        gameHeading.setGravity(Gravity.CENTER_VERTICAL);
        gameHeading.addView(heading(R.string.hush_quick_games, 0),
                new LinearLayout.LayoutParams(largeText ? -1 : 0, ViewGroup.LayoutParams.WRAP_CONTENT,
                        largeText ? 0 : 1));
        final TextView all = text(R.string.hush_all_games, 14, dark ? 0xFFCBE3D4 : pine, true);
        all.setGravity(Gravity.CENTER);
        all.setMinHeight(dp(48));
        all.setOnClickListener(v -> actions.allGames());
        gameHeading.addView(all);
        games.addView(gameHeading, fullTop(0));

        final boolean stacked = context.getResources().getConfiguration().screenWidthDp < 360
                || context.getResources().getConfiguration().fontScale > 1.2f;
        if (stacked) {
            games.addView(gameCard(R.string.hush_game_2048, R.string.hush_game_2048_hint,
                    "2  4\n  2  8", "2048"), fullTop(8));
            games.addView(gameCard(R.string.hush_game_snake, R.string.hush_game_snake_hint,
                    "· · ●\n■ ■ ■", "snake"), fullTop(12));
        } else {
            final LinearLayout row = horizontal();
            final LinearLayout.LayoutParams first = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            first.setMarginEnd(dp(6));
            row.addView(gameCard(R.string.hush_game_2048, R.string.hush_game_2048_hint,
                    "2  4\n2  8", "2048"), first);
            final LinearLayout.LayoutParams second = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            second.setMarginStart(dp(6));
            row.addView(gameCard(R.string.hush_game_snake, R.string.hush_game_snake_hint,
                    "· ●\n■■■", "snake"), second);
            games.addView(row, fullTop(8));
        }
        games.addView(gameCard(R.string.hush_game_sudoku, R.string.hush_game_sudoku_hint,
                "1 2 3\n4 5 6\n7 8 9", "sudoku"), fullTop(12));

        final LinearLayout librarySection = vertical();
        librarySection.addView(heading(R.string.hush_your_library, 0));
        final LinearLayout library = vertical();
        library.setBackground(shape(card, 20, border));
        library.addView(libraryCell(R.drawable.ic_hush_history, R.string.action_history,
                R.string.hush_library_history_hint, actions::history));
        library.addView(divider(), new LinearLayout.LayoutParams(-1, dp(1)));
        library.addView(libraryCell(R.drawable.ic_hush_saved, R.string.library_saved,
                R.string.hush_library_saved_hint, actions::saved));
        library.addView(divider(), new LinearLayout.LayoutParams(-1, dp(1)));
        library.addView(libraryCell(R.drawable.ic_hush_download, R.string.downloads,
                R.string.hush_library_downloads_hint, actions::downloads));
        librarySection.addView(library, fullTop(12));
        // Same children, reparented only when the measured window crosses a breakpoint.
        final android.widget.FrameLayout adaptive = new android.widget.FrameLayout(context) {
            boolean wide;
            int previous = -1;
            @Override protected void onMeasure(int w, int h) {
                int width = MeasureSpec.getSize(w);
                boolean next = width >= dp(840) && getResources().getConfiguration().fontScale <= 1.3f;
                if (previous != width || next != wide) {
                    previous = width; wide = next;
                    detach(personal); detach(games); detach(librarySection);
                    removeAllViews(); detach(root); root.removeAllViews();
                    int gutter = width >= dp(840) ? 32 : width >= dp(600) ? 24 : 20;
                    root.setPadding(dp(gutter), dp(20), dp(gutter), dp(28));
                    if (wide) {
                        personal.addView(librarySection, fullTop(24));
                        root.addView(new HushUi.Panes(context, personal, games), fullTop(0));
                    } else {
                        root.addView(personal, fullTop(0)); root.addView(games, fullTop(24));
                        root.addView(librarySection, fullTop(24));
                    }
                    addView(new HushUi.Bounded(context, root, 1120), new android.widget.FrameLayout.LayoutParams(-1,-2));
                }
                super.onMeasure(w,h);
            }
        };
        return adaptive;
    }

    private static void detach(View view) {
        if (view.getParent() instanceof ViewGroup) ((ViewGroup)view.getParent()).removeView(view);
    }

    private View breakCard() {
        final LinearLayout introduction = horizontal();
        introduction.setGravity(Gravity.CENTER_VERTICAL);
        final Ring ring = new Ring(context);
        introduction.addView(ring, new LinearLayout.LayoutParams(dp(64), dp(64)));
        final LinearLayout words = vertical();
        introduction.addView(words);
        words.addView(text(R.string.hush_moment_yourself, 19, 0xFFF6F5F2, true));
        words.addView(text(R.string.hush_breathe_or_meditate, 13, 0xFFD4E4D8, false));
        final LinearLayout actionsRow = horizontal();
        final MaterialButton breathe = pill(R.string.hush_breathe, 0xFFF6F5F2, pine);
        breathe.setOnClickListener(v -> actions.breathe());
        final MaterialButton meditate = pill(R.string.hush_meditate, 0xFF597768, 0xFFFFFFFF);
        meditate.setOnClickListener(v -> actions.meditate());
        actionsRow.addView(breathe); actionsRow.addView(meditate);
        final LinearLayout hero = new LinearLayout(context) {
            int previousWidth = -1;
            boolean previousLarge;
            @Override protected void onMeasure(int widthSpec, int heightSpec) {
                int available = MeasureSpec.getSize(widthSpec) - dp(40);
                boolean large = getResources().getConfiguration().fontScale > 1.3f;
                if (available != previousWidth || large != previousLarge) {
                    previousWidth = available; previousLarge = large;
                    boolean stackIntro = large || available < dp(250);
                    introduction.setOrientation(stackIntro ? VERTICAL : HORIZONTAL);
                    introduction.setGravity(stackIntro ? Gravity.CENTER_HORIZONTAL : Gravity.CENTER_VERTICAL);
                    LinearLayout.LayoutParams wordsLp = new LinearLayout.LayoutParams(
                            stackIntro ? -1 : 0, -2, stackIntro ? 0 : 1);
                    if (stackIntro) wordsLp.topMargin=dp(12); else wordsLp.setMarginStart(dp(16));
                    words.setLayoutParams(wordsLp);
                    boolean stackActions = large && available < dp(420);
                    actionsRow.setOrientation(stackActions ? VERTICAL : HORIZONTAL);
                    for (int i=0; i<2; i++) {
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                stackActions ? -1 : 0, -2, stackActions ? 0 : 1);
                        if(i==1) { if(stackActions)lp.topMargin=dp(12); else lp.setMarginStart(dp(8)); }
                        actionsRow.getChildAt(i).setLayoutParams(lp);
                    }
                }
                super.onMeasure(widthSpec, heightSpec);
            }
        };
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(16),dp(12),dp(16),dp(12));
        hero.setBackground(shape(dark ? 0xFF254537 : pine,24,0));
        hero.addView(introduction,fullTop(0));hero.addView(actionsRow,fullTop(16));
        return hero;
    }

    private View gameCard(@StringRes final int name, @StringRes final int hint,
                          final String preview, final String game) {
        final LinearLayout item = horizontal();
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(16), dp(10), dp(14), dp(10));
        item.setBackground(shape(card, game.equals("sudoku") ? 18 : 20, border));
        item.setMinimumHeight(dp(72));
        item.setClickable(true);
        item.setFocusable(true);
        item.setContentDescription(context.getString(name) + ", " + context.getString(hint));
        item.setOnClickListener(v -> actions.game(game));
        final LinearLayout labels = vertical();
        labels.addView(text(name, 18, ink, true));
        labels.addView(text(hint, 12, muted, false));
        item.addView(labels, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        final GamePreview grid = new GamePreview(context, game);
        final LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(dp(64), dp(64));
        previewLp.setMarginStart(dp(8)); item.addView(grid, previewLp);
        HushUi.clickable(item, card, game.equals("sudoku") ? 18 : 20, border);
        return item;
    }

    private View libraryCell(final int iconRes, @StringRes final int title,
                             @StringRes final int subtitle, final Runnable click) {
        final LinearLayout cell = horizontal();
        cell.setGravity(Gravity.CENTER_VERTICAL);
        cell.setPadding(dp(16), dp(8), dp(16), dp(8));
        cell.setMinimumHeight(dp(56));
        final android.widget.ImageView icon = new android.widget.ImageView(context);
        icon.setImageResource(iconRes); icon.setImageTintList(ColorStateList.valueOf(ink));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        cell.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        final LinearLayout labels = vertical();
        labels.addView(text(title, 15, ink, true));
        labels.addView(text(subtitle, 12, muted, false));
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(0, -2, 1);
        labelLp.setMarginStart(dp(12)); cell.addView(labels, labelLp);
        final android.widget.ImageView arrow = new android.widget.ImageView(context);
        arrow.setImageResource(R.drawable.ic_hush_chevron);
        arrow.setImageTintList(ColorStateList.valueOf(muted));
        arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        cell.addView(arrow,new LinearLayout.LayoutParams(dp(20),dp(20)));
        cell.setContentDescription(context.getString(title));
        cell.setOnClickListener(v -> click.run());
        HushUi.clickable(cell, card, 20, 0);
        return cell;
    }

    private MaterialButton pill(@StringRes final int label, final int fill, final int textColor) {
        final MaterialButton button = new MaterialButton(context);
        button.setText(label);
        button.setTextColor(textColor);
        button.setBackgroundTintList(ColorStateList.valueOf(fill));
        button.setCornerRadius(dp(24));
        button.setMaxLines(3); button.setSingleLine(false);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setMinHeight(dp(48));
        button.setMinimumHeight(dp(48));
        return button;
    }

    private TextView heading(@StringRes final int label, final int unused) {
        return text(label, 23, ink, true);
    }

    private TextView text(@StringRes final int label, final int size, final int color,
                          final boolean strong) {
        final TextView view = new TextView(context);
        view.setText(label);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        view.setTextColor(color);
        if (strong) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return view;
    }

    private LinearLayout vertical() {
        final LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    private LinearLayout horizontal() {
        final LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.HORIZONTAL);
        return view;
    }

    private View divider() {
        final View view = new View(context);
        view.setBackgroundColor(border);
        return view;
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    }

    private LinearLayout.LayoutParams fullTop(final int top) {
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(top);
        return params;
    }

    private GradientDrawable shape(final int fill, final int radius, final int stroke) {
        final GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (stroke != 0) {
            drawable.setStroke(dp(1), stroke);
        }
        return drawable;
    }

    private int color(@AttrRes final int attribute) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attribute, value, true);
        if (value.resourceId != 0) {
            return ContextCompat.getColor(context, value.resourceId);
        }
        return value.data;
    }

    private int dp(final int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static final class Ring extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Ring(final Context context) {
            super(context);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        @Override
        protected void onDraw(final Canvas canvas) {
            super.onDraw(canvas);
            final float scale = getResources().getDisplayMetrics().density;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4 * scale);
            paint.setStrokeCap(Paint.Cap.ROUND);
            final float inset = 5 * scale;
            final RectF rect = new RectF(inset, inset, getWidth() - inset, getHeight() - inset);
            paint.setColor(0xFF809F8C);
            canvas.drawOval(rect, paint);
            paint.setColor(0xFFF6F5F2);
            canvas.drawArc(rect, -90, 210, false, paint);
        }
    }
}
