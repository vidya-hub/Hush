package org.schabi.newpipe.fragments.list.search;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags;
import static org.schabi.newpipe.ktx.ViewUtils.animate;
import static org.schabi.newpipe.util.ExtractorHelper.showMetaInfoInTextView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;
import org.schabi.newpipe.databinding.FragmentSearchBinding;
import org.schabi.newpipe.error.ErrorInfo;
import org.schabi.newpipe.error.ErrorUtil;
import org.schabi.newpipe.error.ReCaptchaActivity;
import org.schabi.newpipe.error.UserAction;
import org.schabi.newpipe.database.stream.StreamStatisticsEntry;
import org.schabi.newpipe.database.stream.model.StreamStateEntity;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabInfo;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.search.filter.Filter;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.fragments.BackPressable;
import org.schabi.newpipe.fragments.list.BaseListFragment;
import org.schabi.newpipe.fragments.list.search.filter.SearchFilterLogic;
import org.schabi.newpipe.fragments.list.search.filter.SearchFilterUI;
import org.schabi.newpipe.ktx.AnimationType;
import org.schabi.newpipe.ktx.ExceptionUtils;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.local.profile.ProfileStore;
import org.schabi.newpipe.hush.games.GamesFragment;
import org.schabi.newpipe.hush.games.GameStateStore;
import org.schabi.newpipe.player.PlayerService;
import org.schabi.newpipe.player.helper.PlayerHolder;
import org.schabi.newpipe.player.playqueue.PlayQueue;
import org.schabi.newpipe.player.playqueue.PlayQueueItem;
import org.schabi.newpipe.settings.NewPipeSettings;
import org.schabi.newpipe.util.Constants;
import org.schabi.newpipe.util.DeviceUtils;
import org.schabi.newpipe.util.ExtractorHelper;
import org.schabi.newpipe.util.KeyboardUtil;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.PicassoHelper;
import org.schabi.newpipe.util.ServiceHelper;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchFragment extends BaseListFragment<SearchInfo, ListExtractor.InfoItemsPage<?>>
        implements BackPressable, SearchFilterLogic.Callback, SearchFilterDialog.Callback {
    private static final String YOUTUBE_MUSIC_FILTER_PREFIX = "music_";
    private static final String SEARCH_FILTER_LAST_SERVICE_KEY = "search_filter_last_service";
    private static final String SEARCH_FILTER_LAST_UI_SERVICE_KEY_PREFIX =
            "search_filter_last_ui_service_";
    private static final String SEARCH_FILTER_CONTENT_KEY_PREFIX = "search_filter_content_";
    private static final String SEARCH_FILTER_SORT_KEY_PREFIX = "search_filter_sort_";

    /*//////////////////////////////////////////////////////////////////////////
    // Search
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * The suggestions will only be fetched from network if the query meet this threshold (>=).
     * (local ones will be fetched regardless of the length)
     */
    private static final int THRESHOLD_NETWORK_SUGGESTION = 1;

    /**
     * How much time have to pass without emitting a item (i.e. the user stop typing)
     * to fetch/show the suggestions, in milliseconds.
     */
    private static final int SUGGESTIONS_DEBOUNCE = 120; //ms
    private final PublishSubject<String> suggestionPublisher = PublishSubject.create();

    protected int serviceId = Constants.NO_SERVICE_ID;

    // these three represents the current search query
    String searchString;

    /**
     * No content filter should add like contentFilter = all
     * be aware of this when implementing an extractor.
     */
    ArrayList<FilterItem> selectedContentFilter = new ArrayList<>();

    ArrayList<FilterItem> selectedSortFilter;

    // these represents the last search
    String lastSearchedString;

    String searchSuggestion;

    boolean isCorrectedSearch;

    MetaInfo[] metaInfo;

    boolean channelSearchMode;

    String channelUrl;

    String channelName;

    ListLinkHandler channelSearchHandler;

    boolean wasSearchFocused = false;

    private StreamingService service;
    private Page nextPage;
    private boolean showLocalSuggestions = true;
    private boolean showRemoteSuggestions = true;

    private Disposable searchDisposable;
    private Disposable suggestionDisposable;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private int homeScrollY;

    private SuggestionListAdapter suggestionListAdapter;
    private HistoryRecordManager historyRecordManager;

    /*//////////////////////////////////////////////////////////////////////////
    // Views
    //////////////////////////////////////////////////////////////////////////*/

    private FragmentSearchBinding searchBinding;

    private View searchToolbarContainer;
    private EditText searchEditText;
    private View searchClear;
    private View searchFilter;
    private View searchSubmit;

    private SearchFilterUI searchFilterUi;
    private boolean isTv;
    private boolean useOldSearchFilter;

    private boolean suggestionsPanelVisible = false;
    /** Centered search field instead of the activity toolbar. */
    private boolean centeredSearch;
    private boolean showingResults;
    private ViewTreeObserver.OnGlobalLayoutListener searchSafeAreaListener;
    private int searchBaseBottomMargin;
    private String photoProfileId;
    private final androidx.activity.result.ActivityResultLauncher<String> profilePhotoPicker =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                    this::onProfilePhotoPicked);

    /*////////////////////////////////////////////////////////////////////////*/

    private TextWatcher textWatcher;

    public ArrayList<Integer> userSelectedContentFilterList;

    ArrayList<Integer> userSelectedSortFilterList = null;

    public static SearchFragment getInstance(final int serviceId, final String searchString) {
        final SearchFragment searchFragment = new SearchFragment();

        List<FilterItem> defaultContentFilter = new ArrayList<>();
        List<FilterItem> defaultSortFilter = new ArrayList<>();

        try {
            StreamingService service = NewPipe.getService(serviceId);
            searchFragment.service = service;
            defaultContentFilter.add(service.getSearchQHFactory().getFilterItem(0)); // 默认 "all"
        } catch (Exception e) {
            Log.e("Search", "Failed to initialize default filters", e);
        }

        searchFragment.setQuery(serviceId, searchString, defaultContentFilter, defaultSortFilter);
        searchFragment.restorePersistedSearchFilters(PreferenceManager
                .getDefaultSharedPreferences(App.getApp()));
        searchFragment.restoreSelectedFilters();

        if (!TextUtils.isEmpty(searchString)) {
            searchFragment.setSearchOnResume();
        }

        return searchFragment;
    }

    public static int getPersistedSearchServiceId(final Context context,
                                                  final int fallbackServiceId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!shouldRememberSearchFilters(prefs)) {
            return fallbackServiceId;
        }
        final int serviceId = prefs.getInt(SEARCH_FILTER_LAST_SERVICE_KEY, fallbackServiceId);
        try {
            NewPipe.getService(serviceId);
            return serviceId;
        } catch (final Exception ignored) {
            return fallbackServiceId;
        }
    }

    public static void setPersistedSearchServiceId(final Context context, final int serviceId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!shouldRememberSearchFilters(prefs)) {
            return;
        }
        try {
            NewPipe.getService(serviceId);
            prefs.edit()
                    .putInt(SEARCH_FILTER_LAST_SERVICE_KEY, serviceId)
                    .apply();
        } catch (final Exception ignored) {
        }
    }

    public static int getPersistedSearchContentFilterId(final Context context,
                                                        final int filterServiceId,
                                                        final int fallbackFilterId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!shouldRememberSearchFilters(prefs)) {
            return fallbackFilterId;
        }
        return prefs.getInt(getSearchFilterContentKey(filterServiceId), fallbackFilterId);
    }

    public static ArrayList<Integer> getPersistedSearchSortFilterIds(final Context context,
                                                                     final int filterServiceId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!shouldRememberSearchFilters(prefs)) {
            return new ArrayList<>();
        }
        return deserializeFilterIds(prefs.getString(getSearchFilterSortKey(filterServiceId), ""));
    }

    public static SearchFragment getChannelInstance(final int serviceId,
                                                    final String channelUrl,
                                                    final String channelName) {
        final SearchFragment searchFragment = getInstance(serviceId, "");
        searchFragment.channelSearchMode = true;
        searchFragment.channelUrl = channelUrl;
        searchFragment.channelName = channelName;
        return searchFragment;
    }


    /**
     * Set wasLoading to true so when the fragment onResume is called, the initial search is done.
     */
    private void setSearchOnResume() {
        wasLoading.set(true);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Fragment's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        showLocalSuggestions = NewPipeSettings.showLocalSearchSuggestions(activity, prefs);
        showRemoteSuggestions = NewPipeSettings.showRemoteSearchSuggestions(activity, prefs);

        suggestionListAdapter = new SuggestionListAdapter(activity);
        historyRecordManager = new HistoryRecordManager(context);

        isTv = DeviceUtils.isTv(context);

        useOldSearchFilter = isTv || prefs.getBoolean(
                context.getString(R.string.use_old_search_filter_key), false);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        if (useOldSearchFilter && !channelSearchMode) {
            searchFilterUi = new SearchFilterUI(this, getContext());
        }
        updateService();
        restorePersistedSearchFilters();
        if (useOldSearchFilter && !channelSearchMode) {
            searchFilterUi.restorePreviouslySelectedFilters(
                    userSelectedContentFilterList,
                    userSelectedSortFilterList);
        }
        restoreSelectedFilters();
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View rootView, final Bundle savedInstanceState) {
        searchBinding = FragmentSearchBinding.bind(rootView);
        super.onViewCreated(rootView, savedInstanceState);
        showSearchOnStart();
        initSearchListeners();

        if (!TextUtils.isEmpty(searchString) && infoListAdapter.getItemsList().isEmpty()) {
            searchEditText.setText(searchString);
            search(searchString);
        }
    }

    private void updateService() {
        try {
            service = NewPipe.getService(serviceId);
            if (useOldSearchFilter && searchFilterUi != null) {
                searchFilterUi.updateService(service);
            }
        } catch (final Exception e) {
            ErrorUtil.showUiErrorSnackbar(this, "Getting service for id " + serviceId, e);
        }
    }

    @Override
    public void onStart() {
        if (DEBUG) {
            Log.d(TAG, "onStart() called");
        }
        super.onStart();

        updateService();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (centeredSearch && searchBinding != null) {
            homeScrollY = searchBinding.homeSpacer.getScrollY();
        }

        wasSearchFocused = searchEditText.hasFocus();

        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }
        disposables.clear();
        hideKeyboardSearch();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(TAG, "onResume() called");
        }
        super.onResume();

        if (suggestionDisposable == null || suggestionDisposable.isDisposed()) {
            initSuggestionObserver();
        }

        handleSearchSuggestion();

        showMetaInfoInTextView(metaInfo == null ? null : Arrays.asList(metaInfo),
                searchBinding.searchMetaInfoTextView, searchBinding.searchMetaInfoSeparator,
                disposables);

        if (centeredSearch && activity instanceof org.schabi.newpipe.MainActivity) {
            ((org.schabi.newpipe.MainActivity) activity).setSearchChrome(true);
            updateProfileLabel();
            updateIncognitoHint();
            refreshNowPlaying();
            searchBinding.homeSpacer.post(() -> {
                if (searchBinding != null) {
                    searchBinding.homeSpacer.scrollTo(0, homeScrollY);
                }
            });
        }
        if (centeredSearch && TextUtils.isEmpty(searchString)) {
            applySearchChrome(false);
            hideKeyboardSearch();
        } else if (TextUtils.isEmpty(searchString) || wasSearchFocused) {
            showKeyboardSearch();
            showSuggestionsPanel();
        } else {
            if (centeredSearch) {
                applySearchChrome(true);
            }
            hideKeyboardSearch();
            hideSuggestionsPanel();
        }
        wasSearchFocused = false;
    }

    @Override
    public void onDestroyView() {
        if (DEBUG) {
            Log.d(TAG, "onDestroyView() called");
        }
        unsetSearchListeners();

        if (searchSafeAreaListener != null) {
            searchBinding.getRoot().getViewTreeObserver()
                    .removeOnGlobalLayoutListener(searchSafeAreaListener);
            searchSafeAreaListener = null;
        }

        if (centeredSearch) {
            searchBinding = null;
            super.onDestroyView();
            return;
        }
        updateSearchActionLayout(searchSubmit, 48, 0);
        updateSearchActionLayout(searchFilter, 48, 48);
        updateSearchActionLayout(searchClear, 48, 0);
        searchClear.setVisibility(View.VISIBLE);
        searchFilter.setVisibility(View.GONE);
        searchSubmit.setVisibility(View.GONE);
        updateSearchEditTextMargin();

        searchBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }
        disposables.clear();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == ReCaptchaActivity.RECAPTCHA_REQUEST) {
            if (resultCode == Activity.RESULT_OK
                    && !TextUtils.isEmpty(searchString)) {
                search();
            } else {
                Log.e(TAG, "ReCaptcha failed");
            }
        } else {
            Log.e(TAG, "Request code from activity not supported [" + requestCode + "]");
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        centeredSearch = !channelSearchMode && searchBinding.homeSearchEditText != null;
        final RecyclerView suggestionRecycler = centeredSearch
                ? searchBinding.homeSuggestionsList
                : searchBinding.suggestionsList;
        suggestionRecycler.setAdapter(suggestionListAdapter);
        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull final RecyclerView recyclerView,
                                        @NonNull final RecyclerView.ViewHolder viewHolder) {
                return getSuggestionMovementFlags(viewHolder);
            }

            @Override
            public boolean onMove(@NonNull final RecyclerView recyclerView,
                                  @NonNull final RecyclerView.ViewHolder viewHolder,
                                  @NonNull final RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
                onSuggestionItemSwiped(viewHolder);
            }
        }).attachToRecyclerView(suggestionRecycler);

        searchToolbarContainer = activity.findViewById(R.id.toolbar_search_container);
        if (centeredSearch) {
            searchBaseBottomMargin = ((LinearLayout.LayoutParams)
                    searchBinding.homeSearchGroup.getLayoutParams()).bottomMargin;
            searchSafeAreaListener = () -> {
                if (searchBinding == null) {
                    return;
                }
                final View view = searchBinding.getRoot();
                final Rect visible = new Rect();
                view.getWindowVisibleDisplayFrame(visible);
                final int[] location = new int[2];
                view.getLocationOnScreen(location);
                final int overlap = Math.max(0,
                        location[1] + view.getHeight() - visible.bottom);
                final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                        searchBinding.homeSearchGroup.getLayoutParams();
                final int bottomMargin = searchBaseBottomMargin + overlap;
                if (params.bottomMargin != bottomMargin) {
                    params.bottomMargin = bottomMargin;
                    searchBinding.homeSearchGroup.setLayoutParams(params);
                }
            };
            searchBinding.getRoot().getViewTreeObserver()
                    .addOnGlobalLayoutListener(searchSafeAreaListener);
            searchEditText = searchBinding.homeSearchEditText;
            searchClear = searchBinding.homeSearchClear;
            searchSubmit = null;
            searchFilter = searchToolbarContainer.findViewById(R.id.toolbar_search_filter);
            searchToolbarContainer.setVisibility(View.GONE);
            searchFilter.setVisibility(View.GONE);
            searchBinding.profileButton.setOnClickListener(v -> showProfileDialog());
            searchBinding.homeMenu.setOnClickListener(this::showHomeMenu);
            searchBinding.homeContent.removeAllViews();
            searchBinding.homeContent.setPadding(0, 0, 0, 0);
            searchBinding.homeContent.setGravity(android.view.Gravity.TOP);
            searchBinding.homeContent.addView(HomeDashboard.build(activity,
                    new HomeDashboard.Actions() {
                        @Override public void breathe() { openBreakSheet(false); }
                        @Override public void meditate() { openBreakSheet(true); }
                        @Override public void game(final String game) { openGame(game); }
                        @Override public void allGames() { openGame(null); }
                        @Override public void history() { openLibrary(
                                new org.schabi.newpipe.local.library.HistoryLibraryFragment()); }
                        @Override public void saved() { openLibrary(
                                new org.schabi.newpipe.local.library.SavedLibraryFragment()); }
                        @Override public void downloads() {
                            NavigationHelper.openDownloads(activity);
                        }
                    }));
            HomeDashboard.styleSearch(searchBinding, activity);
            searchBinding.homeSpacer.setAlpha(0f);
            searchBinding.homeSpacer.animate().alpha(1f).setDuration(200).start();
            searchBinding.homeViewToggle.setOnClickListener(v -> toggleResultsLayout());
            searchBinding.homeSearchLeading.setOnClickListener(v -> {
                if (showingResults) {
                    returnToHome();
                }
            });
            searchBinding.nowPlayingTitle.setOnClickListener(v -> openNowPlaying());
            searchBinding.nowPlayingThumb.setOnClickListener(v -> openNowPlaying());
            searchBinding.nowPlayingToggle.setOnClickListener(v -> {
                if (activity != null) {
                    activity.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE));
                    searchBinding.nowPlayingToggle.postDelayed(this::refreshNowPlaying, 200);
                }
            });
            searchBinding.nowPlayingClose.setOnClickListener(v -> {
                if (activity != null) {
                    activity.sendBroadcast(new Intent(PlayerService.ACTION_CLOSE));
                    searchBinding.nowPlayingBar.setVisibility(View.GONE);
                    searchBinding.homeColumn.setPadding(0, 0, 0, 0);
                }
            });
            updateProfileLabel();
            applySearchChrome(false);
            return;
        }
        searchBinding.profileButton.setVisibility(View.GONE);
        searchEditText = searchToolbarContainer.findViewById(R.id.toolbar_search_edit_text);
        searchClear = searchToolbarContainer.findViewById(R.id.toolbar_search_clear);
        searchFilter = searchToolbarContainer.findViewById(R.id.toolbar_search_filter);
        searchSubmit = searchToolbarContainer.findViewById(R.id.toolbar_search_submit);

        updateSearchActionLayout(searchSubmit, 40, 0);
        updateSearchActionLayout(searchFilter, 40, 40);
        updateSearchActionLayout(searchClear, 40,
                useOldSearchFilter || channelSearchMode ? 40 : 80);
        searchClear.setVisibility(View.GONE);
        searchFilter.setVisibility(useOldSearchFilter || channelSearchMode
                ? View.GONE : View.VISIBLE);
        searchSubmit.setVisibility(View.VISIBLE);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // State Saving
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void writeTo(final Queue<Object> objectsToSave) {
        super.writeTo(objectsToSave);
        objectsToSave.add(nextPage);
        objectsToSave.add(metaInfo);
        objectsToSave.add(channelSearchHandler);
    }

    @Override
    public void readFrom(@NonNull final Queue<Object> savedObjects) throws Exception {
        super.readFrom(savedObjects);
        nextPage = (Page) savedObjects.poll();
        metaInfo = (MetaInfo[]) savedObjects.poll();
        channelSearchHandler = (ListLinkHandler) savedObjects.poll();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle bundle) {
        try{
            searchString = searchEditText != null
                    ? searchEditText.getText().toString()
                    : searchString;

            userSelectedContentFilterList = new ArrayList<>();
            if (!selectedContentFilter.isEmpty()) {
                userSelectedContentFilterList.add(selectedContentFilter.get(0).getIdentifier());
            }
            userSelectedSortFilterList = new ArrayList<>();
            if (selectedSortFilter != null) {
                for (final FilterItem filterItem : selectedSortFilter) {
                    userSelectedSortFilterList.add(filterItem.getIdentifier());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onSaveInstanceState(bundle);
        bundle.putInt("serviceId", serviceId);
        bundle.putString("searchString", searchString);
        bundle.putString("lastSearchedString", lastSearchedString);
        bundle.putString("searchSuggestion", searchSuggestion);
        bundle.putBoolean("isCorrectedSearch", isCorrectedSearch);
        bundle.putBoolean("channelSearchMode", channelSearchMode);
        bundle.putString("channelUrl", channelUrl);
        bundle.putString("channelName", channelName);
        bundle.putBoolean("wasSearchFocused", wasSearchFocused);
        bundle.putIntegerArrayList("userSelectedContentFilterList", userSelectedContentFilterList);
        bundle.putIntegerArrayList("userSelectedSortFilterList", userSelectedSortFilterList);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceId = savedInstanceState.getInt("serviceId", Constants.NO_SERVICE_ID);
        searchString = savedInstanceState.getString("searchString");
        lastSearchedString = savedInstanceState.getString("lastSearchedString");
        searchSuggestion = savedInstanceState.getString("searchSuggestion");
        isCorrectedSearch = savedInstanceState.getBoolean("isCorrectedSearch", false);
        channelSearchMode = savedInstanceState.getBoolean("channelSearchMode", false);
        channelUrl = savedInstanceState.getString("channelUrl");
        channelName = savedInstanceState.getString("channelName");
        wasSearchFocused = savedInstanceState.getBoolean("wasSearchFocused", false);
        userSelectedContentFilterList = savedInstanceState.getIntegerArrayList("userSelectedContentFilterList");
        userSelectedSortFilterList = savedInstanceState.getIntegerArrayList("userSelectedSortFilterList");
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init's
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void reloadContent() {

    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
                                    @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final ActionBar supportActionBar = activity.getSupportActionBar();
        if (centeredSearch) {
            if (activity instanceof org.schabi.newpipe.MainActivity) {
                ((org.schabi.newpipe.MainActivity) activity).setSearchChrome(true);
            }
            return;
        }
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (service == null) {
            Log.w(TAG, "onCreateOptionsMenu() called with null service");
            updateService();
        }

        if (useOldSearchFilter && !channelSearchMode && searchFilterUi != null
                && service != null) {
            searchFilterUi.createSearchUI(menu);
        }
    }

    public boolean usesCenteredSearch() {
        return centeredSearch;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (useOldSearchFilter && !channelSearchMode && searchFilterUi != null) {
            return searchFilterUi.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Search
    //////////////////////////////////////////////////////////////////////////*/

    private void showSearchOnStart() {
        if (DEBUG) {
            Log.d(TAG, "showSearchOnStart() called, searchQuery → "
                    + searchString
                    + ", lastSearchedQuery → "
                    + lastSearchedString);
        }
        searchEditText.setText(searchString);
        searchEditText.setHint(channelSearchMode
                ? getString(R.string.search_inside_channel, channelName)
                : getString(centeredSearch ? R.string.search_youtube : R.string.search));

        if (centeredSearch) {
            searchEditText.setText(searchString);
            return;
        }
        if (TextUtils.isEmpty(searchString) || TextUtils.isEmpty(searchEditText.getText())) {
            searchToolbarContainer.setTranslationX(100);
            searchToolbarContainer.setAlpha(0.0f);
            searchToolbarContainer.setVisibility(View.VISIBLE);
            searchToolbarContainer.animate()
                    .translationX(0)
                    .alpha(1.0f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator()).start();
        } else {
            searchToolbarContainer.setTranslationX(0);
            searchToolbarContainer.setAlpha(1.0f);
            searchToolbarContainer.setVisibility(View.VISIBLE);
        }

        updateToolbarActionViews();
    }

    private void updateToolbarActionViews() {
        final boolean hasQuery = !TextUtils.isEmpty(searchEditText.getText());
        searchClear.setVisibility(hasQuery ? View.VISIBLE : View.GONE);
        if (channelSearchMode) {
            searchFilter.setVisibility(View.GONE);
        }
        updateSearchEditTextMargin();
    }

    private void updateSearchEditTextMargin() {
        if (centeredSearch) {
            return;
        }
        final ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) searchEditText.getLayoutParams();
        final int rightMargin = Math.max(getSearchActionEnd(searchSubmit),
                Math.max(getSearchActionEnd(searchFilter), getSearchActionEnd(searchClear)));

        if (layoutParams.rightMargin != rightMargin) {
            layoutParams.rightMargin = rightMargin;
            searchEditText.setLayoutParams(layoutParams);
        }
    }

    private int getSearchActionEnd(final View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return 0;
        }

        final ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return layoutParams.rightMargin + layoutParams.width;
    }

    private void updateSearchActionLayout(final View view,
                                          final int widthDp,
                                          final int marginDp) {
        final ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        final int width = DeviceUtils.dpToPx(widthDp, activity);
        final int rightMargin = DeviceUtils.dpToPx(marginDp, activity);

        if (layoutParams.width != width || layoutParams.rightMargin != rightMargin) {
            layoutParams.width = width;
            layoutParams.rightMargin = rightMargin;
            view.setLayoutParams(layoutParams);
        }
    }

    private void initSearchListeners() {
        if (DEBUG) {
            Log.d(TAG, "initSearchListeners() called");
        }
        searchClear.setOnClickListener(v -> {
            if (DEBUG) {
                Log.d(TAG, "onClick() called with: v = [" + v + "]");
            }
            searchBinding.correctSuggestion.setVisibility(View.GONE);
            searchEditText.setText("");
            searchString = "";
            suggestionListAdapter.setItems(new ArrayList<>());
            if (centeredSearch) {
                applySearchChrome(false);
            }
            showKeyboardSearch();
        });

        TooltipCompat.setTooltipText(searchClear, getString(R.string.clear));
        if (!channelSearchMode) {
            TooltipCompat.setTooltipText(searchFilter, getString(R.string.filter));
        }
        if (searchSubmit != null) {
            TooltipCompat.setTooltipText(searchSubmit, getString(R.string.search));
            searchSubmit.setOnClickListener(v -> search());
        }

        if (!channelSearchMode) {
            searchFilter.setOnClickListener(v -> showFilterDialog());
        }

        searchEditText.setOnClickListener(v -> {
            if (DEBUG) {
                Log.d(TAG, "onClick() called with: v = [" + v + "]");
            }
            if ((showLocalSuggestions || showRemoteSuggestions) && !isErrorPanelVisible()) {
                showSuggestionsPanel();
            }
            if (DeviceUtils.isTv(getContext())) {
                showKeyboardSearch();
            }
        });

        searchEditText.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (DEBUG) {
                Log.d(TAG, "onFocusChange() called with: "
                        + "v = [" + v + "], hasFocus = [" + hasFocus + "]");
            }
            if ((showLocalSuggestions || showRemoteSuggestions)
                    && hasFocus && !isErrorPanelVisible()) {
                showSuggestionsPanel();
            }
        });

        suggestionListAdapter.setListener(new SuggestionListAdapter.OnSuggestionItemSelected() {
            @Override
            public void onSuggestionItemSelected(final SuggestionItem item) {
                search(item.query);
                searchEditText.setText(item.query);
            }

            @Override
            public void onSuggestionItemInserted(final SuggestionItem item) {
                searchEditText.setText(item.query);
                searchEditText.setSelection(searchEditText.getText().length());
            }

            @Override
            public void onSuggestionItemLongClick(final SuggestionItem item) {
                if (item.fromHistory) {
                    showDeleteSuggestionDialog(item);
                }
            }
        });

        if (textWatcher != null) {
            searchEditText.removeTextChangedListener(textWatcher);
        }
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start,
                                          final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                                      final int before, final int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                // Remove rich text formatting
                for (final CharacterStyle span : s.getSpans(0, s.length(), CharacterStyle.class)) {
                    s.removeSpan(span);
                }

                updateToolbarActionViews();

                final String newText = searchEditText.getText().toString();
                if (centeredSearch && !showingResults && searchBinding != null) {
                    updateHomeContentVisibility();
                }
                suggestionPublisher.onNext(newText);
            }
        };
        searchEditText.addTextChangedListener(textWatcher);
        searchEditText.setOnEditorActionListener(
                (TextView v, int actionId, KeyEvent event) -> {
                    if (DEBUG) {
                        Log.d(TAG, "onEditorAction() called with: v = [" + v + "], "
                                + "actionId = [" + actionId + "], event = [" + event + "]");
                    }
                    if (actionId == EditorInfo.IME_ACTION_PREVIOUS) {
                        hideKeyboardSearch();
                        return true;
                    }
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_UP)) {
                        search(searchEditText.getText().toString().trim());
                        return true;
                    }
                    return false;
                });

        if (suggestionDisposable == null || suggestionDisposable.isDisposed()) {
            initSuggestionObserver();
        }
    }

    private void unsetSearchListeners() {
        if (DEBUG) {
            Log.d(TAG, "unsetSearchListeners() called");
        }
        searchClear.setOnClickListener(null);
        searchClear.setOnLongClickListener(null);
        searchFilter.setOnClickListener(null);
        if (searchSubmit != null) {
            searchSubmit.setOnClickListener(null);
        }
        searchEditText.setOnClickListener(null);
        searchEditText.setOnFocusChangeListener(null);
        searchEditText.setOnEditorActionListener(null);

        if (textWatcher != null) {
            searchEditText.removeTextChangedListener(textWatcher);
        }
        textWatcher = null;
    }

    private void showSuggestionsPanel() {
        if (DEBUG) {
            Log.d(TAG, "showSuggestionsPanel() called");
        }
        if (centeredSearch && searchBinding != null) {
            if (showingResults) {
                searchBinding.resultsContainer.setVisibility(View.GONE);
                searchBinding.homeSpacer.setVisibility(View.GONE);
                searchBinding.homeSuggestionsList.setVisibility(View.VISIBLE);
                suggestionsPanelVisible = true;
            } else {
                applySearchChrome(false);
            }
            return;
        }
        suggestionsPanelVisible = true;
        animate(searchBinding.suggestionsPanel, true, 200,
                AnimationType.LIGHT_SLIDE_AND_ALPHA);
    }

    private void hideSuggestionsPanel() {
        if (DEBUG) {
            Log.d(TAG, "hideSuggestionsPanel() called");
        }
        if (centeredSearch && searchBinding != null) {
            if (showingResults) {
                applySearchChrome(true);
            } else {
                searchBinding.homeSuggestionsList.setVisibility(View.GONE);
                suggestionsPanelVisible = false;
            }
            return;
        }
        suggestionsPanelVisible = false;
        animate(searchBinding.suggestionsPanel, false, 200,
                AnimationType.LIGHT_SLIDE_AND_ALPHA);
    }

    private void applySearchChrome(final boolean results) {
        if (!centeredSearch || searchBinding == null) {
            return;
        }
        showingResults = results;
        searchBinding.homeHeader.setVisibility(results ? View.GONE : View.VISIBLE);
        searchBinding.homeViewToggle.setVisibility(results ? View.VISIBLE : View.GONE);
        updateViewToggleIcon();
        final ViewGroup.MarginLayoutParams clearLp =
                (ViewGroup.MarginLayoutParams) searchBinding.homeSearchClear.getLayoutParams();
        final ViewGroup.MarginLayoutParams fieldLp =
                (ViewGroup.MarginLayoutParams) searchBinding.homeSearchEditText.getLayoutParams();
        final float density = searchBinding.getRoot().getResources().getDisplayMetrics().density;
        clearLp.setMarginEnd((int) ((results ? 48 : 0) * density));
        fieldLp.setMarginEnd((int) ((results ? 96 : 48) * density));
        searchBinding.homeSearchClear.setLayoutParams(clearLp);
        searchBinding.homeSearchEditText.setLayoutParams(fieldLp);
        updateHomeContentVisibility();
        searchBinding.resultsContainer.setVisibility(results ? View.VISIBLE : View.GONE);
        searchBinding.homeSearchLeading.setImageResource(
                results ? R.drawable.ic_arrow_back : R.drawable.ic_search);
        searchBinding.homeSearchLeading.setContentDescription(
                getString(results ? R.string.back : R.string.search));
        if (activity instanceof org.schabi.newpipe.MainActivity) {
            ((org.schabi.newpipe.MainActivity) activity).setSearchChrome(true);
        }
        refreshNowPlaying();
    }

    private void updateHomeContentVisibility() {
        if (!centeredSearch || searchBinding == null || searchEditText == null) {
            return;
        }
        final boolean hasQuery = !TextUtils.isEmpty(searchEditText.getText().toString().trim());
        searchBinding.homeSuggestionsList.setVisibility(
                !showingResults && hasQuery ? View.VISIBLE : View.GONE);
        searchBinding.homeSpacer.setVisibility(
                !showingResults && !hasQuery ? View.VISIBLE : View.GONE);
        suggestionsPanelVisible = !showingResults && hasQuery;
    }

    private void returnToHome() {
        searchString = "";
        lastSearchedString = "";
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        if (infoListAdapter != null) {
            infoListAdapter.clearStreamItemList();
        }
        applySearchChrome(false);
        suggestionPublisher.onNext("");
        hideKeyboardSearch();
    }

    private void updateProfileLabel() {
        if (searchBinding == null || activity == null) {
            return;
        }
        final ProfileStore.Profile active = ProfileStore.getActive(activity);
        searchBinding.profileName.setText(active.name);
        searchBinding.profileButton.setContentDescription(active.name);
        searchBinding.profileMonogram.setText(monogram(active.name));
        showProfilePhoto(searchBinding.profilePhoto, searchBinding.profileMonogram, active.id);
    }

    private static String monogram(final String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return new String(Character.toChars(name.codePointAt(0))).toUpperCase(Locale.getDefault());
    }

    private void showProfileDialog() {
        if (activity == null) {
            return;
        }
        final android.app.Dialog dialog = new android.app.Dialog(activity);
        final View sheet = LayoutInflater.from(activity).inflate(R.layout.sheet_profiles, null);
        dialog.setContentView(sheet);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(
                    android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
            dialog.getWindow().setLayout(
                    (int) (320 * activity.getResources().getDisplayMetrics().density),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        sheet.setBackgroundResource(R.drawable.bg_profile_dialog);
        final android.widget.GridLayout list = sheet.findViewById(R.id.profile_sheet_list);
        final View createRow = sheet.findViewById(R.id.profile_sheet_create);
        final EditText nameInput = sheet.findViewById(R.id.profile_sheet_name);
        final String activeId = ProfileStore.getActive(activity).id;
        for (final ProfileStore.Profile profile : ProfileStore.getProfiles(activity)) {
            final View cell = LayoutInflater.from(activity)
                    .inflate(R.layout.item_profile_cell, list, false);
            final TextView letter = cell.findViewById(R.id.profile_cell_letter);
            final ImageView photo = cell.findViewById(R.id.profile_cell_photo);
            final TextView label = cell.findViewById(R.id.profile_cell_name);
            letter.setText(monogram(profile.name));
            label.setText(profile.name);
            showProfilePhoto(photo, letter, profile.id);
            if (profile.id.equals(activeId)) {
                letter.setBackgroundResource(R.drawable.bg_profile_monogram);
                cell.setBackgroundResource(R.drawable.bg_profile_active);
            }
            cell.setOnClickListener(v -> {
                dialog.dismiss();
                if (!profile.id.equals(activeId) && activity != null) {
                    ProfileStore.switchTo(activity, profile.id);
                    activity.recreate();
                }
            });
            cell.setOnLongClickListener(v -> {
                photoProfileId = profile.id;
                profilePhotoPicker.launch("image/*");
                dialog.dismiss();
                return true;
            });
            list.addView(cell);
        }
        sheet.findViewById(R.id.profile_sheet_new).setOnClickListener(v -> {
            createRow.setVisibility(View.VISIBLE);
            nameInput.requestFocus();
            KeyboardUtil.showKeyboard(activity, nameInput);
        });
        sheet.findViewById(R.id.profile_sheet_create_button).setOnClickListener(v -> {
            final String name = nameInput.getText().toString().trim();
            if (name.isEmpty() || activity == null) {
                return;
            }
            try {
                final ProfileStore.Profile created = ProfileStore.create(activity, name);
                dialog.dismiss();
                ProfileStore.switchTo(activity, created.id);
                activity.recreate();
            } catch (final IllegalArgumentException ignored) {
                Toast.makeText(activity, R.string.profile_name_taken, Toast.LENGTH_SHORT).show();
            }
        });
        final View deleteButton = sheet.findViewById(R.id.profile_sheet_delete);
        deleteButton.setVisibility(ProfileStore.canDelete(activity) ? View.VISIBLE : View.GONE);
        deleteButton.setOnClickListener(v -> {
            dialog.dismiss();
            confirmDeleteProfile();
        });
        dialog.show();
    }

    private void onProfilePhotoPicked(final android.net.Uri uri) {
        if (uri == null || activity == null || photoProfileId == null) {
            return;
        }
        try (java.io.InputStream in = activity.getContentResolver().openInputStream(uri)) {
            if (in != null) {
                ProfileStore.savePhoto(activity, photoProfileId, in);
            }
        } catch (final java.io.IOException ignored) {
            return;
        }
        updateProfileLabel();
    }

    private void showProfilePhoto(final ImageView photo, final TextView letter, final String id) {
        if (activity == null) {
            return;
        }
        final java.io.File file = ProfileStore.photoFile(activity, id);
        photo.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(final View view, final android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        photo.setClipToOutline(true);
        if (file.exists()) {
            photo.setImageBitmap(android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath()));
            photo.setVisibility(View.VISIBLE);
            letter.setVisibility(View.INVISIBLE);
        } else {
            photo.setVisibility(View.GONE);
            letter.setVisibility(View.VISIBLE);
        }
    }

    private void styleProfileSheet(final BottomSheetDialog dialog) {
        final View sheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null || activity == null) {
            return;
        }
        final android.util.TypedValue value = new android.util.TypedValue();
        activity.getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorSurfaceContainerLow, value, true);
        final float radius = 28f * activity.getResources().getDisplayMetrics().density;
        final MaterialShapeDrawable background = new MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                        .setTopLeftCornerSize(radius)
                        .setTopRightCornerSize(radius)
                        .build());
        background.setFillColor(ColorStateList.valueOf(value.data));
        sheet.setBackground(background);
    }


    private void refreshNowPlaying() {
        if (!centeredSearch || searchBinding == null) {
            return;
        }
        final PlayerHolder holder = PlayerHolder.getInstance();
        if (!holder.isBackgroundAudio() || isMiniPlayerShowing()) {
            searchBinding.nowPlayingBar.setVisibility(View.GONE);
            searchBinding.homeColumn.setPadding(0, 0, 0, 0);
            return;
        }
        searchBinding.nowPlayingBar.setVisibility(View.VISIBLE);
        if (activity != null) {
            final int pad = (int) (72 * activity.getResources().getDisplayMetrics().density);
            searchBinding.homeColumn.setPadding(0, 0, 0, pad);
        }
        final PlayQueue queue = holder.getPlayQueue();
        final PlayQueueItem item = queue == null ? null : queue.getItem();
        if (item != null) {
            searchBinding.nowPlayingTitle.setText(item.getTitle());
            if (item.getThumbnailUrl() != null) {
                PicassoHelper.loadThumbnail(item.getThumbnailUrl())
                        .into(searchBinding.nowPlayingThumb);
            }
        } else {
            searchBinding.nowPlayingTitle.setText(R.string.unknown_content);
        }
        searchBinding.nowPlayingToggle.setImageResource(
                holder.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
    }

    private boolean isMiniPlayerShowing() {
        if (activity == null) {
            return false;
        }
        final View holder = activity.findViewById(R.id.fragment_player_holder);
        if (holder == null) {
            return false;
        }
        try {
            return com.google.android.material.bottomsheet.BottomSheetBehavior.from(holder)
                    .getState() != com.google.android.material.bottomsheet.BottomSheetBehavior
                    .STATE_HIDDEN;
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }

    private void openNowPlaying() {
        if (activity == null) {
            return;
        }
        final PlayQueue queue = PlayerHolder.getInstance().getPlayQueue();
        final PlayQueueItem item = queue == null ? null : queue.getItem();
        if (item == null) {
            return;
        }
        NavigationHelper.openVideoDetailFragment(activity, activity.getSupportFragmentManager(),
                item.getServiceId(), item.getUrl(), item.getTitle(), queue, true);
    }


    private void showHomeMenu(final View anchor) {
        if (activity == null) {
            return;
        }
        final boolean incognito = HistoryRecordManager.isIncognito(activity);
        final java.util.ArrayList<org.schabi.newpipe.util.HushActionSheet.Action> actions =
                new java.util.ArrayList<>();
        actions.add(new org.schabi.newpipe.util.HushActionSheet.Action(
                R.drawable.ic_history, getString(R.string.action_history), null, false,
                () -> openLibrary(new org.schabi.newpipe.local.library.HistoryLibraryFragment())));
        actions.add(new org.schabi.newpipe.util.HushActionSheet.Action(
                R.drawable.ic_file_download, getString(R.string.downloads), null, false,
                () -> NavigationHelper.openDownloads(activity)));
        actions.add(new org.schabi.newpipe.util.HushActionSheet.Action(
                R.drawable.ic_bookmark, getString(R.string.library_saved), null, false,
                () -> openLibrary(new org.schabi.newpipe.local.library.SavedLibraryFragment())));
        actions.add(new org.schabi.newpipe.util.HushActionSheet.Action(
                R.drawable.ic_settings, getString(R.string.settings), null, false,
                () -> NavigationHelper.openSettings(activity)));
        actions.add(new org.schabi.newpipe.util.HushActionSheet.Action(
                R.drawable.ic_info_outline, getString(R.string.tab_about), null, false,
                () -> NavigationHelper.openAbout(activity)));
        actions.add(new org.schabi.newpipe.util.HushActionSheet.Action(
                incognito ? R.drawable.ic_visibility_off : R.drawable.ic_visibility_on,
                getString(R.string.history_incognito),
                getString(incognito ? R.string.home_incognito_on : R.string.home_incognito_off),
                incognito, () -> toggleIncognito()));
        org.schabi.newpipe.util.HushActionSheet.show(activity, getString(R.string.more_options),
                null, actions);
    }

    private void openBreakSheet(final boolean meditation) {
        if (activity == null) {
            return;
        }
        BreakSheet.newInstance(meditation).show(getParentFragmentManager(), "hush_break");
    }

    private void openGame(@Nullable final String game) {
        if (activity == null) {
            return;
        }
        openLibrary(GamesFragment.newInstance(game));
    }

    private void updateIncognitoHint() {
        if (centeredSearch && searchEditText != null && activity != null) {
            final boolean incognito = HistoryRecordManager.isIncognito(activity);
            searchEditText.setHint(incognito
                    ? R.string.history_incognito_search_hint : R.string.hush_search_videos);
        }
    }

    private void toggleIncognito() {
        if (activity == null) {
            return;
        }
        final boolean enabled = !HistoryRecordManager.isIncognito(activity);
        if (!enabled) {
            GameStateStore.clearIncognito();
        }
        PreferenceManager.getDefaultSharedPreferences(activity).edit()
                .putBoolean(HistoryRecordManager.INCOGNITO_KEY, enabled).apply();
        updateIncognitoHint();
    }

    private void openLibrary(final androidx.fragment.app.Fragment fragment) {
        if (activity == null) {
            return;
        }
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void submitSearch(final String query) {
        search(query);
    }

    private void toggleResultsLayout() {
        if (activity == null) {
            return;
        }
        final android.content.SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(activity);
        final String key = getString(R.string.grid_layout_enabled_key);
        prefs.edit().putBoolean(key, !prefs.getBoolean(key, true)).apply();
        refreshItemViewMode();
        updateViewToggleIcon();
    }

    private void updateViewToggleIcon() {
        if (searchBinding == null || activity == null) {
            return;
        }
        final boolean grid = PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(getString(R.string.grid_layout_enabled_key), true);
        searchBinding.homeViewToggle.setImageResource(
                grid ? R.drawable.ic_view_list : R.drawable.ic_grid_view);
    }

    @Override
    protected void showInfoItemDialog(final org.schabi.newpipe.extractor.stream.StreamInfoItem item) {
        try {
            new org.schabi.newpipe.info_list.dialog.InfoItemDialog.Builder(
                    activity, activity, this, item, false)
                    .addAllEntries(
                            org.schabi.newpipe.info_list.dialog.StreamDialogDefaultEntry
                                    .START_HERE_ON_BACKGROUND,
                            org.schabi.newpipe.info_list.dialog.StreamDialogDefaultEntry
                                    .APPEND_PLAYLIST,
                            org.schabi.newpipe.info_list.dialog.StreamDialogDefaultEntry.DOWNLOAD,
                            org.schabi.newpipe.info_list.dialog.StreamDialogDefaultEntry.SHARE)
                    .create()
                    .show();
        } catch (final IllegalArgumentException e) {
            org.schabi.newpipe.info_list.dialog.InfoItemDialog.Builder
                    .reportErrorDuringInitialization(e, item);
        }
    }

    private void confirmDeleteProfile() {
        if (activity == null || !ProfileStore.canDelete(activity)) {
            return;
        }
        final ProfileStore.Profile active = ProfileStore.getActive(activity);
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.profile_delete_title)
                .setMessage(getString(R.string.profile_delete_message, active.name))
                .setPositiveButton(R.string.profile_delete, (dialog, which) -> {
                    ProfileStore.deleteActive(activity);
                    activity.recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showKeyboardSearch() {
        if (DEBUG) {
            Log.d(TAG, "showKeyboardSearch() called");
        }
        KeyboardUtil.showKeyboard(activity, searchEditText);
    }

    private void hideKeyboardSearch() {
        if (DEBUG) {
            Log.d(TAG, "hideKeyboardSearch() called");
        }

        KeyboardUtil.hideKeyboard(activity, searchEditText);
    }

    private void showDeleteSuggestionDialog(final SuggestionItem item) {
        if (activity == null || historyRecordManager == null || searchEditText == null) {
            return;
        }
        final String query = item.query;
        new MaterialAlertDialogBuilder(activity)
                .setTitle(query)
                .setMessage(R.string.delete_item_search_history)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    final Disposable onDelete = historyRecordManager.deleteSearchHistory(query)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    howManyDeleted -> suggestionPublisher
                                            .onNext(searchEditText.getText().toString()),
                                    throwable -> showSnackBarError(new ErrorInfo(throwable,
                                            UserAction.DELETE_FROM_HISTORY,
                                            "Deleting item failed")));
                    disposables.add(onDelete);
                })
                .show();
    }

    @Override
    public boolean onBackPressed() {
        if (suggestionsPanelVisible
                && !TextUtils.isEmpty(searchString)
                && !infoListAdapter.getItemsList().isEmpty()
                && !isLoading.get()) {
            hideSuggestionsPanel();
            hideKeyboardSearch();
            searchEditText.setText(lastSearchedString);
            return true;
        }
        if (centeredSearch && !TextUtils.isEmpty(searchString)) {
            returnToHome();
            return true;
        }
        return false;
    }


    private Observable<List<SuggestionItem>> getLocalSuggestionsObservable(
            final String query, final int similarQueryLimit) {
        final int suggestionsCount = NewPipeSettings.getSearchSuggestionsCount(
                requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        return historyRecordManager
                .getRelatedSearches(query, similarQueryLimit, suggestionsCount)
                .toObservable()
                .map(searchHistoryEntries ->
                    searchHistoryEntries.stream()
                            .map(entry -> new SuggestionItem(true, entry))
                            .collect(Collectors.toList()));
    }

    private Observable<List<SuggestionItem>> getRemoteSuggestionsObservable(final String query) {
        return ExtractorHelper
                .suggestionsFor(serviceId, query)
                .toObservable()
                .map(strings -> {
                    final List<SuggestionItem> result = new ArrayList<>();
                    for (final String entry : strings) {
                        result.add(new SuggestionItem(false, entry));
                    }
                    return result;
                });
    }

    private void initSuggestionObserver() {
        if (DEBUG) {
            Log.d(TAG, "initSuggestionObserver() called");
        }
        if (suggestionDisposable != null) {
            suggestionDisposable.dispose();
        }

        suggestionDisposable = suggestionPublisher
                .debounce(SUGGESTIONS_DEBOUNCE, TimeUnit.MILLISECONDS)
                .startWithItem(searchString == null ? "" : searchString)
                .distinctUntilChanged()
                .switchMap(query -> {
                    // Only show remote suggestions if they are enabled in settings and
                    // the query length is at least THRESHOLD_NETWORK_SUGGESTION
                    final boolean shallShowRemoteSuggestionsNow = !channelSearchMode
                            && showRemoteSuggestions
                            && query.length() >= THRESHOLD_NETWORK_SUGGESTION;

                    final Observable<List<SuggestionItem>> source;
                    if (showLocalSuggestions && shallShowRemoteSuggestionsNow) {
                        source = Observable.combineLatest(
                                getLocalSuggestionsObservable(query, 3)
                                        .subscribeOn(Schedulers.io())
                                        .onErrorReturnItem(Collections.emptyList())
                                        .startWithItem(Collections.emptyList()),
                                getRemoteSuggestionsObservable(query)
                                        .onErrorReturnItem(Collections.emptyList())
                                        .startWithItem(Collections.emptyList()),
                                (local, remote) -> {
                                    final List<SuggestionItem> merged = new ArrayList<>(local);
                                    for (final SuggestionItem item : remote) {
                                        if (!merged.contains(item)) {
                                            merged.add(item);
                                        }
                                    }
                                    return merged;
                                });
                    } else if (showLocalSuggestions) {
                        source = getLocalSuggestionsObservable(query, 25)
                                .subscribeOn(Schedulers.io())
                                .onErrorReturnItem(Collections.emptyList());
                    } else if (shallShowRemoteSuggestionsNow) {
                        source = getRemoteSuggestionsObservable(query)
                                .onErrorReturnItem(Collections.emptyList());
                    } else {
                        source = Observable.just(Collections.emptyList());
                    }
                    return source.map(items -> {
                        if (!items.isEmpty() || query.trim().isEmpty()
                                || (!showLocalSuggestions && !shallShowRemoteSuggestionsNow)) {
                            return items;
                        }
                        return Collections.singletonList(new SuggestionItem(false, query));
                    }).materialize();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listNotification -> {
                            if (listNotification.isOnNext()) {
                                if (listNotification.getValue() != null) {
                                    handleSuggestions(listNotification.getValue());
                                }
                            } else if (listNotification.isOnError()
                                    && listNotification.getError() != null
                                    && !ExceptionUtils.isInterruptedCaused(
                                            listNotification.getError())) {
                                showSnackBarError(new ErrorInfo(listNotification.getError(),
                                        UserAction.GET_SUGGESTIONS, searchString, serviceId));
                            }
                        }, throwable -> showSnackBarError(new ErrorInfo(
                            throwable, UserAction.GET_SUGGESTIONS, searchString, serviceId)));
    }

    @Override
    protected void doInitialLoadLogic() {
        // no-op
    }

    public void search() {
        search(searchEditText.getText().toString());
    }

    private void search(final String theSearchString) {
        if (DEBUG) {
            Log.d(TAG, "search() called with: query = [" + theSearchString + "]");
        }
        if (theSearchString.isEmpty()) {
            return;
        }
        if (centeredSearch && searchEditText != null
                && !theSearchString.contentEquals(searchEditText.getText())) {
            searchEditText.setText(theSearchString);
            searchEditText.setSelection(theSearchString.length());
        }

        if (!channelSearchMode) {
            try {
                final StreamingService streamingService = NewPipe.getServiceByUrl(theSearchString);
                showLoading();
                disposables.add(Observable
                        .fromCallable(() -> NavigationHelper.getIntentByLink(activity,
                                streamingService, theSearchString))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(intent -> {
                            if (!centeredSearch) {
                                getFM().popBackStackImmediate();
                            }
                            activity.startActivity(intent);
                        }, throwable -> showTextError(getString(R.string.unsupported_url))));
                return;
            } catch (final Exception ignored) {
                // Exception occurred, it's not a url
            }
        }

        lastSearchedString = this.searchString;
        this.searchString = theSearchString;
        infoListAdapter.clearStreamItemList();
        hideSuggestionsPanel();
        showMetaInfoInTextView(null, searchBinding.searchMetaInfoTextView,
                searchBinding.searchMetaInfoSeparator, disposables);
        hideKeyboardSearch();
        if (centeredSearch) {
            applySearchChrome(true);
            App.prewarmYoutubeDecoder();
        }
        // Fire-and-forget: deliberately not added to `disposables`, which is cleared by
        // startLoading(), onPause() and onDestroy() and would cancel the history write
        // before it even reached the IO thread.
        historyRecordManager.onSearched(serviceId, theSearchString)
                .doOnError(throwable -> Log.e(TAG,
                        "Failed to save search history for \"" + theSearchString + "\"", throwable))
                .onErrorComplete()
                .subscribe();
        suggestionPublisher.onNext(theSearchString);
        startLoading(false);
    }

    @Override
    public void startLoading(final boolean forceLoad) {
        try{
            super.startLoading(forceLoad);
            disposables.clear();
            if (searchDisposable != null) {
                searchDisposable.dispose();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (channelSearchMode) {
            try {
                channelSearchHandler = getChannelSearchHandler();
            } catch (final Exception e) {
                onItemError(e);
                return;
            }
            searchDisposable = ExtractorHelper.getChannelTab(serviceId, channelSearchHandler, true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnEvent((channelTabInfo, throwable) -> isLoading.set(false))
                    .subscribe(this::handleChannelSearchResult, this::onItemError);
            return;
        }
        searchDisposable = ExtractorHelper.searchFor(serviceId,
                searchString,
                selectedContentFilter,
                selectedSortFilter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((searchResult, throwable) -> isLoading.set(false))
                .subscribe(this::handleResult, this::onItemError);
    }

    @Override
    protected void loadMoreItems() {
        if (!Page.isValid(nextPage)) {
            return;
        }
        isLoading.set(true);
        showListFooter(true);
        if (searchDisposable != null) {
            searchDisposable.dispose();
        }
        if (channelSearchMode) {
            searchDisposable = ExtractorHelper.getMoreChannelTabItems(
                    serviceId,
                    channelSearchHandler,
                    nextPage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnEvent((nextItemsResult, throwable) -> isLoading.set(false))
                    .subscribe(this::handleNextItems, this::onItemError);
            return;
        }
        searchDisposable = ExtractorHelper.getMoreSearchItems(
                serviceId,
                searchString,
                selectedContentFilter,
                selectedSortFilter,
                nextPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((nextItemsResult, throwable) -> isLoading.set(false))
                .subscribe(this::handleNextItems, this::onItemError);
    }

    @Override
    protected boolean hasMoreItems() {
        return Page.isValid(nextPage);
    }

    @Override
    protected void onItemSelected(final InfoItem selectedItem) {
        super.onItemSelected(selectedItem);
        hideKeyboardSearch();
    }

    private void onItemError(final Throwable exception) {
        if (exception instanceof SearchExtractor.NothingFoundException) {
            infoListAdapter.clearStreamItemList();
            showEmptyState();
        } else {
            showError(new ErrorInfo(exception, UserAction.SEARCHED, searchString, serviceId));
        }
    }

    private ListLinkHandler getChannelSearchHandler() throws Exception {
        final StreamingService streamingService = NewPipe.getService(serviceId);
        final String channelId = streamingService.getChannelLHFactory()
                .fromUrl(channelUrl)
                .getId();
        final List<FilterItem> contentFilters = Collections.singletonList(
                new FilterItem(Filter.ITEM_IDENTIFIER_UNKNOWN, ChannelTabs.SEARCH));
        final ListLinkHandler searchHandler = streamingService.getChannelTabLHFactory()
                .fromQuery(channelId, contentFilters, null);
        final String searchUrl = searchHandler.getUrl()
                + "?query=" + URLEncoder.encode(searchString, "UTF-8").replace("+", "%20");
        return new ListLinkHandler(searchUrl, searchUrl, searchHandler.getId(),
                searchHandler.getContentFilters(), searchHandler.getSortFilter());
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void selectedFilters(final List<FilterItem> theSelectedContentFilter,
                                final List<FilterItem> theSelectedSortFilter,
                                final boolean isInit) {

        selectedContentFilter = (ArrayList<FilterItem>) theSelectedContentFilter;
        selectedSortFilter = (ArrayList<FilterItem>) theSelectedSortFilter;
        if (!isInit) {
            savePersistedSearchFilters(getCurrentSearchFilterUiServiceId(),
                    getFilterIds(selectedContentFilter), getFilterIds(selectedSortFilter));
        }
    }

    @Override
    public void onSearchFilterSelected(final int selectedServiceId,
                                       final int contentFilterId,
                                       final ArrayList<Integer> sortFilterIds) {
        final String currentQuery = searchEditText == null
                ? searchString
                : searchEditText.getText().toString();
        final int resolvedServiceId = selectedServiceId == SearchFilterDialog.YOUTUBE_MUSIC_SERVICE_ID
                ? ServiceList.YouTube.getServiceId()
                : selectedServiceId;
        final boolean serviceChanged = serviceId != resolvedServiceId;
        serviceId = resolvedServiceId;
        updateService();

        userSelectedContentFilterList = new ArrayList<>();
        userSelectedContentFilterList.add(contentFilterId);
        userSelectedSortFilterList = sortFilterIds;
        savePersistedSearchFilters(selectedServiceId,
                userSelectedContentFilterList, userSelectedSortFilterList);
        restoreSelectedFilters();

        if (serviceChanged || !TextUtils.isEmpty(currentQuery)) {
            search(currentQuery);
        }
    }

    private void setQuery(final int theServiceId,
                          final String theSearchString,
                          final List<FilterItem> theContentFilter,
                          final List<FilterItem> theSortFilter) {
        serviceId = theServiceId;
        searchString = theSearchString;
        // TODO evermind-zz casting better assert before
        selectedContentFilter = (ArrayList<FilterItem>) theContentFilter;
        selectedSortFilter = (ArrayList<FilterItem>) theSortFilter;
    }

    private void restoreSelectedFilters() {
        if (service == null) {
            return;
        }

        if (userSelectedContentFilterList == null || userSelectedContentFilterList.isEmpty()) {
            userSelectedContentFilterList = new ArrayList<>();
            final FilterItem defaultFilter = service.getSearchQHFactory().getFilterItem(0);
            if (defaultFilter != null) {
                userSelectedContentFilterList.add(defaultFilter.getIdentifier());
            }
        }

        if (userSelectedSortFilterList == null) {
            userSelectedSortFilterList = new ArrayList<>();
        }

        final ArrayList<FilterItem> restoredContentFilters = new ArrayList<>();
        for (final Integer filterId : userSelectedContentFilterList) {
            final FilterItem filterItem = service.getSearchQHFactory().getFilterItem(filterId);
            if (filterItem != null) {
                restoredContentFilters.add(filterItem);
            }
        }

        if (restoredContentFilters.isEmpty()) {
            final FilterItem defaultFilter = service.getSearchQHFactory().getFilterItem(0);
            if (defaultFilter != null) {
                restoredContentFilters.add(defaultFilter);
                userSelectedContentFilterList.clear();
                userSelectedContentFilterList.add(defaultFilter.getIdentifier());
            }
        }

        final ArrayList<FilterItem> restoredSortFilters = new ArrayList<>();
        for (final Integer filterId : userSelectedSortFilterList) {
            final FilterItem filterItem = service.getSearchQHFactory().getFilterItem(filterId);
            if (filterItem != null) {
                restoredSortFilters.add(filterItem);
            }
        }

        selectedContentFilter = restoredContentFilters;
        selectedSortFilter = restoredSortFilters;
        forceVideoContentFilter();
    }

    private void forceVideoContentFilter() {
        if (channelSearchMode || service == null) {
            return;
        }
        final Filter contentFilter = service.getSearchQHFactory().getAvailableContentFilter();
        if (contentFilter == null || contentFilter.getFilterGroups() == null) {
            return;
        }
        for (final FilterGroup group : contentFilter.getFilterGroups()) {
            if (group.filterItems == null) {
                continue;
            }
            for (final FilterItem item : group.filterItems) {
                if (item != null && "videos".equals(item.getName())) {
                    selectedContentFilter = new ArrayList<>();
                    selectedContentFilter.add(item);
                    userSelectedContentFilterList = new ArrayList<>();
                    userSelectedContentFilterList.add(item.getIdentifier());
                    return;
                }
            }
        }
    }

    private void restorePersistedSearchFilters() {
        if (channelSearchMode || activity == null) {
            return;
        }

        restorePersistedSearchFilters(PreferenceManager.getDefaultSharedPreferences(activity));
    }

    private void restorePersistedSearchFilters(final SharedPreferences prefs) {
        if (!shouldRememberSearchFilters(prefs)
                || userSelectedContentFilterList != null || userSelectedSortFilterList != null) {
            return;
        }

        final int filterServiceId = getPersistedSearchFilterUiServiceId(prefs);
        final String contentKey = getSearchFilterContentKey(filterServiceId);
        if (!prefs.contains(contentKey)) {
            return;
        }

        userSelectedContentFilterList = new ArrayList<>();
        userSelectedContentFilterList.add(prefs.getInt(contentKey, 0));
        userSelectedSortFilterList = deserializeFilterIds(
                prefs.getString(getSearchFilterSortKey(filterServiceId), ""));
    }

    private int getPersistedSearchFilterUiServiceId(final SharedPreferences prefs) {
        int filterServiceId = prefs.getInt(
                SEARCH_FILTER_LAST_UI_SERVICE_KEY_PREFIX + serviceId, serviceId);
        if (serviceId != ServiceList.YouTube.getServiceId()
                && filterServiceId == SearchFilterDialog.YOUTUBE_MUSIC_SERVICE_ID) {
            filterServiceId = serviceId;
        }
        return filterServiceId;
    }

    private void savePersistedSearchFilters(final int filterServiceId,
                                            final ArrayList<Integer> contentFilterIds,
                                            final ArrayList<Integer> sortFilterIds) {
        if (channelSearchMode || activity == null || contentFilterIds == null
                || contentFilterIds.isEmpty()) {
            return;
        }

        final int resolvedServiceId = filterServiceId == SearchFilterDialog.YOUTUBE_MUSIC_SERVICE_ID
                ? ServiceList.YouTube.getServiceId()
                : filterServiceId;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (!shouldRememberSearchFilters(prefs)) {
            return;
        }
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SEARCH_FILTER_LAST_SERVICE_KEY, resolvedServiceId);
        editor.putInt(SEARCH_FILTER_LAST_UI_SERVICE_KEY_PREFIX + resolvedServiceId,
                filterServiceId);
        editor.putInt(getSearchFilterContentKey(filterServiceId), contentFilterIds.get(0));
        editor.putString(getSearchFilterSortKey(filterServiceId), serializeFilterIds(sortFilterIds));
        editor.apply();
    }

    private int getCurrentSearchFilterUiServiceId() {
        return shouldUseYoutubeMusicUiService()
                ? SearchFilterDialog.YOUTUBE_MUSIC_SERVICE_ID
                : serviceId;
    }

    private static String getSearchFilterContentKey(final int filterServiceId) {
        return SEARCH_FILTER_CONTENT_KEY_PREFIX + filterServiceId;
    }

    private static String getSearchFilterSortKey(final int filterServiceId) {
        return SEARCH_FILTER_SORT_KEY_PREFIX + filterServiceId;
    }

    private static boolean shouldRememberSearchFilters(final SharedPreferences prefs) {
        return prefs.getBoolean(
                App.getApp().getString(R.string.remember_search_filters_key), false);
    }

    private ArrayList<Integer> getFilterIds(final List<FilterItem> filterItems) {
        final ArrayList<Integer> filterIds = new ArrayList<>();
        if (filterItems != null) {
            for (final FilterItem filterItem : filterItems) {
                filterIds.add(filterItem.getIdentifier());
            }
        }
        return filterIds;
    }

    private String serializeFilterIds(final List<Integer> filterIds) {
        if (filterIds == null || filterIds.isEmpty()) {
            return "";
        }
        return filterIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static ArrayList<Integer> deserializeFilterIds(final String serializedFilterIds) {
        final ArrayList<Integer> filterIds = new ArrayList<>();
        if (TextUtils.isEmpty(serializedFilterIds)) {
            return filterIds;
        }

        for (final String filterId : serializedFilterIds.split(",")) {
            try {
                filterIds.add(Integer.parseInt(filterId));
            } catch (final NumberFormatException ignored) {
            }
        }
        return filterIds;
    }

    private void showFilterDialog() {
        if (useOldSearchFilter) {
            return;
        }

        if (getChildFragmentManager().findFragmentByTag(SearchFilterDialog.TAG) != null) {
            return;
        }

        final String currentQuery = searchEditText == null
                ? (searchString == null ? "" : searchString)
                : searchEditText.getText().toString();

        final ArrayList<Integer> serviceIds = new ArrayList<>();
        final List<StreamingService> sortedServices = new ArrayList<>(NewPipe.getServices());
        sortedServices.sort(Comparator
                .comparingInt((StreamingService streamingService) -> {
                    if (streamingService.getServiceId() == ServiceList.YouTube.getServiceId()) {
                        return 0;
                    }
                    return ServiceHelper.isBeta(streamingService) ? 3 : 2;
                })
                .thenComparing(streamingService -> streamingService.getServiceInfo().getName()));
        for (final StreamingService streamingService : sortedServices) {
            serviceIds.add(streamingService.getServiceId());
            if (streamingService.getServiceId() == ServiceList.YouTube.getServiceId()) {
                serviceIds.add(SearchFilterDialog.YOUTUBE_MUSIC_SERVICE_ID);
            }
        }

        final int dialogServiceId = shouldUseYoutubeMusicUiService()
                ? SearchFilterDialog.YOUTUBE_MUSIC_SERVICE_ID
                : serviceId;

        final int selectedContentFilterId = selectedContentFilter.isEmpty()
                ? -1
                : selectedContentFilter.get(0).getIdentifier();
        final ArrayList<Integer> selectedSortFilterIds = new ArrayList<>();
        if (selectedSortFilter != null) {
            for (final FilterItem filterItem : selectedSortFilter) {
                selectedSortFilterIds.add(filterItem.getIdentifier());
            }
        }

        SearchFilterDialog.newInstance(
                serviceIds,
                dialogServiceId,
                selectedContentFilterId,
                selectedSortFilterIds,
                currentQuery
        ).show(getChildFragmentManager(), SearchFilterDialog.TAG);
    }

    private boolean shouldUseYoutubeMusicUiService() {
        if (serviceId != ServiceList.YouTube.getServiceId() || selectedContentFilter.isEmpty()) {
            return false;
        }

        final String filterName = selectedContentFilter.get(0).getName();
        return filterName != null && filterName.startsWith(YOUTUBE_MUSIC_FILTER_PREFIX);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Suggestion Results
    //////////////////////////////////////////////////////////////////////////*/

    public void handleSuggestions(@NonNull final List<SuggestionItem> suggestions) {
        if (DEBUG) {
            Log.d(TAG, "handleSuggestions() called with: suggestions = [" + suggestions + "]");
        }
        if (searchBinding == null || suggestionListAdapter == null) {
            return;
        }
        suggestionListAdapter.setItems(suggestions);
        final RecyclerView list = centeredSearch
                ? searchBinding.homeSuggestionsList : searchBinding.suggestionsList;
        if ((!centeredSearch || !showingResults) && !suggestions.isEmpty()) {
            list.scrollToPosition(0);
        }
        if (suggestionsPanelVisible && isErrorPanelVisible()) {
            hideLoading();
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void hideLoading() {
        super.hideLoading();
        showListFooter(false);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Search Results
    //////////////////////////////////////////////////////////////////////////*/

    private void handleChannelSearchResult(@NonNull final ChannelTabInfo result) {
        if (!result.getErrors().isEmpty()) {
            showSnackBarError(new ErrorInfo(result.getErrors(), UserAction.SEARCHED,
                    searchString, serviceId));
        }

        nextPage = result.getNextPage();
        hideLoading();

        if (infoListAdapter.getItemsList().isEmpty()) {
            if (!result.getRelatedItems().isEmpty()) {
                infoListAdapter.addInfoItemList(result.getRelatedItems());
                showListFooter(hasMoreItems());
            } else {
                infoListAdapter.clearStreamItemList();
                showEmptyState();
            }
        }
    }

    @Override
    public void handleResult(@NonNull final SearchInfo result) {
        final List<Throwable> exceptions = result.getErrors();
        if (!exceptions.isEmpty()
                && !(exceptions.size() == 1
                && exceptions.get(0) instanceof SearchExtractor.NothingFoundException)) {
            showSnackBarError(new ErrorInfo(result.getErrors(), UserAction.SEARCHED,
                    searchString, serviceId));
        }

        searchSuggestion = result.getSearchSuggestion();
        isCorrectedSearch = result.isCorrectedSearch();

        // List<MetaInfo> cannot be bundled without creating some containers
        metaInfo = result.getMetaInfo().toArray(new MetaInfo[0]);
        showMetaInfoInTextView(result.getMetaInfo(), searchBinding.searchMetaInfoTextView,
                searchBinding.searchMetaInfoSeparator, disposables);

        handleSearchSuggestion();

        lastSearchedString = searchString;
        nextPage = result.getNextPage();

        if (infoListAdapter.getItemsList().isEmpty()) {
            if (!result.getRelatedItems().isEmpty()) {
                infoListAdapter.addInfoItemList(result.getRelatedItems());
            } else {
                infoListAdapter.clearStreamItemList();
                showEmptyState();
                return;
            }
        }

        super.handleResult(result);
    }

    private void handleSearchSuggestion() {
        if (TextUtils.isEmpty(searchSuggestion)) {
            searchBinding.correctSuggestion.setVisibility(View.GONE);
        } else {
            final String helperText = getString(isCorrectedSearch
                    ? R.string.search_showing_result_for
                    : R.string.did_you_mean);

            final String highlightedSearchSuggestion =
                    "<b><i>" + Html.escapeHtml(searchSuggestion) + "</i></b>";
            final String text = String.format(helperText, highlightedSearchSuggestion);
            searchBinding.correctSuggestion.setText(HtmlCompat.fromHtml(text,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));

            searchBinding.correctSuggestion.setOnClickListener(v -> {
                searchBinding.correctSuggestion.setVisibility(View.GONE);
                search(searchSuggestion);
                searchEditText.setText(searchSuggestion);
            });

            searchBinding.correctSuggestion.setOnLongClickListener(v -> {
                searchEditText.setText(searchSuggestion);
                searchEditText.setSelection(searchSuggestion.length());
                showKeyboardSearch();
                return true;
            });

            searchBinding.correctSuggestion.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void handleNextItems(final ListExtractor.InfoItemsPage<?> result) {
        showListFooter(false);
        infoListAdapter.addInfoItemList(result.getItems());
        nextPage = result.getNextPage();

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(new ErrorInfo(result.getErrors(), UserAction.SEARCHED,
                    "\"" + searchString + "\" → pageUrl: " + nextPage.getUrl() + ", "
                            + "pageIds: " + nextPage.getIds() + ", "
                            + "pageCookies: " + nextPage.getCookies(),
                    serviceId));
        }
        super.handleNextItems(result);
    }

    @Override
    public void handleError() {
        super.handleError();
        hideSuggestionsPanel();
        hideKeyboardSearch();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Suggestion item touch helper
    //////////////////////////////////////////////////////////////////////////*/

    public int getSuggestionMovementFlags(@NonNull final RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            return 0;
        }

        final SuggestionItem item = suggestionListAdapter.getItem(position);
        return item.fromHistory ? makeMovementFlags(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) : 0;
    }

    public void onSuggestionItemSwiped(@NonNull final RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getBindingAdapterPosition();
        final String query = suggestionListAdapter.getItem(position).query;
        final Disposable onDelete = historyRecordManager.deleteSearchHistory(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        howManyDeleted -> suggestionPublisher
                                .onNext(searchEditText.getText().toString()),
                        throwable -> showSnackBarError(new ErrorInfo(throwable,
                                UserAction.DELETE_FROM_HISTORY, "Deleting item failed")));
        disposables.add(onDelete);
    }
}
