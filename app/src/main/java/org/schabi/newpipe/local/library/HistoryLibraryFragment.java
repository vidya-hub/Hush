package org.schabi.newpipe.local.library;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.schabi.newpipe.BaseFragment;
import org.schabi.newpipe.R;
import org.schabi.newpipe.database.history.model.StreamHistoryEntry;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.hush.games.GameStateStore;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.PicassoHelper;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public final class HistoryLibraryFragment extends BaseFragment {
    private final CompositeDisposable disposables = new CompositeDisposable();
    private HistoryRecordManager records;
    private RecyclerView list;
    private TextView empty;
    private View clear;
    private View header;
    private ViewGroup historyPage;
    private boolean searchTab;
    private android.view.MenuItem clearMenu;
    private List<String> searches = Collections.emptyList();
    private List<StreamHistoryEntry> watched = Collections.emptyList();

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history_library, container, false);
    }

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        historyPage = (ViewGroup) rootView;
        header = rootView.findViewById(R.id.history_header);
        org.schabi.newpipe.hush.ui.HushUi.bindContentWidth(rootView);
        list = rootView.findViewById(R.id.library_list);
        empty = rootView.findViewById(R.id.library_empty);
        list.setLayoutManager(new LinearLayoutManager(activity));
        records = new HistoryRecordManager(activity);
        final SwitchCompat incognito = rootView.findViewById(R.id.history_incognito);
        incognito.setChecked(HistoryRecordManager.isIncognito(activity));
        final TextView summary=rootView.findViewById(R.id.history_privacy_summary);
        summary.setText(incognito.isChecked()?R.string.history_incognito_summary:R.string.hush_history_recording);
        incognito.setOnCheckedChangeListener((button, enabled) -> {
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                    .putBoolean(HistoryRecordManager.INCOGNITO_KEY, enabled).apply();
            if (!enabled) GameStateStore.clearIncognito();
            summary.setText(enabled?R.string.history_incognito_summary:R.string.hush_history_recording);
        });
        final TabLayout tabs = rootView.findViewById(R.id.history_tabs);
        final float fontScale = getResources().getConfiguration().fontScale;
        if (fontScale > 1.3f) {
            final ViewGroup.LayoutParams tabLayout = tabs.getLayoutParams();
            tabLayout.height = org.schabi.newpipe.hush.ui.HushUi.dp(requireContext(),
                    (float) Math.ceil(48 * fontScale));
            tabs.setLayoutParams(tabLayout);
        }
        tabs.addTab(tabs.newTab().setText(R.string.history_watch_tab));
        tabs.addTab(tabs.newTab().setText(R.string.history_search_tab));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(final TabLayout.Tab tab) {
                searchTab = tab.getPosition() == 1;
                render();
            }
            @Override public void onTabUnselected(final TabLayout.Tab tab) { }
            @Override public void onTabReselected(final TabLayout.Tab tab) { }
        });
        clear = rootView.findViewById(R.id.history_clear);
        clear.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(R.string.history_clear)
                .setMessage(R.string.history_clear_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.history_clear, (dialog, which) -> {
                    if (searchTab) {
                        disposables.add(records.deleteCompleteSearchHistory()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(deleted -> { }, error -> { }));
                    } else {
                        disposables.add(records.deleteWholeStreamHistory()
                                .flatMap(deleted -> records.deleteCompleteStreamStateHistory())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(deleted -> { }, error -> { }));
                    }
                }).show());
        render();
    }

    @Override
    public void onResume() {
        super.onResume();
        LibraryChrome.show(this, R.string.action_history);
        disposables.clear();
        disposables.add(records.getCompleteSearchHistory()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    searches = value == null ? Collections.emptyList() : value;
                    render();
                }, error -> render()));
        disposables.add(records.getStreamHistory()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    watched = value == null ? Collections.emptyList() : value;
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
        final List<Object> rows = new java.util.ArrayList<>();
        if (searchTab) {
            rows.addAll(searches);
            empty.setText(R.string.history_search_empty);
        } else {
            rows.addAll(watched);
            empty.setText(R.string.history_watch_empty);
        }
        empty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        if (clear != null) {
            clear.setVisibility(View.GONE);
        }
        if (clearMenu != null) {
            clearMenu.setVisible(!rows.isEmpty());
        }
        final boolean scrollHeader = (getResources().getConfiguration().fontScale > 1.3f
                    || getResources().getConfiguration().screenHeightDp < 480);
        list.setAdapter(null);
        if (header.getParent() instanceof ViewGroup) {
            ((ViewGroup) header.getParent()).removeView(header);
        }
        if (scrollHeader) {
            header.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            final RecyclerView.Adapter<RecyclerView.ViewHolder> heading =
                    new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(
                        @NonNull final ViewGroup parent, final int type) {
                    return new RecyclerView.ViewHolder(header) { };
                }
                @Override public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder,
                                                       final int position) { }
                @Override public int getItemCount() { return 1; }
            };
            list.setAdapter(new androidx.recyclerview.widget.ConcatAdapter(heading,
                    new HistoryAdapter(rows), new RecyclerView.Adapter<RecyclerView.ViewHolder>(){
                @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int type){
                    TextView message=new TextView(parent.getContext());message.setText(empty.getText());message.setTextSize(16);
                    int padding=org.schabi.newpipe.hush.ui.HushUi.dp(parent.getContext(),24);
                    message.setPadding(padding,padding,padding,padding);
                    return new RecyclerView.ViewHolder(message){};
                }
                @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,int position){}
                @Override public int getItemCount(){return rows.isEmpty()?1:0;}
            }));
            empty.setVisibility(View.GONE);
        } else {
            historyPage.addView(header, 0, new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            list.setAdapter(new HistoryAdapter(rows));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final android.view.Menu menu,
                                    @NonNull final android.view.MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        clearMenu = menu.add(R.string.history_clear);
        clearMenu.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER);
        clearMenu.setVisible(searchTab ? !searches.isEmpty() : !watched.isEmpty());
        clearMenu.setOnMenuItemClickListener(item -> {
            if (clear != null) clear.performClick();
            return true;
        });
    }

    private void openSearch(final String query) {
        if (!getParentFragmentManager().popBackStackImmediate()) {
            return;
        }
        final androidx.fragment.app.Fragment fragment = getParentFragmentManager()
                .findFragmentById(R.id.fragment_holder);
        if (fragment instanceof org.schabi.newpipe.fragments.list.search.SearchFragment) {
            ((org.schabi.newpipe.fragments.list.search.SearchFragment) fragment).submitSearch(query);
        }
    }

    private final class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int SEARCH = 1;
        private static final int STREAM = 2;
        private final List<Object> rows;

        private HistoryAdapter(final List<Object> rows) {
            this.rows = rows;
        }

        @Override
        public int getItemViewType(final int position) {
            final Object row = rows.get(position);
            return row instanceof String ? SEARCH : STREAM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                                           final int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == SEARCH) {
                return new RecyclerView.ViewHolder(inflater.inflate(
                        R.layout.item_library_search, parent, false)) {
                };
            }
            return new RecyclerView.ViewHolder(inflater.inflate(
                    (getResources().getConfiguration().fontScale > 1.3f
                            || getResources().getConfiguration().screenWidthDp < 360
                            ? R.layout.list_stream_grid_item : R.layout.list_stream_item), parent, false)) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder,
                                     final int position) {
            org.schabi.newpipe.hush.ui.HushIcons.apply(holder.itemView);
            holder.itemView.setPadding(0, holder.itemView.getPaddingTop(), 0, holder.itemView.getPaddingBottom());
            final Object row = rows.get(position);
            if (row instanceof String) {
                final String query = (String) row;
                ((TextView) holder.itemView).setText(query);
                holder.itemView.setOnClickListener(v -> openSearch(query));
                holder.itemView.setOnLongClickListener(v -> {
                    confirmDelete(() -> disposables.add(records.deleteSearchHistory(query)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(deleted -> { }, error -> { })));
                    return true;
                });
                return;
            }
            final StreamHistoryEntry entry = (StreamHistoryEntry) row;
            final StreamInfoItem item = entry.toStreamInfoItem();
            final TextView title = holder.itemView.findViewById(R.id.itemVideoTitleView);
            final TextView uploader = holder.itemView.findViewById(R.id.itemUploaderView);
            final TextView duration = holder.itemView.findViewById(R.id.itemDurationView);
            final View progress = holder.itemView.findViewById(R.id.itemProgressView);
            final View more = holder.itemView.findViewById(R.id.item_more);
            title.setText(item.getName());
            uploader.setText(item.getUploaderName());
            if (item.getDuration() > 0) {
                duration.setText(org.schabi.newpipe.util.Localization
                        .getDurationString(item.getDuration()));
                duration.setVisibility(View.VISIBLE);
            } else {
                duration.setVisibility(View.GONE);
            }
            progress.setVisibility(View.GONE);
            if (item.getThumbnailUrl() != null) {
                PicassoHelper.loadScaledDownThumbnail(holder.itemView.getContext(),
                        item.getThumbnailUrl()).into(
                        (android.widget.ImageView) holder.itemView
                                .findViewById(R.id.itemThumbnailView));
            }
            holder.itemView.setOnClickListener(v -> NavigationHelper.openVideoDetailFragment(
                    requireContext(), getParentFragmentManager(), item.getServiceId(),
                    item.getUrl(), item.getName(), null, false));
            more.setOnClickListener(v -> VideoActions.show(HistoryLibraryFragment.this, item));
            holder.itemView.setOnLongClickListener(v -> {
                confirmDelete(() -> disposables.add(records.deleteStreamHistoryAndState(
                        entry.getStreamId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> { }, error -> { })));
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }

    private void confirmDelete(final Runnable action) {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.history_delete_item)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> action.run())
                .show();
    }
}
