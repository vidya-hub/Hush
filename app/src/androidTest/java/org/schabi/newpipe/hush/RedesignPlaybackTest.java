package org.schabi.newpipe.hush;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.fragments.detail.VideoDetailFragment;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.player.Player;
import org.schabi.newpipe.player.helper.PlayerHolder;
import org.schabi.newpipe.util.NavigationHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Network smoke test: actual video must advance through collapse and expansion. */
@RunWith(AndroidJUnit4.class)
public class RedesignPlaybackTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private MainActivity activity;
    private void ui(Runnable action){instrumentation.runOnMainSync(action);}
    @Test public void samePlayerSurvivesFloatingAndExpansion() throws Exception {
        var context=instrumentation.getTargetContext();
        var preferences=PreferenceManager.getDefaultSharedPreferences(context);
        String autoplayKey=context.getString(R.string.autoplay_key);
        String previousAutoplay=preferences.getString(autoplayKey,null);
        preferences.edit().putString(autoplayKey,context.getString(R.string.autoplay_always_key)).commit();
        boolean hadPrivacy=preferences.contains(HistoryRecordManager.INCOGNITO_KEY);
        boolean privacy=preferences.getBoolean(HistoryRecordManager.INCOGNITO_KEY,false);
        preferences.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY,true).commit();
        try {
            activity=(MainActivity)instrumentation.startActivitySync(new Intent(context,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            instrumentation.waitForIdleSync();
            ui(()->NavigationHelper.openVideoDetailFragment(activity,activity.getSupportFragmentManager(),0,
                    "https://www.youtube.com/watch?v=G-eNlqqkn1w","Playback smoke test",null,false));
            long deadline=SystemClock.uptimeMillis()+60000;
            AtomicReference<Player> reference=new AtomicReference<>();
            Field playerField=PlayerHolder.class.getDeclaredField("player");playerField.setAccessible(true);
            while(SystemClock.uptimeMillis()<deadline){
                ui(()->{
                    try {
                        Player p=(Player)playerField.get(PlayerHolder.getInstance());
                        if(p!=null && p.simpleExoPlayer!=null){p.simpleExoPlayer.play();if(p.simpleExoPlayer.isPlaying())reference.set(p);}
                    }catch(IllegalAccessException e){throw new AssertionError(e);}
                });
                if(reference.get()!=null)break;
                SystemClock.sleep(500);
            }
            assertNotNull("A real YouTube stream must start before the floating playback assertions",reference.get());
            Player player=reference.get();Object decoder=player.simpleExoPlayer;
            capture("watch");
            final long[] positions=new long[3];
            ui(()->{
                positions[0]=player.simpleExoPlayer.getCurrentPosition();
                BottomSheetBehavior.from(activity.findViewById(R.id.fragment_player_holder))
                        .setState(BottomSheetBehavior.STATE_COLLAPSED);
            });SystemClock.sleep(2500);instrumentation.waitForIdleSync();
            ui(()->{
                assertTrue(activity.getHushChrome().isFloating());
                assertTrue(activity.findViewById(R.id.hush_floating_video).isShown());
                assertEquals(View.INVISIBLE,activity.findViewById(R.id.fragment_player_holder).getVisibility());
                assertTrue(activity.findViewById(R.id.hush_nav_search).isShown());
                assertSame(decoder,player.simpleExoPlayer);
                assertTrue(player.getRootView().getParent() instanceof android.view.ViewGroup);
                positions[1]=player.simpleExoPlayer.getCurrentPosition();
                assertTrue("Playback advances while floating",positions[1]>positions[0]);
            });capture("floating");
            ui(()->((org.schabi.newpipe.fragments.list.search.SearchFragment)activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_holder)).submitSearch("lofi"));
            SystemClock.sleep(1800);instrumentation.waitForIdleSync();
            ui(()->{
                assertTrue(activity.getHushChrome().isFloating());
                assertEquals(activity.getHushChrome().isRail(),activity.findViewById(R.id.hush_nav_search).isShown());
                assertEquals("lofi",((android.widget.EditText)activity.findViewById(R.id.home_search_edit_text)).getText().toString());
                assertEquals(View.GONE,activity.findViewById(R.id.home_header).getVisibility());
                assertSame(decoder,player.simpleExoPlayer);
            });capture("floating-results");
            ui(()->activity.findViewById(R.id.hush_floating_expand).performClick());SystemClock.sleep(600);
            ui(()->BottomSheetBehavior.from(activity.findViewById(R.id.fragment_player_holder)).setState(BottomSheetBehavior.STATE_COLLAPSED));
            SystemClock.sleep(600);ui(()->assertEquals("lofi",((android.widget.EditText)activity.findViewById(R.id.home_search_edit_text)).getText().toString()));
            ui(()->activity.findViewById(R.id.home_search_leading).performClick());SystemClock.sleep(600);

            ui(()->activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_holder,org.schabi.newpipe.hush.games.GamesFragment.newInstance("2048"))
                    .addToBackStack(null).commit());SystemClock.sleep(800);instrumentation.waitForIdleSync();
            ui(()->{
                assertTrue("Floating video overlays the full-height game page",activity.findViewById(R.id.fragment_holder).getBottom()
                        > activity.findViewById(R.id.hush_floating_video).getY());
                assertSame(decoder,player.simpleExoPlayer);
            });capture("floating-game");
            ui(()->activity.getSupportFragmentManager().popBackStackImmediate());SystemClock.sleep(800);

            ui(()->activity.findViewById(R.id.hush_floating_expand).performClick());
            SystemClock.sleep(2500);instrumentation.waitForIdleSync();
            ui(()->{
                assertFalse(activity.getHushChrome().isFloating());
                assertEquals(View.VISIBLE,activity.findViewById(R.id.fragment_player_holder).getVisibility());
                assertFalse(activity.findViewById(R.id.hush_nav_search).isShown());
                assertSame(decoder,player.simpleExoPlayer);
                assertSame(activity.findViewById(R.id.player_placeholder),player.getRootView().getParent());
                positions[2]=player.simpleExoPlayer.getCurrentPosition();
                assertTrue("Playback advances after expansion",positions[2]>positions[1]);
            });capture("watch-expanded");
            ui(()->BottomSheetBehavior.from(activity.findViewById(R.id.fragment_player_holder)).setState(BottomSheetBehavior.STATE_COLLAPSED));
            SystemClock.sleep(600);ui(()->activity.findViewById(R.id.hush_floating_close).performClick());
            SystemClock.sleep(600);ui(()->assertFalse(activity.getHushChrome().isFloating()));
        } finally {
            ui(()->PlayerHolder.getInstance().stopService());
            var edit=preferences.edit();if(previousAutoplay==null)edit.remove(autoplayKey);else edit.putString(autoplayKey,previousAutoplay);if(hadPrivacy)edit.putBoolean(HistoryRecordManager.INCOGNITO_KEY,privacy);else edit.remove(HistoryRecordManager.INCOGNITO_KEY);edit.commit();
        }
    }
    private void capture(String name) throws Exception {
        Bitmap bitmap=instrumentation.getUiAutomation().takeScreenshot();assertNotNull(bitmap);
        File directory=new File(instrumentation.getTargetContext().getFilesDir(),"redesign-proof");directory.mkdirs();
        try(FileOutputStream out=new FileOutputStream(new File(directory,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();
    }
}
