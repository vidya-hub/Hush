package org.schabi.newpipe.player.geometry;

/** Shared proportional sizing for surfaces and textures; independent of Android for testing. */
public final class VideoGeometry {
    private VideoGeometry() { }
    public static float aspect(int width,int height,float pixelRatio,int rotation) {
        if(width<=0||height<=0)return 0;
        float pixels=Float.isFinite(pixelRatio)&&pixelRatio>0?pixelRatio:1;
        float ratio=width*pixels/height;
        return Math.floorMod(rotation,180)==0?ratio:1/ratio;
    }
    public static float[] size(float width,float height,float ratio,boolean crop) {
        if(width<=0||height<=0||!Float.isFinite(ratio)||ratio<=0)return new float[]{Math.max(0,width),Math.max(0,height)};
        if((ratio>width/height)==crop)return new float[]{height*ratio,height};
        return new float[]{width,width/ratio};
    }
}
