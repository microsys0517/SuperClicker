package com.superclicker.service;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
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
import com.superclicker.util.Logger;
public class FloatingControlService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private TextView tvStatus, tvStep, tvLoop;
    private ScriptEngine engine;
    private boolean isHidden = false;
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createFloatingView();
        startForegroundNotification();
    }
    private void startForegroundNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this, "service_channel")
            .setContentTitle("超级点击器").setContentText("运行中...")
            .setSmallIcon(R.drawable.ic_play).setContentIntent(pi).build();
        startForeground(1001, notification);
    }
    private void createFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_control, null);
        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0; params.y = 300;
        windowManager.addView(floatingView, params);
        tvStatus = floatingView.findViewById(R.id.tv_floating_status);
        tvStep = floatingView.findViewById(R.id.tv_floating_step);
        tvLoop = floatingView.findViewById(R.id.tv_floating_loop);
        floatingView.findViewById(R.id.btn_floating_play).setOnClickListener(v -> sendBroadcast(new Intent("com.superclicker.ACTION_PLAY")));
        floatingView.findViewById(R.id.btn_floating_pause).setOnClickListener(v -> { if (engine != null) engine.togglePause(); });
        floatingView.findViewById(R.id.btn_floating_stop).setOnClickListener(v -> { if (engine != null) engine.stop(); });
        floatingView.findViewById(R.id.btn_floating_hide).setOnClickListener(v -> {
            int vis = isHidden ? View.VISIBLE : View.GONE;
            floatingView.findViewById(R.id.layout_controls).setVisibility(vis);
            floatingView.findViewById(R.id.layout_info).setVisibility(vis);
            isHidden = !isHidden;
        });
        floatingView.findViewById(R.id.btn_floating_close).setOnClickListener(v -> stopSelf());
        setupDrag();
    }
    public void bindEngine(ScriptEngine engine) {
        this.engine = engine;
        engine.setListener(new ScriptEngine.StateListener() {
            public void onStateChanged(ScriptEngine.State state) {
                switch (state) {
                    case IDLE: tvStatus.setText("就绪"); break;
                    case RUNNING: tvStatus.setText("运行中"); break;
                    case PAUSED: tvStatus.setText("已暂停"); break;
                    case STOPPING: tvStatus.setText("停止中"); break;
                }
            }
            public void onStepChanged(int c, int t, Step s) { tvStep.setText("步骤 " + (c + 1) + "/" + t); }
            public void onLoopChanged(int c, int t) { tvLoop.setText("第 " + c + " 轮"); }
            public void onError(int i, Step s, String e) { tvStatus.setText("错误"); }
            public void onComplete() { tvStatus.setText("已完成"); }
        });
    }
    private void setupDrag() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            int initX, initY; float initTouchX, initTouchY; boolean dragging;
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = params.x; initY = params.y;
                        initTouchX = event.getRawX(); initTouchY = event.getRawY();
                        dragging = false; return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - initTouchX;
                        float dy = event.getRawY() - initTouchY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) dragging = true;
                        params.x = initX + (int) dx; params.y = initY + (int) dy;
                        windowManager.updateViewLayout(floatingView, params); return true;
                    case MotionEvent.ACTION_UP: return dragging;
                }
                return false;
            }
        });
    }
    @Override public void onDestroy() { super.onDestroy(); if (floatingView != null) windowManager.removeView(floatingView); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
