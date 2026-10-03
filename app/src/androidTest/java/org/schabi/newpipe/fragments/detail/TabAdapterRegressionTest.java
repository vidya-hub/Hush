package org.schabi.newpipe.fragments.detail;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager.widget.ViewPager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.MainActivity;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TabAdapterRegressionTest {
    @Test public void replacingAndReorderingWatchTabsKeepsOneFragmentPerTab() {
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        var activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.waitForIdleSync();
        try {
            instrumentation.runOnMainSync(()->{
                ViewPager pager=new ViewPager(activity);pager.setId(View.generateViewId());
                activity.setContentView(pager);
                TabAdapter adapter=new TabAdapter(activity.getSupportFragmentManager());
                pager.setAdapter(adapter);
                adapter.addFragment(new Fragment(),"comments");
                adapter.addFragment(new DescriptionFragment(),"description");
                adapter.addFragment(new Fragment(),"queue");
                measure(pager);
                for(int i=0;i<10;i++){
                    adapter.updateItem("description",new DescriptionFragment());
                    adapter.removeItem(0);
                    pager.setCurrentItem(0,false);
                    measure(pager);
                    assertTrue(adapter.getItem(0) instanceof DescriptionFragment);
                    assertTrue("The adapter must return the displayed description",adapter.getItem(0).isAdded());
                    adapter.clearAllItems();
                    adapter.addFragment(new Fragment(),"comments");
                    adapter.addFragment(new DescriptionFragment(),"description");
                    adapter.addFragment(new Fragment(),"queue");
                    measure(pager);
                }
                pager.setAdapter(null);
            });
        } finally {instrumentation.runOnMainSync(activity::finish);}
    }
    @Test public void repeatedInstantiationDuringTabRefreshAddsDescriptionOnlyOnce() {
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        var activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.waitForIdleSync();
        try {
            instrumentation.runOnMainSync(()->{
                ViewPager pager=new ViewPager(activity);pager.setId(View.generateViewId());
                activity.setContentView(pager);
                TabAdapter adapter=new TabAdapter(activity.getSupportFragmentManager());
                DescriptionFragment description=new DescriptionFragment();
                adapter.addFragment(description,"description");
                adapter.startUpdate(pager);
                Object first=adapter.instantiateItem(pager,0);
                Object second=adapter.instantiateItem(pager,0);
                assertSame(first,second);
                adapter.finishUpdate(pager);
                assertTrue(description.isAdded());
                assertEquals(1,activity.getSupportFragmentManager().getFragments().stream()
                    .filter(f->f==description).count());
            });
        } finally {instrumentation.runOnMainSync(activity::finish);}
    }
    private static void measure(ViewGroup pager){
        pager.measure(View.MeasureSpec.makeMeasureSpec(1000,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(700,View.MeasureSpec.EXACTLY));
        pager.layout(0,0,1000,700);
    }
}
