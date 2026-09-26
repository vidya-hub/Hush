package org.schabi.newpipe.fragments.list.search;

import org.junit.Test;
import static org.junit.Assert.*;

public final class BreakControllerTest {
    @Test public void meditationPauseExcludesBackgroundTime() {
        BreakController session = new BreakController();
        session.meditation = true;
        session.minutes = 1;
        session.start(1000);
        session.pause(11_000);
        assertEquals(10_000, session.elapsed(101_000));
        session.resume(101_000);
        assertEquals(15_000, session.elapsed(106_000));
        session.finish();
        assertEquals(60_000, session.elapsed(999_000));
        assertFalse(session.active);
        assertTrue(session.completed);
    }
    @Test public void stopAndNewControllerAlwaysOpenIdle() {
        BreakController session = new BreakController();
        session.start(0);
        session.pause(2000);
        session.stop();
        assertEquals(0, session.elapsed(9000));
        assertFalse(session.active);
        assertFalse(session.paused);
        assertFalse(new BreakController().active);
    }
}
