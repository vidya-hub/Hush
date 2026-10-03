package org.schabi.newpipe.fragments.detail;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Owns one transaction per Watch pager update, including both additions and removals. */
public class TabAdapter extends PagerAdapter {
    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    // ViewPager sees only complete snapshots, even if tab data changes in a lifecycle callback.
    private final List<Fragment> publishedFragments = new ArrayList<>();
    private final FragmentManager fragmentManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<Fragment> additions = identitySet();
    private final Set<Fragment> removals = identitySet();
    private final String tagPrefix = "hush:watch:" + UUID.randomUUID() + ":";
    private final IdentityHashMap<Fragment, String> tags = new IdentityHashMap<>();
    private Fragment primary;
    private boolean committing;
    private boolean publishing;
    private boolean updateRequested;
    private boolean updatePosted;
    private boolean restoredChildrenRemoved;
    private int batchDepth;
    private boolean disposed;
    private Runnable tabsChangedListener;

    public TabAdapter(final FragmentManager manager) {
        fragmentManager = manager;
    }

    public void setTabsChangedListener(final Runnable listener) {
        tabsChangedListener = listener;
    }

    public void dispose() {
        disposed = true;
        handler.removeCallbacksAndMessages(null);
        tabsChangedListener = null;
    }

    private static Set<Fragment> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    public void beginUpdates() {
        batchDepth++;
    }

    public void endUpdates() {
        if (batchDepth == 0) {
            throw new IllegalStateException("Unbalanced tab update");
        }
        if (--batchDepth == 0 && updateRequested) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void startUpdate(@NonNull final ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("Watch pager requires a view ID");
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final Fragment fragment = publishedFragments.get(position);
        removals.remove(fragment);
        additions.add(fragment);
        tags.computeIfAbsent(fragment, ignored -> tagPrefix + UUID.randomUUID());
        if (fragment != primary) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull final ViewGroup container, final int position,
                            @NonNull final Object object) {
        final Fragment fragment = (Fragment) object;
        additions.remove(fragment);
        removals.add(fragment);
        if (fragment == primary) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
            primary = null;
        }
    }

    @Override
    public void setPrimaryItem(@NonNull final ViewGroup container, final int position,
                               @NonNull final Object object) {
        final Fragment fragment = (Fragment) object;
        if (fragment != primary) {
            if (primary != null) {
                primary.setMenuVisibility(false);
                primary.setUserVisibleHint(false);
            }
            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);
            primary = fragment;
        }
    }

    @Override
    public void finishUpdate(@NonNull final ViewGroup container) {
        if (committing) {
            return;
        }
        if (!restoredChildrenRemoved) {
            // The parent restores the current stream and selected tab. Old pager children must
            // not override fresh tab instances with the previous stream's content or tag.
            for (final Fragment fragment : fragmentManager.getFragments()) {
                if (fragment.getId() == container.getId()
                        && !publishedFragments.contains(fragment)) {
                    removals.add(fragment);
                }
            }
            restoredChildrenRemoved = true;
        }
        if (additions.isEmpty() && removals.isEmpty()) {
            return;
        }
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (final Fragment fragment : removals) {
            transaction.remove(fragment);
            tags.remove(fragment);
        }
        for (final Fragment fragment : additions) {
            if (fragment.isDetached()) {
                transaction.attach(fragment);
            } else if (!fragment.isAdded()) {
                transaction.add(container.getId(), fragment, tags.get(fragment));
            }
        }
        // Release pending operations BEFORE lifecycle callbacks run. A partially executed
        // transaction must never be retried by the next ViewPager.onMeasure().
        additions.clear();
        removals.clear();
        committing = true;
        try {
            transaction.commitNowAllowingStateLoss();
        } finally {
            committing = false;
            postRequestedUpdate();
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull final View view, @NonNull final Object object) {
        return ((Fragment) object).getView() == view;
    }

    @NonNull
    public Fragment getItem(final int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return publishedFragments.size();
    }

    public void addFragment(final Fragment fragment, final String title) {
        if (canChangeTabs()) {
            fragments.add(fragment);
            titles.add(title);
            notifyDataSetChanged();
        }
    }

    public void clearAllItems() {
        if (canChangeTabs()) {
            fragments.clear();
            titles.clear();
            notifyDataSetChanged();
        }
    }

    public void removeItem(final int position) {
        if (canChangeTabs()) {
            fragments.remove(position);
            titles.remove(position);
            notifyDataSetChanged();
        }
    }

    public void updateItem(final int position, final Fragment fragment) {
        if (canChangeTabs()) {
            fragments.set(position, fragment);
            notifyDataSetChanged();
        }
    }

    public void updateItem(final String title, final Fragment fragment) {
        final int index = titles.indexOf(title);
        if (index != -1) {
            updateItem(index, fragment);
        }
    }

    @Override
    public int getItemPosition(@NonNull final Object object) {
        final int index = publishedFragments.indexOf(object);
        return index == -1 ? POSITION_NONE : index;
    }

    public int getItemPositionByTitle(final String title) {
        return titles.indexOf(title);
    }

    @Nullable
    public String getItemTitle(final int position) {
        return position < 0 || position >= titles.size() ? null : titles.get(position);
    }

    public void notifyDataSetUpdate() {
        notifyDataSetChanged();
    }

    private boolean canChangeTabs() {
        if (disposed || fragmentManager.isStateSaved() || fragmentManager.isDestroyed()) {
            Log.d("TabAdapter", "Ignoring tab update after parent state was saved/destroyed");
            return false;
        }
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        if (!canChangeTabs()) {
            return;
        }
        updateRequested = true;
        if (batchDepth != 0) {
            return;
        }
        if (committing || publishing) {
            postRequestedUpdate();
            return;
        }
        updateRequested = false;
        publishedFragments.clear();
        publishedFragments.addAll(fragments);
        publishing = true;
        try {
            // Do not swallow lifecycle errors and leave ViewPager half updated.
            super.notifyDataSetChanged();
            if (tabsChangedListener != null) {
                tabsChangedListener.run();
            }
        } finally {
            publishing = false;
            postRequestedUpdate();
        }
    }

    private void postRequestedUpdate() {
        if (disposed || !updateRequested || updatePosted || committing || publishing || batchDepth != 0) {
            return;
        }
        updatePosted = true;
        handler.post(() -> {
            updatePosted = false;
            notifyDataSetChanged();
        });
    }
}
