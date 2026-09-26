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
        card = dark ? 0xFF203D30 : 0xFFF1F6F2;
        border = dark ? 0xFF496858 : 0xFFD8E3DA;
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
        root.addView(heading(R.string.hush_take_break, 0));
        root.addView(breakCard(), fullTop(12));

        final LinearLayout gameHeading = horizontal();
        gameHeading.setGravity(Gravity.CENTER_VERTICAL);
        gameHeading.addView(heading(R.string.hush_quick_games, 0),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        final TextView all = text(R.string.hush_all_games, 14, dark ? 0xFFCBE3D4 : pine, true);
        all.setGravity(Gravity.CENTER);
        all.setMinHeight(dp(48));
        all.setOnClickListener(v -> actions.allGames());
        gameHeading.addView(all);
        root.addView(gameHeading, fullTop(24));

        final boolean stacked = context.getResources().getConfiguration().screenWidthDp < 360
                || context.getResources().getConfiguration().fontScale > 1.2f;
        if (stacked) {
            root.addView(gameCard(R.string.hush_game_2048, R.string.hush_game_2048_hint,
                    "2  4\n  2  8", "2048"), fullTop(8));
            root.addView(gameCard(R.string.hush_game_snake, R.string.hush_game_snake_hint,
                    "· · ●\n■ ■ ■", "snake"), fullTop(12));
        } else {
            final LinearLayout row = horizontal();
            final LinearLayout.LayoutParams first = new LinearLayout.LayoutParams(0, dp(126), 1);
            first.setMarginEnd(dp(6));
            row.addView(gameCard(R.string.hush_game_2048, R.string.hush_game_2048_hint,
                    "2  4\n2  8", "2048"), first);
            final LinearLayout.LayoutParams second = new LinearLayout.LayoutParams(0, dp(126), 1);
            second.setMarginStart(dp(6));
            row.addView(gameCard(R.string.hush_game_snake, R.string.hush_game_snake_hint,
                    "· ●\n■■■", "snake"), second);
            root.addView(row, fullTop(8));
        }
        root.addView(gameCard(R.string.hush_game_sudoku, R.string.hush_game_sudoku_hint,
                "1 2 3\n4 5 6\n7 8 9", "sudoku"), fullTop(12));

        root.addView(heading(R.string.hush_your_library, 0), fullTop(24));
        final LinearLayout library = horizontal();
        library.setGravity(Gravity.CENTER_VERTICAL);
        library.setBackground(shape(card, 20, border));
        library.addView(libraryCell(R.drawable.ic_history, R.string.action_history,
                R.string.hush_library_history_hint, actions::history), weighted());
        library.addView(divider(), new LinearLayout.LayoutParams(dp(1), dp(70)));
        library.addView(libraryCell(R.drawable.ic_bookmark, R.string.library_saved,
                R.string.hush_library_saved_hint, actions::saved), weighted());
        library.addView(divider(), new LinearLayout.LayoutParams(dp(1), dp(70)));
        library.addView(libraryCell(R.drawable.ic_file_download, R.string.downloads,
                R.string.hush_library_downloads_hint, actions::downloads), weighted());
        root.addView(library, fullTop(12));
        return root;
    }

    private View breakCard() {
        final boolean roomyActions = context.getResources().getConfiguration().fontScale > 1.2f
                || context.getResources().getConfiguration().screenWidthDp < 360;
        final LinearLayout hero = roomyActions ? vertical() : horizontal();
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(20), dp(20), dp(20), dp(20));
        hero.setBackground(shape(dark ? 0xFF254537 : pine, 24, 0));
        final LinearLayout introduction = roomyActions ? horizontal() : hero;
        introduction.setGravity(Gravity.CENTER_VERTICAL);
        if (roomyActions) hero.addView(introduction);
        final Ring ring = new Ring(context);
        introduction.addView(ring, new LinearLayout.LayoutParams(dp(78), dp(78)));
        final LinearLayout words = vertical();
        final LinearLayout.LayoutParams wordsLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        wordsLp.setMarginStart(dp(16));
        introduction.addView(words, wordsLp);
        words.addView(text(R.string.hush_moment_yourself, 19, 0xFFF6F5F2, true));
        words.addView(text(R.string.hush_breathe_or_meditate, 13, 0xFFD4E4D8, false));
        final LinearLayout actionsRow = horizontal();
        final MaterialButton breathe = pill(R.string.hush_breathe, 0xFFF6F5F2, pine);
        breathe.setOnClickListener(v -> actions.breathe());
        final MaterialButton meditate = pill(R.string.hush_meditate, 0xFF597768, 0xFFFFFFFF);
        final LinearLayout.LayoutParams breatheLp = roomyActions
                ? new LinearLayout.LayoutParams(0, dp(48), 1)
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        actionsRow.addView(breathe, breatheLp);
        final LinearLayout.LayoutParams meditateLp = new LinearLayout.LayoutParams(roomyActions
                ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT, dp(48), roomyActions ? 1 : 0);
        meditateLp.setMarginStart(dp(8));
        meditate.setOnClickListener(v -> actions.meditate());
        actionsRow.addView(meditate, meditateLp);
        if (roomyActions) hero.addView(actionsRow, fullTop(12));
        else words.addView(actionsRow, fullTop(10));
        hero.setMinimumHeight(dp(142));
        return hero;
    }

    private View gameCard(@StringRes final int name, @StringRes final int hint,
                          final String preview, final String game) {
        final LinearLayout item = horizontal();
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(16), dp(14), dp(14), dp(14));
        item.setBackground(shape(card, game.equals("sudoku") ? 18 : 20, border));
        item.setMinimumHeight(game.equals("sudoku") ? dp(80) : dp(126));
        item.setClickable(true);
        item.setFocusable(true);
        item.setContentDescription(context.getString(name) + ", " + context.getString(hint));
        item.setOnClickListener(v -> actions.game(game));
        final LinearLayout labels = vertical();
        labels.addView(text(name, 18, ink, true));
        labels.addView(text(hint, 12, muted, false));
        item.addView(labels, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        final TextView grid = new TextView(context);
        grid.setText(preview);
        grid.setTextColor(dark ? 0xFFD1E4D6 : pine);
        grid.setTextSize(TypedValue.COMPLEX_UNIT_SP, game.equals("sudoku") ? 12 : 17);
        grid.setTypeface(android.graphics.Typeface.MONOSPACE);
        grid.setGravity(Gravity.CENTER);
        grid.setMinWidth(dp(game.equals("sudoku") ? 64 : 72));
        grid.setMinHeight(dp(game.equals("sudoku") ? 56 : 72));
        grid.setPadding(dp(7), dp(5), dp(7), dp(5));
        grid.setBackground(shape(dark ? 0xFF29483A : 0xFFE9F0EB, 10, 0));
        grid.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        item.addView(grid);
        return item;
    }

    private View libraryCell(final int iconRes, @StringRes final int title,
                             @StringRes final int subtitle, final Runnable click) {
        final LinearLayout cell = vertical();
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(3), dp(12), dp(3), dp(12));
        cell.setMinimumHeight(dp(104));
        final android.widget.ImageView icon = new android.widget.ImageView(context);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(ink));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        final LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(26), dp(26));
        iconLp.gravity = Gravity.CENTER_HORIZONTAL;
        cell.addView(icon, iconLp);
        final TextView label = text(title, 13, ink, true);
        label.setGravity(Gravity.CENTER);
        cell.addView(label);
        final TextView sub = text(subtitle, 10, muted, false);
        sub.setGravity(Gravity.CENTER);
        sub.setMaxLines(2);
        cell.addView(sub);
        cell.setContentDescription(context.getString(title) + ", " + context.getString(subtitle));
        cell.setOnClickListener(v -> click.run());
        cell.setClickable(true);
        cell.setFocusable(true);
        return cell;
    }

    private MaterialButton pill(@StringRes final int label, final int fill, final int textColor) {
        final MaterialButton button = new MaterialButton(context);
        button.setText(label);
        button.setTextColor(textColor);
        button.setBackgroundTintList(ColorStateList.valueOf(fill));
        button.setCornerRadius(dp(24));
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
