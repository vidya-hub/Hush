package org.schabi.newpipe.hush;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.material.button.MaterialButton;
import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.hush.ui.HushUi;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HomeSpacingTest {
    @Test public void shortcutGuttersAndLabelsFitTheWindow() throws Exception {
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        var preferences=PreferenceManager.getDefaultSharedPreferences(instrumentation.getTargetContext());
        boolean hadPrivacy=preferences.contains(HistoryRecordManager.INCOGNITO_KEY);
        boolean privacy=preferences.getBoolean(HistoryRecordManager.INCOGNITO_KEY,false);
        preferences.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY,true).commit();
        MainActivity activity=null;
        try {
            activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            final MainActivity target=activity;
            SystemClock.sleep(1000);instrumentation.waitForIdleSync();
            instrumentation.runOnMainSync(()->{
                LinearLayout shortcuts=target.findViewById(R.id.home_content).findViewWithTag("home-library-shortcuts");
                assertNotNull(shortcuts);assertEquals(3,shortcuts.getChildCount());
                int[] searchPosition=new int[2],shortcutPosition=new int[2];
                View search=target.findViewById(R.id.home_search_dock);
                search.getLocationOnScreen(searchPosition);shortcuts.getLocationOnScreen(shortcutPosition);
                assertEquals("No blank recent-query space",HushUi.dp(target,16),shortcutPosition[1]-searchPosition[1]-search.getHeight());
                for(int i=0;i<3;i++){
                    MaterialButton button=(MaterialButton)shortcuts.getChildAt(i);
                    assertEquals(1,button.getLineCount());
                    assertTrue("Label fits inside its card",button.getPaint().measureText(button.getText().toString())<=button.getWidth()-button.getPaddingLeft()-button.getPaddingRight());
                    assertEquals(HushUi.dp(target,8),button.getIconPadding());
                    assertTrue(button.getHeight()>=HushUi.dp(target,48));
                    if(i>0){
                        View previous=shortcuts.getChildAt(i-1);
                        assertEquals(previous.getHeight(),button.getHeight());
                        if(shortcuts.getOrientation()==LinearLayout.HORIZONTAL){
                            assertTrue(Math.abs(previous.getWidth()-button.getWidth())<=1);
                            assertEquals(HushUi.dp(target,8),button.getLeft()-previous.getRight());
                        } else assertEquals(HushUi.dp(target,8),button.getTop()-previous.getBottom());
                    }
                }
            });
            Bitmap image=instrumentation.getUiAutomation().takeScreenshot();assertNotNull(image);
            File directory=new File(instrumentation.getTargetContext().getFilesDir(),"fixes-proof");directory.mkdirs();
            try(FileOutputStream out=new FileOutputStream(new File(directory,"home.png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}
            image.recycle();
        } finally {
            if(activity!=null){final MainActivity target=activity;instrumentation.runOnMainSync(target::finish);}
            var edit=preferences.edit();if(hadPrivacy)edit.putBoolean(HistoryRecordManager.INCOGNITO_KEY,privacy);else edit.remove(HistoryRecordManager.INCOGNITO_KEY);edit.commit();
        }
    }
}
