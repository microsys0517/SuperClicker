package com.superclicker.engine;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.superclicker.model.ExecutionConfig;
import com.superclicker.model.Script;
import com.superclicker.model.Step;
import com.superclicker.model.StepType;
import com.superclicker.service.ClickAccessibilityService;
import com.superclicker.util.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class ScriptEngine {
    private static final String TAG = "Engine";
    public enum State { IDLE, RUNNING, PAUSED, STOPPING }
    public interface StateListener {
        void onStateChanged(State state);
        void onStepChanged(int currentStep, int totalSteps, Step step);
        void onLoopChanged(int currentLoop, int totalLoops);
        void onError(int stepIndex, Step step, String error);
        void onComplete();
    }
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final StepExecutor stepExecutor;
    private Script script;
    private List<Step> steps;
    private ExecutionConfig config;
    private State state = State.IDLE;
    private StateListener listener;
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final AtomicInteger currentLoop = new AtomicInteger(0);
    private final Map<String, Integer> counters = new HashMap<>();
    private long startTime;
    public ScriptEngine(Context context) {
        this.context = context;
        this.stepExecutor = new StepExecutor(context);
    }
    public void setListener(StateListener l) { this.listener = l; }
    public State getState() { return state; }
    public void start(Script script, List<Step> steps) {
        if (state == State.RUNNING) return;
        this.script = script; this.steps = steps; this.config = script.config;
        this.counters.clear(); this.currentLoop.set(0);
        this.startTime = SystemClock.elapsedRealtime();
        stopped.set(false); paused.set(false);
        setState(State.RUNNING);
        Logger.i(TAG, "脚本开始: " + script.name + " (共" + steps.size() + "步)");
        executor.execute(this::runLoop);
    }
    public void stop() { stopped.set(true); paused.set(false); setState(State.STOPPING); Logger.i(TAG, "脚本停止"); }
    public void togglePause() {
        if (state == State.RUNNING) { paused.set(true); setState(State.PAUSED); }
        else if (state == State.PAUSED) { paused.set(false); synchronized (paused) { paused.notifyAll(); } setState(State.RUNNING); }
    }
    private void runLoop() {
        int totalLoops = config.loopCount == 0 ? Integer.MAX_VALUE : config.loopCount;
        for (int loop = 0; loop < totalLoops && !stopped.get(); loop++) {
            currentLoop.set(loop + 1);
            notifyLoopChanged(loop + 1, config.loopCount);
            for (int i = config.startStep; i < steps.size() && !stopped.get(); i++) {
                waitIfPaused();
                if (stopped.get()) break;
                Step step = steps.get(i);
                if (!step.enabled) continue;
                notifyStepChanged(i, steps.size(), step);
                if (step.waitBefore > 0) sleep(step.waitBefore);
                boolean success = executeStepWithRetry(step);
                if (step.type == StepType.JUMP && success) {
                    int target = step.jumpTarget;
                    if (target >= 0 && target < steps.size()) { i = target - 1; continue; }
                }
                if (step.type == StepType.STOP) { stopped.set(true); break; }
                if (step.type == StepType.PAUSE) { paused.set(true); setState(State.PAUSED); waitIfPaused(); }
                if (step.waitAfter > 0) sleep(step.waitAfter);
            }
            if (stopped.get()) break;
            if (loop < totalLoops - 1 && config.loopInterval > 0) sleep(config.loopInterval);
        }
        setState(State.IDLE);
        Logger.i(TAG, "脚本执行完成");
        mainHandler.post(() -> { if (listener != null) listener.onComplete(); });
    }
    private boolean executeStepWithRetry(Step step) {
        int maxRetry = Math.max(step.retryCount, 1);
        for (int attempt = 0; attempt < maxRetry && !stopped.get(); attempt++) {
            if (attempt > 0) sleep(step.retryDelay);
            try {
                boolean result = executeStep(step);
                step.lastResult = result;
                if (result) return true;
            } catch (Exception e) { Logger.e(TAG, "步骤[" + step.order + "] 异常: " + e.getMessage()); }
        }
        return false;
    }
    private boolean executeStep(Step step) throws Exception {
        ClickAccessibilityService svc = ClickAccessibilityService.getInstance();
        if (svc == null) throw new Exception("无障碍服务未开启");
        switch (step.type) {
            case CLICK: return stepExecutor.executeClick(svc, step);
            case SWIPE: return stepExecutor.executeSwipe(svc, step);
            case LONG_PRESS: return stepExecutor.executeLongPress(svc, step);
            case MULTI_TOUCH: return stepExecutor.executeMultiTouch(svc, step);
            case IMAGE_MATCH: return stepExecutor.executeImageMatch(svc, step);
            case COLOR_MATCH: return stepExecutor.executeColorMatch(svc, step);
            case TEXT_MATCH: return stepExecutor.executeTextMatch(svc, step);
            case NUMBER_COMPARE: return stepExecutor.executeNumberCompare(step);
            case TEXT_COMPARE: return stepExecutor.executeTextCompare(step);
            case TIME_COMPARE: return stepExecutor.executeTimeCompare(step);
            case COUNTER: stepExecutor.executeCounter(step, counters); return true;
            case INPUT_TEXT: return stepExecutor.executeInputText(step);
            case BACK_KEY: svc.pressBack(); return true;
            case HOME_KEY: svc.pressHome(); return true;
            case MENU_KEY: svc.pressRecents(); return true;
            case JUMP: case STOP: case PAUSE: case EMPTY: case SUB_RULE: return true;
            default: return false;
        }
    }
    private void waitIfPaused() {
        if (paused.get() && !stopped.get()) {
            synchronized (paused) { try { while (paused.get() && !stopped.get()) paused.wait(); } catch (InterruptedException ignored) {} }
        }
    }
    private void sleep(long ms) {
        if (ms <= 0) return;
        long end = SystemClock.elapsedRealtime() + ms;
        while (SystemClock.elapsedRealtime() < end && !stopped.get()) {
            try { Thread.sleep(Math.min(end - SystemClock.elapsedRealtime(), 100)); } catch (InterruptedException ignored) { if (stopped.get()) break; }
        }
    }
    private void setState(State s) { this.state = s; mainHandler.post(() -> { if (listener != null) listener.onStateChanged(s); }); }
    private void notifyStepChanged(int c, int t, Step s) { mainHandler.post(() -> { if (listener != null) listener.onStepChanged(c, t, s); }); }
    private void notifyLoopChanged(int c, int t) { mainHandler.post(() -> { if (listener != null) listener.onLoopChanged(c, t); }); }
}
