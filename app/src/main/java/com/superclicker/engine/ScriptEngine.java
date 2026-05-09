package com.superclicker.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.superclicker.model.Script;
import com.superclicker.model.Step;
import com.superclicker.model.StepType;
import com.superclicker.service.ClickService;
import com.superclicker.service.FloatingService;
import com.superclicker.util.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ScriptEngine {

    private static final String TAG = "Engine";
    private static ScriptEngine current;

    public enum State { IDLE, RUNNING, PAUSED }
    public interface Listener {
        void onStateChanged(State s);
        void onComplete();
    }

    private final Context ctx;
    private final Handler main = new Handler(Looper.getMainLooper());
    private State state = State.IDLE;
    private Listener listener;
    private volatile boolean stopped = false;
    private volatile boolean paused = false;
    private Thread worker;
    private BroadcastReceiver receiver;

    public ScriptEngine(Context ctx) { this.ctx = ctx.getApplicationContext(); }

    public static ScriptEngine getCurrent() { return current; }
    public State getState() { return state; }
    public void setListener(Listener l) { this.listener = l; }

    public void start(Script script, List<Step> steps) {
        if (state == State.RUNNING) return;
        current = this;
        stopped = false;
        paused = false;
        setState(State.RUNNING);

        // 注册广播接收器
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("com.superclicker.ACTION_PAUSE".equals(action)) togglePause();
                else if ("com.superclicker.ACTION_STOP".equals(action)) stop();
                else if ("com.superclicker.ACTION_PLAY".equals(action)) togglePause();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.superclicker.ACTION_PAUSE");
        filter.addAction("com.superclicker.ACTION_STOP");
        filter.addAction("com.superclicker.ACTION_PLAY");
        ctx.registerReceiver(receiver, filter);

        worker = new Thread(() -> run(script, steps));
        worker.start();
    }

    public void stop() {
        stopped = true;
        paused = false;
        synchronized (this) { notifyAll(); }
        setState(State.IDLE);
        Logger.i(TAG, "脚本停止");
        cleanup();
    }

    public void togglePause() {
        if (state == State.RUNNING) {
            paused = true;
            setState(State.PAUSED);
            Logger.i(TAG, "脚本暂停");
        } else if (state == State.PAUSED) {
            paused = false;
            synchronized (this) { notifyAll(); }
            setState(State.RUNNING);
            Logger.i(TAG, "脚本继续");
        }
    }

    private void run(Script script, List<Step> steps) {
        FloatingService fs = FloatingService.getInstance();
        int loops = script.config.loopCount == 0 ? Integer.MAX_VALUE : script.config.loopCount;

        Logger.i(TAG, "开始执行: " + script.name + " (" + steps.size() + "步, " + (loops == Integer.MAX_VALUE ? "无限" : loops) + "轮)");

        try {
            for (int loop = 0; loop < loops && !stopped; loop++) {
                if (fs != null) fs.setLoop("第" + (loop + 1) + "轮");

                for (int i = script.config.startStep; i < steps.size() && !stopped; i++) {
                    waitIfPaused();
                    if (stopped) break;

                    Step step = steps.get(i);
                    if (!step.enabled) continue;

                    if (fs != null) fs.setStep("步骤" + (i + 1) + "/" + steps.size());
                    if (fs != null) fs.setStatus("执行: " + step.type.label);

                    Logger.d(TAG, "执行步骤[" + (i + 1) + "] " + step.type.label);

                    // 执行前等待
                    if (step.waitBefore > 0) sleep(step.waitBefore);

                    // 执行步骤
                    boolean ok = execute(step);

                    if (!ok) {
                        Logger.w(TAG, "步骤[" + (i + 1) + "] 执行失败");
                        // 重试
                        for (int r = 0; r < step.retryCount && !stopped; r++) {
                            sleep(step.retryDelay);
                            Logger.d(TAG, "重试步骤[" + (i + 1) + "] 第" + (r + 1) + "次");
                            ok = execute(step);
                            if (ok) break;
                        }
                    }

                    // 处理跳转
                    if (step.type == StepType.JUMP && step.jumpTarget >= 0 && step.jumpTarget < steps.size()) {
                        i = step.jumpTarget - 1;
                        Logger.d(TAG, "跳转到步骤" + step.jumpTarget);
                        continue;
                    }

                    if (step.type == StepType.STOP) {
                        Logger.i(TAG, "遇到停止步骤");
                        stopped = true;
                        break;
                    }

                    // 执行后等待
                    if (step.waitAfter > 0) sleep(step.waitAfter);
                }

                if (stopped) break;

                // 循环间隔
                if (loop < loops - 1 && script.config.loopInterval > 0) {
                    sleep(script.config.loopInterval);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "执行异常: " + e.getMessage());
        }

        Logger.i(TAG, "脚本执行完成");

        if (fs != null) {
            fs.setStatus("已完成");
            fs.setStep("");
            fs.setLoop("");
        }

        setState(State.IDLE);
        cleanup();
        main.post(() -> { if (listener != null) listener.onComplete(); });
    }

    private boolean execute(Step step) {
        ClickService cs = ClickService.getInstance();
        if (cs == null && needsService(step.type)) {
            Logger.e(TAG, "无障碍服务未开启");
            return false;
        }

        try {
            switch (step.type) {
                case CLICK:
                    if (cs == null) return false;
                    CountDownLatch c1 = new CountDownLatch(1);
                    cs.click(step.x1, step.y1, () -> c1.countDown());
                    c1.await(5, TimeUnit.SECONDS);
                    Logger.d(TAG, "点击 (" + step.x1 + "," + step.y1 + ")");
                    return true;

                case SWIPE:
                    if (cs == null) return false;
                    CountDownLatch c2 = new CountDownLatch(1);
                    cs.swipe(step.x1, step.y1, step.x2, step.y2, step.duration, () -> c2.countDown());
                    c2.await(10, TimeUnit.SECONDS);
                    Logger.d(TAG, "滑动 (" + step.x1 + "," + step.y1 + ")->(" + step.x2 + "," + step.y2 + ")");
                    return true;

                case LONG_PRESS:
                    if (cs == null) return false;
                    CountDownLatch c3 = new CountDownLatch(1);
                    cs.longPress(step.x1, step.y1, step.duration > 0 ? step.duration : 1000, () -> c3.countDown());
                    c3.await(step.duration + 5000, TimeUnit.MILLISECONDS);
                    Logger.d(TAG, "长按 (" + step.x1 + "," + step.y1 + ") " + step.duration + "ms");
                    return true;

                case BACK_KEY:
                    if (cs == null) return false;
                    cs.pressBack();
                    Logger.d(TAG, "返回键");
                    return true;

                case HOME_KEY:
                    if (cs == null) return false;
                    cs.pressHome();
                    Logger.d(TAG, "Home键");
                    return true;

                case MENU_KEY:
                    if (cs == null) return false;
                    cs.pressRecents();
                    Logger.d(TAG, "菜单键");
                    return true;

                case JUMP:
                    return true;

                case STOP:
                    return true;

                case PAUSE:
                    paused = true;
                    setState(State.PAUSED);
                    waitIfPaused();
                    return true;

                case EMPTY:
                    Logger.d(TAG, "空操作");
                    return true;

                case INPUT_TEXT:
                    Logger.d(TAG, "输入: " + step.inputText);
                    return true;

                case COUNTER:
                    Logger.d(TAG, "计数: " + step.counterName + " " + step.counterOp + " " + step.counterValue);
                    return true;

                case NUMBER_COMPARE:
                case TEXT_COMPARE:
                case TIME_COMPARE:
                    Logger.d(TAG, "比较: " + step.compareValue1 + " " + step.compareOperator + " " + step.compareValue2);
                    return true;

                case IMAGE_MATCH:
                case COLOR_MATCH:
                case TEXT_MATCH:
                    Logger.d(TAG, "识别: " + step.type.label + " (基础版)");
                    return true;

                case MULTI_TOUCH:
                    Logger.d(TAG, "多点触控");
                    return true;

                case SUB_RULE:
                    Logger.d(TAG, "子规则: " + step.subScriptId);
                    return true;

                default:
                    return false;
            }
        } catch (Exception e) {
            Logger.e(TAG, "执行异常: " + e.getMessage());
            return false;
        }
    }

    private boolean needsService(StepType type) {
        switch (type) {
            case CLICK: case SWIPE: case LONG_PRESS: case MULTI_TOUCH:
            case BACK_KEY: case HOME_KEY: case MENU_KEY:
                return true;
            default:
                return false;
        }
    }

    private void waitIfPaused() {
        if (paused && !stopped) {
            synchronized (this) {
                try { while (paused && !stopped) wait(); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void sleep(long ms) {
        if (ms <= 0) return;
        long end = SystemClock.elapsedRealtime() + ms;
        while (SystemClock.elapsedRealtime() < end && !stopped) {
            try { Thread.sleep(Math.min(end - SystemClock.elapsedRealtime(), 100)); }
            catch (InterruptedException e) { if (stopped) break; }
        }
    }

    private void setState(State s) {
        state = s;
        main.post(() -> { if (listener != null) listener.onStateChanged(s); });
    }

    private void cleanup() {
        try {
            if (receiver != null) {
                ctx.unregisterReceiver(receiver);
                receiver = null;
            }
        } catch (Exception e) {}
        current = null;
    }
}
