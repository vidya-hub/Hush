package org.schabi.newpipe.hush.games;

import android.content.Intent;
import android.os.SystemClock;
import android.view.Choreographer;
import android.widget.TextView;
import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.fragments.list.search.BreakSessionFragment;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

/** Real UI captures of isolated demonstration rounds; user saves are restored. */
@RunWith(AndroidJUnit4.class)
public class GameShowcaseCaptureTest {
    private final android.app.Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private MainActivity activity;
    private void ui(Runnable action){instrumentation.runOnMainSync(action);}
    private void settle(){SystemClock.sleep(450);instrumentation.waitForIdleSync();}
    @Test public void captureGameplayAndBreathing() throws Exception {
        var context=instrumentation.getTargetContext();var prefs=PreferenceManager.getDefaultSharedPreferences(context);
        boolean hadPrivacy=prefs.contains(HistoryRecordManager.INCOGNITO_KEY),privacy=prefs.getBoolean(HistoryRecordManager.INCOGNITO_KEY,false);
        var field=GameStateStore.class.getDeclaredField("PRIVATE_ROUNDS");field.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String,String> rounds=(Map<String,String>)field.get(null);
        Map<String,String> backup;synchronized(rounds){backup=new HashMap<>(rounds);rounds.clear();}
        prefs.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY,true).commit();
        final Choreographer.FrameCallback[] pilot={null};
        try {
            activity=(MainActivity)instrumentation.startActivitySync(new Intent(context,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));settle();
            var tiles=(GameScreens.Twenty48Screen)open("2048");
            for(int i=0;i<28;i++){final int direction=new int[]{0,3,1,2}[i%4];ui(()->tiles.move(direction));SystemClock.sleep(250);}
            settle();ui(()->assertTrue("Capture an actual scored round",tiles.model.score>0));capture("game-2048");

            var snake=(GameScreens.SnakeScreen)open("snake");
            ui(()->{
                snake.model.paused=false;
                for(int i=0;i<180&&snake.model.score<4&&snake.model.alive;i++){snake.model.turn(nextSnakeTurn(snake.model));snake.model.tick();}
                assertTrue(snake.model.alive);assertTrue(snake.model.score>=4);
                snake.model.paused=true;snake.board.snap();snake.model.turn(nextSnakeTurn(snake.model));snake.start();
                pilot[0]=time->{if(!snake.model.alive||snake.model.paused)return;snake.model.turn(nextSnakeTurn(snake.model));Choreographer.getInstance().postFrameCallback(pilot[0]);};
                Choreographer.getInstance().postFrameCallback(pilot[0]);
            });settle();capture("game-snake");
            ui(()->{Choreographer.getInstance().removeFrameCallback(pilot[0]);snake.pause();});pilot[0]=null;

            var sudoku=(GameScreens.SudokuScreen)open("sudoku");
            ui(()->{
                int filled=0;
                for(int i=0;i<81&&filled<5;i++)if(sudoku.model.clues[i]==0){sudoku.select(i);sudoku.keypad.getChildAt(sudoku.model.solution[i]-1).performClick();filled++;}
                assertFalse(sudoku.model.paused);
            });settle();capture("game-sudoku");

            var arithmetic=(GameScreens.Make24Screen)open("make24");
            ui(()->{arithmetic.tiles.get(0).performClick();arithmetic.tiles.get(1).performClick();arithmetic.operations[0].performClick();});settle();
            ui(()->{assertEquals(3,arithmetic.model.terms.size());arithmetic.tiles.get(0).performClick();arithmetic.tiles.get(2).performClick();});
            settle();capture("game-make24");

            var breathe=BreakSessionFragment.newInstance(false);
            ui(()->activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,breathe).commitNow());settle();capture("breathe-setup");
            var primary=BreakSessionFragment.class.getDeclaredField("primary");primary.setAccessible(true);
            var button=(android.view.View)primary.get(breathe);ui(button::performClick);SystemClock.sleep(1200);
            var phase=BreakSessionFragment.class.getDeclaredField("phase");phase.setAccessible(true);
            ui(()->assertFalse(((TextView)read(phase,breathe)).getText().toString().isEmpty()));capture("breathe-active");
        } finally {
            ui(()->{if(pilot[0]!=null)Choreographer.getInstance().removeFrameCallback(pilot[0]);if(activity!=null){activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,GamesFragment.newInstance(null)).commitNow();activity.finish();}});
            synchronized(rounds){rounds.clear();rounds.putAll(backup);}
            var edit=prefs.edit();if(hadPrivacy)edit.putBoolean(HistoryRecordManager.INCOGNITO_KEY,privacy);else edit.remove(HistoryRecordManager.INCOGNITO_KEY);edit.commit();
        }
    }
    @Test public void breathingActionsRefreshStateControls() throws Exception {
        var context=instrumentation.getTargetContext();
        activity=(MainActivity)instrumentation.startActivitySync(new Intent(context,MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        var breathe=BreakSessionFragment.newInstance(false);
        try {
            ui(()->activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,breathe).commitNow());settle();capture("breathe-setup");
            var primary=BreakSessionFragment.class.getDeclaredField("primary");primary.setAccessible(true);
            var options=BreakSessionFragment.class.getDeclaredField("options");options.setAccessible(true);
            var button=(TextView)primary.get(breathe);
            ui(button::performClick);settle();
            ui(()->{
                assertEquals(context.getString(R.string.breath_pause),button.getText().toString());
                assertEquals(android.view.View.GONE,((android.view.View)read(options,breathe)).getVisibility());
            });capture("breathe-active");
            ui(button::performClick);settle();ui(()->assertEquals(context.getString(R.string.breath_resume),button.getText().toString()));
            ui(button::performClick);settle();ui(()->assertEquals(context.getString(R.string.breath_pause),button.getText().toString()));
        } finally {ui(activity::finish);}
    }
    private Object read(java.lang.reflect.Field field,Object object){try{return field.get(object);}catch(IllegalAccessException error){throw new AssertionError(error);}}
    private GameScreens.Screen open(String game) throws Exception {
        ui(()->activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,GamesFragment.newInstance(game)).commitNow());settle();
        var field=GamesFragment.class.getDeclaredField("screen");field.setAccessible(true);
        return (GameScreens.Screen)field.get(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_holder));
    }
    private int nextSnakeTurn(GameModels.Snake snake){
        int size=GameModels.Snake.SIZE,head=snake.body.peekLast();boolean[] blocked=new boolean[size*size];
        for(int cell:snake.body)blocked[cell]=true;blocked[snake.body.peekFirst()]=false;
        int[] previous=new int[size*size];Arrays.fill(previous,-1);previous[head]=head;
        var queue=new java.util.ArrayDeque<Integer>();queue.add(head);
        while(!queue.isEmpty()&&previous[snake.food]<0){
            int cell=queue.remove();int x=cell%size,y=cell/size;
            int[] next={y>0?cell-size:-1,x<size-1?cell+1:-1,y<size-1?cell+size:-1,x>0?cell-1:-1};
            for(int candidate:next)if(candidate>=0&&!blocked[candidate]&&previous[candidate]<0){previous[candidate]=cell;queue.add(candidate);}
        }
        if(previous[snake.food]<0)return snake.direction;
        int next=snake.food;while(previous[next]!=head&&next!=head)next=previous[next];
        return next==head-size?0:next==head+1?1:next==head+size?2:3;
    }
    private void capture(String name) throws Exception {
        settle();var bitmap=instrumentation.getUiAutomation().takeScreenshot();
        var directory=new java.io.File(instrumentation.getTargetContext().getFilesDir(),"game-showcase");directory.mkdirs();
        try(var output=new java.io.FileOutputStream(new java.io.File(directory,name+".png"))){bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,output);}finally{bitmap.recycle();}
    }
}
