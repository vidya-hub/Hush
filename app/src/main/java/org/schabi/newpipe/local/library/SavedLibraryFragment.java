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
    private TextView empty;
    private List<PlaylistMetadataEntry> items = Collections.emptyList();

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_list, container, false);
    }

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {
        list = rootView.findViewById(R.id.library_list);
        empty = rootView.findViewById(R.id.library_empty);
        empty.setText(R.string.library_saved_empty);
        list.setLayoutManager(new LinearLayoutManager(activity));
        playlists = new LocalPlaylistManager(NewPipeDatabase.getInstance(activity));
    }

    @Override
    public void onResume() {
        super.onResume();
        LibraryChrome.show(this, R.string.library_saved);
        disposables.clear();
        disposables.add(playlists.getPlaylists()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    items = value == null ? Collections.emptyList() : value;
                    render();
                }, error -> render()));
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        super.onDestroyView();
    }

    private void render() {
        if (list == null) {
            return;
        }
        empty.setVisibility(View.GONE);
        list.setAdapter(new PlaylistAdapter());
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
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.create_playlist)
                .setView(input)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create, (dialog, which) -> {
                    final String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        return;
                    }
                    disposables.add(playlists.createEmptyPlaylist(name)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(id -> {
                            }, error -> Toast.makeText(activity,
                                    R.string.general_error, Toast.LENGTH_SHORT).show()));
                })
                .show();
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
                holder.itemView.setOnClickListener(v -> createPlaylist());
                return;
            }
            final PlaylistMetadataEntry entry = items.get(position - 1);
            ((TextView) holder.itemView.findViewById(R.id.playlist_name)).setText(entry.name);
            final int count = (int) entry.streamCount;
            ((TextView) holder.itemView.findViewById(R.id.playlist_count)).setText(
                    getResources().getQuantityString(R.plurals.videos, count, count));
            holder.itemView.setOnClickListener(v -> NavigationHelper.openLocalPlaylistFragment(
                    getParentFragmentManager(), entry.uid, entry.name));
        }

        @Override
        public int getItemCount() {
            return items.size() + 1;
        }
    }
}
