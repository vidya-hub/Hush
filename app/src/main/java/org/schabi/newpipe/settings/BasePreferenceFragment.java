package org.schabi.newpipe.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.util.ThemeHelper;

import java.util.Objects;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {
    protected final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    protected static final boolean DEBUG = MainActivity.DEBUG;

    SharedPreferences defaultPreferences;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        super.onCreate(savedInstanceState);
    }

    protected void addPreferencesFromResourceRegistry() {
        addPreferencesFromResource(
                SettingsResourceRegistry.getInstance().getPreferencesResId(this.getClass()));
    }

    @Override
    public void onViewCreated(@NonNull final View rootView,
                              @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        setDivider(null);
        getListView().setClipToPadding(false);
        final androidx.recyclerview.widget.RecyclerView list = getListView();
        list.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull final android.graphics.Rect outRect,
                                       @NonNull final View view,
                                       @NonNull final androidx.recyclerview.widget.RecyclerView parent,
                                       @NonNull final androidx.recyclerview.widget.RecyclerView.State state) {
                // Preference rows supply their own 16dp inset; add only the missing gutter.
                final int side = Math.max(0,
                        org.schabi.newpipe.hush.ui.HushUi.contentSide(rootView, rootView.getWidth())
                        - org.schabi.newpipe.hush.ui.HushUi.dp(rootView.getContext(), 16));
                outRect.set(side, 0, side, 0);
            }
        });
        rootView.addOnLayoutChangeListener((view, l, t, r, b, ol, ot, or, ob) -> {
            if (r - l != or - ol) list.invalidateItemDecorations();
        });
        ThemeHelper.setTitleToAppCompatActivity(getActivity(), getPreferenceScreen().getTitle());
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeHelper.setTitleToAppCompatActivity(getActivity(), getPreferenceScreen().getTitle());
    }

    @NonNull
    public final Preference requirePreference(@StringRes final int resId) {
        final Preference preference = findPreference(getString(resId));
        Objects.requireNonNull(preference);
        return preference;
    }
}
