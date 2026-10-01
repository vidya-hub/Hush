package org.schabi.newpipe.hush.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import org.schabi.newpipe.R;
import org.schabi.newpipe.player.PlayerService;
import org.schabi.newpipe.player.helper.PlayerHolder;
import org.schabi.newpipe.player.playqueue.PlayQueue;
import org.schabi.newpipe.player.playqueue.PlayQueueItem;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.PicassoHelper;

/** One compact background-audio surface; the existing player sheet always wins. */
public final class CompactPlaybackBar extends LinearLayout {
    private final ImageView artwork;
    private final ImageView toggle;
    private final TextView title;
    private final LinearLayout metadata, actions;
    private final Runnable observer = () -> post(this::refresh);
    private BottomSheetBehavior<View> behavior;
    private int originalBottomPadding;
    private View chrome;
    private String shownUrl;
    private Boolean shownPlaying;
    private final BottomSheetBehavior.BottomSheetCallback sheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override public void onStateChanged(View sheet, int state) { refresh(); }
        @Override public void onSlide(View sheet, float offset) { refresh(); }
    };
    public CompactPlaybackBar(Context context) {
        super(context); setOrientation(HORIZONTAL); setGravity(Gravity.CENTER_VERTICAL);
        metadata=new LinearLayout(context);metadata.setGravity(Gravity.CENTER_VERTICAL);
        actions=new LinearLayout(context);actions.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        addView(metadata,new LayoutParams(0,-2,1));addView(actions,new LayoutParams(-2,-2));
        int ink = HushUi.color(context, com.google.android.material.R.attr.colorOnSurface);
        setPadding(HushUi.dp(context,16),HushUi.dp(context,8),HushUi.dp(context,8),HushUi.dp(context,8));
        setMinimumHeight(HushUi.dp(context,64));
        setBackground(HushUi.shape(context,HushUi.color(context,
                com.google.android.material.R.attr.colorSurfaceContainer),20,
                HushUi.color(context,com.google.android.material.R.attr.colorOutlineVariant)));
        artwork = new ImageView(context); artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        android.graphics.drawable.Drawable fallback = androidx.core.content.ContextCompat
                .getDrawable(context, R.drawable.ic_hush_music);
        if (fallback != null) {
            fallback = androidx.core.graphics.drawable.DrawableCompat.wrap(fallback).mutate();
            androidx.core.graphics.drawable.DrawableCompat.setTint(fallback, ink);
            artwork.setImageDrawable(fallback);
        }
        metadata.addView(artwork,new LayoutParams(HushUi.dp(context,40),HushUi.dp(context,40)));
        title = new TextView(context); title.setTextColor(ink); title.setTextSize(15);
        title.setMaxLines(2); title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LayoutParams textLp = new LayoutParams(0,-2,1); textLp.setMarginStart(HushUi.dp(context,12));
        LinearLayout words=new LinearLayout(context);words.setOrientation(VERTICAL);
        words.addView(title,new LayoutParams(-1,-2));
        TextView mode=new TextView(context);mode.setText(R.string.hush_audio_only);mode.setTextSize(13);
        mode.setTextColor(HushUi.color(context,com.google.android.material.R.attr.colorOnSurfaceVariant));
        words.addView(mode,new LayoutParams(-1,-2));metadata.addView(words,textLp);
        toggle = new ImageView(context); toggle.setImageTintList(ColorStateList.valueOf(ink));
        toggle.setPadding(HushUi.dp(context,12),HushUi.dp(context,12),HushUi.dp(context,12),HushUi.dp(context,12));
        actions.addView(toggle,new LayoutParams(HushUi.dp(context,48),HushUi.dp(context,48)));
        ImageView queueButton=new ImageView(context);queueButton.setImageDrawable(HushIcons.drawable(context,"queue"));
        queueButton.setPadding(HushUi.dp(context,12),HushUi.dp(context,12),HushUi.dp(context,12),HushUi.dp(context,12));
        queueButton.setContentDescription(context.getString(R.string.title_activity_play_queue));
        actions.addView(queueButton,new LayoutParams(HushUi.dp(context,48),HushUi.dp(context,48)));
        queueButton.setOnClickListener(v->context.startActivity(NavigationHelper.getPlayQueueActivityIntent(context)));
        ImageView close=new ImageView(context);close.setImageDrawable(HushIcons.drawable(context,"close"));
        close.setContentDescription(context.getString(R.string.close));
        close.setPadding(HushUi.dp(context,12),HushUi.dp(context,12),HushUi.dp(context,12),HushUi.dp(context,12));
        actions.addView(close,new LayoutParams(HushUi.dp(context,48),HushUi.dp(context,48)));
        close.setOnClickListener(v->context.sendBroadcast(new Intent(PlayerService.ACTION_CLOSE)));
        setOnClickListener(v -> openPlayer());
        toggle.setOnClickListener(v -> {
            context.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE)); postDelayed(this::refresh,150);
        });
        setVisibility(GONE);
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        boolean stack=MeasureSpec.getSize(widthSpec)<HushUi.dp(getContext(),420)
                || getResources().getConfiguration().fontScale>=1.5f;
        setOrientation(stack?VERTICAL:HORIZONTAL);
        metadata.setLayoutParams(new LayoutParams(stack?-1:0,-2,stack?0:1));
        LayoutParams lp=new LayoutParams(stack?-1:-2,-2);lp.topMargin=stack?HushUi.dp(getContext(),8):0;
        actions.setLayoutParams(lp);super.onMeasure(widthSpec,heightSpec);
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow(); PlayerHolder.getInstance().addUiObserver(observer);
        chrome = getParent() instanceof View ? (View) getParent() : null;
        if (chrome != null) originalBottomPadding = chrome.getPaddingBottom();
        View sheet = getRootView().findViewById(R.id.fragment_player_holder);
        if (sheet != null) try {
            behavior = BottomSheetBehavior.from(sheet); behavior.addBottomSheetCallback(sheetCallback);
        } catch (IllegalArgumentException ignored) { }
        refresh();
    }
    @Override protected void onDetachedFromWindow() {
        PlayerHolder.getInstance().removeUiObserver(observer); removeCallbacks(observer);
        if (behavior != null) behavior.removeBottomSheetCallback(sheetCallback);
        if (chrome != null) chrome.setPadding(chrome.getPaddingLeft(), chrome.getPaddingTop(),
                chrome.getPaddingRight(), originalBottomPadding);
        chrome = null; behavior = null; super.onDetachedFromWindow();
    }
    public void refresh() {
        PlayerHolder holder = PlayerHolder.getInstance();
        boolean visible = holder.isBackgroundAudio() && (behavior == null
                || (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN
                || behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED));
        setVisibility(visible ? VISIBLE : GONE);
        if (chrome != null) {
            int reserved = 0;
            int bottom = originalBottomPadding + reserved;
            if (chrome.getPaddingBottom() != bottom) chrome.setPadding(chrome.getPaddingLeft(),
                    chrome.getPaddingTop(), chrome.getPaddingRight(), bottom);
        }
        if (!visible) return;
        PlayQueue queue = holder.getPlayQueue(); PlayQueueItem item = queue == null ? null : queue.getItem();
        if (item != null && !java.util.Objects.equals(item.getUrl(), shownUrl)) {
            shownUrl = item.getUrl();
            title.setText(item.getTitle());
            if (item.getThumbnailUrl() != null) {
                PicassoHelper.loadThumbnail(item.getThumbnailUrl()).into(artwork);
            }
        }
        if (shownPlaying == null || holder.isPlaying() != shownPlaying) {
            shownPlaying = holder.isPlaying();
            toggle.setImageDrawable(HushIcons.drawable(getContext(),shownPlaying ? "pause" : "play"));
            toggle.setContentDescription(getContext().getString(shownPlaying
                    ? R.string.breath_pause : R.string.play_audio));
        }
    }
    private void openPlayer() {
        if (!(getContext() instanceof FragmentActivity)) return;
        PlayQueue queue = PlayerHolder.getInstance().getPlayQueue();
        PlayQueueItem item = queue == null ? null : queue.getItem();
        if (item != null) NavigationHelper.openVideoDetailFragment(getContext(),
                ((FragmentActivity)getContext()).getSupportFragmentManager(), item.getServiceId(),
                item.getUrl(), item.getTitle(), queue, true);
    }
}
