package org.schabi.newpipe.local.library;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.info_list.dialog.InfoItemDialog;
import org.schabi.newpipe.info_list.dialog.StreamDialogDefaultEntry;

public final class VideoActions {
    private VideoActions() {
    }

    public static void show(@NonNull final Fragment fragment,
                            @NonNull final StreamInfoItem item) {
        if (fragment.getActivity() == null) {
            return;
        }
        try {
            new InfoItemDialog.Builder(fragment.getActivity(), fragment.requireContext(),
                    fragment, item, false)
                    .addAllEntries(
                            StreamDialogDefaultEntry.START_HERE_ON_BACKGROUND,
                            StreamDialogDefaultEntry.APPEND_PLAYLIST,
                            StreamDialogDefaultEntry.DOWNLOAD,
                            StreamDialogDefaultEntry.SHARE)
                    .create()
                    .show();
        } catch (final IllegalArgumentException error) {
            InfoItemDialog.Builder.reportErrorDuringInitialization(error, item);
        }
    }
}
