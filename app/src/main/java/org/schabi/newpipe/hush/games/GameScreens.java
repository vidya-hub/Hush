package org.schabi.newpipe.hush.games;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.schabi.newpipe.R;
import org.schabi.newpipe.hush.ui.HushUi;
import java.util.Random;

/** Each controller owns exactly one engine and its screen. The fragment owns navigation only. */
final class GameScreens {
    interface Screen { View view(); void pause(); void resume(); void save(); }
    static Screen create(Fragment fragment,String game){
        if("2048".equals(game))return new Twenty48Screen(fragment);
        if("snake".equals(game))return new SnakeScreen(fragment);
        if("sudoku".equals(game))return new SudokuScreen(fragment);
        return new Make24Screen(fragment);
    }
    abstract static class Base implements Screen {
        final Fragment fragment;
        final Context context;
        final GameStateStore store;
        final Random random=new Random();
        final Handler handler=new Handler(Looper.getMainLooper());
        final LinearLayout boardPane, tools;
        final View screen;
        final int ink,muted,card,border,accent;
        Base(Fragment fragment){
            this.fragment=fragment;context=fragment.requireContext();store=new GameStateStore(context);
            ink=HushUi.color(context,com.google.android.material.R.attr.colorOnSurface);
            muted=HushUi.color(context,com.google.android.material.R.attr.colorOnSurfaceVariant);
            card=HushUi.color(context,com.google.android.material.R.attr.colorSurfaceContainer);
            border=HushUi.color(context,com.google.android.material.R.attr.colorOutlineVariant);
            accent=HushUi.color(context,androidx.appcompat.R.attr.colorPrimary);
            boardPane=column();boardPane.setGravity(Gravity.CENTER_HORIZONTAL);tools=column();
            LinearLayout content=column();content.setPadding(dp(22),dp(12),dp(22),dp(28)); HushUi.bindContentWidth(content,480,864);
            HushUi.Panes panes=new HushUi.Panes(context,new HushUi.Bounded(context,boardPane,480),new HushUi.Bounded(context,tools,360));
            panes.setBreakpointDp(840);panes.setTag("hush-content-panes");content.addView(panes);screen=content;
        }
        @Override public View view(){return screen;}
        @Override public void resume(){ }
        @Override public void pause(){handler.removeCallbacksAndMessages(null);save();}
        int dp(int value){return HushUi.dp(context,value);}
        String str(int id){return context.getString(id);}
        LinearLayout column(){LinearLayout l=new LinearLayout(context);l.setOrientation(LinearLayout.VERTICAL);return l;}
        LinearLayout row(){LinearLayout l=new LinearLayout(context);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
        TextView text(String value,int size,boolean bold){TextView t=new TextView(context);t.setText(value);t.setTextSize(size);t.setTextColor(ink);
            if(bold)t.setTypeface(t.getTypeface(),1);return t;}
        MaterialButton button(int title,int icon,boolean primary){MaterialButton b=new MaterialButton(context);if(title!=0)b.setText(title);
            HushUi.style(b,primary);if(icon!=0){b.setIcon(org.schabi.newpipe.hush.ui.HushIcons.drawable(b.getContext(),icon));b.setIconSize(dp(20));}return b;}
        LinearLayout.LayoutParams top(int gap){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(gap);return lp;}
        void stats(LinearLayout parent,TextView score,TextView best){LinearLayout row=row();
            for(TextView t:new TextView[]{score,best}){t.setGravity(Gravity.CENTER);t.setPadding(dp(12),dp(12),dp(12),dp(12));
                t.setBackground(HushUi.shape(context,card,20,border));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
                if(t==best)lp.setMarginStart(dp(12));row.addView(t,lp);}parent.addView(row,top(0));}
        void confirm(Runnable action){pause();new MaterialAlertDialogBuilder(context).setTitle(R.string.hush_new_game)
                .setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.hush_new_game,(d,w)->action.run()).show();}
        void touchBoard(View board){board.getParent().requestDisallowInterceptTouchEvent(true);}
        void announce(View v,int text){v.announceForAccessibility(str(text));}
    }
    static final class Twenty48Screen extends Base {
        final GameModels.Twenty48 model;
        final GameBoards.TwentyBoard board;
        final TextView score,best;
        final MaterialButton undo;
        final java.util.ArrayDeque<Integer> queued=new java.util.ArrayDeque<>(2);
        boolean overShown;
        Twenty48Screen(Fragment f){super(f);model=new GameModels.Twenty48(random);model.load(store.read("2048"));
            board=new GameBoards.TwentyBoard(context,model);board.interrupted=queued::clear;boardPane.addView(board,top(0));
            score=text("",18,true);best=text("",18,true);stats(tools,score,best);
            undo=button(R.string.hush_undo,R.drawable.ic_hush_undo,false);tools.addView(undo,top(20));
            undo.setOnClickListener(v->{board.settle();queued.clear();if(model.undo()){overShown=false;save();render();}});
            MaterialButton reset=button(R.string.hush_new_game,R.drawable.ic_hush_restart,false);tools.addView(reset,top(12));
            reset.setOnClickListener(v->confirm(()->{board.settle();model.reset();overShown=false;queued.clear();save();render();}));
            TextView instruction=text(str(R.string.hush_swipe_move),15,false);instruction.setTextColor(muted);tools.addView(instruction,top(20));
            float[] down=new float[2];boolean[] gesture={false,false};
            board.setOnTouchListener((v,e)->{
                switch(e.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        touchBoard(board);down[0]=e.getX();down[1]=e.getY();gesture[0]=true;gesture[1]=false;return true;
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        if(gesture[0]&&!gesture[1]){
                            float dx=e.getX()-down[0],dy=e.getY()-down[1];
                            if(Math.max(Math.abs(dx),Math.abs(dy))>dp(24)){
                                gesture[1]=true;move(Math.abs(dx)>Math.abs(dy)?dx>0?1:0:dy>0?3:2);
                            }
                        }
                        if(e.getActionMasked()==MotionEvent.ACTION_UP){
                            if(gesture[0])v.performClick();gesture[0]=false;
                            board.getParent().requestDisallowInterceptTouchEvent(false);
                        }return true;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        gesture[0]=false;board.getParent().requestDisallowInterceptTouchEvent(false);return true;
                    default:return true;
                }
            });
            String[] labels={"Move left","Move right","Move up","Move down"};
            for(int i=0;i<4;i++){final int direction=i;ViewCompat.replaceAccessibilityAction(board,
                    new AccessibilityNodeInfoCompat.AccessibilityActionCompat(0x01020000+i,labels[i]),labels[i],
                    (v,a)->{move(direction);return true;});}
            render();
        }
        void move(int direction){
            if(board.animating()){if(queued.size()<2)queued.addLast(direction);return;}
            boolean wasOver=model.gameOver;
            GameModels.Twenty48.MoveResult result=model.moveDetailed(direction);
            if(result.changed||wasOver!=model.gameOver)save();render();
            if(result.changed)board.animate(result,()->{
                if(showOutcome()){queued.clear();return;}
                if(!queued.isEmpty())move(queued.removeFirst());
            });else showOutcome();
        }
        boolean showOutcome(){
            if(!fragment.isAdded()||fragment.getView()==null)return true;
            if(model.won&&!model.acknowledged){model.acknowledged=true;board.celebrate();save();
                new MaterialAlertDialogBuilder(context).setTitle(R.string.hush_you_win).setPositiveButton(R.string.hush_continue,null)
                    .setNegativeButton(R.string.hush_finish,(d,w)->{model.gameOver=true;save();board.invalidate();fragment.getParentFragmentManager().popBackStack();}).show();return true;
            } else if(model.gameOver&&!overShown){overShown=true;announce(board,R.string.hush_game_over);
                new MaterialAlertDialogBuilder(context).setTitle(R.string.hush_game_over).setNegativeButton(R.string.close,null)
                    .setPositiveButton(R.string.hush_new_game,(d,w)->{model.reset();overShown=false;save();render();}).show();return true;
            }
            return model.gameOver;
        }
        void render(){score.setText(str(R.string.hush_score)+"\n"+model.score);best.setText(str(R.string.hush_best)+"\n"+model.best);
            undo.setEnabled(model.canUndo());board.invalidate();}
        @Override public void pause(){board.settle();queued.clear();super.pause();}
        @Override public void save(){store.write("2048",model.save());}
    }
    static final class SnakeScreen extends Base {
        final GameModels.Snake model;
        final GameBoards.SnakeBoard board;
        final TextView score,best,hint;
        final MaterialButton playback;
        boolean started;
        boolean swiped,held,gestureActive;
        float x,y;
        long downAt;
        android.animation.ValueAnimator holding;
        final Runnable holdRestart=this::restartHeld;
        void restartHeld(){if(!swiped&&model.paused){held=true;reset();}}
        final GameStepClock stepClock=new GameStepClock();
        int renderedScore=-1,renderedBest=-1,renderedState=-1;
        boolean running;
        final Choreographer.FrameCallback tick=this::frame;
        void frame(long now){
            if(!running||model.paused||!model.alive)return;
            int steps=stepClock.advance(now);
            for(int i=0;i<steps&&model.alive;i++)step();
            board.progress=stepClock.progress(now);board.invalidate();
            if(running&&model.alive)Choreographer.getInstance().postFrameCallback(tick);
        }
        void step(){
            int old=model.score;board.beforeTick();model.tick();board.afterTick(model.score>old);
            if(model.score!=old||!model.alive){render();save();}
            if(!model.alive){running=false;announce(board,model.won?R.string.hush_you_win:R.string.hush_game_over);}
        }
        SnakeScreen(Fragment f){super(f);model=new GameModels.Snake(random);org.json.JSONObject saved=store.read("snake");model.load(saved);
            started=saved.optBoolean("started",saved.has("body"));score=text("",17,true);best=text("",17,true);
            stats(tools,score,best);board=new GameBoards.SnakeBoard(context,model);boardPane.addView(board,top(0));
            hint=text("",14,false);hint.setTextColor(muted);hint.setGravity(Gravity.CENTER);boardPane.addView(hint,top(12));
            playback=button(R.string.breath_resume,R.drawable.ic_hush_play,true);
            playback.setOnClickListener(v->{if(!model.alive)reset();if(model.paused)start();else pauseRound();
                });
            tools.addView(playback,top(16));
            MaterialButton restart=button(R.string.hush_new_game,R.drawable.ic_hush_restart,false);
            restart.setOnClickListener(v->confirm(this::reset));tools.addView(restart,top(12));
            board.setOnTouchListener((v,e)->touch(e));
            String[] actions={"Steer up","Steer right","Steer down","Steer left"};
            for(int i=0;i<4;i++){final int direction=i;ViewCompat.replaceAccessibilityAction(board,
                new AccessibilityNodeInfoCompat.AccessibilityActionCompat(0x01020100+i,actions[i]),actions[i],
                (v,a)->{model.turn(direction);if(model.paused&&model.alive)start();render();return true;});}
            ViewCompat.replaceAccessibilityAction(board,AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK,
                    "Pause or resume Snake",(v,a)->{if(!model.alive)reset();else if(model.paused)start();else pauseRound();return true;});
            ViewCompat.replaceAccessibilityAction(board,AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_LONG_CLICK,
                    "Restart paused round",(v,a)->{if(!model.paused)return false;reset();return true;});
            render();
        }
        boolean touch(MotionEvent e){
            switch(e.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    touchBoard(board);gestureActive=true;x=e.getX();y=e.getY();swiped=false;held=false;downAt=SystemClock.elapsedRealtime();
                    if(model.paused&&model.alive&&started){
                        holding=android.animation.ValueAnimator.ofFloat(0,1);holding.setDuration(600);
                        holding.addUpdateListener(a->{board.hold=(float)a.getAnimatedValue();board.invalidate();});holding.start();
                        handler.postDelayed(holdRestart,600);
                    }return true;
                case MotionEvent.ACTION_MOVE:
                    if(!gestureActive)return true;
                    float dx=e.getX()-x,dy=e.getY()-y;
                    if(Math.max(Math.abs(dx),Math.abs(dy))>=dp(18)){
                        cancelHold();swiped=true;model.turn(Math.abs(dx)>Math.abs(dy)?dx>0?1:3:dy>0?2:0);
                        x=e.getX();y=e.getY();if(model.alive&&model.paused)start();
                    }return true;
                case MotionEvent.ACTION_UP:
                    board.getParent().requestDisallowInterceptTouchEvent(false);
                    if(!gestureActive)return true;gestureActive=false;
                    cancelHold();if(!swiped&&!held&&SystemClock.elapsedRealtime()-downAt<600){
                        if(!model.alive){reset();start();}else {if(model.paused)start();else pauseRound();}
                    }save();render();vClick();return true;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_POINTER_DOWN:
                    gestureActive=false;cancelHold();board.invalidate();
                    board.getParent().requestDisallowInterceptTouchEvent(false);return true;
                default:return true;
            }
        }
        void vClick(){board.performClick();}
        void cancelHold(){handler.removeCallbacks(holdRestart);if(holding!=null){holding.cancel();holding=null;}board.hold=0;}
        void start(){
            if(!model.alive||running)return;
            cancelHold();started=true;model.paused=false;running=true;
            board.snap();stepClock.reset(System.nanoTime());step();render();save();
            if(running)Choreographer.getInstance().postFrameCallback(tick);
        }
        void pauseRound(){
            running=false;Choreographer.getInstance().removeFrameCallback(tick);
            model.paused=true;board.snap();save();render();
        }
        void reset(){
            running=false;Choreographer.getInstance().removeFrameCallback(tick);cancelHold();
            model.reset();board.snap();started=false;board.hold=0;save();render();
        }
        void render(){
            if(renderedScore!=model.score){score.setText(str(R.string.hush_score)+"\n"+model.score);renderedScore=model.score;}
            if(renderedBest!=model.best){best.setText(str(R.string.hush_best)+"\n"+model.best);renderedBest=model.best;}
            int state=model.won?4:!model.alive?3:!started?0:model.paused?1:2;
            if(state!=renderedState){
                renderedState=state;
                playback.setText(state==0||state==3||state==4?R.string.start:model.paused?R.string.breath_resume:R.string.breath_pause);
                playback.setIcon(org.schabi.newpipe.hush.ui.HushIcons.drawable(context,model.paused?"play":"pause"));
                hint.setText(R.string.hush_snake_running);
                hint.setVisibility(state==2?View.VISIBLE:View.GONE);
                board.title=model.won?str(R.string.hush_you_win):!model.alive?str(R.string.hush_game_over):!started?str(R.string.hush_swipe_start):str(R.string.hush_paused);
                board.hint=!model.alive?str(R.string.hush_tap_to_play):!started?str(R.string.hush_swipe_to_steer):str(R.string.hush_snake_paused);
            }
            board.invalidate();
        }
        @Override public void pause(){gestureActive=false;pauseRound();cancelHold();super.pause();}
        @Override public void save(){org.json.JSONObject saved=model.save();try{saved.put("started",started);}catch(org.json.JSONException ignored){}store.write("snake",saved);}
    }
    static final class SudokuScreen extends Base {
        final GameModels.Sudoku model;
        final GameBoards.SudokuBoard board;
        final TextView clock;
        final MaterialButton pause,notes;
        final GridLayout keypad;
        final LinearLayout toolRow;
        boolean pencil;
        long renderedSeconds=-1;
        Boolean renderedPaused;
        final Runnable tick=new Runnable(){@Override public void run(){renderClock();if(!model.paused)handler.postDelayed(this,1000);}};
        SudokuScreen(Fragment f){super(f);model=new GameModels.Sudoku(random);model.load(store.read("sudoku"));
            board=new GameBoards.SudokuBoard(context,model);boardPane.addView(board,top(0));
            LinearLayout stats=row();MaterialButton difficulty=button(0,0,false);difficulty.setText(model.difficulty==0?R.string.hush_easy:R.string.hush_medium);
            difficulty.setOnClickListener(v->{pause();new MaterialAlertDialogBuilder(context).setItems(new String[]{str(R.string.hush_easy),str(R.string.hush_medium)},
                    (d,w)->{model.pause(SystemClock.elapsedRealtime());model.newPuzzle(w);difficulty.setText(w==0?R.string.hush_easy:R.string.hush_medium);save();render();}).show();});
            LinearLayout.LayoutParams easyLp=new LinearLayout.LayoutParams(0,-2,1);easyLp.setMarginEnd(dp(6));
            stats.addView(difficulty,easyLp);
            clock=text("",16,true);clock.setGravity(Gravity.CENTER);clock.setMinHeight(dp(48));
            clock.setBackground(HushUi.shape(context,card,14,border));
            LinearLayout.LayoutParams clockLp=new LinearLayout.LayoutParams(0,dp(48),1);
            clockLp.setMarginStart(dp(6));clockLp.setMarginEnd(dp(6));stats.addView(clock,clockLp);
            pause=button(0,R.drawable.ic_hush_play,false);pause.setContentDescription(str(R.string.breath_resume));
            LinearLayout.LayoutParams pauseLp=new LinearLayout.LayoutParams(0,-2,1);pauseLp.setMarginStart(dp(6));
            stats.addView(pause,pauseLp);pause.setOnClickListener(v->toggle());tools.addView(stats,top(0));
            keypad=new GridLayout(context);keypad.setColumnCount(3);
            for(int n=1;n<=9;n++){final int number=n;MaterialButton key=button(0,0,false);key.setText(Integer.toString(n));key.setCornerRadius(dp(12));
                GridLayout.LayoutParams lp=new GridLayout.LayoutParams(GridLayout.spec((n-1)/3),GridLayout.spec((n-1)%3,1f));
                lp.width=0;lp.height=-2;lp.setMargins(n%3==1?0:dp(6),dp(6),n%3==0?0:dp(6),dp(6));keypad.addView(key,lp);
                key.setOnClickListener(v->{if(model.paused)return;model.set(model.selected,number,pencil);save();board.highlight(100);checkSolved();});}
            tools.addView(keypad,top(20));
            boolean stackTools = context.getResources().getConfiguration().fontScale > 1.3f;
            toolRow = stackTools ? column() : row();
            notes=button(R.string.hush_pencil,R.drawable.ic_hush_notes,false);notes.setOnClickListener(v->{pencil=!pencil;HushUi.style(notes,pencil); if(!stackTools)notes.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);});
            MaterialButton erase=button(R.string.hush_erase,R.drawable.ic_hush_erase,false);erase.setOnClickListener(v->{if(!model.paused){model.erase(model.selected);save();board.highlight(100);}});
            MaterialButton hint=button(R.string.hush_hint,R.drawable.ic_hush_hint,false);hint.setOnClickListener(v->{if(!model.paused){model.hint();save();board.highlight(300);checkSolved();}});
            for(MaterialButton b:new MaterialButton[]{notes,erase,hint}){
                b.setIconGravity(stackTools?MaterialButton.ICON_GRAVITY_TEXT_START:MaterialButton.ICON_GRAVITY_TEXT_TOP);
                b.setPadding(dp(8),dp(8),dp(8),dp(8));b.setIconPadding(dp(4));b.setTextSize(14);b.setMinHeight(dp(72));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(stackTools ? -1 : 0,-2,stackTools ? 0 : 1);
                lp.setMargins(b==notes?0:dp(6),stackTools ? dp(12) : 0,b==hint?0:dp(6),0);toolRow.addView(b,lp);}
            tools.addView(toolRow,top(16));MaterialButton reset=button(R.string.hush_new_puzzle,R.drawable.ic_hush_restart,false);
            reset.setOnClickListener(v->confirm(()->{model.newPuzzle(model.difficulty);save();render();}));tools.addView(reset,top(16));
            board.onSelect(this::select);
            final boolean[] gesture={false};
            board.setOnTouchListener((v,e)->{
                switch(e.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:touchBoard(board);gesture[0]=true;return true;
                    case MotionEvent.ACTION_UP:
                        board.getParent().requestDisallowInterceptTouchEvent(false);
                        boolean active=gesture[0];gesture[0]=false;
                        if(active&&e.getX()>=0&&e.getY()>=0&&e.getX()<board.getWidth()&&e.getY()<board.getHeight()){
                            select((int)(e.getY()*9/board.getHeight())*9+(int)(e.getX()*9/board.getWidth()));
                            board.performClick();
                        }return true;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        gesture[0]=false;board.getParent().requestDisallowInterceptTouchEvent(false);return true;
                    default:return true;
                }
            });render();
        }
        void select(int cell) {
            if (model.paused) toggle();
            if(model.paused||cell<0||cell>=81)return;
            model.selected=cell;board.highlight(120);save();
        }
        void toggle(){if(model.paused)model.resume(SystemClock.elapsedRealtime());else model.pause(SystemClock.elapsedRealtime());
            handler.removeCallbacks(tick);handler.post(tick);save();render();}
        void checkSolved(){if(!model.complete())return;model.pause(SystemClock.elapsedRealtime());save();render();board.celebrate();announce(board,R.string.hush_round_solved);
            new MaterialAlertDialogBuilder(context).setTitle(R.string.hush_round_solved).setPositiveButton(R.string.hush_new_puzzle,
                    (d,w)->{model.newPuzzle(model.difficulty);save();render();}).setNegativeButton(R.string.close,null).show();}
        void renderClock(){
            long seconds=model.elapsed(SystemClock.elapsedRealtime())/1000;
            if(seconds!=renderedSeconds){
                renderedSeconds=seconds;clock.setText(String.format(java.util.Locale.getDefault(),"%02d:%02d",seconds/60,seconds%60));
            }
        }
        void render(){
            renderClock();
            if(renderedPaused==null||renderedPaused!=model.paused){
                renderedPaused=model.paused;
                pause.setIcon(org.schabi.newpipe.hush.ui.HushIcons.drawable(pause.getContext(),model.paused?"play":"pause"));
                pause.setContentDescription(str(model.paused?R.string.breath_resume:R.string.breath_pause));
                for(int i=0;i<keypad.getChildCount();i++)keypad.getChildAt(i).setEnabled(!model.paused);
                for(int i=0;i<toolRow.getChildCount();i++)toolRow.getChildAt(i).setEnabled(!model.paused);
            }
            board.invalidate();
        }
        @Override public void pause(){model.pause(SystemClock.elapsedRealtime());render();super.pause();}
        @Override public void resume(){render();}
        @Override public void save(){store.write("sudoku",model.save(SystemClock.elapsedRealtime()));}
    }
    static final class Make24Screen extends Base {
        final GameModels.Make24 model;
        final LinearLayout terms;
        final TextView trail,preview;
        final MaterialButton[] operations=new MaterialButton[4];
        final MaterialButton undo,next,swap;
        int first=-1,second=-1;
        final java.util.List<GameModels.Make24.Term> displayed=new java.util.ArrayList<>();
        final java.util.List<TextView> tiles=new java.util.ArrayList<>();
        Make24Screen(Fragment f){super(f);model=new GameModels.Make24(random);model.load(store.read("make24"));
            boardPane.addView(text(str(R.string.hush_goal_24),23,true),top(8));terms=column();boardPane.addView(terms,top(24));
            preview=text("",16,false);
            LinearLayout ops=row();for(int i=0;i<4;i++){final int operation=i;MaterialButton b=button(0,0,false);operations[i]=b;b.setText(Character.toString(GameModels.Make24.OPERATIONS[i]));b.setTextSize(24);b.setPadding(dp(8),dp(8),dp(8),dp(8));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(6),0,dp(6),0);ops.addView(b,lp);
                b.setOnClickListener(v->{if(model.combine(first,second,operation)){first=second=-1;save();render();if(HushUi.motion(context)){terms.setTranslationY(dp(10));terms.setAlpha(.4f);terms.animate().translationY(0).alpha(1).setDuration(210).start();}if(model.solved)announce(terms,R.string.hush_round_solved);}else preview.setText(R.string.hush_invalid_move);});}
            boardPane.addView(ops,top(24));preview.setGravity(Gravity.CENTER);boardPane.addView(preview,top(20));
            swap=button(0,R.drawable.ic_hush_swap,false);swap.setContentDescription("Swap selected operands");swap.setOnClickListener(v->{int old=first;first=second;second=old;render();});boardPane.addView(swap,top(12));
            trail=text("",17,false);trail.setPadding(dp(20),dp(20),dp(20),dp(20));trail.setBackground(HushUi.shape(context,card,20,border));tools.addView(trail,top(8));
            undo=button(R.string.hush_undo,R.drawable.ic_hush_undo,false);undo.setOnClickListener(v->{if(model.undo()){first=second=-1;save();render();}});tools.addView(undo,top(16));
            MaterialButton hint=button(R.string.hush_hint,R.drawable.ic_hush_hint,false);hint.setOnClickListener(v->trail.setText(model.hint()));tools.addView(hint,top(12));
            MaterialButton skip=button(R.string.hush_skip,0,false);skip.setOnClickListener(v->{model.next();first=second=-1;save();render();});tools.addView(skip,top(12));
            next=button(R.string.hush_next_round,0,true);next.setOnClickListener(v->{model.next();first=second=-1;save();render();});tools.addView(next,top(12));render();
        }
        void render(){
            if(!displayed.equals(model.terms)){
                terms.removeAllViews();tiles.clear();displayed.clear();displayed.addAll(model.terms);
                int columns=context.getResources().getConfiguration().fontScale>1.3f?2:4;
                LinearLayout tileRow=null;
                for(int i=0;i<model.terms.size();i++){
                    if(i%columns==0){tileRow=row();terms.addView(tileRow,top(i==0?0:12));}
                    final int index=i;TextView tile=text("",24,true);tiles.add(tile);
                    tile.setGravity(Gravity.CENTER);tile.setMinHeight(dp(84));tile.setPadding(dp(4),dp(8),dp(4),dp(8));
                    HushUi.clickable(tile,card,20,border);
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
                    if(i%columns>0)lp.setMarginStart(dp(8));tileRow.addView(tile,lp);
                    tile.setOnClickListener(v->{
                        if(first==index){first=second;second=-1;}else if(second==index)second=-1;
                        else if(first<0)first=index;else second=index;render();
                    });
                }
            }
            for(int i=0;i<tiles.size();i++){
                TextView tile=tiles.get(i);GameModels.Make24.Term term=model.terms.get(i);
                String value=(i==first?"① ":i==second?"② ":"")+term;
                if(!value.contentEquals(tile.getText()))tile.setText(value);
                var background=(android.graphics.drawable.RippleDrawable)tile.getBackground();
                ((android.graphics.drawable.GradientDrawable)background.getDrawable(0)).setColor(
                        i==first||i==second?HushUi.color(context,com.google.android.material.R.attr.colorPrimaryContainer):card);
                tile.setContentDescription((i==first?"First operand. ":i==second?"Second operand. ":"")+term.expression);
                tile.setSelected(i==first||i==second);
            }
            swap.setEnabled(first>=0 && second>=0 && !model.solved);
            for(MaterialButton op:operations)op.setEnabled(first>=0&&second>=0&&!model.solved);
            preview.setText(first>=0&&second>=0?model.terms.get(first)+"  …  "+model.terms.get(second):str(R.string.hush_choose_terms));
            StringBuilder expression=new StringBuilder();for(GameModels.Make24.Term term:model.terms)if(term.expression.length()>1)expression.append(term.expression).append(" = ").append(term).append('\n');
            trail.setText(model.solved?str(R.string.hush_round_solved):expression.length()>0?expression.toString():str(R.string.hush_use_numbers));
            undo.setEnabled(model.canUndo());next.setVisibility(model.solved?View.VISIBLE:View.GONE);
        }
        @Override public void pause(){terms.animate().cancel();terms.setTranslationY(0);terms.setAlpha(1);super.pause();}
        @Override public void save(){store.write("make24",model.save());}
    }
}
