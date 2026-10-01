package org.schabi.newpipe.hush.ui;
import org.junit.Test;
import static org.junit.Assert.*;
public class FloatingBoundsTest {
    @Test public void leavesNavigationAndGestureAreaClear(){
        FloatingBounds b=FloatingBounds.inWindow(390,844,0,0,24,34,0,84,200,22,12);
        assertEquals(200,b.width);assertEquals(113,b.height);assertEquals(168f,b.maxX,0);
        assertTrue(b.maxY+b.height<=844-34-84-12);
    }
    @Test public void keyboardReplacesSystemBottomRatherThanAddingToIt(){
        FloatingBounds b=FloatingBounds.inWindow(390,844,0,0,24,34,320,0,200,22,12);
        assertEquals(399f,b.maxY,0);
    }
    @Test public void narrowWindowsAndSideCutoutsNeverOverflow(){
        FloatingBounds b=FloatingBounds.inWindow(230,640,20,12,32,24,0,84,200,22,12);
        assertEquals(154,b.width);assertEquals(42f,b.minX,0);assertEquals(b.minX,b.maxX,0);
        assertTrue(b.maxX+b.width<=230-12-22);
    }
    @Test public void draggingClampsEveryEdge(){
        FloatingBounds b=FloatingBounds.inWindow(800,600,0,0,20,24,0,0,200,22,12);
        assertEquals(b.minX,b.x(-100),0);assertEquals(b.maxX,b.x(900),0);
        assertEquals(b.minY,b.y(-10),0);assertEquals(b.maxY,b.y(900),0);
    }
}
