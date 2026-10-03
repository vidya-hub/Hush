package org.schabi.newpipe.fragments.detail;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.viewpager.widget.ViewPager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.player.helper.PlayerHolder;
import org.schabi.newpipe.util.InfoCache;
import org.schabi.newpipe.util.NavigationHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Exercises the actual Watch fragment with deterministic cached stream metadata. */
@RunWith(AndroidJUnit4.class)
public class WatchTabsLifecycleTest {
    @Test public void currentDescriptionSurvivesVideoChangesRotationAndActivityRecreation() {
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        var context = instrumentation.getTargetContext();
        var preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String autoplayKey = context.getString(R.string.autoplay_key);
        String tabsKey = context.getString(R.string.video_tabs_key);
        String autoplay = preferences.getString(autoplayKey, null);
        Set<String> previousTabs = preferences.contains(tabsKey)
                ? new HashSet<>(preferences.getStringSet(tabsKey, Set.of())) : null;
        preferences.edit().putString(autoplayKey, context.getString(R.string.autoplay_never_key))
                .putStringSet(tabsKey, Set.of("comments", "description", "related")).commit();
        final AtomicReference<MainActivity> activity = new AtomicReference<>();
        try {
            activity.set((MainActivity) instrumentation.startActivitySync(new Intent(context, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)));
            instrumentation.waitForIdleSync();
            for (int cycle = 0; cycle < 12; cycle++) {
                String name = "Watch lifecycle fixture " + cycle;
                String url = "https://www.youtube.com/watch?v=G-eNlqqkn1w&hush_test=" + cycle;
                StreamInfo info = new StreamInfo(0, url, url, StreamType.VIDEO_STREAM,
                        "G-eNlqqkn1w", name, 0);
                info.setDescription(new Description(name, Description.PLAIN_TEXT));
                info.setSupportComments(false);
                info.setSupportRelatedItems(false);
                info.setUploaderName("Hush test fixture");
                InfoCache.getInstance().putInfo(0, url, info, InfoItem.InfoType.STREAM);
                instrumentation.runOnMainSync(() -> NavigationHelper.openVideoDetailFragment(
                        activity.get(), activity.get().getSupportFragmentManager(), 0, url, name, null, false));
                awaitDescription(activity, name);
                if (cycle % 3 == 0) {
                    final int orientation = cycle % 2 == 0 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    instrumentation.runOnMainSync(() -> activity.get().setRequestedOrientation(orientation));
                    SystemClock.sleep(500);
                    awaitDescription(activity, name);
                    instrumentation.runOnMainSync(() -> activity.get().recreate());
                    long deadline = SystemClock.uptimeMillis() + 10000;
                    MainActivity previous = activity.get();
                    while (SystemClock.uptimeMillis() < deadline && activity.get() == previous) {
                        instrumentation.runOnMainSync(() -> {
                            for (var candidate : ActivityLifecycleMonitorRegistry.getInstance()
                                    .getActivitiesInStage(Stage.RESUMED)) {
                                if (candidate instanceof MainActivity && candidate != previous) {
                                    activity.set((MainActivity) candidate);
                                }
                            }
                        });
                        SystemClock.sleep(100);
                    }
                    assertNotSame("Activity recreation must actually happen", previous, activity.get());
                    awaitDescription(activity, name);
                }
            }
        } finally {
            instrumentation.runOnMainSync(() -> {
                PlayerHolder.getInstance().stopService();
                if (activity.get() != null) activity.get().finish();
            });
            var edit = preferences.edit();
            if (autoplay == null) edit.remove(autoplayKey); else edit.putString(autoplayKey, autoplay);
            if (previousTabs == null) edit.remove(tabsKey); else edit.putStringSet(tabsKey, previousTabs);
            edit.commit();
        }
    }

    private static void awaitDescription(AtomicReference<MainActivity> activity, String name) {
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        final boolean[] matched = {false};
        long deadline = SystemClock.uptimeMillis() + 10000;
        while (SystemClock.uptimeMillis() < deadline && !matched[0]) {
            instrumentation.runOnMainSync(() -> {
                ViewPager pager = activity.get().findViewById(R.id.view_pager);
                if (pager == null || !(pager.getAdapter() instanceof TabAdapter)) return;
                TabAdapter adapter = (TabAdapter) pager.getAdapter();
                int index = adapter.getItemPositionByTitle("DESCRIPTION TAB");
                if (index < 0 || !(adapter.getItem(index) instanceof DescriptionFragment)) return;
                pager.setCurrentItem(index, false);
                DescriptionFragment description = (DescriptionFragment) adapter.getItem(index);
                if (!description.isAdded() || description.streamInfo == null) return;
                assertEquals("Watch must display the current video's description", name, description.streamInfo.getName());
                assertEquals(1, description.getParentFragmentManager().getFragments().stream()
                        .filter(fragment -> fragment instanceof DescriptionFragment).count());
                matched[0] = true;
            });
            SystemClock.sleep(100);
        }
        assertTrue("Current description must be attached: " + name, matched[0]);
    }
}
