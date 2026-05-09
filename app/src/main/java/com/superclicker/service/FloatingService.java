package com.superclicker.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.superclicker.MainActivity;
import com.superclicker.R;

public class FloatingService extends Service {

    private static final String TAG = "FloatingService";
    private WindowManager wm;
    private View view;
    private WindowManager.LayoutParams params;
    private TextView tvStatus, tvStep, tvLoop;
    private boolean hidden = false;

    // 全局引用，供 ScriptEngine 获取
    private static FloatingService instance;
    public static FloatingService getInstance() { return instance; }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            startForegroundSafe();
            createWidget();
            Log.i(TAG, "悬浮窗服务已启动");
        } catch (Exception e) {
            Log.e(TAG, "服务启动失败: " + e.getMessage());
        }
    }

    private void startForegroundSafe() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);

            Notification.Builder nb;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nb = new Notification.Builder(this, App.CHANNEL_ID);
            } else {
                nb = new Notification.Builder(this);
            }
            startForeground(2001, nb
                .setContentTitle("超级点击器")
                .setContentText("运行中")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi).build());
        } catch (Exception e) {
            Log.e(TAG, "前台通知失败: " + e.getMessage());
        }
    }

    private void createWidget() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        view = LayoutInflater.from(this).inflate(R.layout.floating_control, null);

        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 300;

        wm.addView(view, params);

        tvStatus = view.findViewById(R.id.tv_floating_status);
        tvStep = view.findViewById(R.id.tv_floating_step);
        tvLoop = view.findViewById(R.id.tv_floating_loop);

        view.findViewById(R.id.btn_floating_play).setOnClickListener(v -> {
            Intent i = new Intent("com.superclicker.ACTION_PLAY");
            sendBroadcast(i);
            Log.i(TAG, "点击播放");
        });

        view.findViewById(R.id.btn_floating_pause).setOnClickListener(v -> {
            Intent i = new Intent("com.superclicker.ACTION_PAUSE");
            sendBroadcast(i);
        });

        view.findViewById(R.id.btn_floating_stop).setOnClickListener(v -> {
            Intent i = new Intent("com.superclicker.ACTION_STOP");
            sendBroadcast(i);
        });

        view.findViewById(R.id.btn_floating_hide).setOnClickListener(v -> {
            int vis = hidden ? View.VISIBLE : View.GONE;
            view.findViewById(R.id.layout_controls).setVisibility(vis);
            view.findViewById(R.id.layout_info).setVisibility(vis);
            hidden = !hidden;
        });

        view.findViewById(R.id.btn_floating_close).setOnClickListener(v -> stopSelf());

        setupDrag();
    }

    // 供 ScriptEngine 更新 UI
    public void setStatus(String text) {
        if (tvStatus != null) tvStatus.post(() -> tvStatus.setText(text));
    }

    public void setStep(String text) {
        if (tvStep != null) tvStep.post(() -> tvStep.setText(text));
    }

    public void setLoop(String text) {
        if (tvLoop != null) tvLoop.post(() -> tvLoop.setText(text));
    }

    private void setupDrag() {
        view.setOnTouchListener(new View.OnTouchListener() {
            int ix, iy; float tx, ty; boolean drag;
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ix = params.x; iy = params.y;
                        tx = e.getRawX(); ty = e.getRawY();
                        drag = false; return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - tx, dy = e.getRawY() - ty;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) drag = true;
                        params.x = ix + (int) dx; params.y = iy + (int) dy;
                        wm.updateViewLayout(view, params); return true;
                    case MotionEvent.ACTION_UP: return drag;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        try { if (view != null && wm != null) wm.removeView(view); } catch (Exception e) {}
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
