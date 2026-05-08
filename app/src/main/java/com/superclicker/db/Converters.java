package com.superclicker.db;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.superclicker.model.ExecutionConfig;
import com.superclicker.model.StepType;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromStepType(StepType type) { return type == null ? null : type.name(); }

    @TypeConverter
    public static StepType toStepType(String name) { return name == null ? null : StepType.valueOf(name); }

    @TypeConverter
    public static String fromExecutionConfig(ExecutionConfig config) { return config == null ? null : gson.toJson(config); }

    @TypeConverter
    public static ExecutionConfig toExecutionConfig(String json) { return json == null ? new ExecutionConfig() : gson.fromJson(json, ExecutionConfig.class); }
}
