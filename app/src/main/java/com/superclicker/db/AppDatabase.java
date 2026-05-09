package com.superclicker.db;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.superclicker.model.Script;
import com.superclicker.model.Step;
@Database(entities = {Script.class, Step.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract ScriptDao scriptDao();
    public abstract StepDao stepDao();
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "superclicker.db").build();
                }
            }
        }
        return INSTANCE;
    }
}
