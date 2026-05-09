package com.superclicker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import com.superclicker.engine.ScriptEngine;
import com.superclicker.model.Step;

public class FloatingControlService extends Service {

    private static final String TAG = "FloatingCtrl";

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private TextView tvStatus;
    private TextView tvStep;
    private TextView tvLoop;
    private ScriptEngine engine;
    private boolean isHidden = false;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Log.i(TAG, "服务 onCreate");
            startForegroundSafely();
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            createFloatingView();
            Log.i(TAG, "服务初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "服务 onCreate 异常: " + e.getMessage(), e);
        }
    }

    private void startForegroundSafely() {
        try {
            // 确保通知渠道存在
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null && nm.getNotificationChannel("service_channel") == null) {
                    NotificationChannel ch = new NotificationChannel(
                        "service_channel", "后台服务", NotificationManager.IMPORTANCE_LOW);
                    nm.createNotificationChannel(ch);
                }
            }

            Intent intent = new Intent(this, MainActivity.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);

            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, "service_channel");
            } else {
                builder = new Notification.Builder(this);
            }

            Notification notification = builder
                .setContentTitle("超级点击器")
                .setContentText("运行中...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .build();

            startForeground(1001, notification);
            Log.i(TAG, "前台通知已启动");
        } catch (Exception e) {
            Log.e(TAG, "启动前台通知失败: " + e.getMessage(), e);
        }
    }

    private void createFloatingView() {
        try {
            floatingView = LayoutInflater.from(this).inflate(R.layout.floating_control, null);

            params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 300;

            windowManager.addView(floatingView, params);

            tvStatus = floatingView.findViewById(R.id.tv_floating_status);
            tvStep = floatingView.findViewById(R.id.tv_floating_step);
            tvLoop = floatingView.findViewById(R.id.tv_floating_loop);

            ImageButton btnPlay = floatingView.findViewById(R.id.btn_floating_play);
            ImageButton btnPause = floatingView.findViewById(R.id.btn_floating_pause);
            ImageButton btnStop = floatingView.findViewById(R.id.btn_floating_stop);
            ImageButton btnHide = floatingView.findViewById(R.id.btn_floating_hide);
            ImageButton btnClose = floatingView.findViewById(R.id.btn_floating_close);

            if (btnPlay != null) btnPlay.setOnClickListener(v -> {
                try {
                    sendBroadcast(new Intent("com.superclicker.ACTION_PLAY"));
                } catch (Exception e) {
                    Log.e(TAG, "播放按钮异常: " + e.getMessage());
                }
            });

            if (btnPause != null) btnPause.setOnClickListener(v -> {
                try {
                    if (engine != null) engine.togglePause();
                } catch (Exception e) {
                    Log.e(TAG, "暂停按钮异常: " + e.getMessage());
                }
            });

            if (btnStop != null) btnStop.setOnClickListener(v -> {
                try {
                    if (engine != null) engine.stop();
                } catch (Exception e) {
                    Log.e(TAG, "停止按钮异常: " + e.getMessage());
                }
            });

            if (btnHide != null) btnHide.setOnClickListener(v -> {
                try {
                    int vis = isHidden ? View.VISIBLE : View.GONE;
                    View controls = floatingView.findViewById(R.id.layout_controls);
                    View info = floatingView.findViewById(R.id.layout_info);
                    if (controls != null) controls.setVisibility(vis);
                    if (info != null) info.setVisibility(vis);
                    isHidden = !isHidden;
                } catch (Exception e) {
                    Log.e(TAG, "隐藏按钮异常: " + e.getMessage());
                }
            });

            if (btnClose != null) btnClose.setOnClickListener(v -> stopSelf());

            setupDrag();
            Log.i(TAG, "悬浮窗创建成功");
        } catch (Exception e) {
            Log.e(TAG, "创建悬浮窗失败: " + e.getMessage(), e);
        }
    }

    public void bindEngine(ScriptEngine engine) {
        this.engine = engine;
        try {
            engine.setListener(new ScriptEngine.StateListener() {
                @Override
                public void onStateChanged(ScriptEngine.State state) {
                    if (tvStatus == null) return;
                    switch (state) {
                        case IDLE: tvStatus.setText("就绪"); break;
                        case RUNNING: tvStatus.setText("运行中"); break;
                        case PAUSED: tvStatus.setText("已暂停"); break;
                        case STOPPING: tvStatus.setText("停止中"); break;
                    }
                }

                @Override
                public void onStepChanged(int c, int t, Step s) {
                    if (tvStep != null) tvStep.setText("步骤 " + (c + 1) + "/" + t);
                }

                @Override
                public void onLoopChanged(int c, int t) {
                    if (tvLoop != null) tvLoop.setText("第 " + c + " 轮");
                }

                @Override
                public void onError(int i, Step s, String e) {
                    if (tvStatus != null) tvStatus.setText("错误");
                }

                @Override
                public void onComplete() {
                    if (tvStatus != null) tvStatus.setText("已完成");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "绑定引擎失败: " + e.getMessage(), e);
        }
    }

    private void setupDrag() {
        if (floatingView == null) return;
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initX, initY;
            private float initTouchX, initTouchY;
            private boolean dragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initX = params.x;
                            initY = params.y;
                            initTouchX = event.getRawX();
                            initTouchY = event.getRawY();
                            dragging = false;
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            float dx = event.getRawX() - initTouchX;
                            float dy = event.getRawY() - initTouchY;
                            if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                                dragging = true;
                            }
                            params.x = initX + (int) dx;
                            params.y = initY + (int) dy;
                            windowManager.updateViewLayout(floatingView, params);
                            return true;
                        case MotionEvent.ACTION_UP:
                            return dragging;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "拖动异常: " + e.getMessage());
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (floatingView != null && windowManager != null) {
                windowManager.removeView(floatingView);
            }
            Log.i(TAG, "服务已销毁");
        } catch (Exception e) {
            Log.e(TAG, "服务销毁异常: " + e.getMessage(), e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
