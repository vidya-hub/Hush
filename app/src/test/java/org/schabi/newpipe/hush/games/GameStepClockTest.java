package org.schabi.newpipe.hush.games;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameStepClockTest {
    @Test public void frameJitterDoesNotAccumulateStepDrift() {
        var clock=new GameStepClock();clock.reset(0);
        assertEquals(0,clock.advance(160_000_000));
        assertEquals(1,clock.advance(180_000_000));
        assertEquals(10/170f,clock.progress(180_000_000),.0001f);
        assertEquals(1,clock.advance(342_000_000));
        assertEquals(2/170f,clock.progress(342_000_000),.0001f);
        assertEquals(0,clock.advance(350_000_000));
    }
    @Test public void suspensionDoesNotReplayManySteps() {
        var clock=new GameStepClock();clock.reset(0);
        assertEquals(1,clock.advance(9_000_000_000L));
        assertEquals(0,clock.progress(9_000_000_000L),0);
        assertEquals(0,clock.advance(9_016_000_000L));
    }
    @Test public void resumeStartsAFreshCadence() {
        var clock=new GameStepClock();clock.reset(0);clock.advance(170_000_000);
        clock.reset(3_000_000_000L);
        assertEquals(0,clock.advance(3_100_000_000L));
        assertEquals(1,clock.advance(3_170_000_000L));
    }
}
