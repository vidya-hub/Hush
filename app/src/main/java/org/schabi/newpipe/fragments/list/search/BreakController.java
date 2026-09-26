package org.schabi.newpipe.fragments.list.search;

import androidx.lifecycle.ViewModel;

/** In-memory session state survives rotation, but never restores a running session after process death. */
public final class BreakController extends ViewModel {
    final BreathingSession breathing = new BreathingSession();
    BreathingSession.Technique technique = BreathingSession.Technique.CALM;
    boolean initialized;
    boolean meditation;
    boolean sound = true;
    boolean active;
    boolean paused;
    boolean completed;
    int minutes = 3;
    long elapsedBeforePause;
    long resumedAt;
    BreathingSession.Phase lastPhase;
    long elapsed(long now) { return elapsedBeforePause + (active && !paused ? Math.max(0,now-resumedAt) : 0); }
    void start(long now) {
        active=true; paused=false; completed=false; elapsedBeforePause=0; resumedAt=now; lastPhase=null;
        if (!meditation) breathing.start(technique,minutes,now);
    }
    void pause(long now) {
        if (!active || paused) return;
        elapsedBeforePause=elapsed(now); paused=true; breathing.pause(now);
    }
    void resume(long now) {
        if (!active || !paused) return;
        paused=false; resumedAt=now; breathing.resume(now);
    }
    void finish() { elapsedBeforePause=minutes*60_000L; active=false; paused=false; completed=true; breathing.stop(); }
    void stop() { active=false; paused=false; completed=false; elapsedBeforePause=0; breathing.stop(); }
}
