package org.schabi.newpipe.hush.games;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.local.profile.ProfileStore;

import java.util.HashMap;
import java.util.Map;

/** Small, profile-scoped game saves. Incognito play never touches persistent storage. */
public final class GameStateStore {
    private static final Map<String, String> PRIVATE_ROUNDS = new HashMap<>();
    private final Context app;
    private final String profileId;

    public GameStateStore(@NonNull final Context context) {
        app = context.getApplicationContext();
        profileId = ProfileStore.getActive(app).id;
    }

    public JSONObject read(@NonNull final String game) {
        final String raw;
        if (HistoryRecordManager.isIncognito(app)) {
            synchronized (PRIVATE_ROUNDS) {
                raw = PRIVATE_ROUNDS.get(profileId + ":" + game);
            }
        } else {
            raw = prefs().getString(game, null);
        }
        if (raw == null) {
            return new JSONObject();
        }
        try {
            return new JSONObject(raw);
        } catch (final JSONException ignored) {
            return new JSONObject();
        }
    }

    public void write(@NonNull final String game, @NonNull final JSONObject state) {
        if (HistoryRecordManager.isIncognito(app)) {
            synchronized (PRIVATE_ROUNDS) {
                PRIVATE_ROUNDS.put(profileId + ":" + game, state.toString());
            }
        } else {
            prefs().edit().putString(game, state.toString()).apply();
        }
    }

    public static void clearIncognito() {
        synchronized (PRIVATE_ROUNDS) {
            PRIVATE_ROUNDS.clear();
        }
    }

    public static void deleteProfile(@NonNull final Context context,
                                     @NonNull final String profileId) {
        context.getApplicationContext().getSharedPreferences("hush_games_" + profileId,
                Context.MODE_PRIVATE).edit().clear().apply();
        synchronized (PRIVATE_ROUNDS) {
            PRIVATE_ROUNDS.keySet().removeIf(key -> key.startsWith(profileId + ":"));
        }
    }

    private SharedPreferences prefs() {
        return app.getSharedPreferences("hush_games_" + profileId, Context.MODE_PRIVATE);
    }
}
