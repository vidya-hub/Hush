package org.schabi.newpipe.hush.games;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;
import androidx.core.content.res.ResourcesCompat;
import org.schabi.newpipe.R;
import org.schabi.newpipe.hush.ui.HushUi;
import java.util.ArrayList;
import java.util.List;

/** Board rendering only. Rules and saved state belong to the engines. */
final class GameBoards {
    private GameBoards() { }
    abstract static class Square extends View {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        final int ink, muted, accent, card, surface, border;
        private ValueAnimator celebration;
        private float celebrationProgress = 1;
        void celebrate() {
            if (!HushUi.motion(getContext())) return;
            if (celebration != null) celebration.cancel();
            celebration = ValueAnimator.ofFloat(0, 1);
            celebration.setDuration(650);
            celebration.addUpdateListener(a -> {
                celebrationProgress = (float) a.getAnimatedValue(); invalidate();
            });
            celebration.start();
        }
        void drawCelebration(Canvas canvas) {
            if (celebrationProgress >= 1) return;
            canvas.save(); canvas.clipRect(0, 0, getWidth(), getHeight());
            paint.setStyle(Paint.Style.FILL); paint.setColor(accent);
            paint.setAlpha(Math.round(255 * (1 - celebrationProgress)));
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI / 8;
                float distance = getWidth() * .45f * celebrationProgress;
                canvas.drawCircle(getWidth() / 2f + (float) Math.cos(angle) * distance,
                        getHeight() / 2f + (float) Math.sin(angle) * distance,
                        HushUi.dp(getContext(), 3), paint);
            }
            paint.setAlpha(255); canvas.restore();
        }
        @Override protected void onDetachedFromWindow() {
            if (celebration != null) celebration.cancel();
            super.onDetachedFromWindow();
        }
        Square(Context c) {
            super(c); ink=HushUi.color(c,com.google.android.material.R.attr.colorOnSurface);
            muted=HushUi.color(c,com.google.android.material.R.attr.colorOnSurfaceVariant);
            accent=HushUi.color(c,androidx.appcompat.R.attr.colorPrimary);
            card=HushUi.color(c,com.google.android.material.R.attr.colorSurfaceContainer);
            surface=HushUi.color(c,com.google.android.material.R.attr.colorSurface);
            border=HushUi.color(c,com.google.android.material.R.attr.colorOutlineVariant);
            paint.setTypeface(ResourcesCompat.getFont(c,R.font.manrope_family)); setFocusable(true);
        }
        @Override protected void onMeasure(int w,int h) {
            int height=getRootView().getHeight();
            int max=HushUi.dp(getContext(),520);
            if(height>0)max=Math.min(max,Math.max(HushUi.dp(getContext(),180),height-HushUi.dp(getContext(),200)));
            int side=Math.min(MeasureSpec.getSize(w),max); setMeasuredDimension(side,side);
        }
        /** Keeps strokes and fills inside the view so a border centered on the edge is not clipped. */
        RectF inner(float stroke) {
            float inset = Math.max(1f, stroke);
            return new RectF(inset, inset, Math.max(inset + 1, getWidth() - inset),
                    Math.max(inset + 1, getHeight() - inset));
        }
        void clipRound(Canvas c, float radius) {
            RectF bounds = inner(2f);
            Path path = new Path();
            path.addRoundRect(bounds, radius, radius, Path.Direction.CW);
            c.save();
            c.clipPath(path);
        }
        void overlay(Canvas c,String title,String hint) {
            paint.setStyle(Paint.Style.FILL);paint.setColor((surface&0xffffff)|0xda000000);
            c.drawRoundRect(0,0,getWidth(),getHeight(),HushUi.dp(getContext(),20),HushUi.dp(getContext(),20),paint);
            paint.setColor(ink);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(getWidth()*.065f);
            c.drawText(title,getWidth()/2f,getHeight()/2f-8,paint);
            paint.setColor(muted);paint.setTextSize(getWidth()*.037f);
            c.drawText(hint,getWidth()/2f,getHeight()/2f+25,paint);
        }
    }
    static final class TwentyBoard extends Square {
        final GameModels.Twenty48 model;
        GameModels.Twenty48.MoveResult move;
        ValueAnimator animator;
        float progress=1;
        Runnable after;
        TwentyBoard(Context c,GameModels.Twenty48 model){super(c);this.model=model;setContentDescription("2048 board. Swipe to move tiles.");}
        boolean animating(){return animator!=null;}
        void settle(){if(animator!=null){ValueAnimator old=animator;animator=null;old.cancel();}move=null;progress=1;invalidate();}
        void animate(GameModels.Twenty48.MoveResult result,Runnable finished){
            settle();move=result;after=finished;
            if(!HushUi.motion(getContext())){move=null;invalidate();finished.run();return;}
            progress=0;animator=ValueAnimator.ofFloat(0,1);animator.setDuration(350);
            animator.setInterpolator(new android.view.animation.LinearInterpolator());
            animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});
            animator.addListener(new android.animation.AnimatorListenerAdapter(){
                @Override public void onAnimationEnd(android.animation.Animator a){
                    if(animator!=a)return;animator=null;move=null;invalidate();finished.run();
                }
            });animator.start();
        }
        @Override protected void onSizeChanged(int w,int h,int oldW,int oldH){super.onSizeChanged(w,h,oldW,oldH);settle();}
        @Override protected void onDetachedFromWindow(){settle();super.onDetachedFromWindow();}
        @Override protected void onDraw(Canvas c){
            clipRound(c, 20);
            float cell=getWidth()/4f;paint.setStyle(Paint.Style.FILL);paint.setColor(card);
            c.drawRect(0,0,getWidth(),getHeight(),paint);
            for(int i=0;i<16;i++)tile(c,i%4,i/4,0,cell,1);
            if(move!=null&&progress<130f/350){
                float fraction=progress*350/130;
                for(GameModels.Twenty48.TileMotion motion:move.motions){
                    float x=(motion.from%4)+(motion.to%4-motion.from%4)*fraction;
                    float y=(motion.from/4)+(motion.to/4-motion.from/4)*fraction;
                    tile(c,x,y,motion.value,cell,1);
                }
            }else{
                for(int i=0;i<16;i++){
                    int value=model.board[i];if(value==0)continue;
                    float scale=1;
                    if(move!=null){
                        if(i==move.spawnIndex){if(progress<240f/350)continue;scale=Math.min(1,(progress*350-240)/110);}
                        else if(progress<240f/350){
                            boolean merged=false;for(GameModels.Twenty48.TileMotion motion:move.motions)if(motion.to==i&&motion.merged)merged=true;
                            if(merged)scale=1+.12f*(float)Math.sin(Math.PI*(progress*350-130)/110);
                        }
                    }
                    tile(c,i%4,i/4,value,cell,scale);
                }
            }
            if(move!=null&&move.scoreDelta>0){paint.setColor(accent);paint.setTextSize(cell*.3f);paint.setTextAlign(Paint.Align.RIGHT);
                paint.setAlpha((int)(255*(1-progress)));c.drawText("+"+move.scoreDelta,getWidth()-12,30-progress*15,paint);paint.setAlpha(255);}
            c.restore();
            drawCelebration(c);
        }
        private void tile(Canvas c,float x,float y,int value,float cell,float scale){
            float padding=HushUi.dp(getContext(),4);float side=(cell-padding*2)*scale;
            float cx=(x+.5f)*cell,cy=(y+.5f)*cell;
            int fill=value==0?surface:value<8?0xffe9e5db:value<32?0xffd3dfcd:value<128?0xffb6ceae:value<512?0xff89ab83:0xff52705d;
            paint.setColor(fill);c.drawRoundRect(cx-side/2,cy-side/2,cx+side/2,cy+side/2,12,12,paint);
            if(value>0){paint.setColor(value>=512?0xfff6f3eb:0xff203529);paint.setTextSize(cell*(value>=1024?.24f:.34f));paint.setTextAlign(Paint.Align.CENTER);
                c.drawText(Integer.toString(value),cx,cy-(paint.ascent()+paint.descent())/2,paint);}
        }
    }
    static final class SnakeBoard extends Square {
        final GameModels.Snake model;
        List<Integer> previous=new ArrayList<>();
        long movedAt, foodAt;
        String title="",hint="";
        float hold;
        SnakeBoard(Context c,GameModels.Snake model){super(c);this.model=model;setContentDescription("Snake. Swipe to steer, tap to pause or resume, hold while paused to restart.");}
        void beforeTick(){previous=new ArrayList<>(model.body);}
        void afterTick(boolean ate){movedAt=SystemClock.elapsedRealtime();if(ate)foodAt=movedAt;invalidate();}
        @Override protected void onDraw(Canvas c){
            clipRound(c, 20);
            paint.setStyle(Paint.Style.FILL);paint.setColor(card);c.drawRect(0,0,getWidth(),getHeight(),paint);
            RectF bounds=inner(2f);
            float cell=bounds.width()/(float)GameModels.Snake.SIZE;
            paint.setColor(border);paint.setStrokeWidth(1);
            for(int i=1;i<GameModels.Snake.SIZE;i++){
                c.drawLine(bounds.left+i*cell,bounds.top,bounds.left+i*cell,bounds.bottom,paint);
                c.drawLine(bounds.left,bounds.top+i*cell,bounds.right,bounds.top+i*cell,paint);}
            float foodScale=HushUi.motion(getContext())?1+.25f*(1-Math.min(1,(SystemClock.elapsedRealtime()-foodAt)/160f)):1;
            paint.setColor(0xffcf9e68);c.drawCircle(bounds.left+(model.food%GameModels.Snake.SIZE+.5f)*cell,
                    bounds.top+(model.food/GameModels.Snake.SIZE+.5f)*cell,cell*.34f*foodScale,paint);
            float fraction=!model.alive||model.paused||!HushUi.motion(getContext())?1:Math.min(1,(SystemClock.elapsedRealtime()-movedAt)/170f);
            List<Integer> body=new ArrayList<>(model.body);paint.setColor(accent);
            for(int i=0;i<body.size();i++){
                int position=body.get(i);float x=position%GameModels.Snake.SIZE,y=position/GameModels.Snake.SIZE;
                if(i<previous.size()&&fraction<1){int old=previous.get(i);x=old%GameModels.Snake.SIZE+(x-old%GameModels.Snake.SIZE)*fraction;
                    y=old/GameModels.Snake.SIZE+(y-old/GameModels.Snake.SIZE)*fraction;}
                c.drawRoundRect(new RectF(bounds.left+x*cell+1,bounds.top+y*cell+1,bounds.left+(x+1)*cell-1,bounds.top+(y+1)*cell-1),cell*.24f,cell*.24f,paint);
            }
            if(model.paused||!model.alive){overlay(c,title,hint);
                if(hold>0){paint.setColor(accent);c.drawRect(getWidth()*.2f,getHeight()*.65f,getWidth()*(.2f+.6f*hold),getHeight()*.65f+6,paint);}}
            c.restore();
            if(!model.paused&&model.alive&&fraction<1)postInvalidateOnAnimation();
        }
    }
    static final class SudokuBoard extends Square {
        final GameModels.Sudoku model;
        ValueAnimator pulse;
        float selection=1;
        private java.util.function.IntConsumer select;
        final androidx.customview.widget.ExploreByTouchHelper cells;
        SudokuBoard(Context c,GameModels.Sudoku model){
            super(c);this.model=model;setContentDescription("Sudoku board");
            cells = new androidx.customview.widget.ExploreByTouchHelper(this) {
                @Override protected int getVirtualViewAt(float x, float y) {
                    if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return INVALID_ID;
                    return (int) (y * 9 / getHeight()) * 9 + (int) (x * 9 / getWidth());
                }
                @Override protected void getVisibleVirtualViews(List<Integer> ids) {
                    for (int i = 0; i < 81; i++) ids.add(i);
                }
                @Override protected void onPopulateNodeForVirtualView(int id,
                        androidx.core.view.accessibility.AccessibilityNodeInfoCompat node) {
                    float side = getWidth() / 9f;
                    node.setBoundsInParent(new android.graphics.Rect((int) (id % 9 * side),
                            (int) (id / 9 * side), (int) ((id % 9 + 1) * side),
                            (int) ((id / 9 + 1) * side)));
                    node.setContentDescription("Row " + (id / 9 + 1) + ", column " + (id % 9 + 1)
                            + (model.cells[id] == 0 ? ", empty" : ", " + model.cells[id])
                            + (model.clues[id] > 0 ? ", fixed clue" : "")
                            + (model.conflict(id) ? ", conflict" : ""));
                    node.setSelected(model.selected == id);
                    node.setClickable(true);
                    node.addAction(androidx.core.view.accessibility.AccessibilityNodeInfoCompat.ACTION_CLICK);
                }
                @Override protected boolean onPerformActionForVirtualView(int id, int action,
                        android.os.Bundle args) {
                    if (action != androidx.core.view.accessibility.AccessibilityNodeInfoCompat.ACTION_CLICK
                            || select == null) return false;
                    select.accept(id); invalidateRoot(); return true;
                }
            };
            androidx.core.view.ViewCompat.setAccessibilityDelegate(this, cells);
        }
        void onSelect(java.util.function.IntConsumer action) { select = action; }
        @Override public boolean dispatchHoverEvent(android.view.MotionEvent event) {
            return cells.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
        }
        @Override public boolean dispatchKeyEvent(android.view.KeyEvent event) {
            return cells.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
        }
        @Override protected void onFocusChanged(boolean focus, int direction,
                android.graphics.Rect previous) {
            super.onFocusChanged(focus, direction, previous); cells.onFocusChanged(focus, direction, previous);
        }
        void highlight(int duration){
            cells.invalidateRoot();
            if(pulse!=null)pulse.cancel();
            if(!HushUi.motion(getContext())){selection=1;invalidate();return;}
            pulse=ValueAnimator.ofFloat(0,1);pulse.setDuration(duration);
            pulse.addUpdateListener(a->{selection=(float)a.getAnimatedValue();invalidate();});pulse.start();
        }
        @Override protected void onDetachedFromWindow(){if(pulse!=null)pulse.cancel();super.onDetachedFromWindow();}
        @Override protected void onDraw(Canvas c){
            float stroke=2f; RectF bounds=inner(stroke); float side=bounds.width()/9f;
            c.save(); c.clipRect(bounds);
            paint.setStyle(Paint.Style.FILL); paint.setColor(surface); c.drawRect(bounds,paint);
            for(int i=0;i<81;i++){
                int row=i/9,col=i%9;float x=bounds.left+col*side,y=bounds.top+row*side;
                if(i==model.selected){paint.setColor(accent);paint.setAlpha(50+(int)(35*selection));c.drawRect(x,y,x+side,y+side,paint);paint.setAlpha(255);}
                else if(model.selected>=0&&(row==model.selected/9||col==model.selected%9||row/3==model.selected/27&&col/3==model.selected%9/3)){
                    paint.setColor(card);c.drawRect(x,y,x+side,y+side,paint);}
                paint.setTextAlign(Paint.Align.CENTER);
                if(model.cells[i]>0){paint.setColor(model.conflict(i)?0xffb75443:model.clues[i]>0?ink:accent);paint.setTextSize(side*.48f);
                    c.drawText(Integer.toString(model.cells[i]),x+side/2,y+side/2-(paint.ascent()+paint.descent())/2,paint);}
                else {paint.setColor(muted);paint.setTextSize(side*.2f);
                    for(int n=1;n<=9;n++)if((model.notes[i]&1<<n)!=0)c.drawText(Integer.toString(n),x+(n-1)%3*side/3+side/6,y+(n-1)/3*side/3+side/4,paint);}
            }
            for(int line=1;line<9;line++){paint.setColor(line%3==0?ink:border);paint.setStrokeWidth(line%3==0?2:1);
                float pos=bounds.left+line*side;
                c.drawLine(pos,bounds.top,pos,bounds.bottom,paint);c.drawLine(bounds.left,bounds.top+(line*side),bounds.right,bounds.top+(line*side),paint);}
            c.restore();
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(stroke);paint.setColor(ink);
            c.drawRect(bounds,paint);paint.setStyle(Paint.Style.FILL);
            if(model.paused&&!model.complete())overlay(c,getContext().getString(R.string.hush_paused),getContext().getString(R.string.hush_tap_to_resume));
            drawCelebration(c);
        }
    }
}
