package com.superclicker.util;

import android.os.Handler;
import android.os.Looper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Logger {
    public interface LogListener { void onNewLog(String message); }
    private static final List<String> logs = new ArrayList<>();
    private static final List<LogListener> listeners = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    private static final Handler h = new Handler(Looper.getMainLooper());

    public static void addListener(LogListener l) { synchronized (listeners) { listeners.add(l); } }
    public static void removeListener(LogListener l) { synchronized (listeners) { listeners.remove(l); } }

    public static void d(String tag, String msg) { add("D", tag, msg); }
    public static void i(String tag, String msg) { add("I", tag, msg); }
    public static void w(String tag, String msg) { add("W", tag, msg); }
    public static void e(String tag, String msg) { add("E", tag, msg); }

    private static void add(String level, String tag, String msg) {
        String line = sdf.format(new Date()) + " [" + level + "/" + tag + "] " + msg;
        synchronized (logs) {
            logs.add(line);
            if (logs.size() > 5000) logs.remove(0);
        }
        h.post(() -> { synchronized (listeners) { for (LogListener l : listeners) l.onNewLog(line); } });
    }

    public static List<String> getLogs() { synchronized (logs) { return new ArrayList<>(logs); } }
    public static void clear() { synchronized (logs) { logs.clear(); } }
}
