package com.zybooks.projecttwonickburnetteweightlossoption;
//UPDATED enhancement 3

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {WeightEntryEntity.class}, version = 4, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WeightDao weightDao();

    private volatile static AppDatabase INSTANCE;  // volatile here? static volatile AppDatabase...

    public static AppDatabase getInstance(Context context) { // some migration structure drafted with chatGPT assistance
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "weight_tracker.db"  // match existing db filename
                    )
                    .addMigrations(MIGRATION_3_4) // prevent destructive rebuilds
                    .allowMainThreadQueries() // to be removed later
                    //.fallbackToDestructiveMigration() // use only to wipe data CAREFUL!
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            // create new table with NOT NULLs that match @Entity
            db.execSQL("CREATE TABLE IF NOT EXISTS weights_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "date TEXT NOT NULL, " +
                    "WEIGHT text not null)");

            // COPY DATA, REPLACE ANY NULLS WITH SAFE DEFAULTS
            db.execSQL("INSERT INTO weights_new (id, date, weight) " +
                    "SELECT " +
                    " id, " +
                    " COALESCE(date, '2000-01-01') AS date, " +
                    " COALESCE(weight, '') AS weight " +
                    "FROM weights");

            // swap tables
            db.execSQL("DROP TABLE weights");
            db.execSQL("ALTER TABLE weights_new RENAME TO weights");
        }
    };
}
