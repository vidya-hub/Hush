package org.schabi.newpipe.hush;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schabi.newpipe.hush.games.GameStateStore;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.local.profile.ProfileStore;
import java.util.UUID;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class GameStorageIsolationTest {
    @Test public void privateRoundNeverOverwritesRegularRound() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wasPrivate = HistoryRecordManager.isIncognito(context);
        String game = "qa_" + UUID.randomUUID();
        SharedPreferences games = context.getSharedPreferences("hush_games_"
                + ProfileStore.getActive(context).id, Context.MODE_PRIVATE);
        try {
            preferences.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY, false).commit();
            GameStateStore store = new GameStateStore(context);
            store.write(game, new JSONObject().put("score", 12));
            String regular = games.getString(game, null);
            preferences.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY, true).commit();
            assertFalse(store.read(game).has("score"));
            store.write(game, new JSONObject().put("score", 99));
            assertEquals(99, store.read(game).getInt("score"));
            assertEquals(regular, games.getString(game, null));
            preferences.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY, false).commit();
            assertEquals(12, store.read(game).getInt("score"));
            GameStateStore.clearIncognito();
            preferences.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY, true).commit();
            assertFalse(store.read(game).has("score"));
        } finally {
            games.edit().remove(game).commit();
            preferences.edit().putBoolean(HistoryRecordManager.INCOGNITO_KEY, wasPrivate).commit();
        }
    }

    @Test public void deletingProfileClearsOnlyItsGameFile() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String first = "qa_" + UUID.randomUUID(), second = "qa_" + UUID.randomUUID();
        SharedPreferences a = context.getSharedPreferences("hush_games_" + first, Context.MODE_PRIVATE);
        SharedPreferences b = context.getSharedPreferences("hush_games_" + second, Context.MODE_PRIVATE);
        try {
            a.edit().putString("2048", "first").commit();
            b.edit().putString("2048", "second").commit();
            GameStateStore.deleteProfile(context, first);
            assertFalse(a.contains("2048"));
            assertEquals("second", b.getString("2048", null));
        } finally { a.edit().clear().commit(); b.edit().clear().commit(); }
    }
}
