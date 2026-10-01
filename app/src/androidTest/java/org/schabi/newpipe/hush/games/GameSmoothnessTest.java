package org.schabi.newpipe.hush.games;

import android.content.Intent;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.FrameMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GameSmoothnessTest {
    private final android.app.Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private MainActivity activity;
    private boolean privacy,hadPrivacy;
    private Map<String,String> rounds,backup;
    private void ui(Runnable action){instrumentation.runOnMainSync(action);}
    @Before public void setup() throws Exception {
        var context=instrumentation.getTargetContext();var prefs=PreferenceManager.getDefaultSharedPreferences(context);
        hadPrivacy=prefs.contains(HistoryRecordManager.INCOGNITO_KEY);privacy=prefs.getBoolean(HistoryRecordManager.INCOGNITO_KEY,false);
        var field=GameStateStore.class.getDeclaredField("PRIVATE_ROUNDS");field.setAccessible(true);
        rounds=(Map<String,String>)field.get(null);synchronized(rounds){backup=new HashMap<>(rounds);rounds.clear();}
        prefs.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY,true).commit();
        activity=(MainActivity)instrumentation.startActivitySync(new Intent(context,MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
    @After public void cleanup() {
        if(activity!=null)ui(()->{activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,GamesFragment.newInstance(null)).commitNow();activity.finish();});
        synchronized(rounds){rounds.clear();rounds.putAll(backup);}
        var edit=PreferenceManager.getDefaultSharedPreferences(instrumentation.getTargetContext()).edit();
        if(hadPrivacy)edit.putBoolean(HistoryRecordManager.INCOGNITO_KEY,privacy);else edit.remove(HistoryRecordManager.INCOGNITO_KEY);edit.commit();
    }
    private GameScreens.Screen open(String game) throws Exception {
        ui(()->activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,GamesFragment.newInstance(game)).commitNow());
        instrumentation.waitForIdleSync();SystemClock.sleep(700);
        var field=GamesFragment.class.getDeclaredField("screen");field.setAccessible(true);
        return (GameScreens.Screen)field.get(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_holder));
    }
    @Test public void snakeMovementDoesNotRelayoutControls() throws Exception {
        var snake=(GameScreens.SnakeScreen)open("snake");
        var layouts=new AtomicInteger();var frames=new ArrayList<Long>();var drawTimes=new ArrayList<Long>();
        android.view.ViewTreeObserver.OnGlobalLayoutListener listener=layouts::incrementAndGet;
        android.view.Window.OnFrameMetricsAvailableListener metrics=(window,frame,dropped)->{synchronized(frames){frames.add(frame.getMetric(FrameMetrics.TOTAL_DURATION));drawTimes.add(frame.getMetric(FrameMetrics.DRAW_DURATION));}};
        final Choreographer.FrameCallback[] steering=new Choreographer.FrameCallback[1];
        steering[0]=time->{
            if(!snake.model.alive||snake.model.paused)return;
            int head=snake.model.body.peekLast(),x=head%GameModels.Snake.SIZE,y=head/GameModels.Snake.SIZE;
            if(snake.model.direction==1&&x>=15)snake.model.turn(2);
            else if(snake.model.direction==2&&y>=15)snake.model.turn(3);
            else if(snake.model.direction==3&&x<=2)snake.model.turn(0);
            else if(snake.model.direction==0&&y<=2)snake.model.turn(1);
            Choreographer.getInstance().postFrameCallback(steering[0]);
        };
        ui(()->{snake.reset();snake.model.food=0;snake.start();Choreographer.getInstance().postFrameCallback(steering[0]);});
        SystemClock.sleep(500);instrumentation.waitForIdleSync();
        ui(()->{activity.getWindow().addOnFrameMetricsAvailableListener(metrics,new android.os.Handler(android.os.Looper.getMainLooper()));snake.view().getViewTreeObserver().addOnGlobalLayoutListener(listener);});
        SystemClock.sleep(3000);
        ui(()->{Choreographer.getInstance().removeFrameCallback(steering[0]);snake.view().getViewTreeObserver().removeOnGlobalLayoutListener(listener);activity.getWindow().removeOnFrameMetricsAvailableListener(metrics);snake.pause();});
        var durations=new ArrayList<Long>(frames);java.util.Collections.sort(durations);java.util.Collections.sort(drawTimes);
        var result=new org.json.JSONObject().put("layoutPassesDuringMovement",layouts.get()).put("frames",durations.size())
                .put("p95TotalMs",durations.isEmpty()?0:durations.get((durations.size()-1)*95/100)/1000000.0)
                .put("maxTotalMs",durations.isEmpty()?0:durations.get(durations.size()-1)/1000000.0)
                .put("p95DrawMs",drawTimes.isEmpty()?0:drawTimes.get((drawTimes.size()-1)*95/100)/1000000.0);
        var directory=new java.io.File(instrumentation.getTargetContext().getFilesDir(),"game-smoothness");directory.mkdirs();
        try(var file=new java.io.FileWriter(new java.io.File(directory,"snake-metrics.json"))){file.write(result.toString(2));}
        assertTrue("Snake should stay alive during the controlled circuit",snake.model.alive);
        assertTrue("Snake movement must not trigger page layout every step; measured "+layouts.get(),layouts.get()<=2);
        // Host software rendering speed is recorded, not used as a flaky gameplay assertion.
        assertFalse("Running Snake must produce display callbacks",durations.isEmpty());
        capture("snake");
    }
    @Test public void twenty48RespondsBeforeFingerLiftAndCancelDoesNotReplay() throws Exception {
        var game=(GameScreens.Twenty48Screen)open("2048");
        ui(()->{
            java.util.Arrays.fill(game.model.board,0);game.model.board[0]=2;
            int[] before=game.model.board.clone();long time=SystemClock.uptimeMillis();
            touch(game.board,time,MotionEvent.ACTION_DOWN,20,20);
            touch(game.board,time+20,MotionEvent.ACTION_MOVE,game.board.getWidth()*.8f,20);
            assertFalse("Swipe commits as soon as its threshold is reached",java.util.Arrays.equals(before,game.model.board));
            int[] committed=game.model.board.clone();touch(game.board,time+40,MotionEvent.ACTION_CANCEL,20,20);
            touch(game.board,time+50,MotionEvent.ACTION_UP,20,20);assertArrayEquals(committed,game.model.board);
        });
        capture("2048");
    }
    @Test public void sudokuFirstTapResumesAndSelectsTheTappedCell() throws Exception {
        var game=(GameScreens.SudokuScreen)open("sudoku");
        ui(()->{
            assertTrue(game.model.paused);long time=SystemClock.uptimeMillis();
            touch(game.board,time,MotionEvent.ACTION_DOWN,game.board.getWidth()/18f,game.board.getHeight()/18f);
            touch(game.board,time+20,MotionEvent.ACTION_UP,game.board.getWidth()/18f,game.board.getHeight()/18f);
            assertFalse(game.model.paused);assertEquals("The first tap also selects its cell",0,game.model.selected);
        });
        capture("sudoku");
    }
    @Test public void make24SelectionPreservesTermViews() throws Exception {
        var game=(GameScreens.Make24Screen)open("make24");
        ui(()->{
            View first=((ViewGroup)game.terms.getChildAt(0)).getChildAt(0);first.requestFocus();first.performClick();
            assertSame("Operand selection must not tear down the row",first,((ViewGroup)game.terms.getChildAt(0)).getChildAt(0));
        });
        capture("make24");
    }
    @Test public void snakeCancelledTouchCannotRestartOrPauseTheRound() throws Exception {
        var snake=(GameScreens.SnakeScreen)open("snake");
        ui(()->{
            snake.reset();snake.start();long time=SystemClock.uptimeMillis();
            touch(snake.board,time,MotionEvent.ACTION_DOWN,30,30);
            touch(snake.board,time+20,MotionEvent.ACTION_CANCEL,30,30);
            touch(snake.board,time+40,MotionEvent.ACTION_UP,30,30);
            assertFalse("A cancelled tap must not pause the game",snake.model.paused);
            snake.pauseRound();
            touch(snake.board,time+50,MotionEvent.ACTION_DOWN,30,30);
            touch(snake.board,time+70,MotionEvent.ACTION_CANCEL,30,30);
        });
        SystemClock.sleep(700);
        ui(()->{assertTrue(snake.started);assertTrue(snake.model.paused);assertEquals(0,snake.board.hold,0);});
    }
    @Test public void snakeConfirmationPausesMovementAndCancelKeepsItsRound() throws Exception {
        var snake=(GameScreens.SnakeScreen)open("snake");final String[] body={null};
        ui(()->{snake.reset();snake.start();snake.confirm(snake::reset);body[0]=snake.model.body.toString();});
        SystemClock.sleep(500);
        ui(()->{assertTrue(snake.model.paused);assertEquals(body[0],snake.model.body.toString());});
        instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);
        ui(()->{assertEquals(body[0],snake.model.body.toString());snake.start();assertFalse(snake.model.paused);snake.pause();});
    }
    @Test public void sudokuCancelledOrOutsideTapDoesNotSelectOrResume() throws Exception {
        var game=(GameScreens.SudokuScreen)open("sudoku");
        ui(()->{
            long time=SystemClock.uptimeMillis();
            touch(game.board,time,MotionEvent.ACTION_DOWN,20,20);
            touch(game.board,time+20,MotionEvent.ACTION_CANCEL,20,20);
            touch(game.board,time+30,MotionEvent.ACTION_UP,20,20);
            assertEquals(-1,game.model.selected);assertTrue(game.model.paused);
            touch(game.board,time+40,MotionEvent.ACTION_DOWN,20,20);
            touch(game.board,time+50,MotionEvent.ACTION_UP,-20,20);
            assertEquals(-1,game.model.selected);assertTrue(game.model.paused);
        });
    }
    @Test public void twenty48InterruptedAnimationDropsBufferedMoves() throws Exception {
        var game=(GameScreens.Twenty48Screen)open("2048");
        ui(()->{
            java.util.Arrays.fill(game.model.board,0);game.model.board[0]=2;
            game.move(1);game.move(3);game.move(0);
            assertFalse(game.queued.isEmpty());game.pause();assertTrue(game.queued.isEmpty());
            assertFalse(game.board.animating());
        });
    }
    @Test public void snakeGrowthInterpolatesTheNewHeadFromTheOldHead() throws Exception {
        var snake=(GameScreens.SnakeScreen)open("snake");
        var previousField=GameBoards.SnakeBoard.class.getDeclaredField("previous");previousField.setAccessible(true);
        var currentField=GameBoards.SnakeBoard.class.getDeclaredField("current");currentField.setAccessible(true);
        int[] previous=(int[])previousField.get(snake.board),current=(int[])currentField.get(snake.board);
        ui(()->{
            snake.reset();int oldHead=snake.model.body.peekLast();snake.model.food=oldHead+1;
            snake.start();assertEquals(4,snake.model.body.size());
            assertEquals("Growing head must start at the former head",oldHead,previous[3]);
            assertEquals(oldHead+1,current[3]);snake.pause();
        });
    }
    private void capture(String name) throws Exception {
        instrumentation.waitForIdleSync();
        var directory=new java.io.File(instrumentation.getTargetContext().getFilesDir(),"game-smoothness");directory.mkdirs();
        var bitmap=instrumentation.getUiAutomation().takeScreenshot();
        try(var output=new java.io.FileOutputStream(new java.io.File(directory,name+".png"))){
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,output);
        }finally{bitmap.recycle();}
    }
    private void touch(View view,long time,int action,float x,float y){MotionEvent event=MotionEvent.obtain(time,time,action,x,y,0);view.dispatchTouchEvent(event);event.recycle();}
}
