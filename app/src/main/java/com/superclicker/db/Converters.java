package com.superclicker.db;
import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.superclicker.model.ExecutionConfig;
import com.superclicker.model.StepType;
public class Converters {
    private static final Gson gson = new Gson();
    @TypeConverter public static String fromStepType(StepType t) { return t == null ? null : t.name(); }
    @TypeConverter public static StepType toStepType(String n) { return n == null ? null : StepType.valueOf(n); }
    @TypeConverter public static String fromConfig(ExecutionConfig c) { return c == null ? null : gson.toJson(c); }
    @TypeConverter public static ExecutionConfig toConfig(String j) { return j == null ? new ExecutionConfig() : gson.fromJson(j, ExecutionConfig.class); }
}
