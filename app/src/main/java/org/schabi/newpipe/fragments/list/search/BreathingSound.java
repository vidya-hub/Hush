package org.schabi.newpipe.fragments.list.search;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import org.schabi.newpipe.R;

/** Three short local tones, played only when a breathing phase changes. */
final class BreathingSound {
    private final SoundPool pool;
    private final int inhale;
    private final int exhale;
    private final int hold;
    private final java.util.Set<Integer> loaded = new java.util.HashSet<>();
    private int pending;
    private int stream;

    BreathingSound(final Context context) {
        pool = new SoundPool.Builder().setMaxStreams(1)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .build();
        pool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                loaded.add(sampleId);
                if (pending == sampleId) {
                    playLoaded(sampleId);
                    pending = 0;
                }
            }
        });
        inhale = pool.load(context, R.raw.breath_cue_in, 1);
        exhale = pool.load(context, R.raw.breath_cue_out, 1);
        hold = pool.load(context, R.raw.breath_cue_hold, 1);
    }

    void play(final BreathingSession.Phase phase) {
        final int sound = phase == BreathingSession.Phase.INHALE ? inhale
                : phase == BreathingSession.Phase.EXHALE ? exhale : hold;
        if (loaded.contains(sound)) {
            playLoaded(sound);
        } else {
            pending = sound;
        }
    }

    private void playLoaded(final int sound) {
        if (stream != 0) pool.stop(stream);
        stream = pool.play(sound, 0.20f, 0.20f, 1, 0, 1f);
    }

    void cancelPending() {
        pending = 0;
        if (stream != 0) { pool.stop(stream); stream = 0; }
    }

    void release() {
        pool.release();
    }
}
