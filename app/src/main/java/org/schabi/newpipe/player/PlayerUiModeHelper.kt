package org.schabi.newpipe.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.OrientationEventListener
import org.schabi.newpipe.player.helper.PlayerHelper
import org.schabi.newpipe.util.DeviceUtils

/**
 * The single entry point for fullscreen and orientation changes.
 */
object PlayerUiModeHelper {
    private var releaseListener: OrientationEventListener? = null

    @JvmStatic
    fun setFullscreen(player: Player, fullscreen: Boolean) {
        player.setFullscreen(fullscreen)
        applyVideoOrientation(player)
    }

    /**
     * Entering fullscreen always turns the screen to the video's orientation, and leaving it
     * turns back to portrait. With auto-rotate on, the lock is released once the phone is
     * physically held that way, so turning the phone keeps controlling fullscreen.
     */
    @JvmStatic
    fun applyVideoOrientation(player: Player) {
        if (!PlayerHelper.shouldRotateFullscreenToVideoOrientation(player.context) ||
            DeviceUtils.isTv(player.context)
        ) {
            return
        }
        val activity = player.parentActivity ?: return
        if (DeviceUtils.isInMultiWindow(activity)) {
            return
        }

        val landscapeNow = activity.resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        val requestedOrientation = when {
            player.isFullscreen && player.isVerticalVideo ->
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            player.isFullscreen -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            landscapeNow -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        setOrientation(activity, player, requestedOrientation)
        releaseWhenHeld(activity, requestedOrientation)
    }

    @JvmStatic
    fun onOrientationChanged(player: Player, landscape: Boolean) {
        val activity = player.parentActivity ?: return
        if (!player.videoPlayerSelected() ||
            DeviceUtils.isTv(player.context) ||
            DeviceUtils.isInMultiWindow(activity) ||
            !PlayerHelper.shouldRotationControlFullscreen(player.context)
        ) {
            return
        }
        // This transition follows the screen; it must not request another orientation.
        player.setFullscreen(landscape)
    }

    @JvmStatic
    fun setOrientation(
        activity: Activity?,
        player: Player?,
        requestedOrientation: Int,
    ) {
        if (activity != null && activity.requestedOrientation != requestedOrientation) {
            activity.requestedOrientation = requestedOrientation
        }
    }

    private fun releaseWhenHeld(activity: Activity, requestedOrientation: Int) {
        releaseListener?.disable()
        releaseListener = null
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ||
            !PlayerHelper.shouldRotationControlFullscreen(activity)
        ) {
            return
        }
        val wantLandscape = requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        val listener = object : OrientationEventListener(activity.applicationContext) {
            override fun onOrientationChanged(angle: Int) {
                if (angle == ORIENTATION_UNKNOWN) {
                    return
                }
                val heldLandscape = angle in 60..120 || angle in 240..300
                val heldPortrait = angle <= 30 || angle >= 330
                if ((wantLandscape && heldLandscape) || (!wantLandscape && heldPortrait)) {
                    disable()
                    if (releaseListener === this) {
                        releaseListener = null
                    }
                    if (!activity.isFinishing &&
                        activity.requestedOrientation == requestedOrientation
                    ) {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
            }
        }
        if (listener.canDetectOrientation()) {
            releaseListener = listener
            listener.enable()
        }
    }
}
