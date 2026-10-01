package org.schabi.newpipe.hush.games;

/** Fixed simulation steps on display frames, without accumulating callback execution time. */
final class GameStepClock {
    static final long STEP_NANOS = 170_000_000L;
    private long boundary;
    void reset(long now) { boundary=now; }
    int advance(long now) {
        long elapsed=Math.max(0,now-boundary);
        // A suspended or stalled UI must not replay a lethal burst of old movement.
        if(elapsed>=STEP_NANOS*3) {boundary=now;return 1;}
        int steps=(int)(elapsed/STEP_NANOS);
        boundary+=steps*STEP_NANOS;
        return steps;
    }
    float progress(long now) {
        return Math.max(0,Math.min(1,(now-boundary)/(float)STEP_NANOS));
    }
}
