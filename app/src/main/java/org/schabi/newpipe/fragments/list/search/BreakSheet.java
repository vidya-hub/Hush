package org.schabi.newpipe.fragments.list.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.schabi.newpipe.R;

/** A deliberate, on-demand pause. Closing the sheet always ends the session. */
public final class BreakSheet extends BottomSheetDialogFragment {
    private static final String TECHNIQUE_KEY = "hush_breath_technique";
    private static final String DURATION_KEY = "hush_breath_duration";
    private static final String SOUND_KEY = "hush_breath_sound";
    private final BreathingSession breathing = new BreathingSession();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private BreathingSound cues;
    private BreathingSession.Technique technique = BreathingSession.Technique.CALM;
    private BreathingSession.Phase lastPhase;
    private boolean meditation;
    private boolean sound = true;
    private boolean active;
    private boolean paused;
    private int minutes = 3;
    private long elapsedBeforePause;
    private long resumedAt;

    private TextView title;
    private TextView status;
    private TextView clock;
    private BreathingOrbView orb;
    private ImageView stillMark;
    private MaterialButton breatheTab;
    private MaterialButton meditateTab;
    private MaterialButton techniqueButton;
    private MaterialButton[] durationButtons;
    private MaterialButton primary;
    private MaterialButton end;
    private SwitchCompat soundSwitch;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            render();
            if (active && !paused) {
                handler.postDelayed(this, 80);
            }
        }
    };

    public static BreakSheet newInstance(final boolean meditate) {
        final BreakSheet sheet = new BreakSheet();
        final Bundle args = new Bundle();
        args.putBoolean("meditate", meditate);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable final Bundle state) {
        super.onCreate(state);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        meditation = getArguments() != null && getArguments().getBoolean("meditate");
        try {
            technique = BreathingSession.Technique.valueOf(prefs.getString(TECHNIQUE_KEY,
                    BreathingSession.Technique.CALM.name()));
        } catch (final IllegalArgumentException e) {
            technique = BreathingSession.Technique.CALM;
        }
        minutes = prefs.getInt(DURATION_KEY, 3);
        if (minutes != 1 && minutes != 3 && minutes != 5) {
            minutes = 3;
        }
        sound = prefs.getBoolean(SOUND_KEY, true);
        if (state != null) {
            meditation = state.getBoolean("meditate", meditation);
            minutes = state.getInt("minutes", minutes);
            sound = state.getBoolean("sound", sound);
            active = state.getBoolean("active");
            paused = active;
            elapsedBeforePause = state.getLong("elapsed");
            try {
                technique = BreathingSession.Technique.valueOf(state.getString("technique"));
            } catch (final RuntimeException ignored) {
                technique = BreathingSession.Technique.CALM;
            }
            if (active && !meditation) {
                breathing.restore(technique, minutes * 60_000L, elapsedBeforePause);
                lastPhase = breathing.snapshot(SystemClock.elapsedRealtime()).phase;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final android.view.LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle state) {
        final Context c = requireContext();
        final int ink = attribute(com.google.android.material.R.attr.colorOnSurface);
        final int muted = attribute(com.google.android.material.R.attr.colorOnSurfaceVariant);
        final LinearLayout root = column(c);
        root.setPadding(dp(20), dp(10), dp(20), dp(24));
        final ScrollView scroll = new ScrollView(c);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        final LinearLayout content = column(c);
        scroll.addView(content);
        final int availableHeight = c.getResources().getDisplayMetrics().heightPixels;
        root.addView(scroll, new LinearLayout.LayoutParams(-1,
                Math.min(dp(420), Math.round(availableHeight * 0.52f))));

        final View handle = new View(c);
        final android.graphics.drawable.GradientDrawable handleShape = new android.graphics.drawable.GradientDrawable();
        handleShape.setColor(muted);
        handleShape.setCornerRadius(dp(3));
        handle.setBackground(handleShape);
        final LinearLayout.LayoutParams handleLp = new LinearLayout.LayoutParams(dp(40), dp(4));
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(handle, handleLp);

        final android.widget.FrameLayout titleRow = new android.widget.FrameLayout(c);
        title = label(c, R.string.hush_moment_yourself, 25, ink, true);
        title.setGravity(Gravity.CENTER);
        titleRow.addView(title, new android.widget.FrameLayout.LayoutParams(-1, dp(52)));
        final TextView close = label(c, R.string.close, 18, ink, false);
        close.setText("×");
        close.setGravity(Gravity.CENTER);
        close.setContentDescription(getString(R.string.close));
        final android.widget.FrameLayout.LayoutParams closeLp =
                new android.widget.FrameLayout.LayoutParams(dp(48), dp(48), Gravity.END);
        titleRow.addView(close, closeLp);
        close.setOnClickListener(v -> dismiss());
        content.addView(titleRow, top(22));
        final LinearLayout tabs = row(c);
        breatheTab = button(c, R.string.hush_breathe);
        meditateTab = button(c, R.string.hush_meditate);
        tabs.addView(breatheTab, new LinearLayout.LayoutParams(0, dp(48), 1));
        tabs.addView(meditateTab, new LinearLayout.LayoutParams(0, dp(48), 1));
        content.addView(tabs, top(18));
        breatheTab.setOnClickListener(v -> selectMode(false));
        meditateTab.setOnClickListener(v -> selectMode(true));

        final LinearLayout visual = column(c);
        visual.setGravity(Gravity.CENTER);
        orb = new BreathingOrbView(c, null);
        final LinearLayout.LayoutParams orbLp = new LinearLayout.LayoutParams(dp(148), dp(148));
        orbLp.gravity = Gravity.CENTER_HORIZONTAL;
        visual.addView(orb, orbLp);
        stillMark = new ImageView(c);
        stillMark.setImageResource(R.drawable.ic_hush_mark);
        stillMark.setImageTintList(ColorStateList.valueOf(ink));
        final LinearLayout.LayoutParams markLp = new LinearLayout.LayoutParams(dp(100), dp(100));
        markLp.gravity = Gravity.CENTER_HORIZONTAL;
        visual.addView(stillMark, markLp);
        status = label(c, R.string.breath_technique_calm, 17, ink, true);
        status.setGravity(Gravity.CENTER);
        visual.addView(status, top(14));
        clock = label(c, R.string.breath_duration_3, 15, muted, false);
        clock.setGravity(Gravity.CENTER);
        visual.addView(clock, top(4));
        content.addView(visual, top(22));

        techniqueButton = button(c, R.string.breath_technique_calm);
        content.addView(techniqueButton, top(20));
        techniqueButton.setOnClickListener(v -> chooseTechnique());
        content.addView(label(c, R.string.duration, 14, ink, true), top(18));
        final LinearLayout durations = row(c);
        durationButtons = new MaterialButton[3];
        final int[] values = {1, 3, 5};
        final int[] titles = {R.string.breath_duration_1, R.string.breath_duration_3,
                R.string.breath_duration_5};
        for (int i = 0; i < 3; i++) {
            final int chosen = values[i];
            final MaterialButton item = button(c, titles[i]);
            durationButtons[i] = item;
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), 1);
            if (i > 0) {
                lp.setMarginStart(dp(8));
            }
            durations.addView(item, lp);
            item.setOnClickListener(v -> {
                if (active) {
                    return;
                }
                minutes = chosen;
                saveOptions();
                render();
            });
        }
        content.addView(durations, top(8));
        final LinearLayout soundRow = row(c);
        soundRow.setGravity(Gravity.CENTER_VERTICAL);
        final TextView soundLabel = label(c, R.string.breath_sound_label, 16, ink, false);
        soundRow.addView(soundLabel, new LinearLayout.LayoutParams(0, dp(52), 1));
        soundLabel.setGravity(Gravity.CENTER_VERTICAL);
        soundSwitch = new SwitchCompat(c);
        soundSwitch.setChecked(sound);
        soundSwitch.setContentDescription(getString(R.string.breath_sound_label));
        soundSwitch.setOnCheckedChangeListener((view, checked) -> {
            sound = checked;
            saveOptions();
            if (!checked && cues != null) {
                cues.cancelPending();
            }
        });
        soundRow.addView(soundSwitch);
        content.addView(soundRow, top(12));

        primary = button(c, R.string.hush_start_breathing);
        primary.setMinHeight(dp(54));
        root.addView(primary, top(8));
        primary.setOnClickListener(v -> {
            if (!active) {
                start();
            } else if (paused) {
                resume();
            } else {
                pause();
            }
        });
        end = button(c, R.string.breath_end);
        root.addView(end, top(4));
        end.setOnClickListener(v -> stop());
        render();
        return root;
    }

    @Override public void onResume() {
        super.onResume();
        render();
    }

    @Override public void onPause() {
        if (active && !paused) {
            pause();
        }
        super.onPause();
    }

    @Override public void onSaveInstanceState(@NonNull final Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("meditate", meditation);
        out.putBoolean("sound", sound);
        out.putInt("minutes", minutes);
        out.putString("technique", technique.name());
        out.putBoolean("active", active);
        out.putLong("elapsed", elapsed());
    }

    @Override public void onDestroyView() {
        handler.removeCallbacks(tick);
        if (cues != null) {
            cues.release();
            cues = null;
        }
        super.onDestroyView();
    }

    @Override public void onDismiss(@NonNull final android.content.DialogInterface dialog) {
        stop();
        super.onDismiss(dialog);
    }

    private void selectMode(final boolean next) {
        if (active) {
            stop();
        }
        meditation = next;
        render();
    }

    private void chooseTechnique() {
        if (active) {
            return;
        }
        final String[] names = {getString(R.string.breath_technique_calm),
                getString(R.string.breath_technique_box),
                getString(R.string.breath_technique_478)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.hush_breathe)
                .setItems(names, (dialog, which) -> {
                    technique = BreathingSession.Technique.values()[which];
                    saveOptions();
                    render();
                }).show();
    }

    private void start() {
        active = true;
        paused = false;
        elapsedBeforePause = 0;
        resumedAt = SystemClock.elapsedRealtime();
        lastPhase = null;
        if (!meditation) {
            breathing.start(technique, minutes, resumedAt);
        } else if (sound) {
            cue(BreathingSession.Phase.HOLD);
        }
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    private void pause() {
        if (!active || paused) {
            return;
        }
        final long now = SystemClock.elapsedRealtime();
        elapsedBeforePause = elapsed(now);
        paused = true;
        if (!meditation) {
            breathing.pause(now);
        }
        handler.removeCallbacks(tick);
        if (cues != null) {
            cues.cancelPending();
        }
        render();
    }

    private void resume() {
        if (!active || !paused) {
            return;
        }
        paused = false;
        resumedAt = SystemClock.elapsedRealtime();
        if (!meditation) {
            breathing.resume(resumedAt);
            lastPhase = breathing.snapshot(resumedAt).phase;
        }
        handler.post(tick);
        render();
    }

    private void stop() {
        active = false;
        paused = false;
        elapsedBeforePause = 0;
        breathing.stop();
        handler.removeCallbacks(tick);
        if (cues != null) {
            cues.cancelPending();
        }
        if (getView() != null) {
            render();
        }
    }

    private long elapsed() {
        return elapsed(SystemClock.elapsedRealtime());
    }

    private long elapsed(final long now) {
        return elapsedBeforePause + (active && !paused ? Math.max(0, now - resumedAt) : 0);
    }

    private void render() {
        if (getView() == null || primary == null) {
            return;
        }
        final int primaryColor = attribute(R.attr.colorPrimary);
        final int onPrimary = attribute(com.google.android.material.R.attr.colorOnPrimary);
        final int unselected = attribute(com.google.android.material.R.attr.colorSurfaceContainerHigh);
        breatheTab.setBackgroundTintList(ColorStateList.valueOf(meditation ? unselected : primaryColor));
        meditateTab.setBackgroundTintList(ColorStateList.valueOf(meditation ? primaryColor : unselected));
        breatheTab.setTextColor(meditation ? primaryColor : onPrimary);
        meditateTab.setTextColor(meditation ? onPrimary : primaryColor);
        orb.setVisibility(meditation ? View.GONE : View.VISIBLE);
        stillMark.setVisibility(meditation ? View.VISIBLE : View.GONE);
        techniqueButton.setVisibility(meditation ? View.GONE : View.VISIBLE);
        techniqueButton.setText(technique == BreathingSession.Technique.BOX
                ? R.string.breath_technique_box : technique == BreathingSession.Technique.FOUR_SEVEN_EIGHT
                ? R.string.breath_technique_478 : R.string.breath_technique_calm);
        techniqueButton.setBackgroundTintList(ColorStateList.valueOf(unselected));
        techniqueButton.setTextColor(primaryColor);
        techniqueButton.setEnabled(!active);
        for (int i = 0; i < durationButtons.length; i++) {
            final int value = i == 0 ? 1 : i == 1 ? 3 : 5;
            durationButtons[i].setBackgroundTintList(ColorStateList.valueOf(
                    minutes == value ? primaryColor : unselected));
            durationButtons[i].setTextColor(minutes == value ? onPrimary : primaryColor);
            durationButtons[i].setEnabled(!active);
        }
        end.setVisibility(active ? View.VISIBLE : View.GONE);
        primary.setText(!active ? meditation ? R.string.hush_start_meditation
                : R.string.hush_start_breathing : paused ? R.string.breath_resume
                : R.string.breath_pause);
        if (!active) {
            status.setText(meditation ? getString(R.string.hush_quiet_timer)
                    : getString(technique == BreathingSession.Technique.BOX
                    ? R.string.breath_technique_box :
                    technique == BreathingSession.Technique.FOUR_SEVEN_EIGHT
                    ? R.string.breath_technique_478 : R.string.breath_technique_calm));
            clock.setText(meditation ? getString(R.string.hush_meditation_hint)
                    : getString(R.string.breath_duration_minutes, minutes));
            return;
        }
        final long remaining = Math.max(0, minutes * 60_000L - elapsed());
        if (remaining == 0) {
            if (meditation && sound) {
                cue(BreathingSession.Phase.HOLD);
            }
            stop();
            return;
        }
        final int seconds = (int) Math.ceil(remaining / 1000d);
        clock.setText(getString(R.string.breath_time_remaining, seconds / 60, seconds % 60));
        if (meditation) {
            status.setText(R.string.hush_quiet_timer);
        } else {
            final BreathingSession.Snapshot snapshot =
                    breathing.snapshot(SystemClock.elapsedRealtime());
            final int label = snapshot.phase == BreathingSession.Phase.INHALE
                    ? R.string.breath_inhale : snapshot.phase == BreathingSession.Phase.EXHALE
                    ? R.string.breath_exhale : snapshot.phase == BreathingSession.Phase.HOLD
                    ? R.string.breath_hold : R.string.breath_rest;
            status.setText(getString(label, snapshot.secondsLeft));
            final boolean motion = Settings.Global.getFloat(requireContext().getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f;
            orb.setState(snapshot.phase, snapshot.phaseProgress, motion);
            if (sound && !paused && snapshot.phase != lastPhase) {
                cue(snapshot.phase);
            }
            lastPhase = snapshot.phase;
        }
    }

    private void cue(final BreathingSession.Phase phase) {
        if (cues == null) {
            cues = new BreathingSound(requireContext());
        }
        cues.play(phase);
    }

    private void saveOptions() {
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                .putString(TECHNIQUE_KEY, technique.name())
                .putInt(DURATION_KEY, minutes)
                .putBoolean(SOUND_KEY, sound).apply();
    }

    private int attribute(final int id) {
        final TypedValue value = new TypedValue();
        requireContext().getTheme().resolveAttribute(id, value, true);
        return value.resourceId != 0 ? androidx.core.content.ContextCompat.getColor(
                requireContext(), value.resourceId) : value.data;
    }

    private int dp(final int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private LinearLayout column(final Context c) {
        final LinearLayout v = new LinearLayout(c);
        v.setOrientation(LinearLayout.VERTICAL);
        return v;
    }

    private LinearLayout row(final Context c) {
        final LinearLayout v = new LinearLayout(c);
        v.setOrientation(LinearLayout.HORIZONTAL);
        return v;
    }

    private LinearLayout.LayoutParams top(final int margin) {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(margin);
        return lp;
    }

    private TextView label(final Context c, final int text, final int size,
                           final int color, final boolean bold) {
        final TextView view = new TextView(c);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        if (bold) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return view;
    }

    private MaterialButton button(final Context c, final int text) {
        final MaterialButton button = new MaterialButton(c);
        button.setText(text);
        button.setCornerRadius(dp(24));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setMinimumHeight(dp(48));
        return button;
    }
}
