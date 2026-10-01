package org.schabi.newpipe.hush.ui;

/** Window rules in dp, shared by chrome, panes and deterministic boundary tests. */
public final class TabletLayout {
    private TabletLayout() { }
    public static boolean rail(float width, float height) {
        return width >= 840 && width > height;
    }
    public static int gutter(float width) {
        return width >= 840 ? 32 : width >= 600 ? 24 : 22;
    }
    public static float bottomWidth(float width) {
        return Math.max(0, Math.min(560, width - 2 * gutter(width)));
    }
    public static int floatingWidth(float width) {
        return width >= 840 ? 300 : width >= 600 ? 240 : 208;
    }
    public static int mediaColumns(float width, float fontScale) {
        return fontScale >= 1.5f ? 1 : Math.max(1, Math.min(3, (int) (width / 264)));
    }
}
