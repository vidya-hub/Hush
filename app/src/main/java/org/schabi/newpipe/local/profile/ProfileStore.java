package org.schabi.newpipe.local.profile;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.schabi.newpipe.App;
import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.player.helper.PlayerHolder;
import org.schabi.newpipe.util.ServiceHelper;
import org.schabi.newpipe.youtube.LocalDomPoTokenProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Named local profiles. Each one has its own database (search history, watch
 * history, playback position) and its own YouTube cookie session.
 */
public final class ProfileStore {
    public static final String DEFAULT_ID = "default";
    private static final String REGISTRY = "pipe_profiles";
    private static final String KEY_ACTIVE = "active_id";
    private static final String KEY_PROFILES = "profiles";

    private static final Object LOCK = new Object();
    private static volatile boolean applyingSession;

    private ProfileStore() {
    }

    public static final class Profile {
        public final String id;
        public final String name;

        public Profile(@NonNull final String id, @NonNull final String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static void ensure(@NonNull final Context context) {
        final Context app = context.getApplicationContext();
        synchronized (LOCK) {
            final SharedPreferences registry = registry(app);
            if (registry.contains(KEY_PROFILES)) {
                return;
            }
            final List<Profile> profiles = new ArrayList<>();
            profiles.add(new Profile(DEFAULT_ID,
                    app.getString(R.string.profile_default_name)));
            writeProfiles(registry, profiles, DEFAULT_ID);
            copyDefaultCookiesIntoSession(app, DEFAULT_ID);
        }
    }

    @NonNull
    public static List<Profile> getProfiles(@NonNull final Context context) {
        ensure(context);
        synchronized (LOCK) {
            return readProfiles(registry(context.getApplicationContext()));
        }
    }

    @NonNull
    public static Profile getActive(@NonNull final Context context) {
        ensure(context);
        synchronized (LOCK) {
            final SharedPreferences registry = registry(context.getApplicationContext());
            final String activeId = registry.getString(KEY_ACTIVE, DEFAULT_ID);
            for (final Profile profile : readProfiles(registry)) {
                if (profile.id.equals(activeId)) {
                    return profile;
                }
            }
            final Profile fallback = readProfiles(registry).get(0);
            registry.edit().putString(KEY_ACTIVE, fallback.id).apply();
            return fallback;
        }
    }

    @NonNull
    public static String databaseName(@NonNull final Context context) {
        final Profile active = getActive(context);
        if (DEFAULT_ID.equals(active.id)) {
            return "newpipe.db";
        }
        return "newpipe_" + active.id + ".db";
    }

    @NonNull
    public static Profile create(@NonNull final Context context, @NonNull final String rawName) {
        final String name = rawName.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty profile name");
        }
        ensure(context);
        synchronized (LOCK) {
            final SharedPreferences registry = registry(context.getApplicationContext());
            final List<Profile> profiles = readProfiles(registry);
            for (final Profile profile : profiles) {
                if (profile.name.equalsIgnoreCase(name)) {
                    throw new IllegalArgumentException("duplicate profile name");
                }
            }
            final Profile created = new Profile(newId(), name);
            profiles.add(created);
            writeProfiles(registry, profiles, registry.getString(KEY_ACTIVE, DEFAULT_ID));
            return created;
        }
    }

    public static boolean canDelete(@NonNull final Context context) {
        return getProfiles(context).size() > 1;
    }

    public static void deleteActive(@NonNull final Context context) {
        final Context app = context.getApplicationContext();
        final Profile active = getActive(app);
        final List<Profile> profiles = getProfiles(app);
        if (profiles.size() < 2) {
            return;
        }
        Profile next = profiles.get(0);
        if (next.id.equals(active.id)) {
            next = profiles.get(1);
        }
        switchTo(app, next.id);
        synchronized (LOCK) {
            final SharedPreferences registry = registry(app);
            final List<Profile> remaining = readProfiles(registry);
            remaining.removeIf(profile -> profile.id.equals(active.id));
            writeProfiles(registry, remaining, next.id);
        }
        app.getSharedPreferences(sessionName(active.id), Context.MODE_PRIVATE)
                .edit().clear().apply();
        if (!DEFAULT_ID.equals(active.id)) {
            app.deleteDatabase(databaseNameFor(active.id));
        } else {
            app.deleteDatabase("newpipe.db");
        }
        deletePhoto(app, active.id);
        org.schabi.newpipe.hush.games.GameStateStore.deleteProfile(app, active.id);
    }

    @NonNull
    public static java.io.File photoFile(@NonNull final Context context,
                                          @NonNull final String profileId) {
        final java.io.File dir = new java.io.File(
                context.getApplicationContext().getFilesDir(), "profile_photos");
        return new java.io.File(dir, profileId + ".jpg");
    }

