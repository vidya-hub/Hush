package org.schabi.newpipe.hush.ui;

/** Pixel geometry shared by the host and tests, independent of Android view lifecycle. */
public final class FloatingBounds {
    public final int width, height;
    public final float minX, maxX, minY, maxY;
    private FloatingBounds(int width,int height,float minX,float maxX,float minY,float maxY) {
        this.width=width;this.height=height;this.minX=minX;this.maxX=maxX;this.minY=minY;this.maxY=maxY;
    }
    public static FloatingBounds inWindow(int windowWidth,int windowHeight,int left,int right,
            int top,int bottom,int keyboard,int navigation,int preferredWidth,int gutter,int gap) {
        int width=Math.max(1,Math.min(preferredWidth,windowWidth-left-right-2*gutter));
        int availableHeight=Math.max(1,windowHeight-Math.max(bottom,keyboard)-navigation-top-2*gap);
        width=Math.min(width,Math.max(1,(int)(availableHeight*16f/9)));
        int height=Math.max(1,Math.round(width*9f/16));
        float minX=left+gutter,minY=top+gap;
        return new FloatingBounds(width,height,minX,Math.max(minX,windowWidth-right-gutter-width),
                minY,Math.max(minY,windowHeight-Math.max(bottom,keyboard)-navigation-gap-height));
    }
    public float x(float value){return Math.max(minX,Math.min(maxX,value));}
    public float y(float value){return Math.max(minY,Math.min(maxY,value));}
}
