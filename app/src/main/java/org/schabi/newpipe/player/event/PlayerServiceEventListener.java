package org.schabi.newpipe.player.event;

import com.google.android.exoplayer2.PlaybackException;

public interface PlayerServiceEventListener extends PlayerEventListener {
    void onFullscreenStateChanged(boolean fullscreen);

    /**
     * @param progress 0 while the page is at rest, 1 when the swipe has covered the player height
     */
    default void onFullscreenSwipe(float progress) {
    }

    void onMoreOptionsLongClicked();

    void onPlayerError(PlaybackException error, boolean isCatchableException);

    void hideSystemUiIfNeeded();
}
