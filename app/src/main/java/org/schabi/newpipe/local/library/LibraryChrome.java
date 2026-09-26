package org.schabi.newpipe.local.library;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import org.schabi.newpipe.MainActivity;

final class LibraryChrome {
    private LibraryChrome() {
    }

    static void show(final Fragment fragment, @StringRes final int title) {
        if (!(fragment.getActivity() instanceof MainActivity)) {
            return;
        }
        final MainActivity activity = (MainActivity) fragment.getActivity();
        activity.setSearchChrome(false);
        final ActionBar bar = activity.getSupportActionBar();
        if (bar != null) {
            bar.setTitle(title);
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
