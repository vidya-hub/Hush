package org.schabi.newpipe.hush.ui;
import org.junit.Test;
import static org.junit.Assert.*;
public class TabletLayoutTest {
    @Test public void railRequiresBothWidthAndLandscapeWindow(){
        assertFalse(TabletLayout.rail(839,600));assertTrue(TabletLayout.rail(840,600));
        assertFalse(TabletLayout.rail(900,1280));assertFalse(TabletLayout.rail(600,900));
        assertFalse(TabletLayout.rail(840,840));
    }
    @Test public void bottomBarRetainsEqualGuttersWithoutStretching(){
        assertEquals(560,TabletLayout.bottomWidth(800),0);
        assertEquals(555,TabletLayout.bottomWidth(599),0);
        assertEquals(552,TabletLayout.bottomWidth(600),0);
        assertEquals(256,TabletLayout.bottomWidth(300),0);
    }
    @Test public void mediaAndFloatingSurfacesRespondToAvailableWidthAndText(){
        assertEquals(3,TabletLayout.mediaColumns(1100,1));assertEquals(2,TabletLayout.mediaColumns(700,1));
        assertEquals(1,TabletLayout.mediaColumns(1100,2));
        assertEquals(300,TabletLayout.floatingWidth(1280));assertEquals(240,TabletLayout.floatingWidth(600));
        assertEquals(208,TabletLayout.floatingWidth(599));
    }
    @Test public void floatingShrinksToShortKeyboardSafeRectangle(){
        FloatingBounds b=FloatingBounds.inWindow(600,400,0,0,24,24,260,0,300,24,12);
        assertTrue(b.maxY+b.height<=128);assertEquals(b.width*9f/16,b.height,1);
    }
}
