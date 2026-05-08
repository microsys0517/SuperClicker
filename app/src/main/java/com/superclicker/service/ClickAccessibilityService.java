package com.superclicker.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;
import com.superclicker.util.Logger;

public class ClickAccessibilityService extends AccessibilityService {
    private static final String TAG = "ClickService";
    private static ClickAccessibilityService instance;

    public interface GestureCallback {
        void onCompleted();
        void onCancelled();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Logger.i(TAG, "无障碍服务已连接");
    }

    public static ClickAccessibilityService getInstance() { return instance; }
    public static boolean isRunning() { return instance != null; }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public void performClick(int x, int y, GestureCallback cb) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder b = new GestureDescription.Builder();
        b.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
        dispatchGesture(b.build(), new GestureDescription.GestureResultCallback() {
            public void onCompleted(GestureDescription d) { if (cb != null) cb.onCompleted(); }
            public void onCancelled(GestureDescription d) { if (cb != null) cb.onCancelled(); }
        }, null);
    }

    public void performLongPress(int x, int y, long duration, GestureCallback cb) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder b = new GestureDescription.Builder();
        b.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
        dispatchGesture(b.build(), new GestureDescription.GestureResultCallback() {
            public void onCompleted(GestureDescription d) { if (cb != null) cb.onCompleted(); }
            public void onCancelled(GestureDescription d) { if (cb != null) cb.onCancelled(); }
        }, null);
    }

    public void performSwipe(int x1, int y1, int x2, int y2, long duration, GestureCallback cb) {
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        GestureDescription.Builder b = new GestureDescription.Builder();
        b.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
        dispatchGesture(b.build(), new GestureDescription.GestureResultCallback() {
            public void onCompleted(GestureDescription d) { if (cb != null) cb.onCompleted(); }
            public void onCancelled(GestureDescription d) { if (cb != null) cb.onCancelled(); }
        }, null);
    }

    public void performMultiTouch(int[][] points, long[] delays, GestureCallback cb) {
        if (points == null || points.length == 0) { if (cb != null) cb.onCancelled(); return; }
        GestureDescription.Builder b = new GestureDescription.Builder();
        for (int i = 0; i < points.length; i++) {
            Path path = new Path();
            path.moveTo(points[i][0], points[i][1]);
            long start = (delays != null && i < delays.length) ? delays[i] : 0;
            b.addStroke(new GestureDescription.StrokeDescription(path, start, 100));
        }
        dispatchGesture(b.build(), new GestureDescription.GestureResultCallback() {
            public void onCompleted(GestureDescription d) { if (cb != null) cb.onCompleted(); }
            public void onCancelled(GestureDescription d) { if (cb != null) cb.onCancelled(); }
        }, null);
    }

    public void pressBack() { performGlobalAction(GLOBAL_ACTION_BACK); }
    public void pressHome() { performGlobalAction(GLOBAL_ACTION_HOME); }
    public void pressRecents() { performGlobalAction(GLOBAL_ACTION_RECENTS); }

    public int[] getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return new int[]{dm.widthPixels, dm.heightPixels};
    }
}