    public static void savePhoto(@NonNull final Context context,
                                 @NonNull final String profileId,
                                 @NonNull final java.io.InputStream in) throws java.io.IOException {
        final java.io.File file = photoFile(context, profileId);
        final java.io.File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (java.io.OutputStream out = new java.io.FileOutputStream(file)) {
            final byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    public static void deletePhoto(@NonNull final Context context,
                                   @NonNull final String profileId) {
        final java.io.File file = photoFile(context, profileId);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void switchTo(@NonNull final Context context, @NonNull final String profileId) {
        final Context app = context.getApplicationContext();
        ensure(app);
        final Profile current = getActive(app);
        if (current.id.equals(profileId)) {
            return;
        }
        captureSessionFromDefaultPrefs(app);
        synchronized (LOCK) {
            registry(app).edit().putString(KEY_ACTIVE, profileId).apply();
        }
        applySessionToDefaultPrefs(app);
        NewPipeDatabase.close();
        ServiceHelper.initService(app, ServiceList.YouTube.getServiceId());
        App.reconcileYoutubePlayerClient(app);
        LocalDomPoTokenProvider.INSTANCE.reset();
        PlayerHolder.getInstance().rebindHistory();
    }

    public static boolean isApplyingSession() {
        return applyingSession;
    }

    public static void captureSessionFromDefaultPrefs(@NonNull final Context context) {
        if (applyingSession) {
            return;
        }
        final Context app = context.getApplicationContext();
        ensure(app);
        final SharedPreferences defaults = PreferenceManager.getDefaultSharedPreferences(app);
        final String cookiesKey = app.getString(R.string.youtube_cookies_key);
        final String potKey = app.getString(R.string.youtube_po_token_key);
        session(app, getActive(app).id).edit()
                .putString(cookiesKey, defaults.getString(cookiesKey, ""))
                .putString(potKey, defaults.getString(potKey, ""))
                .apply();
    }

    public static void applySessionToDefaultPrefs(@NonNull final Context context) {
        final Context app = context.getApplicationContext();
        ensure(app);
        final String cookiesKey = app.getString(R.string.youtube_cookies_key);
        final String potKey = app.getString(R.string.youtube_po_token_key);
        final SharedPreferences session = session(app, getActive(app).id);
        applyingSession = true;
        try {
            PreferenceManager.getDefaultSharedPreferences(app).edit()
                    .putString(cookiesKey, session.getString(cookiesKey, ""))
                    .putString(potKey, session.getString(potKey, ""))
                    .apply();
        } finally {
            applyingSession = false;
        }
    }

    private static void copyDefaultCookiesIntoSession(@NonNull final Context app,
                                                       @NonNull final String profileId) {
        final SharedPreferences defaults = PreferenceManager.getDefaultSharedPreferences(app);
        final String cookiesKey = app.getString(R.string.youtube_cookies_key);
        final String potKey = app.getString(R.string.youtube_po_token_key);
        session(app, profileId).edit()
                .putString(cookiesKey, defaults.getString(cookiesKey, ""))
                .putString(potKey, defaults.getString(potKey, ""))
                .apply();
    }

    @NonNull
    private static String databaseNameFor(@NonNull final String profileId) {
        if (DEFAULT_ID.equals(profileId)) {
            return "newpipe.db";
        }
        return "newpipe_" + profileId + ".db";
    }

    @NonNull
    private static String sessionName(@NonNull final String profileId) {
        return "pipe_session_" + profileId;
    }

    @NonNull
    private static SharedPreferences session(@NonNull final Context app,
                                              @NonNull final String profileId) {
        return app.getSharedPreferences(sessionName(profileId), Context.MODE_PRIVATE);
    }

    @NonNull
    private static SharedPreferences registry(@NonNull final Context app) {
        return app.getSharedPreferences(REGISTRY, Context.MODE_PRIVATE);
    }

    @NonNull
    private static List<Profile> readProfiles(@NonNull final SharedPreferences registry) {
        final List<Profile> profiles = new ArrayList<>();
        final String raw = registry.getString(KEY_PROFILES, "[]");
        try {
            final JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                final JSONObject object = array.getJSONObject(i);
                final String id = object.optString("id", "");
                final String name = object.optString("name", "");
                if (!id.isEmpty() && !name.isEmpty()) {
                    profiles.add(new Profile(id, name));
                }
            }
        } catch (final JSONException ignored) {
            // fall through to the default profile below
        }
        if (profiles.isEmpty()) {
            profiles.add(new Profile(DEFAULT_ID, "Personal"));
        }
        return profiles;
    }

    private static void writeProfiles(@NonNull final SharedPreferences registry,
                                      @NonNull final List<Profile> profiles,
                                      @Nullable final String activeId) {
        final JSONArray array = new JSONArray();
        for (final Profile profile : profiles) {
            final JSONObject object = new JSONObject();
            try {
                object.put("id", profile.id);
                object.put("name", profile.name);
            } catch (final JSONException ignored) {
                continue;
            }
            array.put(object);
        }
        String resolvedActive = activeId;
        boolean found = false;
        for (final Profile profile : profiles) {
            if (profile.id.equals(resolvedActive)) {
                found = true;
                break;
            }
        }
        if (!found && !profiles.isEmpty()) {
            resolvedActive = profiles.get(0).id;
        }
        registry.edit()
                .putString(KEY_PROFILES, array.toString())
                .putString(KEY_ACTIVE, resolvedActive)
                .apply();
    }

    @NonNull
    private static String newId() {
        return "p" + UUID.randomUUID().toString().replace("-", "")
                .toLowerCase(Locale.US);
    }
}
