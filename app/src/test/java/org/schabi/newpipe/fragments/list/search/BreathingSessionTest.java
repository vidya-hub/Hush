package org.schabi.newpipe.fragments.list.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BreathingSessionTest {
    @Test
    public void calmMovesFromInhaleToExhaleAndEndsOnCycleBoundary() {
        final BreathingSession session = new BreathingSession();
        session.start(BreathingSession.Technique.CALM, 1, 1000);
        assertEquals(BreathingSession.Phase.INHALE, session.snapshot(1000).phase);
        assertEquals(BreathingSession.Phase.EXHALE, session.snapshot(5000).phase);
        assertEquals(6, session.snapshot(5000).secondsLeft);
        assertFalse(session.snapshot(60_999).complete);
        assertTrue(session.snapshot(61_000).complete);
    }

    @Test
    public void boxCyclesThroughFourPhases() {
        final BreathingSession session = new BreathingSession();
        session.start(BreathingSession.Technique.BOX, 3, 0);
        assertEquals(BreathingSession.Phase.HOLD, session.snapshot(4_000).phase);
        assertEquals(BreathingSession.Phase.EXHALE, session.snapshot(8_000).phase);
        assertEquals(BreathingSession.Phase.REST, session.snapshot(12_000).phase);
        assertEquals(BreathingSession.Phase.INHALE, session.snapshot(16_000).phase);
    }

    @Test
    public void pauseFreezesClockUntilResumed() {
        final BreathingSession session = new BreathingSession();
        session.start(BreathingSession.Technique.FOUR_SEVEN_EIGHT, 1, 0);
        session.pause(5_000);
        assertEquals(5_000, session.snapshot(55_000).elapsedMs);
        session.resume(55_000);
        assertEquals(8_000, session.snapshot(58_000).elapsedMs);
        assertEquals(BreathingSession.Phase.HOLD, session.snapshot(58_000).phase);
    }

    @Test
    public void boxEndsAtRequestedMinuteEvenMidCycle() {
        final BreathingSession session = new BreathingSession();
        session.start(BreathingSession.Technique.BOX, 1, 0);
        assertFalse(session.snapshot(59_999).complete);
        assertTrue(session.snapshot(60_000).complete);
    }

    @Test
    public void restoredSessionStaysPaused() {
        final BreathingSession session = new BreathingSession();
        session.restore(BreathingSession.Technique.CALM, 180_000, 31_000);
        assertTrue(session.isPaused());
        assertEquals(31_000, session.snapshot(80_000).elapsedMs);
    }
}
