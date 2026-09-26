package org.schabi.newpipe.fragments.list.search;

/** Clock-driven breathing state, independent of Android animation timing. */
public final class BreathingSession {
    public enum Phase { INHALE, HOLD, EXHALE, REST }

    public enum Technique {
        CALM(new Phase[]{Phase.INHALE, Phase.EXHALE}, new int[]{4, 6}),
        BOX(new Phase[]{Phase.INHALE, Phase.HOLD, Phase.EXHALE, Phase.REST},
                new int[]{4, 4, 4, 4}),
        FOUR_SEVEN_EIGHT(new Phase[]{Phase.INHALE, Phase.HOLD, Phase.EXHALE},
                new int[]{4, 7, 8});

        private final Phase[] phases;
        private final int[] seconds;
        private final long cycleMs;

        Technique(final Phase[] phases, final int[] seconds) {
            this.phases = phases;
            this.seconds = seconds;
            long total = 0;
            for (final int duration : seconds) {
                total += duration * 1000L;
            }
            cycleMs = total;
        }
    }

    public static final class Snapshot {
        public final Phase phase;
        public final int phaseIndex;
        public final int secondsLeft;
        public final float phaseProgress;
        public final long elapsedMs;
        public final long totalMs;
        public final boolean complete;

        private Snapshot(final Phase phase, final int phaseIndex, final int secondsLeft,
                         final float phaseProgress, final long elapsedMs, final long totalMs,
                         final boolean complete) {
            this.phase = phase;
            this.phaseIndex = phaseIndex;
            this.secondsLeft = secondsLeft;
            this.phaseProgress = phaseProgress;
            this.elapsedMs = elapsedMs;
            this.totalMs = totalMs;
            this.complete = complete;
        }
    }

    private Technique technique = Technique.CALM;
    private long targetMs;
    private long elapsedBeforePauseMs;
    private long resumedAtMs;
    private boolean active;
    private boolean paused;

    public void start(final Technique selected, final int minutes, final long nowMs) {
        technique = selected;
        final long requestedMs = Math.max(1, minutes) * 60_000L;
        targetMs = requestedMs;
        elapsedBeforePauseMs = 0;
        resumedAtMs = nowMs;
        active = true;
        paused = false;
    }

    /** Restore an interrupted session as paused; no phase cue is replayed. */
    public void restore(final Technique selected, final long totalMs, final long elapsedMs) {
        technique = selected;
        targetMs = Math.max(1L, totalMs);
        elapsedBeforePauseMs = Math.min(targetMs, Math.max(0L, elapsedMs));
        resumedAtMs = 0L;
        active = elapsedBeforePauseMs < targetMs;
        paused = active;
    }

    public void pause(final long nowMs) {
        if (active && !paused) {
            elapsedBeforePauseMs += Math.max(0, nowMs - resumedAtMs);
            paused = true;
        }
    }

    public void resume(final long nowMs) {
        if (active && paused) {
            resumedAtMs = nowMs;
            paused = false;
        }
    }

    public void stop() {
        active = false;
        paused = false;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPaused() {
        return paused;
    }

    public Snapshot snapshot(final long nowMs) {
        final long elapsed = Math.min(targetMs, elapsedBeforePauseMs
                + (active && !paused ? Math.max(0, nowMs - resumedAtMs) : 0));
        final boolean complete = active && elapsed >= targetMs;
        long offset = complete ? technique.cycleMs - 1 : elapsed % technique.cycleMs;
        for (int i = 0; i < technique.phases.length; i++) {
            final long phaseMs = technique.seconds[i] * 1000L;
            if (offset < phaseMs) {
                return new Snapshot(technique.phases[i], i,
                        (int) Math.ceil((phaseMs - offset) / 1000.0),
                        offset / (float) phaseMs, elapsed, targetMs, complete);
            }
            offset -= phaseMs;
        }
        throw new IllegalStateException("Breathing phase not found");
    }
}
