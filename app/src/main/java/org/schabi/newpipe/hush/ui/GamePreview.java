package org.schabi.newpipe.hush.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Decorative, offline previews; not interactive boards or stored game state. */
public final class GamePreview extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tile = new RectF();
    private final String game;
    private final int accent;
    private final int surface;
    public GamePreview(Context c, String game) {
        super(c); this.game = game;
        accent = HushUi.color(c, androidx.appcompat.R.attr.colorPrimary);
        surface = HushUi.color(c, com.google.android.material.R.attr.colorSurfaceContainerHigh);
        paint.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(c,
                org.schabi.newpipe.R.font.manrope_family));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    @Override protected void onDraw(Canvas canvas) {
        float cell = Math.min(getWidth(), getHeight()) / 4f;
        paint.setTextAlign(Paint.Align.CENTER);
        if ("make24".equals(game)) {
            paint.setColor(surface);
            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), cell / 2, cell / 2, paint);
            paint.setColor(accent); paint.setTextSize(cell * 1.65f);
            canvas.drawText("24", getWidth() / 2f, getHeight() / 2f
                    - (paint.ascent() + paint.descent()) / 2, paint);
        } else if ("snake".equals(game)) {
            paint.setColor(surface); canvas.drawRoundRect(0, 0, getWidth(), getHeight(), cell / 3, cell / 3, paint);
            paint.setColor(accent);
            for (int i = 0; i < 5; i++) {
                float x = (i < 2 ? 0 : i - 2) * cell + cell / 4;
                float y = (i < 2 ? i : 2) * cell + cell / 4;
                canvas.drawRoundRect(x, y, x + cell * .8f, y + cell * .8f, cell / 4, cell / 4, paint);
            }
            canvas.drawCircle(cell * 3.25f, cell, cell / 4, paint);
        } else {
            int n = "sudoku".equals(game) ? 3 : 2;
            float side = Math.min(getWidth(), getHeight()) / (float)n;
            int[] values = {2,4,8,16};
            for (int i = 0; i < n*n; i++) {
                float x = i % n * side, y = i / n * side;
                paint.setColor(surface);
                tile.set(x + 2, y + 2, x + side - 2, y + side - 2);
                canvas.drawRoundRect(tile, side / 7, side / 7, paint);
                paint.setColor(accent); paint.setTextSize(side*.45f);
                if (n == 2 || i % 2 == 0) canvas.drawText(Integer.toString(n==2 ? values[i] : i+1),
                        x+side/2, y+side*.65f, paint);
            }
        }
    }
}
