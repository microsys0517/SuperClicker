package com.superclicker.util;
import android.content.Context;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.superclicker.model.Script;
import com.superclicker.model.Step;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class FileUtils {
    private static final Gson gson = new Gson();
    public static String exportScript(Script script, List<Step> steps) {
        Map<String, Object> data = new HashMap<>();
        data.put("script", script); data.put("steps", steps); data.put("version", "1.0");
        return gson.toJson(data);
    }
    public static Map<String, Object> importScript(String json) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> data = gson.fromJson(json, type);
        Script script = gson.fromJson(gson.toJson(data.get("script")), Script.class); script.id = 0;
        List<Step> steps = gson.fromJson(gson.toJson(data.get("steps")), new TypeToken<List<Step>>() {}.getType());
        for (Step s : steps) s.id = 0;
        Map<String, Object> result = new HashMap<>(); result.put("script", script); result.put("steps", steps);
        return result;
    }
    public static boolean saveToUri(Context ctx, Uri uri, String json) {
        try { OutputStream os = ctx.getContentResolver().openOutputStream(uri); if (os == null) return false; os.write(json.getBytes("UTF-8")); os.close(); return true; }
        catch (Exception e) { return false; }
    }
    public static String readFromUri(Context ctx, Uri uri) {
        try {
            InputStream is = ctx.getContentResolver().openInputStream(uri); if (is == null) return null;
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder(); String line; while ((line = r.readLine()) != null) sb.append(line);
            r.close(); return sb.toString();
        } catch (Exception e) { return null; }
    }
}
