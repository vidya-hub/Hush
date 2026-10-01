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

/** The resting Home content. Search scrolls with the page; floating video overlays it. */
final class HomeDashboard {
    interface Actions {
        void search(String query);
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
        final int fill = palette.card;
        final int foreground = palette.ink;
        binding.homeSearchGroup.setBackground(palette.shape(fill, 16, palette.border));
        binding.homeSearchEditText.setTextColor(foreground);
        binding.homeSearchEditText.setHintTextColor(palette.muted);
        binding.homeSearchLeading.setImageTintList(ColorStateList.valueOf(foreground));
        binding.homeSearchClear.setImageTintList(ColorStateList.valueOf(foreground));
        binding.homeViewToggle.setImageTintList(ColorStateList.valueOf(foreground));
    }

    private View create() {
        final android.widget.FrameLayout searchAnchor=new android.widget.FrameLayout(context);
        searchAnchor.setTag("home-search-anchor");
        final LinearLayout recents=horizontal();
        recents.setTag("home-recent-searches");
        final LinearLayout playing=vertical();
        final TextView playingTitle=text(R.string.unknown_content,21,ink,true);
        playingTitle.setMaxLines(2);playingTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        final LinearLayout playingHeader=horizontal();playingHeader.setGravity(Gravity.CENTER_VERTICAL);
        playingHeader.addView(text(R.string.hush_now_playing,12,muted,false));
        final android.widget.ImageView waveform=new android.widget.ImageView(context);
        waveform.setImageDrawable(org.schabi.newpipe.hush.ui.HushIcons.drawable(context,"waveform"));
        waveform.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams waveLayout=new LinearLayout.LayoutParams(dp(36),dp(20));waveLayout.setMarginStart(dp(12));
        playingHeader.addView(waveform,waveLayout);playing.addView(playingHeader);
        playing.addView(playingTitle,fullTop(8));
        final org.schabi.newpipe.player.helper.PlayerHolder holder=org.schabi.newpipe.player.helper.PlayerHolder.getInstance();
        final Runnable playback=()->{
            final org.schabi.newpipe.player.playqueue.PlayQueue queue=holder.getPlayQueue();
            final org.schabi.newpipe.player.playqueue.PlayQueueItem item=queue==null?null:queue.getItem();
            playing.setVisibility(item!=null && !holder.isBackgroundAudio()?View.VISIBLE:View.GONE);
            if(item!=null)playingTitle.setText(item.getTitle());
            waveform.setVisibility(holder.isPlaying()?View.VISIBLE:View.GONE);
        };
        final LinearLayout root=new LinearLayout(context) {
            private final io.reactivex.rxjava3.disposables.CompositeDisposable subscriptions=new io.reactivex.rxjava3.disposables.CompositeDisposable();
            private final Runnable observer=()->post(playback);
            @Override protected void onAttachedToWindow(){
                super.onAttachedToWindow();holder.addUiObserver(observer);playback.run();
                subscriptions.add(new org.schabi.newpipe.local.history.HistoryRecordManager(context).getCompleteSearchHistory()
                    .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(queries->{
                        recents.removeAllViews();
                        if(org.schabi.newpipe.local.history.HistoryRecordManager.isIncognito(context))return;
                        for(String query:queries.subList(0,Math.min(queries.size(),2))){
                            MaterialButton chip=new MaterialButton(context);chip.setText(query);
                            chip.setIcon(org.schabi.newpipe.hush.ui.HushIcons.drawable(context,"history"));
                            HushUi.style(chip,false);chip.setIconSize(dp(18));chip.setMaxLines(1);
                            chip.setOnClickListener(v->actions.search(query));
                            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.setMarginEnd(dp(8));recents.addView(chip,lp);
                        }
                    },error->recents.removeAllViews()));
            }
            @Override protected void onDetachedFromWindow(){subscriptions.clear();holder.removeUiObserver(observer);removeCallbacks(observer);super.onDetachedFromWindow();}
        };
        root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_VERTICAL);root.setPadding(dp(22),dp(32),dp(22),dp(32));
        final LinearLayout hero=vertical(), searchGroup=vertical();
        HushUi.bindContentWidth(root,560,1064);
        android.widget.ImageView ring=new android.widget.ImageView(context);
        ring.setImageDrawable(org.schabi.newpipe.hush.ui.HushIcons.drawable(context,"breath-ring"));
        ring.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        hero.addView(ring,new LinearLayout.LayoutParams(dp(44),dp(44)));
        TextView headline=text(R.string.hush_home_headline,40,ink,false);headline.setLetterSpacing(-0.035f);
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
            float size=r-l>=dp(600)?40:32;
            if(Math.abs(headline.getTextSize()/context.getResources().getDisplayMetrics().scaledDensity-size)>0.1f)headline.setTextSize(size);
        });
        hero.addView(headline,fullTop(20));searchGroup.addView(searchAnchor,fullTop(0));
        android.widget.HorizontalScrollView recentScroll=new android.widget.HorizontalScrollView(context);
        recentScroll.setHorizontalScrollBarEnabled(false);recentScroll.addView(recents);searchGroup.addView(recentScroll,fullTop(12));
        final LinearLayout shortcuts=horizontal();
        shortcuts.setTag("home-library-shortcuts");
        int[] titles={R.string.action_history,R.string.library_saved,R.string.downloads};
        String[] icons={"history","bookmark","download"};Runnable[] callbacks={actions::history,actions::saved,actions::downloads};
        boolean stacked=context.getResources().getConfiguration().fontScale>1.4f;
        if(stacked)shortcuts.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<3;i++){
            MaterialButton button=new MaterialButton(context);button.setText(titles[i]);button.setIcon(org.schabi.newpipe.hush.ui.HushIcons.drawable(context,icons[i]));
            HushUi.style(button,false);button.setIconGravity(MaterialButton.ICON_GRAVITY_TOP);button.setTextSize(13);button.setPadding(dp(8),dp(12),dp(8),dp(12));
            button.setMinHeight(dp(84));button.setMinimumHeight(dp(84));
            Runnable action=callbacks[i];button.setOnClickListener(v->action.run());
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(stacked?-1:0,-2,stacked?0:1);
            if(i>0){if(stacked)lp.topMargin=dp(8);else lp.setMarginStart(dp(8));}shortcuts.addView(button,lp);
        }
        searchGroup.addView(shortcuts,fullTop(20));searchGroup.addView(playing,fullTop(28));
        final org.schabi.newpipe.hush.ui.CompactPlaybackBar audio=new org.schabi.newpipe.hush.ui.CompactPlaybackBar(context);
        searchGroup.addView(audio,fullTop(24));
        HushUi.Bounded heroBound=new HushUi.Bounded(context,hero,560);
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->heroBound.setMaxDp(
                r-l-root.getPaddingLeft()-root.getPaddingRight()>=dp(840)
                && context.getResources().getConfiguration().fontScale<1.5f?480:560));
        HushUi.Panes panes=new HushUi.Panes(context,heroBound,
                new HushUi.Bounded(context,searchGroup,560));
        panes.setBreakpointDp(840);
        root.addView(new HushUi.Bounded(context,panes,1120));
        return root;
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
