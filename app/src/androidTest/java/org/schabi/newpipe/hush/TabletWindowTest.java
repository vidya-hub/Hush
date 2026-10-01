package org.schabi.newpipe.hush;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.hush.ui.HushUi;
import org.schabi.newpipe.hush.ui.TabletLayout;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

/** Window geometry checks use a presentation view; they make no claim about decoder playback. */
@RunWith(AndroidJUnit4.class)
public final class TabletWindowTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private MainActivity activity;
    private void ui(Runnable action){instrumentation.runOnMainSync(action);}
    private void settle(){SystemClock.sleep(1000);instrumentation.waitForIdleSync();}
    @Test public void navigationFloatingAndExpandedWatch() throws Exception {
        activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));settle();
        View[] surface=new View[1];
        final int[] viewport=new int[1];
        ui(()->{
            activity.setPlayerExpanded(false);
            viewport[0]=activity.findViewById(R.id.fragment_holder).getHeight();
            surface[0]=new View(activity);
            surface[0].setBackgroundColor(0xff254537);
            activity.getHushChrome().showVideo(surface[0],()->{},()->activity.getHushChrome().hideVideo());
        });settle();capture("floating-layout");
        ui(()->{
            View floating=activity.findViewById(R.id.hush_floating_video);
            assertTrue("Browsing floating presentation is visible after window settling",floating.isShown());
            float width=activity.getWindow().getDecorView().getWidth()/activity.getResources().getDisplayMetrics().density;
            assertEquals(HushUi.dp(activity,TabletLayout.floatingWidth(width)),floating.getWidth(),2);
            assertEquals(floating.getWidth()*9f/16,floating.getHeight(),1);
            assertTrue("Video overlays the browsing viewport",activity.findViewById(R.id.fragment_holder).getBottom()>floating.getY());
            assertEquals("Showing video must not shrink the page",viewport[0],activity.findViewById(R.id.fragment_holder).getHeight());
            assertEquals(1,((ViewGroup)surface[0].getParent()).getChildCount());
            activity.getHushChrome().suppressVideo(true);
        });settle();
        ui(()->{
            assertFalse(activity.findViewById(R.id.hush_floating_video).isShown());
            activity.getHushChrome().suppressVideo(false);
        });settle();
        ui(()->{
            int height=((View)activity.findViewById(R.id.hush_floating_video).getParent()).getHeight();
            activity.getHushChrome().insets(HushUi.dp(activity,24),0,0,0,height-HushUi.dp(activity,128));
            assertFalse(activity.findViewById(R.id.hush_floating_video).isShown());
            assertTrue("Short keyboard windows retain accessible session actions",activity.findViewById(R.id.hush_floating_audio_fallback).isShown());
            assertEquals(1,((ViewGroup)surface[0].getParent()).getChildCount());
            activity.getHushChrome().insets(0,0,0,0,0);
            activity.getHushChrome().hideVideo();
            assertFalse(activity.findViewById(R.id.hush_floating_audio_fallback).isShown());
        });
        ui(()->org.schabi.newpipe.util.NavigationHelper.openVideoDetailFragment(activity,activity.getSupportFragmentManager(),0,
            "https://www.youtube.com/watch?v=G-eNlqqkn1w","Tablet Watch layout",null,false));settle();
        ui(()->{
            activity.setPlayerExpanded(true);
            assertFalse(activity.findViewById(R.id.hush_nav_search).isShown());
            View root=activity.findViewById(R.id.video_item_detail);
            assertNotNull(root);
            assertNull("Watch never contains the browsing search field",root.findViewById(R.id.home_search_edit_text));
        });settle();capture("watch-layout");
        ui(()->activity.finish());
    }
    private void capture(String name) throws Exception{
        Bitmap image=instrumentation.getUiAutomation().takeScreenshot();assertNotNull(image);
        File folder=new File(instrumentation.getTargetContext().getFilesDir(),"redesign-proof");folder.mkdirs();
        try(FileOutputStream stream=new FileOutputStream(new File(folder,name+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,stream);}
    }
}
