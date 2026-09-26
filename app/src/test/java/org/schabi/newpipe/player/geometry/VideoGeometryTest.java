package org.schabi.newpipe.player.geometry;
import org.junit.Test;
import static org.junit.Assert.*;
public class VideoGeometryTest {
    @Test public void wideVideoFitsWithoutStretch() {
        float[] fit=VideoGeometry.size(1000,1000,16f/9,false);
        assertEquals(1000,fit[0],.01f);assertEquals(562.5f,fit[1],.01f);
        assertEquals(16f/9,fit[0]/fit[1],.001f);
    }
    @Test public void verticalVideoFitsAndUniformlyCrops() {
        float ratio=9f/16;
        float[] fit=VideoGeometry.size(1200,700,ratio,false);
        assertEquals(700,fit[1],.01f);assertEquals(ratio,fit[0]/fit[1],.001f);
        float[] crop=VideoGeometry.size(1200,700,ratio,true);
        assertTrue(crop[1]>=700);assertEquals(1200,crop[0],.01f);assertEquals(ratio,crop[0]/crop[1],.001f);
    }
    @Test public void pixelRatioRotationAndInvalidMetadataAreHandled() {
        assertEquals(4f/3,VideoGeometry.aspect(720,576,16f/15,0),.001f);
        assertEquals(3f/4,VideoGeometry.aspect(720,576,16f/15,90),.001f);
        assertEquals(16f/9,VideoGeometry.aspect(1920,1080,Float.NaN,0),.001f);
        assertEquals(0,VideoGeometry.aspect(0,1080,1,0),0);
        assertArrayEquals(new float[]{300,200},VideoGeometry.size(300,200,0,false),0);
    }
}
