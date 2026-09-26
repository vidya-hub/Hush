package org.schabi.newpipe.hush.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

/** Shared presentation primitives. Decisions use the measured app window, never a device model. */
public final class HushUi {
    private HushUi() { }
    /** Centers a page column at 1120dp and applies the 20/24/32dp window gutters. */
    public static void bindContentWidth(View root) {
        root.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int width = right - left;
            if (width <= 0) return;
            float density = view.getResources().getDisplayMetrics().density;
            int dpWidth = Math.round(width / density);
            int gutterDp = dpWidth >= 840 ? 32 : dpWidth >= 600 ? 24 : 20;
            int side = Math.max(Math.round(gutterDp * density), (width - Math.round(1120 * density)) / 2);
            if (view.getPaddingLeft() != side || view.getPaddingRight() != side) {
                view.setPadding(side, view.getPaddingTop(), side, view.getPaddingBottom());
            }
        });
    }
    /** Navigation bar, gesture home area, and cutout, whichever is deeper. */
    public static int bottomSafeInset(View view) {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(view);
        if (insets == null) return 0;
        Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                | WindowInsetsCompat.Type.displayCutout());
        Insets gestures = insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures());
        Insets tappable = insets.getInsets(WindowInsetsCompat.Type.tappableElement());
        return Math.max(bars.bottom, Math.max(gestures.bottom, tappable.bottom));
    }
    public static int dp(Context c, float value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }
    public static int color(Context c, int attr) {
        TypedValue value = new TypedValue();
        c.getTheme().resolveAttribute(attr, value, true);
        return value.resourceId == 0 ? value.data : ContextCompat.getColor(c, value.resourceId);
    }
    public static boolean motion(Context c) {
        return Settings.Global.getFloat(c.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0;
    }
    public static GradientDrawable shape(Context c, int fill, int radius, int stroke) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(fill);
        result.setCornerRadius(dp(c, radius));
        if (stroke != 0) result.setStroke(dp(c, 1), stroke);
        return result;
    }
    public static void clickable(View view, int fill, int radius, int stroke) {
        Context c = view.getContext();
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(
                color(c, com.google.android.material.R.attr.colorOnSurface) & 0x00ffffff
                        | 0x18000000), shape(c, fill, radius, stroke), null));
        view.setClickable(true);
        view.setFocusable(true);
    }
    public static void style(MaterialButton button, boolean primary) {
        Context c = button.getContext();
        int fill = color(c, primary ? androidx.appcompat.R.attr.colorPrimary
                : com.google.android.material.R.attr.colorSurfaceContainer);
        button.setBackgroundTintList(ColorStateList.valueOf(fill));
        int foreground = color(c, primary ? com.google.android.material.R.attr.colorOnPrimary
                : com.google.android.material.R.attr.colorOnSurface);
        ColorStateList foregrounds = new ColorStateList(new int[][] {
                new int[] {-android.R.attr.state_enabled}, new int[] {}},
                new int[] {(foreground & 0x00ffffff) | 0x61000000, foreground});
        button.setTextColor(foregrounds);
        button.setIconTint(foregrounds);
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        button.setStrokeColor(ColorStateList.valueOf(color(c,
                com.google.android.material.R.attr.colorOutlineVariant)));
        button.setStrokeWidth(primary ? 0 : dp(c, 1));
        button.setCornerRadius(dp(c, 14));
        button.setInsetTop(0); button.setInsetBottom(0);
        button.setMinHeight(dp(c, 48)); button.setMinimumHeight(dp(c, 48));
        button.setMaxLines(3); button.setSingleLine(false);
    }
    /** Centers content and bounds it without hardcoded tablet resource buckets. */
    public static final class Bounded extends FrameLayout {
        private final int maxDp;
        public Bounded(Context c, View content, int maxDp) {
            super(c); this.maxDp = maxDp;
            addView(content, new FrameLayout.LayoutParams(-1, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        }
        @Override protected void onMeasure(int w, int h) {
            int outer = MeasureSpec.getSize(w);
            int available = Math.min(outer, dp(getContext(), maxDp));
            View child = getChildAt(0);
            child.measure(MeasureSpec.makeMeasureSpec(available, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            setMeasuredDimension(outer, resolveSize(child.getMeasuredHeight(), h));
        }
    }
    /** Reflows the same child views on resize, retaining their state and listeners. */
    public static final class Panes extends LinearLayout {
        private boolean expanded;
        private int breakpointDp = 840;
        public void setBreakpointDp(int value) { breakpointDp=value; previousWidth=-1; requestLayout(); }
        private int previousWidth = -1;
        public Panes(Context c, View first, View second) {
            super(c); setOrientation(VERTICAL); setGravity(Gravity.TOP);
            addView(first, new LayoutParams(-1, -2));
            addView(second, new LayoutParams(-1, -2));
        }
        @Override protected void onMeasure(int w, int h) {
            int width = MeasureSpec.getSize(w);
            boolean next = (getRootView().getWidth() > 0 ? getRootView().getWidth() : width + dp(getContext(), 40)) >= dp(getContext(), breakpointDp)
                    && getResources().getConfiguration().fontScale <= 1.3f;
            if (previousWidth != width || next != expanded) {
                expanded = next; previousWidth = width;
                setOrientation(expanded ? HORIZONTAL : VERTICAL);
                for (int i = 0; i < 2; i++) {
                    LayoutParams lp = new LayoutParams(expanded ? 0 : -1, -2, expanded ? 1 : 0);
                    if (i == 1) { lp.topMargin = expanded ? 0 : dp(getContext(), 24);
                        lp.setMarginStart(expanded ? dp(getContext(), 24) : 0); }
                    getChildAt(i).setLayoutParams(lp);
                }
            }
            super.onMeasure(w, h);
        }
    }
}
