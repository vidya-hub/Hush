package org.schabi.newpipe.hush;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.R;
import org.schabi.newpipe.download.DownloadActivity;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

/** Checks the settled standalone surface rather than an activity-transition frame. */
@RunWith(AndroidJUnit4.class)
public class TabletDownloadsSpacingTest {
    @Test public void emptyStateIsCenteredInsidePageGutters() throws Exception {
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        var context=instrumentation.getTargetContext();
        var activity=(DownloadActivity)instrumentation.startActivitySync(new Intent(context,DownloadActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        try {
            instrumentation.waitForIdleSync();SystemClock.sleep(3000);instrumentation.waitForIdleSync();
            instrumentation.runOnMainSync(() -> {
                View icon=activity.findViewById(R.id.downloads_empty_icon);
                View empty=activity.findViewById(R.id.list_empty_view);
                Rect visible=new Rect();assertTrue(icon.getGlobalVisibleRect(visible));
                assertEquals("Empty-state icon is not horizontally clipped",icon.getWidth(),visible.width());
                int[] origin=new int[2];empty.getLocationOnScreen(origin);
                assertEquals("Icon centers within the bounded empty-state column",origin[0]+empty.getWidth()/2,
                        visible.centerX(),2);
            });
            Bitmap shot=instrumentation.getUiAutomation().takeScreenshot();assertNotNull(shot);
            File folder=new File(context.getFilesDir(),"redesign-proof");folder.mkdirs();
            try(var out=new FileOutputStream(new File(folder,"dark-downloads.png"))) {
                shot.compress(Bitmap.CompressFormat.PNG,100,out);
            } finally {shot.recycle();}
        } finally {instrumentation.runOnMainSync(activity::finish);}
    }
}
