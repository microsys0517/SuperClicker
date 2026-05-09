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

    public static ClickAccessibilityService getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public void performClick(int x, int y, final GestureCallback cb) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder b = new GestureDescription.Builder();
        b.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
        GestureDescription gesture = b.build();
        GestureResultCallback callback = new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                if (cb != null) cb.onCompleted();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                if (cb != null) cb.onCancelled();
            }
        };
        dispatchGesture(gesture, callback, null);
    }

    public void performLongPress(int x, int y, long duration, final GestureCallback cb) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder b = new GestureDescription.Builder();
        b.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
        GestureDescription gesture = b.build();
        GestureResultCallback callback = new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                if (cb != null) cb.onCompleted();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                if (cb != null) cb.onCancelled();
            }
        };
        dispatchGesture(gesture, callback, null);
    }

    public void performSwipe(int x1, int y1, int x2, int y2, long duration, final GestureCallback cb) {
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        GestureDescription.Builder b = new GestureDescription.Builder();
        b.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
        GestureDescription gesture = b.build();
        GestureResultCallback callback = new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                if (cb != null) cb.onCompleted();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                if (cb != null) cb.onCancelled();
            }
        };
        dispatchGesture(gesture, callback, null);
    }

    public void performMultiTouch(int[][] points, long[] delays, final GestureCallback cb) {
        if (points == null || points.length == 0) {
            if (cb != null) cb.onCancelled();
            return;
        }
        GestureDescription.Builder b = new GestureDescription.Builder();
        for (int i = 0; i < points.length; i++) {
            Path path = new Path();
            path.moveTo(points[i][0], points[i][1]);
            long star
            b.addStroke(new Gestu

        GestureDescription gesture = b.build();
        G
            @Override
            public void onComplet
                if (cb != nul
            }

            @Overrid
            public void onCancelled(GestureDescriptio

            }
        };
        dis


    public void pressBack() {
        performGlobalAction(GLOBAL_ACT


    public void pressHome() {
        performGlobalA
    }

    public void pressRecents() {
        perfor


    public int[] getScreenSize() {
  
        return new int[]{dm
    }
}
