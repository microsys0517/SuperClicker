package com.superclicker.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class ClickService extends AccessibilityService {

    private static final String TAG = "ClickService";
    private static ClickService instance;

    public interface Callback {
        void onDone();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.i(TAG, "无障碍服务已连接");
    }

    public static ClickService getInstance() { return instance; }
    public static boolean isRunning() { return instance != null; }

    @Override public void onAccessibilityEvent(AccessibilityEvent e) {}
    @Override public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.i(TAG, "无障碍服务已断开");
    }

    public void click(int x, int y, final Callback cb) {
        try {
            Path p = new Path();
            p.moveTo(x, y);
            GestureDescription.Builder b = new GestureDescription.Builder();
            b.addStroke(new GestureDescription.StrokeDescription(p, 0, 50));
            dispatchGesture(b.build(), new GestureResultCallback() {
                @Override public void onCompleted(GestureDescription d) { if (cb != null) cb.onDone(); }
                @Override public void onCancelled(GestureDescription d) { if (cb != null) cb.onDone(); }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "点击失败: " + e.getMessage());
            if (cb != null) cb.onDone();
        }
    }

    public void longPress(int x, int y, long ms, final Callback cb) {
        try {
            Path p = new Path();
            p.moveTo(x, y);
            GestureDescription.Builder b = new GestureDescription.Builder();
            b.addStroke(new GestureDescription.StrokeDescription(p, 0, ms));
            dispatchGesture(b.build(), new GestureResultCallback() {
                @Override public void onCompleted(GestureDescription d) { if (cb != null) cb.onDone(); }
                @Override public void onCancelled(GestureDescription d) { if (cb != null) cb.onDone(); }
            }, null);
        } catch (Exception e) {
            if (cb != null) cb.onDone();
        }
    }

    public void swipe(int x1, int y1, int x2, int y2, long ms, final Callback cb) {
        try {
            Path p = new Path();
            p.moveTo(x1, y1);
            p.lineTo(x2, y2);
            GestureDescription.Builder b = new GestureDescription.Builder();
            b.addStroke(new GestureDescription.StrokeDescription(p, 0, ms));
            dispatchGesture(b.build(), new GestureResultCallback() {
                @Override public void onCompleted(GestureDescription d) { if (cb != null) cb.onDone(); }
                @Override public void onCancelled(GestureDescription d) { if (cb != null) cb.onDone(); }
            }, null);
        } catch (Exception e) {
            if (cb != null) cb.onDone();
        }
    }

    public void pressBack() { try { performGlobalAction(GLOBAL_ACTION_BACK); } catch (Exception e) {} }
    public void pressHome() { try { performGlobalAction(GLOBAL_ACTION_HOME); } catch (Exception e) {} }
    public void pressRecents() { try { performGlobalAction(GLOBAL_ACTION_RECENTS); } catch (Exception e) {} }

    public int[] screenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return new int[]{dm.widthPixels, dm.heightPixels};
    }
}
