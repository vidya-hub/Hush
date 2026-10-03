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
import org.schabi.newpipe.fragments.list.search.SearchFragment;
import org.schabi.newpipe.hush.games.GamesFragment;
import org.schabi.newpipe.fragments.list.search.BreakSessionFragment;
import org.schabi.newpipe.local.library.SavedLibraryFragment;
import org.schabi.newpipe.local.library.HistoryLibraryFragment;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Runs actual fragment transactions and checks rendered chrome without changing library data. */
@RunWith(AndroidJUnit4.class)
public class RedesignNavigationTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private MainActivity activity;
    private String prefix="";
    @Test public void homeRoutesAndLibraryTitles() throws Exception {
        activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        settle();
        ui(()->{
            assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_holder) instanceof SearchFragment);
            assertTrue(activity.findViewById(R.id.hush_nav_search).isShown());
            assertSame("Header belongs to the full-page scroll content",activity.findViewById(R.id.home_content),activity.findViewById(R.id.home_header).getParent());
            View field=activity.findViewById(R.id.home_search_dock);
            assertEquals("Home search belongs inside its scrolling content", "home-search-anchor", ((View)field.getParent()).getTag());
            ViewGroup shortcuts=activity.findViewById(R.id.home_content).findViewWithTag("home-library-shortcuts");
            for(int i=0;i<shortcuts.getChildCount();i++){
                android.widget.TextView shortcut=(android.widget.TextView)shortcuts.getChildAt(i);
                assertEquals("Library labels fit without broken words",1,shortcut.getLineCount());
                assertTrue("Library labels have their full text width",shortcut.getPaint().measureText(shortcut.getText().toString())
                    <=shortcut.getWidth()-shortcut.getPaddingLeft()-shortcut.getPaddingRight());
                var button=(com.google.android.material.button.MaterialButton)shortcut;
                assertEquals("Consistent icon-to-label gap",org.schabi.newpipe.hush.ui.HushUi.dp(activity,8),button.getIconPadding());
                if(i>0){
                    View previous=shortcuts.getChildAt(i-1);
                    assertEquals("Shortcut heights align",previous.getHeight(),shortcut.getHeight());
                    if(((android.widget.LinearLayout)shortcuts).getOrientation()==android.widget.LinearLayout.HORIZONTAL){
                        assertTrue(Math.abs(previous.getWidth()-shortcut.getWidth())<=1);
                        assertEquals("Equal shortcut gutters",org.schabi.newpipe.hush.ui.HushUi.dp(activity,8),shortcut.getLeft()-previous.getRight());
                    }
                }
            }
            activity.findViewById(R.id.hush_nav_games).performClick();
        });settle();capture("games");
        ui(()->{
            assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_holder) instanceof GamesFragment);
            assertTrue(activity.findViewById(R.id.hush_nav_games).isSelected());
            activity.findViewById(R.id.hush_nav_breathe).performClick();
        });settle();capture("breathe"); scrollPageToBottom();capture("breathe-controls");
        ui(()->{
            assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_holder) instanceof BreakSessionFragment);
            assertTrue(activity.findViewById(R.id.hush_nav_breathe).isSelected());
            activity.findViewById(R.id.hush_nav_search).performClick();
        });settle();capture("home"); scrollPageToBottom();capture("home-scrolled");
        ui(()->{
            View field=activity.findViewById(R.id.home_search_dock);
            field.requestRectangleOnScreen(new android.graphics.Rect(0,0,field.getWidth(),field.getHeight()),true);
        });settle();capture("home-search-accessible");
        ui(()->scrollPage(activity.findViewById(R.id.fragment_holder),false));settle();
        ui(()->activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder,new SavedLibraryFragment()).addToBackStack(null).commit());settle();
        ui(()->{
            assertTrue(activity.getSupportActionBar().isShowing());
            assertEquals(activity.getString(R.string.library_saved),activity.getSupportActionBar().getTitle());
            assertEquals(activity.getHushChrome().isRail(),activity.findViewById(R.id.hush_nav_search).isShown());
        });capture("saved");
        ui(()->activity.getSupportFragmentManager().popBackStackImmediate());settle();
        ui(()->activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder,new HistoryLibraryFragment()).addToBackStack(null).commit());settle();
        ui(()->assertEquals(activity.getString(R.string.action_history),activity.getSupportActionBar().getTitle()));capture("history");
        ui(()->{
            View list=activity.findViewById(R.id.library_list);
            if(list instanceof androidx.recyclerview.widget.RecyclerView) {
                if(activity.getResources().getConfiguration().fontScale>1.3f)
                    ((androidx.recyclerview.widget.RecyclerView)list).scrollToPosition(1);
                else list.scrollBy(0,list.getHeight()/2);
            }
        });settle();capture("history-scrolled");
        ui(()->((com.google.android.material.tabs.TabLayout)activity.findViewById(R.id.history_tabs)).getTabAt(1).select());
        settle();capture("history-search");
        ui(()->((com.google.android.material.tabs.TabLayout)activity.findViewById(R.id.history_tabs)).getTabAt(0).select());
        settle();
        ui(()->activity.getSupportFragmentManager().popBackStackImmediate());settle();
    }
    private void assertPaneSpacing() {
        View panes=activity.findViewById(R.id.fragment_holder).findViewWithTag("hush-content-panes");
        assertNotNull(panes);
        android.widget.LinearLayout group=(android.widget.LinearLayout)panes;
        View first=((ViewGroup)group.getChildAt(0)).getChildAt(0);
        View second=((ViewGroup)group.getChildAt(1)).getChildAt(0);
        int[] a=new int[2],b=new int[2];first.getLocationOnScreen(a);second.getLocationOnScreen(b);
        int gap=org.schabi.newpipe.hush.ui.HushUi.dp(activity,24);
        if(group.getOrientation()==android.widget.LinearLayout.HORIZONTAL) {
            assertEquals("Visible pane gap, without hidden centering gutters",gap,b[0]-a[0]-first.getWidth(),1);
            assertEquals("Side panes align at the top",a[1],b[1],1);
        } else {
            assertEquals("Stacked board and controls share a leading edge",a[0],b[0],1);
            assertEquals("Stacked board and controls share their width",first.getWidth(),second.getWidth(),1);
            assertEquals("Stacked section gap",gap,b[1]-a[1]-first.getHeight(),1);
        }
    }
    @Test public void darkScreensAndSecondaryRoutes() throws Exception {
        var context=instrumentation.getTargetContext();
        var preferences=androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String key=context.getString(R.string.theme_key);
        String previous=preferences.getString(key,null);
        preferences.edit().putString(key,context.getString(R.string.dark_theme_key)).commit();
        prefix="dark-";
        try {
            homeRoutesAndLibraryTitles();
            ui(()->{
                SearchFragment search=(SearchFragment)activity.getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
                try {var method=SearchFragment.class.getDeclaredMethod("showProfileDialog");method.setAccessible(true);method.invoke(search);}
                catch(ReflectiveOperationException error){throw new AssertionError(error);}
            });settle();capture("profiles");
            instrumentation.getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);settle();
            ui(()->org.schabi.newpipe.util.NavigationHelper.openDownloads(activity));settle();capture("downloads");
            instrumentation.getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);settle();
            ui(()->org.schabi.newpipe.util.NavigationHelper.openSettings(activity));settle();capture("settings");
            instrumentation.getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);settle();
        } finally {
            var edit=preferences.edit();if(previous==null)edit.remove(key);else edit.putString(key,previous);edit.commit();
            ui(()->activity.finish());
            prefix="";
            activity=(MainActivity)instrumentation.startActivitySync(new Intent(context,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));settle();
        }
    }
    @Test public void temporaryPlaylistRendersRealRowsAndLongTitle() throws Exception {
        var context=instrumentation.getTargetContext();
        var manager=new org.schabi.newpipe.local.playlist.LocalPlaylistManager(org.schabi.newpipe.NewPipeDatabase.getInstance(context));
        String name="Redesign QA — a deliberately long playlist title to check wrapping";
        long id=manager.createEmptyPlaylist(name).blockingGet();
        java.util.List<org.schabi.newpipe.database.stream.model.StreamEntity> fixtures=new java.util.ArrayList<>();
        try {
            for(int i=0;i<3;i++){
                var item=new org.schabi.newpipe.extractor.stream.StreamInfoItem(0,"https://hush-tablet-test.invalid/"+id+"/"+i,
                        i==0?"Tablet QA — a deliberately long video title that should wrap cleanly in a playlist row":"Tablet QA fixture "+(i+1),org.schabi.newpipe.extractor.stream.StreamType.VIDEO_STREAM);
                item.setDuration(180+i*60);item.setUploaderName("Hush test fixture");
                fixtures.add(new org.schabi.newpipe.database.stream.model.StreamEntity(item));
            }
            manager.appendToPlaylist(id,fixtures).blockingGet();
            activity=(MainActivity)instrumentation.startActivitySync(new Intent(context,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));settle();
            ui(()->activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,new SavedLibraryFragment()).addToBackStack(null).commit());
            settle();capture("saved-populated-qa");
            ui(()->activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,
                    org.schabi.newpipe.local.playlist.LocalPlaylistFragment.getInstance(id,name)).addToBackStack(null).commit());
            settle();
            ui(()->{
                assertEquals(name,((android.widget.TextView)activity.findViewById(R.id.playlist_title_view)).getText().toString());
                assertEquals(activity.getHushChrome().isRail(),activity.findViewById(R.id.hush_nav_search).isShown());
            });capture("playlist-detail-qa");
            ui(()->((androidx.recyclerview.widget.RecyclerView)activity.findViewById(R.id.items_list)).scrollToPosition(1));
            settle();capture("playlist-rows-qa");
            ui(()->activity.getSupportFragmentManager().popBackStackImmediate());settle();
            ui(()->activity.getSupportFragmentManager().popBackStackImmediate());settle();
        } finally { assertEquals(Integer.valueOf(1),manager.deletePlaylist(id).blockingGet());
            org.schabi.newpipe.NewPipeDatabase.getInstance(context).streamDAO().delete(fixtures); }
    }
    @Test public void alignmentAndSecondaryScreens() throws Exception {
        var context=instrumentation.getTargetContext();
        activity=(MainActivity)instrumentation.startActivitySync(new Intent(context,MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));settle();
        ui(()->{
            View dock=activity.findViewById(R.id.home_search_dock);
            View nav=(View)activity.findViewById(R.id.hush_nav_search).getParent();
            android.graphics.Rect field=new android.graphics.Rect(),bar=new android.graphics.Rect();
            assertTrue(dock.getGlobalVisibleRect(field));assertTrue(nav.getGlobalVisibleRect(bar));
            if(activity.getHushChrome().isRail()){
                assertTrue("Rail stays outside Home content",bar.right<field.left);
                assertEquals(org.schabi.newpipe.hush.ui.HushUi.dp(context,96),bar.width());
            }else{
                int screen=activity.getWindow().getDecorView().getWidth();
                assertEquals("Bottom navigation horizontal margins",bar.left,screen-bar.right,2);
                assertTrue("Bottom navigation remains bounded",bar.width()<=org.schabi.newpipe.hush.ui.HushUi.dp(context,560));
            }
        });
        var preferences=androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String privacyKey=org.schabi.newpipe.local.history.HistoryRecordManager.INCOGNITO_KEY;
        boolean hadPrivacy=preferences.contains(privacyKey),previousPrivacy=preferences.getBoolean(privacyKey,false);
        preferences.edit().putBoolean(privacyKey,true).commit();
        try {
        for(String game:new String[]{"2048","snake","sudoku","make24"}) {
            ui(()->activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder,GamesFragment.newInstance(game)).addToBackStack(null).commit());
            settle();ui(this::assertPaneSpacing);capture("game-"+game);
            scrollPageToBottom();capture("game-"+game+"-controls");
            ui(()->activity.getSupportFragmentManager().popBackStackImmediate());settle();
        }
        } finally {
            var edit=preferences.edit();if(hadPrivacy)edit.putBoolean(privacyKey,previousPrivacy);else edit.remove(privacyKey);edit.commit();
        }
        ui(()->activity.findViewById(R.id.hush_nav_breathe).performClick());settle();
        ui(()->clickText(activity.findViewById(R.id.fragment_holder),activity.getString(R.string.hush_meditate)));
        settle();capture("meditate");
        ui(()->activity.findViewById(R.id.hush_nav_search).performClick());settle();
        ui(()->{
            try {var method=SearchFragment.class.getDeclaredMethod("showProfileDialog");method.setAccessible(true);
                method.invoke(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_holder));}
            catch(ReflectiveOperationException error){throw new AssertionError(error);}
        });settle();capture("profiles");
        // Inspect the dialog through its accessibility hierarchy, then open the form without creating a profile.
        var automation=instrumentation.getUiAutomation();
        var nodes=automation.getRootInActiveWindow().findAccessibilityNodeInfosByViewId(context.getPackageName()+":id/profile_sheet_new");
        assertFalse(nodes.isEmpty());nodes.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
        settle();capture("profile-create");
        automation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);settle();
        automation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);settle();
        var settings=(org.schabi.newpipe.settings.SettingsActivity)instrumentation.startActivitySync(new Intent(context,org.schabi.newpipe.settings.SettingsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));settle();
        java.util.List<String> classes=new java.util.ArrayList<>();
        ui(()->{
            var category=settings.getSupportFragmentManager().findFragmentById(R.id.settings_categories);
            collectSettings(((androidx.preference.PreferenceFragmentCompat)(category!=null&&category.getView().isShown()?category:
                    settings.getSupportFragmentManager().findFragmentById(R.id.settings_fragment_holder))).getPreferenceScreen(),classes);
        });
        ui(()->{
            View root=settings.getSupportFragmentManager().findFragmentById(R.id.settings_fragment_holder).getView();
            assertEquals("Settings row inset",org.schabi.newpipe.hush.ui.HushUi.contentSide(root,root.getWidth())
                    -org.schabi.newpipe.hush.ui.HushUi.dp(context,16),((androidx.preference.PreferenceFragmentCompat)settings.getSupportFragmentManager()
                            .findFragmentById(R.id.settings_fragment_holder)).getListView().getChildAt(0).getLeft());
        });
        capture("settings");
        try {
            for(int settingsIndex=0;settingsIndex<classes.size();settingsIndex++) {
                String name=classes.get(settingsIndex);
                ui(()->{
                    androidx.fragment.app.Fragment page=settings.getSupportFragmentManager().getFragmentFactory().instantiate(context.getClassLoader(),name);
                    settings.getSupportFragmentManager().beginTransaction().replace(R.id.settings_fragment_holder,page).addToBackStack(null).commit();
                });settle();capture("settings-"+name.substring(name.lastIndexOf('.')+1));
                ui(()->{
                    var fragment=settings.getSupportFragmentManager().findFragmentById(R.id.settings_fragment_holder);
                    if(fragment instanceof androidx.preference.PreferenceFragmentCompat) {
                        var page=(androidx.preference.PreferenceFragmentCompat)fragment;
                        collectSettings(page.getPreferenceScreen(),classes);
                        if(page.getListView().getAdapter()!=null)page.getListView().scrollToPosition(page.getListView().getAdapter().getItemCount()-1);
                    } else if(fragment!=null && fragment.getView()!=null)scrollPage(fragment.getView(),true);
                });settle();capture("settings-"+name.substring(name.lastIndexOf('.')+1)+"-bottom");
                ui(()->settings.getSupportFragmentManager().popBackStackImmediate());settle();
            }
        } finally { ui(settings::finish); }
    }
    private void collectSettings(androidx.preference.PreferenceGroup group,java.util.List<String> classes) {
        for(int i=0;i<group.getPreferenceCount();i++) {
            var row=group.getPreference(i);
            if(row.getFragment()!=null&&!classes.contains(row.getFragment()))classes.add(row.getFragment());
            if(row instanceof androidx.preference.PreferenceGroup)collectSettings((androidx.preference.PreferenceGroup)row,classes);
        }
    }
    private boolean clickText(View view,String text) {
        if(view instanceof android.widget.TextView && text.contentEquals(((android.widget.TextView)view).getText()) && view.isClickable())return view.performClick();
        if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++)if(clickText(((ViewGroup)view).getChildAt(i),text))return true;
        return false;
    }
    private void scrollPageToBottom() {
        ui(()->scrollPage(activity.findViewById(R.id.fragment_holder),true));settle();
    }
    private boolean scrollPage(View view,boolean bottom) {
        if(view instanceof android.widget.ScrollView) {
            ((android.widget.ScrollView)view).fullScroll(bottom?View.FOCUS_DOWN:View.FOCUS_UP);return true;
        }
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)if(scrollPage(((ViewGroup)view).getChildAt(i),bottom))return true;
        return false;
    }
    private void ui(Runnable action){instrumentation.runOnMainSync(action);}
    private void settle(){instrumentation.waitForIdleSync();SystemClock.sleep(800);instrumentation.waitForIdleSync();}
    private void capture(String name) throws Exception {
        Bitmap image=instrumentation.getUiAutomation().takeScreenshot();assertNotNull(image);
        File directory=new File(instrumentation.getTargetContext().getFilesDir(),"redesign-proof");directory.mkdirs();
        try(FileOutputStream out=new FileOutputStream(new File(directory,prefix+name+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();
    }
}
