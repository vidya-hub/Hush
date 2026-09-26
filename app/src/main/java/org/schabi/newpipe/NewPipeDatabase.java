package org.schabi.newpipe;

import static org.schabi.newpipe.database.Migrations.*;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Room;

import org.schabi.newpipe.database.AppDatabase;
import org.schabi.newpipe.local.profile.ProfileStore;

public final class NewPipeDatabase {
    private static volatile AppDatabase databaseInstance;
    private static volatile String openedName;

    private NewPipeDatabase() {
        //no instance
    }

    private static AppDatabase openDatabase(final Context context, final String name) {
        return Room
                .databaseBuilder(context.getApplicationContext(), AppDatabase.class, name)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_7_6, MIGRATION_8_6, MIGRATION_9_6, MIGRATION_6_900, MIGRATION_9_900, MIGRATION_900_901)
                .build();
    }

    @NonNull
    public static AppDatabase getInstance(@NonNull final Context context) {
        final String name = ProfileStore.databaseName(context);
        synchronized (NewPipeDatabase.class) {
            if (databaseInstance != null && name.equals(openedName)) {
                return databaseInstance;
            }
            if (databaseInstance != null) {
                databaseInstance.close();
                databaseInstance = null;
                openedName = null;
            }
            databaseInstance = openDatabase(context, name);
            openedName = name;
            return databaseInstance;
        }
    }

    public static void checkpoint() {
        if (databaseInstance == null) {
            throw new IllegalStateException("database is not initialized");
        }
        final Cursor c = databaseInstance.query("pragma wal_checkpoint(full)", null);
        if (c.moveToFirst() && c.getInt(0) == 1) {
            throw new RuntimeException("Checkpoint was blocked from completing");
        }
    }

    public static void close() {
        synchronized (NewPipeDatabase.class) {
            if (databaseInstance != null) {
                databaseInstance.close();
                databaseInstance = null;
                openedName = null;
            }
        }
    }
}
