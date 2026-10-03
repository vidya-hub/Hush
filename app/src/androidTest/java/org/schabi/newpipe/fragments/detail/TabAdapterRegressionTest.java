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
    @Test public void descriptionRefreshDuringAttachmentDoesNotReplayAdds() {
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        var activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.waitForIdleSync();
        try {
            instrumentation.runOnMainSync(()->{
                android.widget.FrameLayout root=new android.widget.FrameLayout(activity);root.setId(View.generateViewId());
                activity.setContentView(root);
                PagerHostFragment host=new PagerHostFragment();
                activity.getSupportFragmentManager().beginTransaction().add(root.getId(),host,"watch-test").commitNow();
                ViewPager pager=(ViewPager)host.requireView();
                var manager=host.getChildFragmentManager();
                TabAdapter adapter=new TabAdapter(manager);
                final int[] refreshes={0};
                manager.registerFragmentLifecycleCallbacks(new androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks(){
                    @Override public void onFragmentCreated(androidx.fragment.app.FragmentManager fm,Fragment fragment,android.os.Bundle state){
                        if(fragment instanceof DescriptionFragment && refreshes[0]++==0){
                            adapter.updateItem("description",new DescriptionFragment());
                            adapter.notifyDataSetUpdate();
                            measure(pager);
                        }
                    }
                },false);
                pager.setAdapter(adapter);
                adapter.addFragment(new Fragment(),"comments");
                adapter.addFragment(new DescriptionFragment(),"description");
                adapter.addFragment(new Fragment(),"queue");
                measure(pager);
                for(int i=0;i<10;i++){
                    adapter.updateItem("description",new DescriptionFragment());
                    pager.setCurrentItem(1,false);
                    measure(pager);
                }
                assertTrue(adapter.getItem(1).isAdded());
                assertEquals(1,manager.getFragments().stream().filter(f->f instanceof DescriptionFragment).count());
                pager.setAdapter(null);
            });
        } finally {instrumentation.runOnMainSync(activity::finish);}
    }
    @Test public void abortedDescriptionPopulationDoesNotReplayCommittedAdds() {
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        var activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.waitForIdleSync();
        try {
            instrumentation.runOnMainSync(()->{
                android.widget.FrameLayout root=new android.widget.FrameLayout(activity);root.setId(View.generateViewId());
                activity.setContentView(root);
                PagerHostFragment host=new PagerHostFragment();
                activity.getSupportFragmentManager().beginTransaction().add(root.getId(),host,"watch-test").commitNow();
                ViewPager pager=(ViewPager)host.requireView();
                var manager=host.getChildFragmentManager();
                TabAdapter adapter=new TabAdapter(manager);
                DescriptionFragment description=new DescriptionFragment();
                final boolean[] attempted={false};
                var callback=new androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks(){
                    @Override public void onFragmentCreated(androidx.fragment.app.FragmentManager fm,Fragment fragment,android.os.Bundle state){
                        if(fragment==description && !attempted[0]){
                            attempted[0]=true;
                            // Model a nested transaction from a tab lifecycle callback.
                            fm.beginTransaction().add(new Fragment(),"nested").commitNow();
                        }
                    }
                };
                manager.registerFragmentLifecycleCallbacks(callback,false);
                pager.setAdapter(adapter);
                measure(pager);
                try {adapter.addFragment(description,"description");}
                catch(IllegalStateException expected){assertTrue(expected.getMessage().contains("already executing"));}
                manager.unregisterFragmentLifecycleCallbacks(callback);
                // The next measure must not replay the ADD that already ran before the callback.
                measure(pager);
                adapter.finishUpdate(pager);
                assertTrue(description.isAdded());
                pager.setAdapter(null);
            });
        } finally {instrumentation.runOnMainSync(activity::finish);}
    }
    @Test public void lifecycleRefreshPublishesReplacementAfterCommit() {
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        var activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.waitForIdleSync();
        final TabAdapter[] adapter={null};
        final ViewPager[] pager={null};
        final DescriptionFragment original=new DescriptionFragment();
        final DescriptionFragment replacement=new DescriptionFragment();
        try {
            instrumentation.runOnMainSync(()->{
                android.widget.FrameLayout root=new android.widget.FrameLayout(activity);root.setId(View.generateViewId());
                activity.setContentView(root);
                PagerHostFragment host=new PagerHostFragment();
                activity.getSupportFragmentManager().beginTransaction().add(root.getId(),host,"watch-test").commitNow();
                pager[0]=(ViewPager)host.requireView();
                var manager=host.getChildFragmentManager();
                adapter[0]=new TabAdapter(manager);
                manager.registerFragmentLifecycleCallbacks(new androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks(){
                    @Override public void onFragmentCreated(androidx.fragment.app.FragmentManager fm,Fragment fragment,android.os.Bundle state){
                        if(fragment==original){
                            adapter[0].updateItem("description",replacement);
                            assertEquals("ViewPager retains its complete snapshot during attachment",1,adapter[0].getCount());
                        }
                    }
                },false);
                pager[0].setAdapter(adapter[0]);
                measure(pager[0]);
                adapter[0].addFragment(original,"description");
            });
            instrumentation.waitForIdleSync();
            instrumentation.runOnMainSync(()->{
                measure(pager[0]);
                assertTrue("The deferred description must actually be attached",replacement.isAdded());
                assertFalse("The old description must be removed",original.isAdded());
                assertSame(replacement,adapter[0].getItem(0));
                pager[0].setAdapter(null);
            });
        } finally {instrumentation.runOnMainSync(activity::finish);}
    }

    @Test public void recreatingWatchViewReplacesRestoredChildrenWithCurrentTabs() {
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        var activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.waitForIdleSync();
        try {
            instrumentation.runOnMainSync(()->{
                android.widget.FrameLayout root=new android.widget.FrameLayout(activity);root.setId(View.generateViewId());
                activity.setContentView(root);
                PagerHostFragment host=new PagerHostFragment();
                var manager=activity.getSupportFragmentManager();
                manager.beginTransaction().add(root.getId(),host,"watch-test").commitNow();
                ViewPager pager=(ViewPager)host.requireView();
                TabAdapter originalAdapter=new TabAdapter(host.getChildFragmentManager());
                pager.setAdapter(originalAdapter);
                DescriptionFragment original=new DescriptionFragment();
                originalAdapter.addFragment(original,"description");measure(pager);
                assertTrue(original.isAdded());
                originalAdapter.dispose();
                manager.beginTransaction().detach(host).commitNow();
                manager.beginTransaction().attach(host).commitNow();
                ViewPager recreatedPager=(ViewPager)host.requireView();
                TabAdapter currentAdapter=new TabAdapter(host.getChildFragmentManager());
                recreatedPager.setAdapter(currentAdapter);
                DescriptionFragment current=new DescriptionFragment();
                currentAdapter.addFragment(current,"description");measure(recreatedPager);
                assertTrue(current.isAdded());assertFalse(original.isAdded());
                assertEquals(1,host.getChildFragmentManager().getFragments().stream()
                    .filter(f->f instanceof DescriptionFragment).count());
                recreatedPager.setAdapter(null);
            });
        } finally {instrumentation.runOnMainSync(activity::finish);}
    }
    public static class PagerHostFragment extends Fragment {
        @Override public View onCreateView(android.view.LayoutInflater inflater,ViewGroup container,android.os.Bundle state){
            ViewPager pager=new ViewPager(requireContext());pager.setId(org.schabi.newpipe.R.id.view_pager);
            return pager;
        }
    }
    private static void measure(ViewGroup pager){
        pager.measure(View.MeasureSpec.makeMeasureSpec(1000,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(700,View.MeasureSpec.EXACTLY));
        pager.layout(0,0,1000,700);
    }
}
