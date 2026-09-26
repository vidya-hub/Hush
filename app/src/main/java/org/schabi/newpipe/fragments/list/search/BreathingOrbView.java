package org.schabi.newpipe.fragments.list.search;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.google.android.material.color.MaterialColors;

import org.schabi.newpipe.R;

/** A quiet, theme-colored breathing cue with a phase progress ring. */
public final class BreathingOrbView extends View {
    private static final long IDLE_PULSE_MS = 6000;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();
    private BreathingSession.Phase phase = BreathingSession.Phase.REST;
    private float progress;
    private boolean motionEnabled = true;
    private boolean idle = true;
    private ValueAnimator idlePulse;
    private float idlePulseValue;

    public BreathingOrbView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setState(final BreathingSession.Phase nextPhase, final float nextProgress,
                         final boolean animate) {
        phase = nextPhase;
        idle = false;
        progress = Math.max(0f, Math.min(1f, nextProgress));
        motionEnabled = animate;
        stopIdlePulse();
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startIdlePulseIfNeeded();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopIdlePulse();
        super.onDetachedFromWindow();
    }

    /** While resting, the orb breathes on its own unless system animations are disabled. */
    private void startIdlePulseIfNeeded() {
        if (!idle || idlePulse != null || !areAnimationsEnabled()) {
            return;
        }
        idlePulse = ValueAnimator.ofFloat(0f, 1f);
        idlePulse.setDuration(IDLE_PULSE_MS);
        idlePulse.setInterpolator(new LinearInterpolator());
        idlePulse.setRepeatCount(ValueAnimator.INFINITE);
        idlePulse.setRepeatMode(ValueAnimator.REVERSE);
        idlePulse.addUpdateListener(animation -> {
            idlePulseValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        idlePulse.start();
    }

    private void stopIdlePulse() {
        if (idlePulse != null) {
            idlePulse.cancel();
            idlePulse = null;
            idlePulseValue = 0f;
        }
    }

    private boolean areAnimationsEnabled() {
        return Settings.Global.getFloat(getContext().getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        final float size = Math.min(getWidth(), getHeight());
        final float cx = getWidth() / 2f;
        final float cy = getHeight() / 2f;
        final int primary = MaterialColors.getColor(this,
                R.attr.colorPrimary);
        final int container = MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorPrimaryContainer);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(container);
        canvas.drawCircle(cx, cy, size * 0.46f, paint);

        if (idle) {
            final float wave = idlePulse != null ? idlePulseValue : 0f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2f, size * 0.02f));
            paint.setColor(primary);
            paint.setAlpha(140);
            canvas.drawCircle(cx, cy, size * (0.32f + 0.05f * wave), paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(220);
            canvas.drawCircle(cx, cy, size * (0.13f + 0.05f * wave), paint);
            paint.setAlpha(255);
            return;
        }

        float scale = 0.80f;
        if (motionEnabled) {
            if (phase == BreathingSession.Phase.INHALE) {
                scale = 0.62f + 0.28f * progress;
            } else if (phase == BreathingSession.Phase.EXHALE) {
                scale = 0.90f - 0.28f * progress;
            } else if (phase == BreathingSession.Phase.HOLD) {
                scale = 0.90f;
            } else if (phase == BreathingSession.Phase.REST) {
                scale = 0.62f;
            }
        }
        paint.setColor(primary);
        paint.setAlpha(210);
        canvas.drawCircle(cx, cy, size * 0.43f * scale, paint);
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, size * 0.025f));
        paint.setColor(primary);
        arc.set(cx - size * 0.44f, cy - size * 0.44f,
                cx + size * 0.44f, cy + size * 0.44f);
        canvas.drawArc(arc, -90f, !motionEnabled ? 360f : 360f * progress,
                false, paint);
    }
}
