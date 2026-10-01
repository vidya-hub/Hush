package org.schabi.newpipe.settings;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;

public class MainSettingsFragment extends BasePreferenceFragment {
    public static final boolean DEBUG = MainActivity.DEBUG;

    private SettingsActivity settingsActivity;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResourceRegistry();
        applyGeneratedIcons(getPreferenceScreen());

        setHasOptionsMenu(true); // Otherwise onCreateOptionsMenu is not called
    }

    @Override public void onViewCreated(@NonNull android.view.View view, Bundle state){
        super.onViewCreated(view,state);
        if(getId()!=R.id.settings_categories)return;
        getListView().addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration(){
            @Override public void onDraw(@NonNull android.graphics.Canvas canvas,@NonNull androidx.recyclerview.widget.RecyclerView parent,
                    @NonNull androidx.recyclerview.widget.RecyclerView.State state){
                androidx.fragment.app.Fragment selected=getParentFragmentManager().findFragmentById(R.id.settings_fragment_holder);
                if(selected==null || !(parent.getAdapter() instanceof androidx.preference.PreferenceGroupAdapter))return;
                androidx.preference.PreferenceGroupAdapter adapter=(androidx.preference.PreferenceGroupAdapter)parent.getAdapter();
                android.graphics.Paint paint=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
                paint.setColor(org.schabi.newpipe.hush.ui.HushUi.color(requireContext(),com.google.android.material.R.attr.colorPrimaryContainer));
                int radius=org.schabi.newpipe.hush.ui.HushUi.dp(requireContext(),16);
                for(int i=0;i<parent.getChildCount();i++){
                    android.view.View row=parent.getChildAt(i);int position=parent.getChildAdapterPosition(row);
                    androidx.preference.Preference preference=position<0?null:adapter.getItem(position);
                    boolean active=preference!=null && selected.getClass().getName().equals(preference.getFragment());row.setSelected(active);
                    if(active)canvas.drawRoundRect(row.getLeft(),row.getTop(),row.getRight(),row.getBottom(),radius,radius,paint);
                }
            }
        });
    }

    private void applyGeneratedIcons(final androidx.preference.PreferenceGroup group) {
        for (int i=0; i<group.getPreferenceCount(); i++) {
            final androidx.preference.Preference row=group.getPreference(i);
            final String fragment=row.getFragment();
            if (fragment != null) {
                String icon="settings";
                if(fragment.endsWith("VideoAudioSettingsFragment"))icon="headphones";
                else if(fragment.endsWith("GestureSettingsFragment"))icon="gestures";
                else if(fragment.endsWith("DownloadSettingsFragment"))icon="download";
                else if(fragment.endsWith("HistorySettingsFragment"))icon="history";
                else if(fragment.endsWith("NotificationsSettingsFragment"))icon="feed";
                else if(fragment.endsWith("BackupSettingsFragment"))icon="backup";
                else if(fragment.endsWith("AppearanceSettingsFragment"))icon="appearance";
                else if(fragment.endsWith("AccountSettingsFragment"))icon="profile";
                else if(fragment.endsWith("ContentSettingsFragment"))icon="globe";
                else if(fragment.endsWith("FilterSettingsFragment"))icon="content-filter";
                row.setIcon(org.schabi.newpipe.hush.ui.HushIcons.drawable(requireContext(),icon));
            }
            if(row instanceof androidx.preference.PreferenceGroup)applyGeneratedIcons((androidx.preference.PreferenceGroup)row);
        }
    }

    @Override
    public void onCreateOptionsMenu(
            @NonNull final Menu menu,
            @NonNull final MenuInflater inflater
    ) {
        super.onCreateOptionsMenu(menu, inflater);

        // -- Link settings activity and register menu --
        settingsActivity = (SettingsActivity) getActivity();

        inflater.inflate(R.menu.menu_settings_main_fragment, menu);

        final MenuItem menuSearchItem = menu.getItem(0);

        settingsActivity.setMenuSearchItem(menuSearchItem);

        menuSearchItem.setOnMenuItemClickListener(ev -> {
            settingsActivity.setSearchActive(true);
            return true;
        });
    }

    @Override
    public void onDestroy() {
        // Unlink activity so that we don't get memory problems
        if (settingsActivity != null) {
            settingsActivity.setMenuSearchItem(null);
            settingsActivity = null;
        }
        super.onDestroy();
    }
}
