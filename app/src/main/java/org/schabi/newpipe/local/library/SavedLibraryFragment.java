package org.schabi.newpipe.local.library;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.schabi.newpipe.BaseFragment;
import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.R;
import org.schabi.newpipe.database.playlist.PlaylistMetadataEntry;
import org.schabi.newpipe.local.playlist.LocalPlaylistManager;
import org.schabi.newpipe.util.NavigationHelper;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public final class SavedLibraryFragment extends BaseFragment {
    private final CompositeDisposable disposables = new CompositeDisposable();
    private LocalPlaylistManager playlists;
    private RecyclerView list;
    private android.widget.FrameLayout detail;
    private long selected=-1;
    private boolean expanded;
    private TextView empty;
    private View emptyPanel, loadingView, retry;
    private boolean loading = true, failed;
    private List<PlaylistMetadataEntry> items = Collections.emptyList();

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        if(savedInstanceState!=null)selected=savedInstanceState.getLong("tablet-playlist",-1);
        View selector=inflater.inflate(R.layout.fragment_library_list,container,false);
        detail=new android.widget.FrameLayout(requireContext());detail.setId(R.id.hush_playlist_detail);
        org.schabi.newpipe.hush.ui.SettingsPanes panes=new org.schabi.newpipe.hush.ui.SettingsPanes(requireContext());
        panes.addView(selector);panes.addView(detail);
        panes.setOnModeChanged(wide->{
            expanded=wide; detail.setVisibility(wide && !items.isEmpty()?View.VISIBLE:View.GONE);
            if(wide && !items.isEmpty())openPlaylist(items.stream().filter(p->p.uid==selected).findFirst().orElse(items.get(0)));
        });
        return panes;
    }

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {

        list = rootView.findViewById(R.id.library_list);
        empty = rootView.findViewById(R.id.library_empty);
        empty.setText(R.string.library_saved_empty);
        emptyPanel = rootView.findViewById(R.id.library_empty_panel);
        loadingView = rootView.findViewById(R.id.library_loading);
        retry = rootView.findViewById(R.id.library_retry);
        retry.setOnClickListener(v -> loadPlaylists());
        ((android.widget.ImageView)rootView.findViewById(R.id.library_empty_icon)).setImageDrawable(
                org.schabi.newpipe.hush.ui.HushIcons.drawable(activity,"playlist"));
        final com.google.android.material.button.MaterialButton create=rootView.findViewById(R.id.library_empty_create);
        org.schabi.newpipe.hush.ui.HushUi.style(create,true);
        create.setIcon(org.schabi.newpipe.hush.ui.HushIcons.drawable(activity,"add"));
        create.setOnClickListener(v -> createPlaylist());
        list.setLayoutManager(new LinearLayoutManager(activity));
        playlists = new LocalPlaylistManager(NewPipeDatabase.getInstance(activity));
    }

    @Override
    public void onResume() {
        super.onResume();
        LibraryChrome.show(this, R.string.library_saved);
        loadPlaylists();
    }

    private void loadPlaylists() {
        loading = true; failed = false; render();
        disposables.clear();
        disposables.add(playlists.getPlaylists()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    loading = false; failed = false;
                    items = value == null ? Collections.emptyList() : value;
                    render();
                }, error -> { loading = false; failed = true; render(); }));
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state){
        super.onSaveInstanceState(state);state.putLong("tablet-playlist",selected);
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        list = null; empty = null; emptyPanel = null; loadingView = null; retry = null;
        super.onDestroyView();
    }

    private void render() {
        if (list == null) {
            return;
        }
        ((org.schabi.newpipe.hush.ui.SettingsPanes)requireView()).setHasDetail(!items.isEmpty());
        loadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
        retry.setVisibility(failed ? View.VISIBLE : View.GONE);
        emptyPanel.setVisibility(!loading && !failed && items.isEmpty() ? View.VISIBLE : View.GONE);
        list.setVisibility(!loading && !failed && !items.isEmpty() ? View.VISIBLE : View.GONE);
        if(list.getAdapter()==null)list.setAdapter(new PlaylistAdapter());else list.getAdapter().notifyDataSetChanged();
        if(expanded && !items.isEmpty() && items.stream().noneMatch(p->p.uid==selected))openPlaylist(items.get(0));
    }

    private void openPlaylist(final PlaylistMetadataEntry entry){
        if(!expanded){NavigationHelper.openLocalPlaylistFragment(getParentFragmentManager(),entry.uid,entry.name);return;}
        selected=entry.uid;
        if(getChildFragmentManager().isStateSaved())return;
        String tag="tablet-playlist-"+entry.uid;
        androidx.fragment.app.Fragment current=getChildFragmentManager().findFragmentById(R.id.hush_playlist_detail);
        if(current!=null && tag.equals(current.getTag()))return;
        getChildFragmentManager().beginTransaction().replace(R.id.hush_playlist_detail,
                org.schabi.newpipe.local.playlist.LocalPlaylistFragment.getInstance(entry.uid,entry.name),tag).commit();
        if(list.getAdapter()!=null)list.getAdapter().notifyDataSetChanged();
    }

    private void createPlaylist() {
        if (activity == null) {
            return;
        }
        final EditText input = new EditText(activity);
        input.setHint(R.string.name);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        final int pad = (int) (20 * activity.getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);
        final androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.create_playlist)
                .setView(input)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create, null)
                .create();
        dialog.setOnShowListener(shown -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    final String name = input.getText().toString().trim();
                    if (name.isEmpty()) { input.setError(getString(R.string.name)); return; }
                    disposables.add(playlists.createEmptyPlaylist(name)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(id -> dialog.dismiss(), error -> input.setError(getString(R.string.general_error))));
                }));
        dialog.show();
    }

    private final class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public int getItemViewType(final int position) {
            return position == 0 ? 0 : 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                                           final int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == 0) {
                final TextView button = (TextView) inflater.inflate(
                        R.layout.item_library_header, parent, false);
                return new RecyclerView.ViewHolder(button) {
                };
            }
            return new RecyclerView.ViewHolder(inflater.inflate(
                    R.layout.item_playlist_row, parent, false)) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder,
                                     final int position) {
            if (position == 0) {
                ((TextView) holder.itemView).setText(R.string.create_playlist);
                ((TextView) holder.itemView).setCompoundDrawablesRelativeWithIntrinsicBounds(
                        org.schabi.newpipe.hush.ui.HushIcons.drawable(activity,"add"),null,null,null);
                holder.itemView.setOnClickListener(v -> createPlaylist());
                return;
            }
            final PlaylistMetadataEntry entry = items.get(position - 1);
            ((TextView) holder.itemView.findViewById(R.id.playlist_name)).setText(entry.name);
            org.schabi.newpipe.hush.ui.HushIcons.apply(holder.itemView);
            final int count = (int) entry.streamCount;
            ((TextView) holder.itemView.findViewById(R.id.playlist_count)).setText(
                    getResources().getQuantityString(R.plurals.videos, count, count));
            holder.itemView.setSelected(entry.uid==selected);
            holder.itemView.setBackground(org.schabi.newpipe.hush.ui.HushUi.shape(activity,
                    entry.uid==selected?org.schabi.newpipe.hush.ui.HushUi.color(activity,
                    com.google.android.material.R.attr.colorPrimaryContainer):0,16,0));
            holder.itemView.setOnClickListener(v -> openPlaylist(entry));
        }

        @Override
        public int getItemCount() {
            return items.size() + 1;
        }
    }
}
