package com.superclicker.db;
import androidx.room.TypeConverter;import com.google.gson.Gson;
import com.superclicker.model.ExecutionConfig;import com.superclicker.model.StepType;
public class Converters{private static final Gson g=new Gson();
@TypeConverter public static String fromST(StepType t){return t==null?null:t.name();}
@TypeConverter public static StepType toST(String n){return n==null?null:StepType.valueOf(n);}
@TypeConverter public static String fromEC(ExecutionConfig c){return c==null?null:g.toJson(c);}
@TypeConverter public static ExecutionConfig toEC(String j){return j==null?new ExecutionConfig():g.fromJson(j,ExecutionConfig.class);}}
